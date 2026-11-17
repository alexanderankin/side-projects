package info.ankin.simplebackups.backup;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import info.ankin.simplebackups.FilesSupport;
import info.ankin.simplebackups.config.AppSettings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

/** Shared connection policy for reading sources and restoring folders. */
@RequiredArgsConstructor
final class SftpConnection implements AutoCloseable {
    private final Session session;
    @Getter
    @Accessors(fluent = true)
    private final ChannelSftp sftp;

    static SftpConnection open(URI uri, AppSettings settings, Consumer<String> progress) throws IOException {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            var ssh = new JSch();
            // Use app policy only: no ~/.ssh/config and no SSH_AUTH_SOCK / agent repository.
            ssh.setConfigRepository(null);
            ssh.setIdentityRepository(null);
            ssh.setHostKeyRepository(new AcceptNewHostKeys(ssh, FilesSupport.expandHome("~/.ssh/known_hosts")));
            ssh.addIdentity(FilesSupport.expandHome(settings.sshPrivateKey()).toString());
            progress.accept("Connecting to " + uri.getHost());
            String username = uri.getUserInfo() == null ? settings.sshUsername() : uri.getUserInfo();
            session = ssh.getSession(username, uri.getHost(), uri.getPort() == -1 ? 22 : uri.getPort());
            // The repository accepts new keys; strict checking rejects changed/revoked keys.
            session.setConfig("StrictHostKeyChecking", "yes");
            session.setConfig("HashKnownHosts", "no");
            session.setConfig("PreferredAuthentications", "publickey");
            session.setServerAliveInterval(5_000);
            session.setServerAliveCountMax(6);
            session.connect(15_000);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.setAgentForwarding(false);
            sftp.connect(15_000);
            return new SftpConnection(session, sftp);
        } catch (Exception failure) {
            if (sftp != null) sftp.disconnect();
            if (session != null) session.disconnect();
            throw new IOException("Could not connect to SSH server " + uri.getHost() + ": " + failure.getMessage(), failure);
        }
    }

    static String literalPath(String path) {
        // JSch's glob-aware methods need quoting; mkdir takes a literal path without quoting.
        return path.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    @Override
    public void close() {
        try { sftp.disconnect(); }
        finally { session.disconnect(); }
    }
}
