package info.ankin.simplebackups;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * SSH inputs are staged locally; a local URI is used directly without adding path components.
 */
@Data
@Accessors(fluent = true)
final class SourceFolder implements AutoCloseable {
    private final Path path;
    private final boolean temporary;

    static SourceFolder open(URI source, AppSettings settings, Consumer<String> progress) throws IOException {
        if ("file".equals(source.getScheme())) {
            Path path = Path.of(source).toRealPath();
            if (!Files.isDirectory(path)) throw new IOException("Source is not a folder: " + path);
            return new SourceFolder(path, false);
        }
        Path staging = Files.createTempDirectory("simple-backups-ssh-");
        Session session = null;
        ChannelSftp sftp = null;
        try {
            var ssh = new JSch();
            // Use app policy only: no ~/.ssh/config and no SSH_AUTH_SOCK / agent repository.
            ssh.setConfigRepository(null);
            ssh.setIdentityRepository(null);
            ssh.setHostKeyRepository(new AcceptNewHostKeys(ssh, FilesSupport.expandHome("~/.ssh/known_hosts")));
            ssh.addIdentity(FilesSupport.expandHome(settings.sshPrivateKey()).toString());
            progress.accept("Connecting to " + source.getHost());
            String username = source.getUserInfo() == null ? settings.sshUsername() : source.getUserInfo();
            session = ssh.getSession(username, source.getHost(), source.getPort() == -1 ? 22 : source.getPort());
            // The repository accepts new keys; strict checking still rejects changed/revoked keys.
            session.setConfig("StrictHostKeyChecking", "yes");
            session.setConfig("HashKnownHosts", "no");
            session.setConfig("PreferredAuthentications", "publickey");
            session.setServerAliveInterval(5_000);
            session.setServerAliveCountMax(6);
            session.connect(15_000);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.setAgentForwarding(false);
            sftp.connect(15_000);
            if (!sftp.lstat(literalPath(source.getPath())).isDir())
                throw new IOException("SSH source must be a directory, not a symlink or file");
            download(sftp, source.getPath(), staging, progress);
            return new SourceFolder(staging, true);
        } catch (Exception failure) {
            try {
                FilesSupport.deleteTree(staging);
            } catch (IOException cleanup) {
                failure.addSuppressed(cleanup);
            }
            if (failure instanceof IOException io) throw io;
            throw new IOException("Could not read SSH folder " + source + ": " + failure.getMessage(), failure);
        } finally {
            if (sftp != null) sftp.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private static void download(ChannelSftp sftp, String remote, Path local, Consumer<String> progress) throws IOException, SftpException {
        for (var entry : sftp.ls(literalPath(remote))) {
            String name = entry.getFilename();
            if (name.equals(".") || name.equals("..")) continue;
            if (name.contains("/") || name.contains("\\") || name.isBlank())
                throw new IOException("Invalid remote filename: " + name);
            String child = remote + (remote.endsWith("/") ? "" : "/") + name;
            Path target = local.resolve(name);
            var attributes = entry.getAttrs();
            if (attributes.isDir()) {
                Files.createDirectory(target);
                download(sftp, child, target, progress);
            } else if (attributes.isReg()) {
                progress.accept("Reading SSH file: " + child);
                try (var output = Files.newOutputStream(target)) {
                    sftp.get(literalPath(child), output);
                }
            } else {
                throw new IOException("Unsupported SSH entry (links and special files are not followed): " + child);
            }
        }
    }

    private static String literalPath(String path) {
        // JSch's SFTP methods accept glob patterns; configured URIs and listed names are literal.
        return path.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    @Override
    public void close() throws IOException {
        if (temporary) FilesSupport.deleteTree(path);
    }
}
