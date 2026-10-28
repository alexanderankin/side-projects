package info.ankin.simplebackups.backup.snapshot;

import info.ankin.simplebackups.config.AppSettings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.List;

/**
 * Keys are relative to the configured target URI. snapshot.json is written last.
 */
public interface SnapshotStore extends AutoCloseable {
    boolean reserve(long epochMillis) throws IOException;

    List<Long> snapshotIds() throws IOException;

    InputStream open(String key) throws IOException;

    boolean exists(String key) throws IOException;

    void put(String key, Path file) throws IOException;

    void remove(String prefix) throws IOException;

    @Override
    default void close() {
    }

    static SnapshotStore open(URI target, AppSettings settings) throws IOException {
        return switch (target.getScheme()) {
            case "file" -> new Local(Path.of(target));
            case "s3" -> new S3SnapshotStore(target, settings);
            default -> throw new IllegalArgumentException("Unsupported destination: " + target);
        };
    }
}
