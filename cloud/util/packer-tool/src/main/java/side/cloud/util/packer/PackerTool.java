package side.cloud.util.packer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PackerTool {
    final String version;
    @Getter
    final Config config;
    final ObjectMapper objectMapper;

    public PackerTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        version = findPackerVersion();
        config = readPackerToolConfig(objectMapper);
    }

    @SneakyThrows
    static String findPackerVersion() {
        var process = new ProcessBuilder()
                .command("packer", "version")
                .start();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }

        if (process.exitValue() != 0) {
            throw new IllegalStateException("'packer version' failed - could not determine packer version");
        }

        var ver = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (ver.isBlank()) {
            var error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("'packer version' was blank - could not determine packer version, error: " + error);
        }
        return ver;
    }

    @SneakyThrows
    public static Config readPackerToolConfig(ObjectMapper objectMapper) {
        File file = Path.of(System.getProperty("user.home"), ".config", "packer-tool.json").toFile();
        if (!file.exists()) {
            return new Config();
        }
        return objectMapper.readValue(file, Config.class);
    }

    @SneakyThrows
    public static void writePackerToolConfig(ObjectMapper objectMapper, Config config) {
        objectMapper.writeValue(Files.newOutputStream(Path.of(System.getProperty("user.home"), ".config", "packer-tool.json")), config);
    }

    @SneakyThrows
    public void init(File file, SupportedIntegration supportedIntegration) {
        if (supportedIntegration == null)
            supportedIntegration = config.getDefaultIntegration();
        if (supportedIntegration == null)
            throw new IllegalStateException("'supportedIntegration' not specified and default not configured");

        if (!file.getName().endsWith(".pkr.hcl"))
            throw new IllegalArgumentException("file does not end with '.pkr.hcl'");
        var baseName = file.getName().replace(".pkr.hcl", "");

        var resource = getClass().getResourceAsStream(supportedIntegration.name() + ".pkr.hcl");
        if (resource == null)
            throw new IllegalStateException("platform is not supported for initialization: " + supportedIntegration);
        String template;
        try (var ignored = resource) {
            template = new String(ignored.readAllBytes(), StandardCharsets.UTF_8);
        }

        var packerVarsFile = "variables-" + baseName + ".hcl";
        var packerOutputFile = "build/" + baseName + "-manifest.json";
        var tfSourceFile = baseName + ".tf";
        var tfVarsFile = baseName + ".tfvars";

        template = "// packer build -var-file=" + packerVarsFile + " " + file.getName() + "\n" +
                template
                        .replaceAll("PROJECT_NAME", baseName)
                        .replace("build/manifest.json", packerOutputFile);

        Files.writeString(file.toPath(), template);
        log.info("packer file created: {}", file.getAbsolutePath());

        log.info("initializing with packer init...");
        var process = new ProcessBuilder()
                .command("packer", "init", file.getAbsolutePath())
                .directory(file.getParentFile())
                .inheritIO()
                .start();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
        if (process.exitValue() != 0) {
            log.warn("'packer init' command failed");
            throw new IllegalStateException("'packer init' command failed");
        }
        log.info("packer init called");

        try (var tfFile = getClass().getResourceAsStream(supportedIntegration.name() + ".tf")) {
            Objects.requireNonNull(tfFile); // you think someone would just go and partially implement an integration?

            var content = new String(tfFile.readAllBytes(), StandardCharsets.UTF_8);
            content = "// tf apply -var-file=" + tfVarsFile + "\n" + content;
            Files.writeString(new File(file.getParentFile(), tfSourceFile).toPath(), content);
        }
        try (var tfFile = getClass().getResourceAsStream(supportedIntegration.name() + ".tfvars")) {
            Objects.requireNonNull(tfFile); // you think someone would just go and partially implement an integration?

            var content = new String(tfFile.readAllBytes(), StandardCharsets.UTF_8);
            content = content.replace("variables.hcl", packerVarsFile);
            Files.writeString(new File(file.getParentFile(), tfVarsFile).toPath(), content);
        }
    }

    public enum SupportedIntegration {
        /**
         * <a href="https://developer.hashicorp.com/packer/integrations/hashicorp/amazon">developer.hashicorp.com</a>
         */
        AMAZON,
        /**
         * <a href="https://developer.hashicorp.com/packer/integrations/hashicorp/oracle">developer.hashicorp.com</a>
         */
        ORACLE,
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        SupportedIntegration defaultIntegration;
    }
}
