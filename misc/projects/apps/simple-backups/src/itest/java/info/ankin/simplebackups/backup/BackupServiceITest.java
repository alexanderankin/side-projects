package info.ankin.simplebackups.backup;

import com.fasterxml.uuid.Generators;
import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import info.ankin.simplebackups.FilesSupport;
import info.ankin.simplebackups.SimpleBackupsITest;
import info.ankin.simplebackups.backup.engine.BackupEngine;
import info.ankin.simplebackups.backup.snapshot.SnapshotStore;
import info.ankin.simplebackups.config.AppSettings;
import info.ankin.simplebackups.config.BackupConfiguration;
import info.ankin.simplebackups.config.BackupMethod;
import info.ankin.simplebackups.config.ConfigurationStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BackupServiceITest extends SimpleBackupsITest {
    @TempDir
    private Path tempDir;
    private BackupService backupService;
    private FileSystem sftp;
    private String previousHome;
    private AppSettings settings;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        backupService = new BackupService();
        previousHome = System.getProperty("user.home");
        // The real service accepts host keys. do not write to real ~/.ssh.
        Path home = Files.createDirectories(tempDir.resolve("home"));
        System.setProperty("user.home", home.toString());
        var credentials = testcontainersConfig.getSftpContainerCredentials();
        var floci = testcontainersConfig.getFlociContainer();
        settings = new AppSettings(
                AppSettings.AwsAuthMethod.basic,
                "default",
                floci.getAccessKey(),
                floci.getSecretKey(),
                floci.getRegion(),
                credentials.username(),
                testcontainersConfig.getSshPrivateKey().toString(),
                BackupMethod.tar);
        // This connection only arranges/asserts fixtures. BackupService uses its own key-authenticated connection.
        var environment = new SFTPEnvironment()
                .withUsername(credentials.username())
                .withPassword(credentials.password().toCharArray())
                .withConfig("StrictHostKeyChecking", "no")
                .withAgentForwarding(false);
        sftp = FileSystems.newFileSystem(testcontainersConfig.getSftpContainerUri(), environment);
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        try {
            if (sftp != null)
                sftp.close();
        } finally {
            if (previousHome != null) {
                System.setProperty("user.home", previousHome);
            } else {
                System.clearProperty("user.home");
            }
        }
    }

    @SneakyThrows
    @ParameterizedTest(name = "tar lifecycle: {0} source -> {1} destination")
    @CsvSource({
            "file,file",
            "file,s3",
            "ssh,file",
            "ssh,s3",
    })
    void backupDiffChangeDeleteRestoreDiff(String sourceScheme, String destinationScheme) {
        var prefix = "backupDiffChangeDeleteRestoreDiff";
        UUID id = Generators.nameBasedGenerator().generate(prefix + "/" + sourceScheme + "/" + destinationScheme);

        Path source = sourceScheme.equals("ssh")
                ? sftp.getPath("/upload", id.toString(), "source folder")
                : tempDir.resolve("source folder");

        Map<String, byte[]> originalFiles = fixtureFiles();
        writeFixture(source, originalFiles);

        List<String> originalEntries = entries(source);

        var backup = new BackupConfiguration(
                id,
                "Lifecycle " + sourceScheme + " to " + destinationScheme,
                sourceUri(sourceScheme, source),
                destinationUri(destinationScheme, id)
        );
        var configurations = new ConfigurationStore(tempDir.resolve("config"));
        configurations.save(backup);
        configurations.saveSettings(settings);

        // round trip?
        // configurations = new ConfigurationStore(tempDir.resolve("config"));
        // backup = configurations.list().backups().getFirst();
        // settings = configurations.settings();

        var messages = new ArrayList<String>();
        Snapshot snapshot = backupService.backup(backup, settings, messages::add);
        assertFalse(messages.isEmpty());
        assertEquals(BackupMethod.tar, snapshot.method());
        assertEquals(List.of(snapshot), backupService.snapshots(backup, settings));

        try (var store = SnapshotStore.open(backup.to(), settings)) {
            assertTrue(store.exists(snapshot.prefix() + BackupEngine.TAR_ARCHIVE_NAME));
            assertTrue(store.exists(snapshot.prefix() + "snapshot.json"));
        }
        assertEquals(List.of(), backupService.diff(backup, settings, snapshot, messages::add), "Fresh snapshot must match");

        // Same-length changes catch comparisons that accidentally rely only on file size.
        Files.writeString(source.resolve("documents/report.txt"), "after!\n");
        Files.writeString(source.resolve("added.txt"), "created after the snapshot");
        Files.delete(source.resolve("removed.txt"));
        assertEquals(
                List.of("SOURCE ONLY:     added.txt",
                        "CONTENT CHANGED: documents/report.txt",
                        "BACKUP ONLY:     removed.txt"),
                backupService.diff(backup, settings, snapshot, messages::add));

        Snapshot newer = backupService.backup(backup, settings, messages::add);
        assertTrue(newer.epochMillis() > snapshot.epochMillis());
        assertEquals(List.of(newer, snapshot), backupService.snapshots(backup, settings));
        assertEquals(List.of(), backupService.diff(backup, settings, newer, messages::add));

        // lose originals!
        clear(source);
        assertEquals(List.of(), entries(source));
        assertEquals(originalEntries.stream().map(name -> "BACKUP ONLY:     " + name).toList(),
                backupService.diff(backup, settings, snapshot, messages::add),
                "All original entries must now be missing");

        // The UI restores to a local folder, including when the original source is remote.
        Path restored = sourceScheme.equals("file") ? source : tempDir.resolve("restored");
        backupService.restore(backup, settings, snapshot, restored);
        assertRestored(restored, originalFiles, originalEntries);
        var restoredBackup = new BackupConfiguration(backup.id(), backup.name(), restored.toUri(), backup.to());
        assertEquals(List.of(), backupService.diff(restoredBackup, settings, snapshot, messages::add));

        if (sourceScheme.equals("ssh")) {
            // TODO: fix this - Copy the restored contents back over SFTP; the application doesn't offer remote restore.
            // TODO make application offer remote restore
            copyTree(restored, source);
            assertRestored(source, originalFiles, originalEntries);
            assertTrue(Files.readString(tempDir.resolve("home/.ssh/known_hosts")).contains(
                    "]:" + testcontainersConfig.getSftpContainer().getMappedPort(22)));
        }
        assertEquals(List.of(), backupService.diff(backup, settings, snapshot, messages::add), "Restored source must match the original snapshot");
        assertFalse(backupService.diff(backup, settings, newer, messages::add).isEmpty(), "The newer snapshot must still contain the edits");
        assertEquals(List.of(newer, snapshot), backupService.snapshots(backup, settings));
        log.info("Completed {} -> {} lifecycle with {} progress messages", sourceScheme, destinationScheme, messages.size());
    }

    @SneakyThrows
    private static void clear(Path root) {
        try (var children = Files.list(root)) {
            children.forEach(FilesSupport::deleteTree);
        }
    }

    private Map<String, byte[]> fixtureFiles() {
        var files = new TreeMap<String, byte[]>();
        files.put("documents/report.txt", "before\n".getBytes(StandardCharsets.UTF_8));
        files.put("documents/hello world.txt", "Unicode contents: 🌍 café\n".getBytes(StandardCharsets.UTF_8));
        files.put("removed.txt", "restore me".getBytes(StandardCharsets.UTF_8));
        files.put(".hidden", "hidden content".getBytes(StandardCharsets.UTF_8));
        files.put("punctuation [1].txt", "a literal filename".getBytes(StandardCharsets.UTF_8));
        files.put("zero-byte", new byte[0]);
        byte[] binary = new byte[8193];
        new Random(42).nextBytes(binary);
        files.put("binary.dat", binary);
        return files;
    }

    @SneakyThrows
    @Test
    void multipartS3BackupRestoresEveryByte() {
        Path source = Files.createDirectory(tempDir.resolve("large-source"));
        // Incompressible input keeps simple.tar.zst above S3SnapshotStore's 16 MiB multipart threshold.
        byte[] bytes = new byte[17 * 1024 * 1024];
        new Random(123).nextBytes(bytes);
        Files.write(source.resolve("large.bin"), bytes);
        UUID id = UUID.randomUUID();
        var backup = new BackupConfiguration(id, "Multipart S3 backup", source.toUri(), destinationUri("s3", id));
        var messages = new ArrayList<String>();
        Snapshot snapshot = backupService.backup(backup, settings, messages::add);
        try (var s3 = s3Client()) {
            var archive = s3.headObject(b -> b.bucket(backup.to().getHost())
                    .key("nested/backups/" + snapshot.prefix() + BackupEngine.TAR_ARCHIVE_NAME));
            assertTrue(archive.contentLength() > 16 * 1024 * 1024, "This test must exercise multipart upload");
        }
        assertEquals(List.of(), backupService.diff(backup, settings, snapshot, messages::add));
        clear(source);
        assertEquals(List.of("BACKUP ONLY:     large.bin"), backupService.diff(backup, settings, snapshot, messages::add));
        backupService.restore(backup, settings, snapshot, source);
        assertArrayEquals(bytes, Files.readAllBytes(source.resolve("large.bin")));
        assertEquals(List.of(), backupService.diff(backup, settings, snapshot, messages::add));
    }

    @SneakyThrows
    private void writeFixture(Path source, Map<String, byte[]> files) {
        Files.createDirectories(source.resolve("empty/nested"));
        for (var entry : files.entrySet()) {
            Path path = source.resolve(entry.getKey());
            Files.createDirectories(path.getParent());
            Files.write(path, entry.getValue());
        }
    }

    @SneakyThrows
    private URI sourceUri(String scheme, Path source) {
        if (scheme.equals("file")) return source.toUri();
        var endpoint = testcontainersConfig.getSftpContainerUri();
        return new URI("ssh", settings.sshUsername(), endpoint.getHost(), endpoint.getPort(), source.toString(), null, null);
    }

    private URI destinationUri(String scheme, UUID id) {
        if (scheme.equals("file")) return tempDir.resolve("backups").toUri();
        String bucket = "backup-itest-" + id;
        try (var s3 = s3Client()) {
            s3.createBucket(b -> b.bucket(bucket));
        }
        return URI.create("s3://" + bucket + "/nested/backups");
    }

    private S3Client s3Client() {
        return S3Client.builder().endpointOverride(testcontainersConfig.getS3Endpoint())
                .region(Region.of(settings.awsRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(settings.awsAccessKeyId(), settings.awsSecretAccessKey())))
                .build();
    }

    @SneakyThrows
    private List<String> entries(Path root) {
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> !path.equals(root)).map(path -> root.relativize(path).toString()).sorted().toList();
        }
    }

    @SneakyThrows
    private void assertRestored(Path root, Map<String, byte[]> files, List<String> originalEntries) {
        assertEquals(originalEntries, entries(root), "Restore must preserve paths and empty directories, without a wrapper folder");
        for (var file : files.entrySet())
            assertArrayEquals(file.getValue(), Files.readAllBytes(root.resolve(file.getKey())), file.getKey());
    }

    @SneakyThrows
    private void copyTree(Path from, Path to) {
        try (var paths = Files.walk(from)) {
            for (Path path : paths.filter(p -> !p.equals(from)).toList()) {
                // A Path from the local provider cannot be resolved directly against an SFTP Path.
                Path target = to.resolve(from.relativize(path).toString());
                if (Files.isDirectory(path)) Files.createDirectories(target);
                else Files.copy(path, target);
            }
        }
    }
}
