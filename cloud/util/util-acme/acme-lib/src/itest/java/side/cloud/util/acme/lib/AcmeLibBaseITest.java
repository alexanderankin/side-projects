package side.cloud.util.acme.lib;

import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.Network;
import side.cloud.util.acme.lib.challenges.pebble.PebbleChallengeServerClient;
import side.cloud.util.acme.lib.containers.PebbleAcmeServerTestContainer;
import side.cloud.util.acme.lib.containers.PebbleChallengeServerTestContainer;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.Objects;

public abstract class AcmeLibBaseITest {
    protected static PebbleChallengeServerTestContainer pebbleChallengeTestContainer;
    protected static PebbleAcmeServerTestContainer pebbleAcmeTestContainer;

    @BeforeAll
    static void beforeAll() {
        var network = Network.newNetwork();
        pebbleChallengeTestContainer = new PebbleChallengeServerTestContainer();
        pebbleChallengeTestContainer.withNetwork(network).withNetworkAliases("pebble-challenge-server");
        pebbleChallengeTestContainer.start();
        pebbleAcmeTestContainer = new PebbleAcmeServerTestContainer();
        pebbleAcmeTestContainer.withNetwork(network).withNetworkAliases("pebble");
        pebbleAcmeTestContainer.withDnsServer("pebble-challenge-server:" + PebbleChallengeServerTestContainer.DNS_SERVER_PORT);
        pebbleAcmeTestContainer.start();
    }

    protected URI getPebbleDirectoryUrl() {
        Objects.requireNonNull(pebbleAcmeTestContainer, "PebbleAcmeServerTestContainer is not initialized");
        return pebbleAcmeTestContainer.directory();
    }

    protected PebbleChallengeServerClient getPebbleChallengeClient() {
        Objects.requireNonNull(pebbleChallengeTestContainer, "PebbleChallengeTestContainer is not initialized");
        return PebbleChallengeServerClientHolder.client;
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

    private static final class PebbleChallengeServerClientHolder {
        private static final PebbleChallengeServerClient client =
                new PebbleChallengeServerClient(pebbleChallengeTestContainer.baseUrl());
    }
}
