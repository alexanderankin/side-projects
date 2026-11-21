package side.scratch.opaque.client;

import com.codeheadsystems.hofmann.client.accessor.HofmannOpaqueAccessor;
import com.codeheadsystems.hofmann.client.manager.HofmannOpaqueClientManager;
import com.codeheadsystems.hofmann.client.model.ServerConnectionInfo;
import com.codeheadsystems.hofmann.client.model.ServerIdentifier;
import com.codeheadsystems.hofmann.model.opaque.AuthFinishResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
@Command(name = "opaque", mixinStandardHelpOptions = true,
        description = "Register and log in using OPAQUE.",
        subcommands = {OpaqueClientCli.Register.class, OpaqueClientCli.Login.class})
public class OpaqueClientCli implements Runnable {

    private static final ServerIdentifier SERVER_ID = new ServerIdentifier("server");

    @Option(names = "--server", defaultValue = "http://127.0.0.1:8080",
            description = "Server URL (default: ${DEFAULT-VALUE})")
    private URI server;

    public static void main(String[] args) {
        System.exit(new CommandLine(new OpaqueClientCli()).execute(args));
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    private HofmannOpaqueClientManager client() {
        var accessor = new HofmannOpaqueAccessor(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                Map.of(SERVER_ID, new ServerConnectionInfo(server)));
        return new HofmannOpaqueClientManager(accessor);
    }

    @Data
    private static class Credentials {
        @Option(names = {"-u", "--user"}, required = true, description = "User name")
        private String user;
    }

    private abstract static class AuthCommand implements Callable<Integer> {
        @ParentCommand
        private OpaqueClientCli parent;

        @CommandLine.Mixin
        private Credentials credentials;

        @Option(names = "--password-stdin", description = "Read one password line from standard input")
        private boolean passwordStdin;

        @SneakyThrows
        protected Integer execute(boolean register) {
            Console console = System.console();
            char[] password;
            if (passwordStdin) {
                String line = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
                password = line == null ? null : line.toCharArray();
            } else if (console != null) {
                password = console.readPassword("Password: ");
            } else {
                System.err.println("A terminal is required. Use --password-stdin when piping a password.");
                return 2;
            }
            if (password == null || password.length == 0) {
                System.err.println("Password is required.");
                return 2;
            }

            byte[] passwordBytes = null;
            try {
                ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
                passwordBytes = new byte[encoded.remaining()];
                encoded.get(passwordBytes);
                byte[] userBytes = credentials.user.getBytes(StandardCharsets.UTF_8);

                try {
                    if (register) {
                        parent.client().register(SERVER_ID, userBytes, passwordBytes);
                        System.out.println("Registered " + credentials.user);
                    } else {
                        AuthFinishResponse response = parent.client().authenticate(SERVER_ID, userBytes, passwordBytes);
                        log.info("Authenticated {}", credentials.user);
                        System.out.println("Login successful. Bearer token: " + response.token());
                    }
                } catch (SecurityException e) {
                    System.err.println(register ? "Registration failed." : "Login failed.");
                    return 1;
                }
                return 0;
            } finally {
                Arrays.fill(password, '\0');
                if (passwordBytes != null) {
                    Arrays.fill(passwordBytes, (byte) 0);
                }
            }
        }
    }

    @Command(name = "register", description = "Register a user with the server.")
    static class Register extends AuthCommand {
        @Override
        public Integer call() {
            return execute(true);
        }
    }

    @Command(name = "login", description = "Authenticate a registered user.")
    static class Login extends AuthCommand {
        @Override
        public Integer call() {
            return execute(false);
        }
    }
}
