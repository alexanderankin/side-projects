package info.ankin.simplebackups.backup.snapshot;

import info.ankin.simplebackups.FilesSupport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

final class Local implements SnapshotStore {
    private final Path root;

    public Local(Path root) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        Files.createDirectories(this.root.resolve("snapshots"));
    }

    private Path resolve(String key) throws IOException {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root) || !FilesSupport.realLocation(path).startsWith(root.toRealPath()))
            throw new IOException("Snapshot path escapes destination: " + key);
        return path;
    }

    @Override
    public boolean reserve(long epochMillis) throws IOException {
        try {
            Files.createDirectory(resolve("snapshots/" + epochMillis));
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public List<Long> snapshotIds() throws IOException {
        try (var paths = Files.list(resolve("snapshots"))) {
            return paths.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                .map(p -> p.getFileName().toString()).filter(s -> s.matches("[0-9]{1,18}"))
                .map(Long::parseLong).toList();
        }
    }

    @Override
    public InputStream open(String key) throws IOException {
        return Files.newInputStream(resolve(key));
    }

    @Override
    public boolean exists(String key) throws IOException {
        return Files.isRegularFile(resolve(key));
    }

    @Override
    public void put(String key, Path file) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Path partial = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
        try {
            Files.copy(file, partial, StandardCopyOption.REPLACE_EXISTING);
            FilesSupport.move(partial, target);
        } finally {
            Files.deleteIfExists(partial);
        }
    }

    @Override
    public void remove(String prefix) throws IOException {
        FilesSupport.deleteTree(resolve(prefix));
    }
}
