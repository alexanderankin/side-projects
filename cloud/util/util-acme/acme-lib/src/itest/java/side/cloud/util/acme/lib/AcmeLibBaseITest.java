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

import javax.net.ssl.SSLContext;
import java.net.URI;

public abstract class AcmeLibBaseITest {
    protected static PebbleAcmeTestContainer pebbleAcmeTestContainer;

    @BeforeAll
    static void beforeAll() {
        pebbleAcmeTestContainer = new PebbleAcmeTestContainer();
        pebbleAcmeTestContainer.start();
    }

    protected URI getPebbleDirectoryUrl() {
        if (pebbleAcmeTestContainer == null)
            throw new IllegalStateException("PebbleAcmeTestContainer is not initialized");
        return pebbleAcmeTestContainer.directory();
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
