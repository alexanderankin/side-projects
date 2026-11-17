package info.ankin.simplebackups.backup;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.function.Consumer;

/** Contents, rather than size or timestamps, determine whether replacement needs permission. */
public final class RestoreComparison {
    private RestoreComparison() {}

    public static String hash(InputStream input) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var stream = new DigestInputStream(input, digest)) {
                stream.transferTo(OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static Map<String, String> inventory(Path root) throws IOException {
        if (Files.isSymbolicLink(root))
            throw new IOException("Restore path contains a symlink or non-directory: " + root);
        var result = new TreeMap<String, String>();
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS))
            return result;
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS))
            throw new IOException("Restore destination must be a directory: " + root);
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(p -> !p.equals(root)).toList()) {
                String name = root.relativize(path).toString().replace(File.separatorChar, '/');
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    result.put(name, "directory");
                } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    result.put(name, hash(Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)));
                } else {
                    throw new IOException("Cannot restore over a link or special file: " + path);
                }
            }
        }
        return result;
    }

    public static List<String> deletions(Map<String, String> snapshot, Map<String, String> destination) {
        return destination.keySet().stream()
                .filter(name -> !Objects.equals(destination.get(name), snapshot.get(name))).sorted().toList();
    }

    public static void requirePermission(List<String> deletions, boolean allowDelete) throws IOException {
        if (!allowDelete && !deletions.isEmpty())
            throw new IOException("Restore requires deletion or replacement of:\n" + String.join("\n", deletions));
    }

    public static void restoreLocal(Path contents, Path destination, boolean allowDelete, Consumer<String> progress) throws IOException {
        restoreLocal(contents, destination, allowDelete, progress, null);
    }

    public static void restoreLocal(Path contents, Path destination, boolean allowDelete, Consumer<String> progress,
                                    Map<String, String> approvedInventory) throws IOException {
        var snapshot = inventory(contents);
        var existing = inventory(destination);
        if (approvedInventory != null && !existing.equals(approvedInventory))
            throw new IOException("Destination changed after the restore preview. Run Restore again.");
        var deletions = deletions(snapshot, existing);
        requirePermission(deletions, allowDelete);
        // Delete individual entries deepest first; never recursively delete paths absent from the comparison.
        for (String name : deepestFirst(deletions)) {
            progress.accept("Deleting restore destination entry: " + name);
            Files.delete(destination.resolve(name));
        }
        Files.createDirectories(destination);
        for (String name : snapshot.keySet()) {
            Path target = destination.resolve(name);
            if (snapshot.get(name).equals("directory")) {
                Files.createDirectories(target);
            } else if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                Files.copy(contents.resolve(name), target, StandardCopyOption.COPY_ATTRIBUTES);
            } else if (!snapshot.get(name).equals(hash(Files.newInputStream(target, LinkOption.NOFOLLOW_LINKS)))) {
                throw new IOException("Destination changed during restore: " + target);
            }
        }
    }

    static List<String> deepestFirst(List<String> names) {
        return names.stream().sorted(Comparator.<String>comparingInt(name -> name.split("/").length).reversed()
                .thenComparing(Comparator.naturalOrder())).toList();
    }
}
