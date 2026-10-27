package info.ankin.simplebackups;

import lombok.Data;
import lombok.experimental.Accessors;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.function.Consumer;

/** SSH inputs are staged locally; a local URI is used directly without adding path components. */
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
        try (var ssh = new SSHClient()) {
            ssh.loadKnownHosts();
            ssh.setConnectTimeout(15_000);
            ssh.setTimeout(60_000);
            progress.accept("Connecting to " + source.getHost());
            ssh.connect(source.getHost(), source.getPort() == -1 ? 22 : source.getPort());
            String username = source.getUserInfo() == null ? settings.sshUsername() : source.getUserInfo();
            ssh.authPublickey(username, FilesSupport.expandHome(settings.sshPrivateKey()).toString());
            try (var sftp = ssh.newSFTPClient()) {
                if (sftp.lstat(source.getPath()).getType() != FileMode.Type.DIRECTORY)
                    throw new IOException("SSH source must be a directory, not a symlink or file");
                download(sftp, source.getPath(), staging, progress);
            }
            return new SourceFolder(staging, true);
        } catch (Exception failure) {
            try { FilesSupport.deleteTree(staging); } catch (IOException cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
    }

    private static void download(SFTPClient sftp, String remote, Path local, Consumer<String> progress) throws IOException {
        for (var entry : sftp.ls(remote)) {
            String name = entry.getName();
            if (name.equals(".") || name.equals("..")) continue;
            if (name.contains("/") || name.contains("\\") || name.isBlank())
                throw new IOException("Invalid remote filename: " + name);
            String child = remote + (remote.endsWith("/") ? "" : "/") + name;
            Path target = local.resolve(name);
            switch (entry.getAttributes().getType()) {
                case DIRECTORY -> {
                    Files.createDirectory(target);
                    download(sftp, child, target, progress);
                }
                case REGULAR -> {
                    progress.accept("Reading SSH file: " + child);
                    sftp.get(child, target.toString());
                }
                default -> throw new IOException("Unsupported SSH entry (links and special files are not followed): " + child);
            }
        }
    }

    @Override public void close() throws IOException { if (temporary) FilesSupport.deleteTree(path); }
}
