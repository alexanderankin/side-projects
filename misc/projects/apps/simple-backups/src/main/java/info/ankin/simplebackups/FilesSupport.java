package info.ankin.simplebackups;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

public final class FilesSupport {
    private FilesSupport() {
    }

    public static Path expandHome(String path) {
        return path.startsWith("~/") ? Path.of(System.getProperty("user.home"), path.substring(2)) : Path.of(path);
    }

    public static void privatePermissions(Path path, boolean directory) throws IOException {
        if (Files.getFileStore(path).supportsFileAttributeView("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"));
    }

    public static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @NonNull
            @Override
            public FileVisitResult postVisitDirectory(@NonNull Path dir, IOException failure) throws IOException {
                if (failure != null) throw failure;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void requireEmptyDirectory(Path directory) throws IOException {
        if (Files.isSymbolicLink(directory))
            throw new IOException("Choose a real directory, not a symbolic link");
        Files.createDirectories(directory);
        try (var entries = Files.list(directory)) {
            if (entries.findAny().isPresent())
                throw new IOException("Restore requires an empty destination folder");
        }
    }

    // Resolve existing ancestors too, so a symlink cannot hide a destination inside the source.
    public static Path realLocation(Path path) throws IOException {
        path = path.toAbsolutePath().normalize();
        if (Files.exists(path))
            return path.toRealPath();
        return realLocation(path.getParent()).resolve(path.getFileName());
    }
}
