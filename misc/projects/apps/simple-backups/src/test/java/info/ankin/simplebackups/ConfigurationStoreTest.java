package info.ankin.simplebackups;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationStoreTest {
    @TempDir Path root;

    @Test void savesAndImportsTheFourFieldConfiguration() throws Exception {
        var store = new ConfigurationStore(root);
        var backup = new BackupConfiguration(UUID.randomUUID(), "Remote folder", URI.create("ssh://alice@server:2222/path%20with%20spaces"),
                URI.create("s3://example-bucket/backups"));
        store.save(backup);
        Path file = root.resolve("backups").resolve(backup.id() + ".json");
        assertEquals(backup, ConfigurationStore.read(file, BackupConfiguration.class));
        assertEquals(4, ConfigurationStore.JSON.readTree(Files.readString(file)).size());
        assertEquals(backup, store.list().backups().getFirst());
    }

    @Test void validatesImportedJsonAndKeepsOtherConfigurationsVisible() throws Exception {
        var store = new ConfigurationStore(root);
        var backup = new BackupConfiguration(UUID.randomUUID(), "Valid", root.toUri(), root.resolve("target").toUri());
        store.save(backup);
        Path invalid = root.resolve("backups/broken.json");
        Files.writeString(invalid, "{\"name\":\"missing required fields\"}");
        assertThrows(IOException.class, () -> ConfigurationStore.read(invalid, BackupConfiguration.class));
        assertEquals(1, store.list().backups().size());
        assertEquals(1, store.list().errors().size());
    }

    @Test void settingsRoundTripAndMissingFieldsUseDefaults() throws Exception {
        var store = new ConfigurationStore(root);
        assertEquals(BackupMethod.tar, store.settings().preferredMethod());
        var settings = new AppSettings(AppSettings.AwsAuthMethod.basic, "custom", "key", "secret", "eu-west-1", "alice", "~/key", BackupMethod.restic);
        store.saveSettings(settings);
        assertEquals(settings, store.settings());
        assertFalse(settings.toString().contains("secret"));
        Files.writeString(root.resolve("config.json"), "{}");
        assertEquals(AppSettings.defaults(), store.settings());
    }

    @Test void readsExistingJsonDocumentsWithoutChangingTheirFormat() throws Exception {
        UUID id = UUID.randomUUID();
        Path backupFile = root.resolve("backup.json");
        Files.writeString(backupFile, """
                {"id":"%s","name":"Existing backup","from":"ssh://alice@server:2222/data%%20files","to":"s3://bucket/backups"}
                """.formatted(id));
        var backup = ConfigurationStore.read(backupFile, BackupConfiguration.class);
        assertEquals(id, backup.id());
        assertEquals("/data files", backup.from().getPath());

        Path settingsFile = root.resolve("config.json");
        Files.writeString(settingsFile, """
                {"awsAuthMethod":"profile","awsProfile":"personal","awsAccessKeyId":"", "awsSecretAccessKey":"",
                 "awsRegion":"eu-west-1","sshUsername":"alice","sshPrivateKey":"~/key","preferredMethod":"restic"}
                """);
        assertEquals("personal", ConfigurationStore.read(settingsFile, AppSettings.class).awsProfile());
        assertEquals(BackupMethod.restic, ConfigurationStore.read(settingsFile, AppSettings.class).preferredMethod());

        Path snapshotFile = root.resolve("snapshot.json");
        Files.writeString(snapshotFile, """
                {"backupId":"%s","epochMillis":1234,"durationMillis":56,"method":"tar"}
                """.formatted(id));
        assertEquals(new Snapshot(id, 1234, 56, BackupMethod.tar), ConfigurationStore.read(snapshotFile, Snapshot.class));
    }

    @Test void rejectsUnknownMethodsAndInvalidSnapshotMetadata() throws Exception {
        Path file = root.resolve("config.json");
        Files.writeString(file, "{\"preferredMethod\":\"unknown\"}");
        assertThrows(IOException.class, () -> ConfigurationStore.read(file, AppSettings.class));
        Files.writeString(file, "{\"backupId\":\"" + UUID.randomUUID() + "\",\"epochMillis\":-1,\"durationMillis\":0,\"method\":\"tar\"}");
        assertThrows(IOException.class, () -> ConfigurationStore.read(file, Snapshot.class));
    }
}
