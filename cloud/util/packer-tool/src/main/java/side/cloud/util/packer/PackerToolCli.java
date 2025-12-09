package side.cloud.util.packer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command(
        name = "packer-tool",
        description = "tool for wrangling hc packer",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                PackerToolCli.Config.class,
                PackerToolCli.JumpHost.class,
                PackerToolCli.Init.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class PackerToolCli {
    static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
    static PackerTool packerTool;

    static PackerTool getPackerTool() {
        if (packerTool == null) {
            packerTool = new PackerTool(MAPPER);
        }
        return packerTool;
    }

    static void main(String[] args) {
        System.exit(new CommandLine(PackerToolCli.class).execute(args));
    }

    @Command(name = "config", description = "", scope = CommandLine.ScopeType.INHERIT)
    static class Config extends PackerTool.Config {
        @SneakyThrows
        @Command(name = "list")
        void list() {
            System.out.println(MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(getPackerTool().getConfig()));
        }

        @SneakyThrows
        @Command(name = "get")
        void get(@Parameters(index = "0") String name) {
            var node = MAPPER.valueToTree(getPackerTool().getConfig());
            for (String s : name.split("\\.")) {
                node = node.path(s);
            }
            System.out.println(MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(node));
        }

        @SneakyThrows
        @Command(name = "set")
        void set(@Parameters(index = "0") String name, @Parameters(index = "1") String value) {
            var node = MAPPER.valueToTree(getPackerTool().getConfig());
            var setNode = node;
            var setParentNode = setNode;
            String[] nameParts = name.split("\\.");
            for (String s : nameParts) {
                setParentNode = setNode;
                setNode = setNode.path(s);
            }
            if (setParentNode instanceof ObjectNode objectNode) {
                objectNode.put(nameParts[nameParts.length - 1], value);
            }

            PackerTool.writePackerToolConfig(MAPPER, MAPPER.convertValue(node, Config.class));
        }

        @SneakyThrows
        @Command(name = "set-all", description = "overwrites config file with config from standard input")
        void setAll() {
            PackerTool.writePackerToolConfig(MAPPER, MAPPER.readValue(System.in, Config.class));
        }
    }

    @Command(name = "init", description = "", scope = CommandLine.ScopeType.INHERIT)
    static class Init implements Runnable {
        @Parameters(arity = "1..1")
        File packerInitFile;

        @Option(names = {"-o", "--overwrite"})
        boolean overwrite;

        @Option(names = {"-i", "--integration"}, description = "override default integration")
        PackerTool.SupportedIntegration integration;

        @Override
        public void run() {
            if (packerInitFile.exists() && !packerInitFile.isFile())
                throw new IllegalArgumentException(packerInitFile + " is not a file");
            if (packerInitFile.exists() && !overwrite)
                throw new IllegalStateException(packerInitFile + " exists but --overwrite not set");
            if (!packerInitFile.getName().endsWith(".pkr.hcl"))
                throw new IllegalArgumentException(packerInitFile + " does not have '.pkr.hcl' file extension");
            getPackerTool().init(packerInitFile, integration);
        }
    }

    @Slf4j
    @Command(name = "jump-host", description = "", scope = CommandLine.ScopeType.INHERIT, subcommands = {
            JumpHost.SshKeys.class,
            JumpHost.Init.class,
    })
    static class JumpHost {
        @Command(name = "init")
        static class Init implements Runnable {
            @Parameters(index = "0")
            File tfVarsFile;

            @SneakyThrows
            static Map<String, ?> parseTfVarsFile(File tfVarsFile, ObjectMapper objectMapper) {
                String jsonString = "{" + Files.readString(tfVarsFile.toPath()).lines()
                        .map(l -> {
                            var parts = l.split("=", 2);
                            if (parts.length != 2)
                                return null;
                            String name = parts[0];
                            String valueString = parts[1];
                            return "\"" + name.trim() + "\":" + valueString;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(",")) + "}";
                return objectMapper.readValue(jsonString, new TypeReference<>() {
                });
            }

            @SneakyThrows
            @Override
            public void run() {
                if (!tfVarsFile.exists())
                    throw new IllegalArgumentException(tfVarsFile + " does not exist");
                if (!tfVarsFile.getName().endsWith(".hcl"))
                    throw new IllegalArgumentException(tfVarsFile + " is not a .hcl file");
                Map<String, ?> parsedVars = parseTfVarsFile(tfVarsFile, MAPPER);
                if (!(parsedVars.get("jump_host_ip_address") instanceof String host)) {
                    throw new IllegalArgumentException(tfVarsFile + " does not have a jump_host_ip_address");
                }
                if (!(parsedVars.get("jump_host_user") instanceof String user)) {
                    throw new IllegalArgumentException(tfVarsFile + " does not have a jump_host_user");
                }

                var keyPair = SshKeyPairGenerator.generateRsa4096();
                var dir = tfVarsFile.toPath().getParent();
                Files.writeString(dir.resolve("id_rsa.pub"), keyPair.publicKey());
                Files.writeString(dir.resolve("id_rsa"), keyPair.privateKey());
                new JumpHostInitializer(JumpHostInitializer.getJSch())
                        .add(user, host, keyPair.publicKey());
            }
        }

        @Command(name = "ssh-keys")
        static class SshKeys {
            @Command(name = "init", description = "put public key on jump host")
            void init(@Mixin SshOptions sshOptions, @Parameters(index = "1") Path publicKeyToUpload) {
                new JumpHostInitializer(JumpHostInitializer.getJSch(sshOptions.getIdentityKey()))
                        .add(sshOptions.getUser(), sshOptions.getHost(), publicKeyToUpload);
            }

            @Command(name = "clean", description = "remove old public keys from this tool from jump host")
            void clean(@Mixin SshOptions sshOptions,
                       @Option(names = "--timeout", defaultValue = "7") int timeout,
                       @Option(names = "--timeout-units", defaultValue = "DAYS") TimeUnit timeUnit) {
                new JumpHostInitializer(JumpHostInitializer.getJSch(sshOptions.getIdentityKey()))
                        .clean(sshOptions.getUser(), sshOptions.getHost(), Duration.of(timeout, timeUnit.toChronoUnit()));
            }
        }

        @Data
        @Accessors(chain = true)
        static class SshOptions {
            @Option(names = "--user", defaultValue = "ubuntu")
            String user;
            @Option(names = {"-i", "--identity-key"})
            String identityKey;
            @Parameters(index = "0")
            String host;
        }
    }
}
