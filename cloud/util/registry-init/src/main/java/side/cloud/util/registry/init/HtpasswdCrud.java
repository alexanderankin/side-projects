package side.cloud.util.registry.init;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

@RequiredArgsConstructor
public class HtpasswdCrud {
    final SecureRandom secureRandom;

    @SneakyThrows
    public List<String> list(Path htpasswd) {
        return Files.readAllLines(htpasswd);
    }

    public void create(Path htpasswd, String username, String password) {
        create(htpasswd, username, password, false);
    }

    @SneakyThrows
    public void create(Path htpasswd, String username, String password, boolean mustBeNew) {
        String contents;
        if (mustBeNew) {
            contents = "";
        } else
            try {
                contents = Files.readString(htpasswd);
            } catch (IOException e) {
                contents = "";
            }

        String usernamePrefix = username + ":";
        contents.lines()
                .filter(l -> l.startsWith(usernamePrefix))
                .findAny()
                .ifPresent(ignored -> {
                    throw new IllegalArgumentException("username already exists");
                });

        boolean needsNewLine =
                !contents.isEmpty() && contents.charAt(contents.length() - 1) != '\n';

        try (var out = Files.newOutputStream(htpasswd,
                mustBeNew
                        ? new OpenOption[]{CREATE_NEW}
                        : new OpenOption[]{APPEND, CREATE})) {
            out.write(
                    ((needsNewLine ? "\n" : "")
                            + usernamePrefix
                            + bcrypt(password)
                            + "\n")
                            .getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    @SneakyThrows
    public String read(Path htpasswd, String username) {
        String usernamePrefix = username + ":";

        return Files.readAllLines(htpasswd).stream()
                .filter(e -> e.startsWith(usernamePrefix))
                .findAny()
                .orElseThrow(() ->
                        new IllegalArgumentException("username does not exist for reading"));
    }

    @SneakyThrows
    public void update(Path htpasswd, String username, String password) {
        List<String> lines = Files.readAllLines(htpasswd);
        String usernamePrefix = username + ":";

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(usernamePrefix)) {
                lines.set(i, usernamePrefix + bcrypt(password));
                Files.writeString(htpasswd, String.join("\n", lines) + "\n");
                return;
            }
        }

        throw new IllegalArgumentException("username does not exist for update");
    }

    @SneakyThrows
    public void delete(Path htpasswd, String username) {
        List<String> lines = Files.readAllLines(htpasswd);
        String usernamePrefix = username + ":";

        List<String> withoutUser =
                lines.stream()
                        .filter(l -> !l.startsWith(usernamePrefix))
                        .toList();

        if (lines.size() == withoutUser.size()) {
            throw new IllegalArgumentException("username does not exist for delete");
        }

        Files.writeString(htpasswd, String.join("\n", withoutUser) + "\n");
    }

    public String bcrypt(String password) {
        return OpenBSDBCrypt.generate(password.toCharArray(), generateSalt(16), 10);
    }

    public byte[] generateSalt(int bytes) {
        byte[] salt = new byte[bytes];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
