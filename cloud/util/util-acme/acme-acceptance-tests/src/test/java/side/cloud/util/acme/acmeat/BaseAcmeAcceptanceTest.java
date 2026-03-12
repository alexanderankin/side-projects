package side.cloud.util.acme.acmeat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.security.KeyPair;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static side.cloud.util.acme.lib.model.SupportedClientKeyPairAlgorithm.ES256;

public abstract class BaseAcmeAcceptanceTest {
    private static final AtomicBoolean setupLogging = new AtomicBoolean();

    protected static String acmeServerBaseUrl =
            Objects.requireNonNull(System.getProperty("acmeServerBaseUrl"),
                    "acmeServerBaseUrl property is not set");

    protected static KeyPair exampleKey =
            ES256.parse("v1:ES256:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWojydURCSl" +
                    "BV8Phle8Tq0GkgGDG70pYY3jhR2u954IEraR_50aiOHhJYVxSd8OIMynDGKJR" +
                    "QZ76TR0a4DDr23g:MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBA" +
                    "QQgZL-cUlMnz7LicgxsIruvIOvX7nqbkhDrOlVljVNzUEWgCgYIKoZIzj0DAQeh" +
                    "RANCAARaiPJ1REJKUFXw-GV7xOrQaSAYMbvSlhjeOFHa73nggStpH_nRqI4eElhX" +
                    "FJ3w4gzKcMYolFBnvpNHRrgMOvbe");

    @BeforeAll
    static void setup() {
        if (setupLogging.compareAndSet(false, true)) {
            ((LoggerContext) LoggerFactory.getILoggerFactory())
                    .getLogger("side.cloud.util.acme")
                    .setLevel(Level.DEBUG);
        }
    }

    @SneakyThrows
    protected RestClient modifyToTrustAll(RestClient restClient) {
        TrustStrategy trustAllStrategy = (_, _) -> true;

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, trustAllStrategy)
                .build();

        DefaultClientTlsStrategy ts =
                new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(ts).build())
                .build();

        return restClient.mutate()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }
}
