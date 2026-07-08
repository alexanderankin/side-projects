package side.oauthcli;

import info.ankin.projects.picocli.logback.verbosity.LogbackVerbosityMixin;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import picocli.AutoComplete;
import picocli.CommandLine;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@CommandLine.Command(
        name = "oauth-client",
        version = "0.0.1",
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        sortOptions = false,
        subcommands = {
                OAuthClientCli.Clients.class,
                OAuthClientCli.Grants.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class OAuthClientCli {
    static final JsonMapper JSON_MAPPER = JsonMapper.builder().findAndAddModules().build();
    static final RestClient REST_CLIENT = RestClient.builder().build();

    static {
        // LogbackVerbosityMixin.logToStderr();
    }

    @CommandLine.Mixin
    LogbackVerbosityMixin verbosityMixin;

    static void main(String[] args) {
        System.exit(new CommandLine(OAuthClientCli.class)
                .setExecutionExceptionHandler((ex, _, _) -> {
                    System.out.println("error: " + ex.getMessage());
                    log.debug("error: {}", ex.getMessage(), ex);
                    return 1;
                })
                .execute(args));
    }

    @CommandLine.Command(name = "clients")
    static class Clients {
        @CommandLine.Option(names = "--backend")
        ClientsBackendOptions.ClientsBackend backend = ClientsBackendOptions.ClientsBackend.file;

        @CommandLine.ArgGroup
        ClientsBackendOptions backendOptions = new ClientsBackendOptions();

        @CommandLine.Command(name = "list")
        void list() {
            System.out.println(JSON_MAPPER.writeValueAsString(backendOptions.makeType(backend).list()));
        }

        @CommandLine.Command(name = "create")
        void create(@CommandLine.Parameters String name,
                    @CommandLine.ArgGroup(multiplicity = "1") ClientOptions clientOptions) {
            System.out.println(JSON_MAPPER.writeValueAsString(backendOptions.makeType(backend).create(name, clientOptions.readClient(name))));
        }

        @CommandLine.Command(name = "get")
        void get(@CommandLine.Parameters String name) {
            System.out.println(JSON_MAPPER.writeValueAsString(backendOptions.makeType(backend).get(name)));
        }

        @CommandLine.Command(name = "update")
        void update(@CommandLine.Parameters String name,
                    @CommandLine.ArgGroup(multiplicity = "1") ClientOptions clientOptions) {
            System.out.println(JSON_MAPPER.writeValueAsString(backendOptions.makeType(backend).update(name, clientOptions.readClient(name))));
        }

        @CommandLine.Command(name = "delete")
        void delete(@CommandLine.Parameters String name) {
            backendOptions.makeType(backend).delete(name);
        }

        @Data
        @Accessors(chain = true)
        static class ClientOptions {
            @CommandLine.Option(names = {"-j", "--json"})
            String json;

            @CommandLine.ArgGroup(exclusive = false)
            LiteralClientOptions literalClientOptions;

            OAuthClient.OAuthClientClient readClient(String name) {
                LiteralClientOptions co;
                if (json != null) {
                    co = JSON_MAPPER.readValue(json, LiteralClientOptions.class);
                } else {
                    co = literalClientOptions;
                }

                return OAuthClient.OAuthClientClient.of(name, co);
            }

            @Data
            @Accessors(chain = true)
            static class LiteralClientOptions {
                /**
                 * @see OAuthClient.OAuthClientClient#clientId
                 */
                @CommandLine.Option(names = "--client-id")
                String clientId;
                @ToString.Exclude
                @CommandLine.Option(names = "--client-secret")
                String clientSecret;
                @CommandLine.Option(names = "--scope")
                List<String> scope;
                @CommandLine.Option(names = "--token-endpoint")
                String tokenEndpoint;
            }
        }
    }

    @CommandLine.Command(name = "grant")
    static class Grants {
        @CommandLine.Option(names = "--backend")
        ClientsBackendOptions.ClientsBackend backend = ClientsBackendOptions.ClientsBackend.file;

        @CommandLine.ArgGroup
        ClientsBackendOptions backendOptions = new ClientsBackendOptions();

        @CommandLine.Parameters(description = "client name")
        String client;

        OAuthClient tokenService() {
            var clientClient = backendOptions.makeType(backend).get(client);
            Objects.requireNonNull(clientClient, () -> "client '" + client + "' does not exist");

            return new OAuthClient(clientClient, REST_CLIENT);
        }

        @CommandLine.Command(name = "client-credentials", aliases = {"cc", "client_credentials"})
        void clientCredentials() {
            System.out.println(JSON_MAPPER.writeValueAsString(tokenService().clientCredentials()));
        }

        @CommandLine.Command(name = "auth-code", aliases = {"authorization-code", "authorization_code"})
        void authorizationCode(
                @CommandLine.Option(names = "--port") Integer port,
                @CommandLine.Option(names = "--pkce") boolean pkce
        ) {
            Consumer<String> authUrlPresenter = authUrl -> System.out.println("Visit authorization url: " + authUrl);
            var tokenResponse = tokenService().authorizationCode(port, pkce, authUrlPresenter);
            System.out.println(JSON_MAPPER.writeValueAsString(tokenResponse));
        }

        @CommandLine.Command(name = "device", aliases = {"device-grant"})
        void device() {
            Consumer<String> verificationPresenter = System.out::println;
            Runnable waiter = () -> {
                System.out.println("Press any key when verification is complete...");
                try {
                    System.in.readNBytes(1);
                } catch (IOException ignored) {
                }
            };
            System.out.println(JSON_MAPPER.writeValueAsString(tokenService().deviceGrant(verificationPresenter, waiter)));
        }

        @CommandLine.Command(name = "refresh", aliases = {"refresh-token"})
        void refresh(
                @CommandLine.Option(names = {"-r", "--refresh", "--refresh-token"}) String refresh,
                @CommandLine.Option(names = {"-c", "--client-id", "--include-client-id"}) boolean includeClientId
        ) {
            System.out.println(JSON_MAPPER.writeValueAsString(tokenService().refreshGrant(includeClientId, refresh)));
        }
    }

    @Data
    @Accessors(chain = true)
    static class ClientsBackendOptions {
        @CommandLine.ArgGroup(exclusive = false)
        FileBackendOptions fileOptions = new FileBackendOptions();

        @CommandLine.ArgGroup(exclusive = false)
        KeychainBackendOptions keyChainOptions = new KeychainBackendOptions();

        OAuthClientClientBackend makeType(ClientsBackend backend) {
            return switch (backend) {
                case file -> OAuthClientClientBackend.fileBacked(fileOptions.getFile(), JSON_MAPPER);
                case keychain -> OAuthClientClientBackend.keyChainBacked(keyChainOptions.getNamespace(), JSON_MAPPER);
            };
        }

        enum ClientsBackend {
            file,
            keychain,
        }

        @Data
        @Accessors(chain = true)
        static class FileBackendOptions {
            @CommandLine.Option(names = "--backend-file-path")
            Path file = Path.of(System.getProperty("user.home"), ".config", "oauth-client-cli.json");
        }

        @Data
        @Accessors(chain = true)
        static class KeychainBackendOptions {
            @CommandLine.Option(names = "--backend-keychain-namespace")
            String namespace = KeyChain.MacKeyChain.NAMESPACE;
        }
    }
}
