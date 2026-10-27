package info.ankin.simplebackups;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SourceFolderTest {
    private final Session session = mock(Session.class);
    private final ChannelSftp channel = mock(ChannelSftp.class);

    @Test void stagesNestedFilesWithLiteralPathsAndReleasesResources() throws Exception {
        when(session.openChannel("sftp")).thenReturn(channel);
        var directory = attributes(true, false);
        when(channel.lstat("/data\\*\\?")).thenReturn(directory);
        var entries = new Vector<>(List.of(
                entry(".", directory), entry("..", directory), entry("empty", directory),
                entry("file*?.txt", attributes(false, true))));
        when(channel.ls("/data\\*\\?")).thenReturn(entries);
        when(channel.ls("/data\\*\\?/empty")).thenReturn(new Vector<>());
        doAnswer(call -> {
            OutputStream output = call.getArgument(1);
            output.write("remote content 🌍".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(channel).get(eq("/data\\*\\?/file\\*\\?.txt"), any(OutputStream.class));

        try (var constructors = mockConstruction(JSch.class, (ssh, context) ->
                when(ssh.getSession("alice", "server", 2222)).thenReturn(session))) {
            Path staged;
            try (var source = SourceFolder.open(URI.create("ssh://alice@server:2222/data*%3F"), AppSettings.defaults(), ignored -> {})) {
                staged = source.path();
                assertTrue(Files.isDirectory(staged.resolve("empty")));
                assertEquals("remote content 🌍", Files.readString(staged.resolve("file*?.txt")));
            }
            assertFalse(Files.exists(staged), "Temporary source files must be removed on close");
            var ssh = constructors.constructed().getFirst();
            var hostKeys = ArgumentCaptor.forClass(HostKeyRepository.class);
            verify(ssh).setHostKeyRepository(hostKeys.capture());
            assertInstanceOf(AcceptNewHostKeys.class, hostKeys.getValue());
            assertEquals(FilesSupport.expandHome("~/.ssh/known_hosts").toString(), hostKeys.getValue().getKnownHostsRepositoryID());
            verify(ssh).setConfigRepository(null);
            verify(ssh).setIdentityRepository(null);
            verify(ssh).addIdentity(FilesSupport.expandHome(AppSettings.defaults().sshPrivateKey()).toString());
            verify(session).setConfig("StrictHostKeyChecking", "yes");
            verify(session).setConfig("HashKnownHosts", "no");
            verify(session).setConfig("PreferredAuthentications", "publickey");
            verify(session).setServerAliveInterval(5_000);
            verify(session).setServerAliveCountMax(6);
            verify(session, never()).setTimeout(anyInt());
            verify(channel).setAgentForwarding(false);
            verify(session).disconnect();
            verify(channel).disconnect();
        }
    }

    @Test void usesGlobalUsernameAndDefaultPortAndDisconnectsAfterSftpFailure() throws Exception {
        var settings = new AppSettings(null, null, null, null, null, "global-user", "~/custom-key", null);
        when(session.openChannel("sftp")).thenReturn(channel);
        when(channel.lstat("/missing")).thenThrow(new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "No such folder"));
        try (var constructors = mockConstruction(JSch.class, (ssh, context) ->
                when(ssh.getSession("global-user", "server", 22)).thenReturn(session))) {
            var error = assertThrows(IOException.class, () ->
                    SourceFolder.open(URI.create("ssh://server/missing"), settings, ignored -> {}));
            assertTrue(error.getMessage().contains("No such folder"));
            assertInstanceOf(SftpException.class, error.getCause());
            verify(constructors.constructed().getFirst()).addIdentity(FilesSupport.expandHome("~/custom-key").toString());
            verify(session).disconnect();
            verify(channel).disconnect();
        }
    }

    @Test void rejectsRemoteSymlinksInsteadOfSilentlyFollowingThem() throws Exception {
        when(session.openChannel("sftp")).thenReturn(channel);
        var directory = attributes(true, false);
        var entries = new Vector<>(List.of(entry("link", attributes(false, false))));
        when(channel.lstat("/source")).thenReturn(directory);
        when(channel.ls("/source")).thenReturn(entries);
        try (var ignored = mockConstruction(JSch.class, (ssh, context) ->
                when(ssh.getSession(anyString(), anyString(), anyInt())).thenReturn(session))) {
            var error = assertThrows(IOException.class, () ->
                    SourceFolder.open(URI.create("ssh://server/source"), AppSettings.defaults(), message -> {}));
            assertTrue(error.getMessage().contains("links and special files are not followed"));
            verify(channel, never()).get(anyString(), any(OutputStream.class));
            verify(session).disconnect();
            verify(channel).disconnect();
        }
    }

    private SftpATTRS attributes(boolean directory, boolean regular) {
        var attrs = mock(SftpATTRS.class);
        when(attrs.isDir()).thenReturn(directory);
        when(attrs.isReg()).thenReturn(regular);
        return attrs;
    }

    private ChannelSftp.LsEntry entry(String name, SftpATTRS attributes) {
        var entry = mock(ChannelSftp.LsEntry.class);
        when(entry.getFilename()).thenReturn(name);
        when(entry.getAttrs()).thenReturn(attributes);
        return entry;
    }
}
