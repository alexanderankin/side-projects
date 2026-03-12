package side.cloud.util.acme.lib;

import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.SupportedClientKeyPairAlgorithm;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class AcmeClientTest {
    @Test
    void test() {
        var acmeClient = new AcmeClient(
                new AcmeClientService(
                        new AcmeClientService.Config(URI.create("https://localhost:8080/dir"))
                                .setUserAgent("agent")
                                .setKeyPair(SupportedClientKeyPairAlgorithm.RS256.generate())
                )
        );

        acmeClient.getAcmeClientService().setRestClient(
                modifyToTrustAll(acmeClient.getAcmeClientService().getRestClient())
        );

        var account = acmeClient.newAccount(new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        System.out.println(account);

        Instant now = Instant.now();
        var order = acmeClient.newOrder(new AcmeResources.NewOrder()
                .setIdentifiers(List.of(new AcmeIdentifier()))
                .setNotBefore(now)
                .setNotAfter(now.plus(Duration.ofDays(10))));
        System.out.println(order);
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
