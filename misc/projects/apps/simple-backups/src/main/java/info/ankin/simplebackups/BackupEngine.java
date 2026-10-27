package info.ankin.simplebackups;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

interface BackupEngine {
    void create(Path source, Path output, Consumer<String> progress) throws IOException;

    void restore(SnapshotStore store, Snapshot snapshot, Path destination) throws IOException;

    List<String> diff(SnapshotStore store, Snapshot snapshot, Path source) throws IOException;

    static BackupEngine forMethod(BackupMethod method) {
        return switch (method) {
            case tar -> new TarBackupEngine();
            case restic -> new ResticBackupEngine();
        };
    }
}
