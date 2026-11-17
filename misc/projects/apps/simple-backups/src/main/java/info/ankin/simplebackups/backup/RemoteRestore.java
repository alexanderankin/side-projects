package info.ankin.simplebackups.backup;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import info.ankin.simplebackups.config.AppSettings;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static info.ankin.simplebackups.backup.SftpConnection.literalPath;

final class RemoteRestore {
    static Map<String, String> inventory(URI destination, AppSettings settings, Consumer<String> progress) throws IOException {
        try (var connection = SftpConnection.open(destination, settings, progress)) {
            var sftp = connection.sftp();
            String root = inspectDirectory(sftp, destination.getPath());
            var result = new TreeMap<String, String>();
            if (root != null)
                inventory(sftp, root, "", result);
            return result;
        } catch (SftpException failure) {
            throw new IOException("Could not compare SSH destination: " + failure.getMessage(), failure);
        }
    }

    static void restore(Path restored, URI destination, AppSettings settings, boolean allowDelete, Consumer<String> progress, Map<String, String> approvedInventory) throws IOException {
        try (var connection = SftpConnection.open(destination, settings, progress)) {
            var sftp = connection.sftp();
            var before = new TreeMap<String, String>();
            String existingRoot = inspectDirectory(sftp, destination.getPath());
            if (existingRoot != null)
                inventory(sftp, existingRoot, "", before);
            if (!before.equals(approvedInventory))
                throw new IOException("Destination changed after the restore preview. Run Restore again.");
            var deletions = RestoreComparison.deletions(RestoreComparison.inventory(restored), before);
            RestoreComparison.requirePermission(deletions, allowDelete);
            String root = ensureDirectory(sftp, destination.getPath());
            for (String name : RestoreComparison.deepestFirst(deletions)) {
                progress.accept("Deleting restore destination entry: " + name);
                String path = literalPath(root + "/" + name);
                if (before.get(name).equals("directory"))
                    sftp.rmdir(path);
                else
                    sftp.rm(path);
            }
            try {
                upload(sftp, restored, root, progress);
            } catch (Exception failure) {
                throw new IOException("Restore failed; partial files retained at " + destination + ": " + failure.getMessage(), failure);
            }
        } catch (SftpException failure) {
            throw new IOException("Could not restore SSH folder " + destination + ": " + failure.getMessage(), failure);
        }
    }

    private static String inspectDirectory(ChannelSftp sftp, String path) throws IOException, SftpException {
        String current = "/";
        for (String part : path.split("/")) {
            if (part.isEmpty())
                continue;
            current = child(current, part);
            var attrs = attributes(sftp, current);
            if (attrs == null)
                return null;
            if (!attrs.isDir())
                throw new IOException("Restore path contains a symlink or non-directory: " + current);
        }
        return current;
    }

    private static void inventory(ChannelSftp sftp, String root, String relative, Map<String, String> result) throws IOException, SftpException {
        for (var entry : entries(sftp, root)) {
            String path = child(root, entry.getFilename());
            String name = relative + entry.getFilename();
            if (entry.getAttrs().isDir()) {
                result.put(name, "directory");
                inventory(sftp, path, name + "/", result);
            } else if (entry.getAttrs().isReg()) {
                result.put(name, RestoreComparison.hash(sftp.get(literalPath(path))));
            } else {
                throw new IOException("Cannot restore over a link or special file: " + path);
            }
        }
    }

    private static String ensureDirectory(ChannelSftp sftp, String path) throws IOException, SftpException {
        String current = "/";
        for (String part : path.split("/")) {
            if (part.isEmpty())
                continue;
            current = child(current, part);
            var attributes = attributes(sftp, current);
            if (attributes == null)
                sftp.mkdir(current);
            else if (!attributes.isDir())
                throw new IOException("Restore path contains a symlink or non-directory: " + current);
        }
        return current;
    }

    private static void upload(ChannelSftp sftp, Path local, String remote, Consumer<String> progress) throws IOException, SftpException {
        try (var files = Files.list(local)) {
            for (Path file : files.toList()) {
                String target = child(remote, file.getFileName().toString());
                if (Files.isDirectory(file)) {
                    if (attributes(sftp, target) == null)
                        sftp.mkdir(target);
                    upload(sftp, file, target, progress);
                } else {
                    if (attributes(sftp, target) != null) {
                        String localHash = RestoreComparison.hash(Files.newInputStream(file));
                        String remoteHash = RestoreComparison.hash(sftp.get(literalPath(target)));
                        if (!localHash.equals(remoteHash))
                            throw new IOException("Destination changed during restore: " + target);
                        continue;
                    }
                    progress.accept("Restoring SSH file: " + file.getFileName());
                    try (var input = Files.newInputStream(file)) {
                        sftp.put(input, literalPath(target));
                    }
                    sftp.setMtime(literalPath(target), (int) (Files.getLastModifiedTime(file).toMillis() / 1000));
                }
            }
        }
    }

    private static SftpATTRS attributes(ChannelSftp sftp, String path) throws SftpException {
        try {
            return sftp.lstat(literalPath(path));
        } catch (SftpException failure) {
            if (failure.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                return null;
            throw failure;
        }
    }

    private static List<ChannelSftp.LsEntry> entries(ChannelSftp sftp, String path) throws SftpException {
        return sftp.ls(literalPath(path)).stream()
                .filter(entry -> !entry.getFilename().equals(".") && !entry.getFilename().equals("..")).toList();
    }

    private static String child(String parent, String name) throws IOException {
        if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\"))
            throw new IOException("Invalid remote path component: " + name);
        return parent + (parent.endsWith("/") ? "" : "/") + name;
    }
}
