package side.cloud.util.registry.init;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import side.picocli.LogbackVerbosityMixin;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Command(
        name = "registry-init",
        // description = "",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                RegistryInitCli.Init.class,
                RegistryInitCli.Creds.class,
                RegistryInitCli.Util.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class RegistryInitCli {
    @Getter(lazy = true)
    private static final JsonMapper mapper = JsonMapper.builder().build();
    @Getter(lazy = true)
    private static final SecureRandom secureRandom = new SecureRandom();
    @Getter(lazy = true)
    private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
    @Getter(lazy = true)
    private static final HtpasswdCrud htpasswdCrud = new HtpasswdCrud(getSecureRandom());
    @Getter(lazy = true)
    private static final SelfSignedCertGenerator certGen = new SelfSignedCertGenerator(getBouncyCastleProvider(), getSecureRandom());

    @Mixin
    LogbackVerbosityMixin logbackVerbosityMixin;

    static void main(String[] args) {
        System.exit(new CommandLine(new RegistryInitCli()).execute(args));
    }

    @Data
    @Accessors(chain = true)
    @Command(name = "init")
    static class Init implements Runnable {
        @Parameters(index = "0", arity = "1", description = "path to existing directory to init")
        Path dir;

        @Option(names = {"-o", "--overwrite"}, defaultValue = "false")
        boolean overwrite;

        @Option(names = {"-n", "--dry-run"}, defaultValue = "false")
        boolean dryRun;

        @SneakyThrows
        @Override
        public void run() {
            OpenOption[] options = overwrite ? new OpenOption[]{} : new OpenOption[]{StandardOpenOption.CREATE_NEW};

            var h = getHtpasswdCrud();
            var admin = Base64.getUrlEncoder().encodeToString(h.generateSalt(32));
            var htpasswd = "admin:" + h.bcrypt(admin) + "\n";

            SelfSignedCertGenerator certGen = getCertGen();
            SelfSignedCertGenerator.Certificate cert = certGen.generate();
            String compose = new Util.Compose()
                    .setRegistryProps(new Util.Compose.RegistryProps()
                            .setImageTag("3")
                            .setPort(5000)
                            .setLogLevel(Util.Compose.RegistryProps.RegistryLogLevel.info)
                            .setRegistryDir(dir))
                    .render();

            if (dryRun) {
                log.warn("not writing files, dry run selected");
            } else {
                Files.writeString(dir.resolve("registry.htpasswd"), htpasswd, options);
                Files.writeString(dir.resolve("registry.cert.pem"), cert.cert(), options);
                Files.writeString(dir.resolve("registry.key.pem"), cert.key(), options);
                Files.writeString(dir.resolve("registry.admin"), admin, options);
                Files.writeString(dir.resolve("compose.yaml"), compose, options);
            }
        }
    }

    @Command(name = "creds")
    static class Creds {
        @SneakyThrows
        @Command(name = "list")
        void list(@Mixin Dir dir) {
            var result = getHtpasswdCrud().list(dir.resolve());
            System.out.println(getMapper().writeValueAsString(result));
        }

        @SneakyThrows
        @Command(name = "create")
        void create(@Mixin Dir dir,
                    @Parameters(index = "1", arity = "1") String username,
                    @Parameters(index = "2", arity = "1") String password) {
            getHtpasswdCrud().create(dir.resolve(), username, password);
            System.out.println(getMapper().writeValueAsString(username));
        }

        @SneakyThrows
        @Command(name = "read")
        void read(@Mixin Dir dir,
                  @Parameters(index = "1", arity = "1") String username) {
            var entry = getHtpasswdCrud().read(dir.resolve(), username);
            System.out.println(getMapper().writeValueAsString(entry));
        }

        @SneakyThrows
        @Command(name = "update")
        void update(@Mixin Dir dir,
                    @Parameters(index = "1", arity = "1") String username,
                    @Parameters(index = "2", arity = "1") String password) {
            getHtpasswdCrud().update(dir.resolve(), username, password);
            System.out.println(getMapper().writeValueAsString(username));
        }

        @SneakyThrows
        @Command(name = "delete")
        void delete(@Mixin Dir dir,
                    @Parameters(index = "1", arity = "1") String username) {
            getHtpasswdCrud().delete(dir.resolve(), username);
            System.out.println(getMapper().writeValueAsString(username));
        }

        @Data
        @Accessors(chain = true)
        static class Dir {
            @Parameters(index = "0", arity = "1", description = "path to initialized directory to update")
            Path dir;

            @Option(names = {"--htpasswd", "--htpasswd-file"}, defaultValue = "registry.htpasswd")
            String htpasswdName;

            Path resolve() {
                return dir.resolve(htpasswdName);
            }
        }
    }

    @Command(name = "util", subcommands = {
            Util.Compose.class,
            Util.Credentials.class,
    })
    static class Util {
        @Data
        @Accessors(chain = true)
        @Command(name = "compose")
        static class Compose implements Runnable {
            @Mixin
            RegistryProps registryProps;

            @Option(names = {"-o", "--output", "--output-file"}, description = "compose.yaml, or - for stdout")
            Path outputFile;

            @SneakyThrows
            @Override
            public void run() {
                var value = render();
                if (outputFile.toString().equals("-")) {
                    System.out.println(value);
                } else {
                    Files.writeString(outputFile, value);
                }
            }

            public String render() {
                var template = """
                        services:
                          distribution:
                            image: "registry:___IMAGE_TAG___"
                            ports:
                              - "127.0.0.1:___LOCAL_PORT___:5000/tcp"
                            volumes:
                              - '___REGISTRY_DIR___/registry.htpasswd:/htpasswd'
                              - '___REGISTRY_DIR___/registry.cert.pem:/cert.pem'
                              - '___REGISTRY_DIR___/registry.key.pem:/key.pem'
                            environment:
                              REGISTRY_AUTH: htpasswd
                              REGISTRY_AUTH_HTPASSWD_REALM: realm
                              REGISTRY_AUTH_HTPASSWD_PATH: /htpasswd
                              REGISTRY_HTTP_TLS_CERTIFICATE: /cert.pem
                              REGISTRY_HTTP_TLS_KEY: /key.pem
                              OTEL_TRACES_EXPORTER: none
                              OTEL_METRICS_EXPORTER: none
                              REGISTRY_LOG_LEVEL: info
                        """;

                return template
                        .replace("___IMAGE_TAG___", registryProps.getImageTag())
                        .replace("___LOCAL_PORT___", registryProps.getPort().toString())
                        .replace("___REGISTRY_DIR___", registryProps.getRegistryDir().toString());
            }

            @Data
            @Accessors(chain = true)
            static class RegistryProps {
                @Option(names = {"-t", "--tag"}, defaultValue = "3")
                String imageTag;
                @Option(names = "--port", defaultValue = "5000")
                Integer port;
                @Option(names = {"-l", "--log", "--log-level"}, defaultValue = "info")
                RegistryLogLevel logLevel;
                @Option(names = {"-d", "--dir", "--registry-dir"}, required = true)
                Path registryDir;

                // https://distribution.github.io/distribution/about/configuration/#log
                enum RegistryLogLevel {
                    error, warn, info, debug
                }
            }
        }

        @Command(name = "credentials", aliases = {"c", "creds"})
        static class Credentials {
            @SneakyThrows
            @Command(name = "generate", aliases = "g")
            void generate(@Parameters(index = "0", arity = "0..1", defaultValue = "registry.htpasswd") Path htpasswd) {
                HtpasswdCrud htpasswdCrud = getHtpasswdCrud();
                String password = Base64.getUrlEncoder().encodeToString(htpasswdCrud.generateSalt(32));
                htpasswdCrud.create(htpasswd, "admin", password, true);
            }

            @SneakyThrows
            @Command(name = "list")
            void list(@Parameters(index = "0", arity = "1") Path htpasswd) {
                var result = getHtpasswdCrud().list(htpasswd);
                System.out.println(getMapper().writeValueAsString(result));
            }


            @SneakyThrows
            @Command(name = "create")
            void create(@Parameters(index = "0", arity = "1") Path htpasswd,
                        @Parameters(index = "1", arity = "1") String username,
                        @Parameters(index = "2", arity = "1") String password) {
                getHtpasswdCrud().create(htpasswd, username, password);
                System.out.println(getMapper().writeValueAsString(username));
            }

            @SneakyThrows
            @Command(name = "read")
            void read(@Parameters(index = "0", arity = "1") Path htpasswd,
                      @Parameters(index = "1", arity = "1") String username) {
                var entry = getHtpasswdCrud().read(htpasswd, username);
                System.out.println(getMapper().writeValueAsString(entry));
            }

            @SneakyThrows
            @Command(name = "update")
            void update(@Parameters(index = "0", arity = "1") Path htpasswd,
                        @Parameters(index = "1", arity = "1") String username,
                        @Parameters(index = "2", arity = "1") String password) {
                getHtpasswdCrud().update(htpasswd, username, password);
                System.out.println(getMapper().writeValueAsString(username));
            }

            @SneakyThrows
            @Command(name = "delete")
            void delete(@Parameters(index = "0", arity = "1") Path htpasswd,
                        @Parameters(index = "1", arity = "1") String username) {
                getHtpasswdCrud().delete(htpasswd, username);
                System.out.println(getMapper().writeValueAsString(username));
            }
        }
    }
}
