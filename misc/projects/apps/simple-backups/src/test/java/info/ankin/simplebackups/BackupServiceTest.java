package info.ankin.simplebackups;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {
    @TempDir Path root;
    private final BackupService service = new BackupService();
    private final AppSettings settings = AppSettings.defaults();

    @Test void tarRoundTripAndContentDiff() throws Exception {
        Path source = Files.createDirectory(root.resolve("source"));
        Files.createDirectories(source.resolve("nested/empty"));
        Files.writeString(source.resolve("nested/hello world.txt"), "hello 🌍\n");
        Files.write(source.resolve("binary"), new byte[]{0, 1, -1, 42});
        var backup = configuration(source, root.resolve("target"));

        Snapshot snapshot = service.backup(backup, settings, ignored -> {});
        assertTrue(Files.exists(Path.of(backup.to()).resolve(snapshot.prefix()).resolve("simple.tar.zst")));
        assertEquals(List.of(snapshot), service.snapshots(backup, settings));
        assertEquals(List.of(), service.diff(backup, settings, snapshot, ignored -> {}));

        Path restored = root.resolve("restored");
        service.restore(backup, settings, snapshot, restored);
        assertEquals("hello 🌍\n", Files.readString(restored.resolve("nested/hello world.txt")));
        assertArrayEquals(Files.readAllBytes(source.resolve("binary")), Files.readAllBytes(restored.resolve("binary")));
        assertTrue(Files.isDirectory(restored.resolve("nested/empty")));
        assertFalse(Files.exists(restored.resolve("source")), "The archive contains the folder contents, not a wrapper folder");

        Files.writeString(source.resolve("nested/hello world.txt"), "different content");
        Files.delete(source.resolve("binary"));
        Files.writeString(source.resolve("added"), "new");
        assertEquals(List.of("ADDED    added", "REMOVED  binary", "CHANGED  nested/hello world.txt"),
                service.diff(backup, settings, snapshot, ignored -> {}));
    }

    @Test void snapshotsAreNewestFirstAndExcludeOtherBackupsAndIncompleteRuns() throws Exception {
        Path source = Files.createDirectory(root.resolve("source"));
        var backup = configuration(source, root.resolve("target"));
        for (int i = 0; i < 12; i++) service.backup(backup, settings, ignored -> {});
        service.backup(configuration(source, Path.of(backup.to())), settings, ignored -> {});
        Files.createDirectory(Path.of(backup.to()).resolve("snapshots/123"));
        var snapshots = service.snapshots(backup, settings);
        assertEquals(12, snapshots.size());
        for (int i = 1; i < snapshots.size(); i++)
            assertTrue(snapshots.get(i - 1).epochMillis() > snapshots.get(i).epochMillis());
    }

    @Test void rejectsDestinationInsideSourceIncludingSymlinkAlias() throws Exception {
        Path source = Files.createDirectory(root.resolve("source"));
        assertThrows(IOException.class, () -> service.backup(configuration(source, source.resolve("backups")), settings, ignored -> {}));
        Path alias = Files.createSymbolicLink(root.resolve("alias"), source);
        assertThrows(IOException.class, () -> service.backup(configuration(source, alias.resolve("backups")), settings, ignored -> {}));
    }

    @Test void failedArchiveDoesNotPublishASnapshot() throws Exception {
        Path source = Files.createDirectory(root.resolve("source"));
        Files.createSymbolicLink(source.resolve("link"), root.resolve("missing"));
        var backup = configuration(source, root.resolve("target"));
        assertThrows(IOException.class, () -> service.backup(backup, settings, ignored -> {}));
        assertTrue(service.snapshots(backup, settings).isEmpty());
        try (var entries = Files.list(Path.of(backup.to()).resolve("snapshots"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test void refusesToOverwriteExistingRestoreFiles() throws Exception {
        Path source = Files.createDirectory(root.resolve("source"));
        Files.writeString(source.resolve("file"), "backup content");
        var backup = configuration(source, root.resolve("target"));
        var snapshot = service.backup(backup, settings, ignored -> {});
        Path destination = Files.createDirectory(root.resolve("restore"));
        Files.writeString(destination.resolve("file"), "keep this");
        assertThrows(IOException.class, () -> service.restore(backup, settings, snapshot, destination));
        assertEquals("keep this", Files.readString(destination.resolve("file")));
    }

    @Test void rejectsArchiveTraversalBeforeWritingIntoRestoreDestination() throws Exception {
        var snapshot = new Snapshot(UUID.randomUUID(), 123, 0, BackupMethod.tar);
        Path archive = root.resolve("malicious.tar.zst");
        try (var compressed = new ZstdCompressorOutputStream(Files.newOutputStream(archive));
             var tar = new TarArchiveOutputStream(compressed)) {
            var entry = new TarArchiveEntry("../../outside.txt");
            entry.setSize(1);
            tar.putArchiveEntry(entry);
            tar.write(42);
            tar.closeArchiveEntry();
        }
        Path destination = root.resolve("restore");
        try (var store = new SnapshotStore.Local(root.resolve("target"))) {
            store.reserve(123);
            store.put(snapshot.prefix() + TarBackupEngine.ARCHIVE, archive);
            assertThrows(IOException.class, () -> new TarBackupEngine().restore(store, snapshot, destination));
        }
        try (var files = Files.list(destination)) { assertEquals(0, files.count()); }
        assertFalse(Files.exists(root.resolve("outside.txt")));
    }

    @Test void resticRestoreAndDiffReportUnsupportedWithoutAccessingSourceOrTarget() {
        var backup = configuration(root.resolve("missing"), root.resolve("target"));
        var snapshot = new Snapshot(backup.id(), 123, 0, BackupMethod.restic);
        assertThrows(UnsupportedOperationException.class, () -> service.restore(backup, settings, snapshot, root.resolve("restore")));
        assertThrows(UnsupportedOperationException.class, () -> service.diff(backup, settings, snapshot, ignored -> {}));
    }

    private BackupConfiguration configuration(Path from, Path to) {
        return new BackupConfiguration(UUID.randomUUID(), "Test backup", from.toUri(), to.toUri());
    }
}
