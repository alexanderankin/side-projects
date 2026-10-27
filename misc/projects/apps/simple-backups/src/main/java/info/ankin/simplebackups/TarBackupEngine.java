package info.ankin.simplebackups;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.zstandard.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;

final class TarBackupEngine implements BackupEngine {
    static final String ARCHIVE = "simple.tar.zst";

    @Override public void create(Path source, Path output, Consumer<String> progress) throws IOException {
        try (var file = new BufferedOutputStream(Files.newOutputStream(output.resolve(ARCHIVE)));
             var zstd = new ZstdCompressorOutputStream(file);
             var tar = new TarArchiveOutputStream(zstd)) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tar.setAddPaxHeadersForNonAsciiNames(true);
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                private void add(Path path, BasicFileAttributes attrs) throws IOException {
                    String name = relative(source, path);
                    if (name.contains("\\")) throw new IOException("Backslash filenames are not supported: " + path);
                    if (!attrs.isDirectory() && !attrs.isRegularFile())
                        throw new IOException("Unsupported entry (links and special files are not followed): " + path);
                    progress.accept("Archiving: " + name);
                    var entry = new TarArchiveEntry(name + (attrs.isDirectory() ? "/" : ""));
                    entry.setSize(attrs.isDirectory() ? 0 : attrs.size());
                    entry.setModTime(attrs.lastModifiedTime().toMillis());
                    tar.putArchiveEntry(entry);
                    if (attrs.isRegularFile()) {
                        try (var input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) { input.transferTo(tar); }
                        var after = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                        if (after.size() != attrs.size() || !after.lastModifiedTime().equals(attrs.lastModifiedTime()))
                            throw new IOException("File changed during backup; retry when it is idle: " + path);
                    }
                    tar.closeArchiveEntry();
                }
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(source)) add(dir, attrs);
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    add(file, attrs);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override public void restore(SnapshotStore store, Snapshot snapshot, Path destination) throws IOException {
        FilesSupport.requireEmptyDirectory(destination);
        // Validate and decompress completely before copying anything into the user's directory.
        Path staging = Files.createTempDirectory("simple-backups-restore-");
        try {
            extract(store, snapshot, staging);
            Files.walkFileTree(staging, new SimpleFileVisitor<>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(staging)) Files.createDirectory(destination.resolve(staging.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, destination.resolve(staging.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
                    return FileVisitResult.CONTINUE;
                }
            });
        } finally { FilesSupport.deleteTree(staging); }
    }

    private void extract(SnapshotStore store, Snapshot snapshot, Path destination) throws IOException {
        Path root = destination.toAbsolutePath().normalize();
        Set<Path> seen = new HashSet<>();
        try (var input = new BufferedInputStream(store.open(snapshot.prefix() + ARCHIVE));
             var zstd = new ZstdCompressorInputStream(input);
             var tar = new TarArchiveInputStream(zstd)) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                String name = entry.getName();
                Path target = root.resolve(name).normalize();
                if (name.contains("\\") || name.matches("^[A-Za-z]:.*") || name.startsWith("/")
                        || !target.startsWith(root) || target.equals(root) || !seen.add(target))
                    throw new IOException("Unsafe or duplicate archive path: " + name);
                if (!tar.canReadEntryData(entry) || entry.isLink() || entry.isSymbolicLink()
                        || (!entry.isDirectory() && !entry.isFile()))
                    throw new IOException("Unsupported archive entry: " + name);
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                        long copied = tar.transferTo(out);
                        if (copied != entry.getSize()) throw new IOException("Truncated archive entry: " + name);
                    }
                    Files.setLastModifiedTime(target, FileTime.fromMillis(entry.getModTime().getTime()));
                }
            }
        }
    }

    @Override public List<String> diff(SnapshotStore store, Snapshot snapshot, Path source) throws IOException {
        Path staging = Files.createTempDirectory("simple-backups-diff-");
        try {
            extract(store, snapshot, staging);
            Map<String, String> before = fingerprint(staging);
            Map<String, String> after = fingerprint(source);
            Set<String> names = new TreeSet<>(before.keySet());
            names.addAll(after.keySet());
            var changes = new ArrayList<String>();
            for (String name : names) {
                if (!before.containsKey(name)) changes.add("ADDED    " + name);
                else if (!after.containsKey(name)) changes.add("REMOVED  " + name);
                else if (!before.get(name).equals(after.get(name))) changes.add("CHANGED  " + name);
            }
            return changes;
        } finally { FilesSupport.deleteTree(staging); }
    }

    private Map<String, String> fingerprint(Path root) throws IOException {
        Map<String, String> result = new TreeMap<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(root)) result.put(relative(root, dir), "directory");
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isRegularFile()) throw new IOException("Cannot compare link or special file: " + file);
                MessageDigest digest;
                try { digest = MessageDigest.getInstance("SHA-256"); }
                catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
                try (var input = new DigestInputStream(Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS), digest)) {
                    input.transferTo(OutputStream.nullOutputStream());
                }
                result.put(relative(root, file), HexFormat.of().formatHex(digest.digest()));
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace(File.separatorChar, '/');
    }
}
