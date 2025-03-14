package info.ankin.debmaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Command(
        name = "deb-maker",
        description = "deb-maker",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        subcommands = {
                DebMaker.Build.class,
                // Totp4j.List.class,
                // Totp4j.Add.class,
                // Totp4j.Update.class,
                // Totp4j.Remove.class,
                // Totp4j.Generate.class,
                // AutoComplete.GenerateCompletion.class,
        }
)
class DebMaker {
    public static void main(String[] args) {
        // args = new String[]{"build"};
        System.exit(new CommandLine(DebMaker.class).execute(args));
    }

    @SneakyThrows
    static void throwException(Exception e) {
        throw e;
    }

    @Command(
            name = "build",
            description = "build a debian package",
            mixinStandardHelpOptions = true
    )
    static class Build implements Runnable {
        @NotNull
        @CommandLine.Option(names = {"-c", "--configuration"})
        Path configuration;

        @NotNull
        @CommandLine.Option(names = {"-o", "--output-dir"})
        Path outputDirectory;

        @AssertTrue(message = "configurationIsFile")
        boolean configurationIsFile() {
            return configuration == null || configuration.toString().equals("-") || configuration.toFile().isFile();
        }

        @AssertTrue(message = "outputDirectoryIsDirectory")
        boolean outputDirectoryIsDirectory() {
            return outputDirectory == null || outputDirectory.toFile().isDirectory();
        }

        @SneakyThrows
        @Override
        public void run() {
            log.debug("building with flags: {}", this);

            Configuration configuration;
            try (var factory = Validation.buildDefaultValidatorFactory()) {
                Validator validator = factory.getValidator();
                Optional.of(validator.validate(this))
                        .filter(Predicate.not(Set::isEmpty)).ifPresent(e -> throwException(new ConstraintViolationException(e)));

                try {
                    InputStream src = this.configuration.toString().equals("-") ? System.in : new FileInputStream(this.configuration.toFile());
                    configuration = JsonMapper.builder().build().readValue(src, Configuration.class);
                } catch (JsonProcessingException ignored) {
                    InputStream src = this.configuration.toString().equals("-") ? System.in : new FileInputStream(this.configuration.toFile());
                    configuration = YAMLMapper.builder().build().readValue(src, Configuration.class);
                }
                log.debug("building with configuration: {}", configuration);

                Optional.of(validator.validate(configuration))
                        .filter(Predicate.not(Set::isEmpty))
                        .ifPresent(e -> throwException(new ConstraintViolationException(e)));
            }

            var name = configuration.getName();

            Map<String, String> arContents = new LinkedHashMap<>();
            arContents.put(
                    "debian/control",
                    "Package: " + name + "\n" +
                            "Version: " + configuration.getVersion() + "\n" +
                            "Section: utils\n" +
                            "Priority: optional\n" +
                            "Depends: libc6 (>= 2.14)");
            arContents.put(
                    name + ".install",
                    configuration.getFiles().stream()
                            .map(installedFile -> {
                                return name + ".files/" + Path.of(installedFile.getBuildPath()).getFileName() + " " + installedFile.getInstalledPath();
                            })
                            .collect(Collectors.joining(System.lineSeparator()))
            );

            for (Configuration.InstalledFile file : configuration.getFiles()) {
                Path buildPath = Path.of(file.getBuildPath());
                arContents.put(name + ".files/" + buildPath.getFileName(), Files.readString(buildPath));
            }

            var os = new ByteArrayOutputStream();
            try (var arOs = new ArArchiveOutputStream(os)) {
                arOs.setLongFileMode(1);
                for (var fileEntry : arContents.entrySet()) {
                    byte[] value = fileEntry.getValue().getBytes(StandardCharsets.UTF_8);
                    ArArchiveEntry archiveEntry = new ArArchiveEntry(fileEntry.getKey(), value.length, 0, 0, 0x644, 0);
                    arOs.putArchiveEntry(archiveEntry);
                    arOs.write(value);
                    arOs.closeArchiveEntry();
                }
            }

            byte[] contents = os.toByteArray();

            var outputFileName = name + "_" + configuration.getVersion() + ".deb";

            log.debug("writing output .deb as {} to {}", outputFileName, outputDirectory);
            Files.write(outputDirectory.resolve(outputFileName), contents);
        }
    }

    @Data
    @Accessors(chain = true)
    static class Configuration {
        @NotBlank
        String name;
        @NotBlank
        String version = "1.0.0";
        @NotNull
        List<@Valid InstalledFile> files = List.of();

        @Data
        @Accessors(chain = true)
        static class InstalledFile {
            @NotBlank
            String installedPath;
            @NotBlank
            String buildPath;
        }
    }
}
