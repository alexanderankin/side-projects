package info.ankin.simplebackups.backup.engine;

import info.ankin.simplebackups.config.BackupMethod;
import info.ankin.simplebackups.backup.Snapshot;
import info.ankin.simplebackups.backup.snapshot.SnapshotStore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface BackupEngine {
    String TAR_ARCHIVE_NAME = TarBackupEngine.ARCHIVE;

    void create(Path source, Path output, Consumer<String> progress) throws IOException;

    default void restore(SnapshotStore store, Snapshot snapshot, Path destination) throws IOException {
        restore(store, snapshot, destination, false, ignored -> {});
    }

    void restore(SnapshotStore store, Snapshot snapshot, Path destination, boolean allowDelete,
                 Consumer<String> progress) throws IOException;

    List<String> diff(SnapshotStore store, Snapshot snapshot, Path source) throws IOException;

    default void extractRestore(SnapshotStore store, Snapshot snapshot, Path destination) throws IOException {
        throw new UnsupportedOperationException("Restore planning is available for tar snapshots only");
    }

    static BackupEngine forMethod(BackupMethod method) {
        return switch (method) {
            case tar -> new TarBackupEngine();
            case restic -> new ResticBackupEngine();
        };
    }
}
