package totp4j.cli;

import lombok.Data;
import lombok.NonNull;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import totp4j.OtpGenerator;
import totp4j.plumbing.Storage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Command(
        name = "totp4j",
        description = "otp cli based on yitsushi/totp-cli",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        subcommands = {
                Totp4j.List.class,
                Totp4j.Add.class,
                Totp4j.Update.class,
                Totp4j.Remove.class,
                Totp4j.Generate.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class Totp4j {
    public static void main(String[] args) {
        System.exit(new CommandLine(new Totp4j()).execute(args));
    }

    @Data
    @Command(
            name = "list",
            description = "list tokens which have been added",
            mixinStandardHelpOptions = true
    )
    static class List implements Runnable {
        @Override
        public void run() {
            System.out.println("Stored tokens:" +
                    System.lineSeparator() +
                    Storage.folderPerTokenStorage().list().stream().collect(Collectors.joining(System.lineSeparator())));
        }
    }

    @Data
    @Command(
            name = "add",
            description = "set up a new token for OTP generation",
            mixinStandardHelpOptions = true
    )
    static class Add implements Runnable {
        @Parameters(paramLabel = "name")
        String name;

        @Option(names = {"-l", "--length"}, description = "specify OTP generation length (default 6)")
        int length = 6;

        @Option(names = {"-t", "--token"}, description = "the token value")
        String token;

        @Option(names = {"-t:e", "--token-env"}, description = "env var to read the token from")
        String tokenEnv;

        @Override
        public void run() {
            String t = token;
            if (t == null && tokenEnv != null)
                t = System.getenv(tokenEnv);
            if (t == null)
                throw new IllegalStateException("must specify token, no -t, tokenEnv missing/empty");

            Storage<Path> storage = Storage.folderPerTokenStorage();

            if (null != storage.read(name))
                throw new IllegalStateException("cannot use name " + name + ", as it already exists");

            Properties properties = new Properties();
            properties.setProperty("length", String.valueOf(length));
            properties.setProperty("token", t);
            storage.store(name, properties);
            System.out.println("added");
        }
    }

    static class TokenNamesGenerator implements Iterable<String> {
        @Override
        @NonNull
        public Iterator<String> iterator() {
            return Storage.folderPerTokenStorage().list().iterator();
        }
    }

    @Data
    @Command(
            name = "update",
            description = "update the configuration of a token",
            mixinStandardHelpOptions = true
    )
    static class Update implements Runnable {
        @Parameters(paramLabel = "name", completionCandidates = TokenNamesGenerator.class)
        String name;

        // todo make a mixin, this is just here as proof of concept
        @Option(names = {"-l", "--length"}, description = "specify OTP generation length (default 6)")
        int length = 6;

        @Override
        public void run() {
            Storage<Path> storage = Storage.folderPerTokenStorage();

            Properties properties;
            if (null == (properties = storage.read(name)))
                throw new IllegalStateException("cannot update token " + name + ", as it does not exist");

            properties.setProperty("length", String.valueOf(length));
            storage.store(name, properties);
            System.out.println("Updated");
        }
    }

    @Data
    @Command(
            name = "remove",
            description = "remove a token",
            mixinStandardHelpOptions = true
    )
    static class Remove implements Runnable {
        @Parameters(paramLabel = "name", completionCandidates = TokenNamesGenerator.class)
        String name;

        @Override
        public void run() {
            Storage<Path> storage = Storage.folderPerTokenStorage();
            if (null == (storage.read(name)))
                throw new IllegalStateException("cannot remove token " + name + ", as it does not exist");
            storage.remove(name);
            System.out.println("Removed");
        }
    }

    @Data
    @Command(
            name = "generate",
            description = "generate an OTP for a token",
            mixinStandardHelpOptions = true
    )
    static class Generate implements Runnable {
        @Parameters(paramLabel = "name", completionCandidates = TokenNamesGenerator.class)
        String name;

        // boolean follow; // todo

        @Override
        public void run() {
            Storage<Path> storage = Storage.folderPerTokenStorage();

            Properties properties;
            if (null == (properties = storage.read(name)))
                throw new IllegalStateException("cannot generate otp for token " + name + ", as it does not exist");

            int length = Optional.ofNullable(properties.getProperty("length"))
                    .map(Integer::parseInt)
                    .orElse(6);

            String token = Optional.ofNullable(properties.getProperty("token"))
                    .orElseThrow(() -> new IllegalStateException(
                            "token entry was saved to storage, but is missing 'token' field"));

            OtpGenerator.Password password = new OtpGenerator().generate(token, Instant.now(), length);
            System.out.printf("%s (%s seconds remaining)%n", password.getCode(), password.getRemainingTime());
        }
    }
}
