package side.learning.acme.acme4j;

import lombok.SneakyThrows;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.net.URI;
import java.net.URL;
import java.security.KeyPair;

public class Acme4JExamples {
    @SneakyThrows
    static void main() {
        Session session = new Session("acme://letsencrypt.org");

        Account account = new AccountBuilder()
                .addContact("mailto:acme@example.com")
                .agreeToTermsOfService()
                .useKeyPair(KeyPairUtils.createKeyPair(4096))
                .create(session);

        URL accountLocationUrl = account.getLocation();


        try (var connect = session.connect()) {
            URL url = URI.create("https://localhost.com").toURL();
            KeyPair kp = null;
            Login login = session.login(URI.create("https://example.com").toURL(), kp);
            connect.sendCertificateRequest(url, login);
        }
    }
}
