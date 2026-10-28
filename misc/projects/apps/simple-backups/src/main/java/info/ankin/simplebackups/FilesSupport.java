package info.ankin.simplebackups;

import lombok.SneakyThrows;
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

    @SneakyThrows
    public static void privatePermissions(Path path, boolean directory) {
        if (Files.getFileStore(path).supportsFileAttributeView("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"));
    }

    @SneakyThrows
    public static void move(Path from, Path to) {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SneakyThrows
    public static void deleteTree(Path root) {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS))
            return;
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @NonNull
            @Override
            @SneakyThrows
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @NonNull
            @Override
            @SneakyThrows
            public FileVisitResult postVisitDirectory(@NonNull Path dir, IOException failure) {
                if (failure != null) throw failure;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SneakyThrows
    public static void requireEmptyDirectory(Path directory) {
        if (Files.isSymbolicLink(directory))
            throw new IOException("Choose a real directory, not a symbolic link");
        Files.createDirectories(directory);
        try (var entries = Files.list(directory)) {
            if (entries.findAny().isPresent())
                throw new IOException("Restore requires an empty destination folder");
        }
    }

    // Resolve existing ancestors too, so a symlink cannot hide a destination inside the source.
    @SneakyThrows
    public static Path realLocation(Path path) {
        path = path.toAbsolutePath().normalize();
        if (Files.exists(path))
            return path.toRealPath();
        return realLocation(path.getParent()).resolve(path.getFileName());
    }
}
