package info.ankin.simplebackups.backup;

import info.ankin.simplebackups.config.AppSettings;
import info.ankin.simplebackups.config.BackupConfiguration;
import info.ankin.simplebackups.FilesSupport;
import info.ankin.simplebackups.backup.engine.BackupEngine;
import info.ankin.simplebackups.backup.snapshot.SnapshotStore;
import info.ankin.simplebackups.config.BackupMethod;
import info.ankin.simplebackups.config.ConfigurationStore;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public final class BackupService {
    public synchronized Snapshot backup(BackupConfiguration backup, AppSettings settings, Consumer<String> progress) throws IOException {
        log.debug("validating not overlap for local file backups (from {} / to {})", backup.from(), backup.to());
        if (backup.from().getScheme().equals("file") && backup.to().getScheme().equals("file")) {
            Path source = Path.of(backup.from()).toRealPath();
            Path target = FilesSupport.realLocation(Path.of(backup.to()));
            if (target.startsWith(source))
                throw new IOException("The backup destination cannot be inside its source folder");
        }

        log.info("Start backup({}): opening source/snapshot for backup = {}", backup.id(), backup);
        long start = System.nanoTime();

        Path work = Files.createTempDirectory("simple-backups-run-");
        try (var source = SourceFolder.open(backup.from(), settings, progress);
             var store = SnapshotStore.open(backup.to(), settings)) {
            if (work.toRealPath().startsWith(source.path()))
                throw new IOException("The source includes the temporary working folder. Choose a narrower source or change java.io.tmpdir.");

            long epoch = System.currentTimeMillis();
            while (!store.reserve(epoch))
                epoch++;

            String prefix = "snapshots/" + epoch + "/";
            try {
                var engine = BackupEngine.forMethod(settings.preferredMethod());
                engine.create(source.path(), work, progress);
                try (var files = Files.walk(work)) {
                    for (Path file : files.filter(Files::isRegularFile).toList()) {
                        String key = work.relativize(file).toString().replace(java.io.File.separatorChar, '/');
                        progress.accept("Saving: " + key);
                        store.put(prefix + key, file);
                    }
                }
                var snapshot = new Snapshot(backup.id(), epoch, (System.nanoTime() - start) / 1_000_000, settings.preferredMethod());
                Path metadata = work.resolve("snapshot.json");
                ConfigurationStore.write(metadata, snapshot);
                store.put(prefix + "snapshot.json", metadata);
                log.info("End backup({}): completed '{}' snapshot = {} (backup = {})",
                        backup.id(), snapshot.method(), snapshot, backup);
                return snapshot;
            } catch (Exception failure) {
                try {
                    store.remove(prefix);
                } catch (Exception cleanup) {
                    failure.addSuppressed(cleanup);
                }
                throw failure;
            }
        } finally {
            FilesSupport.deleteTree(work);
        }
    }

    public List<Snapshot> snapshots(BackupConfiguration backup, AppSettings settings) throws IOException {
        var snapshots = new ArrayList<Snapshot>();
        try (var store = SnapshotStore.open(backup.to(), settings)) {
            for (long epoch : store.snapshotIds()) {
                String snapshotKey = "snapshots/" + epoch + "/snapshot.json";
                if (!store.exists(snapshotKey))
                    continue;
                InputStream snapshotKeyStream = store.open(snapshotKey);
                try (var snapshotKeyReader = new InputStreamReader(snapshotKeyStream, StandardCharsets.UTF_8)) {
                    var snapshot = ConfigurationStore.read(snapshotKeyReader, Snapshot.class);
                    if (snapshot.epochMillis() != epoch)
                        throw new IOException("Snapshot timestamp does not match its folder: " + epoch);
                    if (snapshot.backupId().equals(backup.id()))
                        snapshots.add(snapshot);
                }
            }
        }
        snapshots.sort(Comparator.comparingLong(Snapshot::epochMillis).reversed());
        return List.copyOf(snapshots);
    }

    public void restore(BackupConfiguration backup, AppSettings settings, Snapshot snapshot, Path destination) throws IOException {
        requireOwner(backup, snapshot);
        var engine = BackupEngine.forMethod(snapshot.method());
        // Let the engine surface its unsupported operation without requiring a network connection.
        if (snapshot.method() == BackupMethod.restic) {
            engine.restore(null, snapshot, destination);
            return;
        }
        try (var store = SnapshotStore.open(backup.to(), settings)) {
            engine.restore(store, snapshot, destination);
        }
    }

    public List<String> diff(BackupConfiguration backup, AppSettings settings, Snapshot snapshot, Consumer<String> progress) throws IOException {
        requireOwner(backup, snapshot);
        var engine = BackupEngine.forMethod(snapshot.method());
        switch (snapshot.method()) {
            case restic -> {
                return engine.diff(null, snapshot, null);
            }
            case tar -> {
                try (var source = SourceFolder.open(backup.from(), settings, progress);
                     var store = SnapshotStore.open(backup.to(), settings)) {
                    return engine.diff(store, snapshot, source.path());
                }
            }
            case null, default -> throw new UnsupportedOperationException();
        }
    }

    private void requireOwner(BackupConfiguration backup, Snapshot snapshot) {
        if (!backup.id().equals(snapshot.backupId()))
            throw new IllegalArgumentException("Snapshot belongs to a different backup");
    }
}
