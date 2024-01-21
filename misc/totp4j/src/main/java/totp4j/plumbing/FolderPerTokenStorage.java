package totp4j.plumbing;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

class FolderPerTokenStorage implements Storage<Path> {
    Path path;

    FolderPerTokenStorage() {
        init(Path.of(System.getProperty("user.home"), ".config", "totp4j", "tokens"));
    }

    @Override
    public void init(Path config) {
        path = config;
        // noinspection ResultOfMethodCallIgnored
        path.toFile().mkdirs();
    }

    @SneakyThrows
    @Override
    public Properties read(String name) {
        Path resolve = path.resolve(name);
        return Files.isRegularFile(resolve) ? parse(Files.readString(resolve, StandardCharsets.UTF_8)) : null;
    }

    @SneakyThrows
    private Properties parse(String s) {
        Properties properties = new Properties();
        properties.load(new StringReader(s));
        return properties;
    }

    @SneakyThrows
    @Override
    public void store(String name, Properties data) {
        try (FileWriter fileWriter = new FileWriter(new File(path.toString(), name), StandardCharsets.UTF_8)) {
            data.store(fileWriter, null);
        }
    }

    @SneakyThrows
    @Override
    public void remove(String name) {
        Files.deleteIfExists(path.resolve(name));
    }

    @SneakyThrows
    @Override
    public List<String> list() {
        try (Stream<Path> list = Files.list(path)) {
            return list.map(Path::getFileName).map(Path::toString).toList();
        }
    }
}
