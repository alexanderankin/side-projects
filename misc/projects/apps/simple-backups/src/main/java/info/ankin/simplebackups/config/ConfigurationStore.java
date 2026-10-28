package info.ankin.simplebackups.config;

import info.ankin.simplebackups.FilesSupport;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ConfigurationStore {
    static final JsonMapper JSON = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    private final Path root;

    @SneakyThrows
    public ConfigurationStore(Path root) {
        this.root = root;
        Files.createDirectories(root.resolve("backups"));
        FilesSupport.privatePermissions(root, true);
        FilesSupport.privatePermissions(root.resolve("backups"), true);
    }

    public static Path defaultRoot() {
        return Path.of(System.getProperty("user.home"), ".config", "side-projects-simple-backups");
    }

    @SneakyThrows
    public static <T> T read(Path path, Class<T> type) {
        try (var reader = Files.newBufferedReader(path)) {
            return read(reader, type);
        } catch (RuntimeException e) {
            throw new IOException("Invalid " + path.getFileName() + ": " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static <T> T read(Reader reader, Class<T> type) {
        try {
            // Jackson uses each model's validating constructor, including settings defaults.
            T value = JSON.readValue(reader, type);
            if (value == null) throw new IOException("JSON must contain an object");
            return value;
        } catch (RuntimeException e) {
            throw new IOException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static void write(Path path, Object value) {
        path = path.toAbsolutePath();
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), ".json-", ".tmp");
        try {
            FilesSupport.privatePermissions(temporary, false);
            Files.writeString(temporary, JSON.writeValueAsString(value) + "\n");
            FilesSupport.move(temporary, path);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @SneakyThrows
    public Listing list() {
        var backups = new ArrayList<BackupConfiguration>();
        var errors = new ArrayList<String>();
        try (var paths = Files.list(root.resolve("backups"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                try {
                    var backup = read(path, BackupConfiguration.class);
                    if (!path.getFileName().toString().equals(backup.id() + ".json"))
                        throw new IOException("Filename must match the backup UUID");
                    backups.add(backup);
                } catch (Exception e) {
                    errors.add(path.getFileName() + ": " + e.getMessage());
                }
            }
        }
        backups.sort(Comparator.comparing(BackupConfiguration::name).thenComparing(BackupConfiguration::id));
        return new Listing(List.copyOf(backups), List.copyOf(errors));
    }

    @SneakyThrows
    public void save(BackupConfiguration backup) {
        write(root.resolve("backups").resolve(backup.id() + ".json"), backup);
    }

    @SneakyThrows
    public AppSettings settings() {
        Path path = root.resolve("config.json");
        return Files.exists(path) ? read(path, AppSettings.class) : AppSettings.defaults();
    }

    @SneakyThrows
    public void saveSettings(AppSettings settings) {
        write(root.resolve("config.json"), settings);
    }

    @Data
    @Accessors(fluent = true)
    public static final class Listing {
        private final List<BackupConfiguration> backups;
        private final List<String> errors;
    }
}
