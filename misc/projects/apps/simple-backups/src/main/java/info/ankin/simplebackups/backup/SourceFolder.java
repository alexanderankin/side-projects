package info.ankin.simplebackups.backup;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import info.ankin.simplebackups.FilesSupport;
import info.ankin.simplebackups.config.AppSettings;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static info.ankin.simplebackups.backup.SftpConnection.literalPath;

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
            if (!Files.isDirectory(path))
                throw new IOException("Source is not a folder: " + path);
            return new SourceFolder(path, false);
        }
        Path staging = Files.createTempDirectory("simple-backups-ssh-");
        try (var connection = SftpConnection.open(source, settings, progress)) {
            var sftp = connection.sftp();
            if (!sftp.lstat(literalPath(source.getPath())).isDir())
                throw new IOException("SSH source must be a directory, not a symlink or file");
            download(sftp, source.getPath(), staging, progress);
            return new SourceFolder(staging, true);
        } catch (Exception failure) {
            try {
                FilesSupport.deleteTree(staging);
            } catch (Exception cleanup) {
                failure.addSuppressed(cleanup);
            }
            if (failure instanceof IOException io)
                throw io;
            throw new IOException("Could not read SSH folder " + source + ": " + failure.getMessage(), failure);
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

    @Override
    public void close() throws IOException {
        if (temporary) FilesSupport.deleteTree(path);
    }
}
