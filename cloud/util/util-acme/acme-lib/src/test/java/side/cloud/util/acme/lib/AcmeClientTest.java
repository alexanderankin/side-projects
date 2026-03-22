package side.cloud.util.acme.lib;

import com.fasterxml.jackson.databind.json.JsonMapper;
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
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Orders;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static side.cloud.util.acme.lib.model.AcmeIdentifier.AcmeIdentifierType.dns;

public class AcmeClientTest {
    AcmeClient acmeClient = new AcmeClient(
            new AcmeClient.Configuration()
                    .setDirectoryUrl(URI.create("https://127.0.0.1:14000/dir")),
            new AcmeClientTemplate(modifyToTrustAll(RestClient.builder().build())),
            JsonMapper.builder().findAndAddModules().build()
    );

    @Test
    void test() {
        // var sckp = SupportedClientKeyPairAlgorithm.ES256.generate();
        var sckp = SupportedClientKeyPair.deserialize("v1:ES256:MFkwEwYHKoZIzj0CAQYIKo" +
                "ZIzj0DAQcDQgAEUDPFEhGFNAmgFheD_cDyofsqTgbkBAx4YCt" +
                "P56aU35pb4TzDqpVFV0QcycypvDrWdFAyG2muh7nCeGGKoZY6Ig" +
                ":MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQg_V" +
                "uL02p_VzsiBYeUpKa7HLMYsIquYRR1j7OBIHb_W1agCgYIKoZIz" +
                "j0DAQehRANCAARQM8USEYU0CaAWF4P9wPKh-ypOBuQEDHhgK0_n" +
                "ppTfmlvhPMOqlUVXRBzJzKm8OtZ0UDIbaa6HucJ4YYqhljoi");
        var account = acmeClient.newAccount(sckp, new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        System.out.println(account);

        var orders = acmeClient.getResource(sckp, account.id(), account.resource().getOrders(), Orders.class);
        System.out.println("orders: " + orders);
        if (orders.getOrders().isEmpty()) {
            Instant now = Instant.now();
            var order = acmeClient.newOrder(sckp, account.id(), new AcmeResources.NewOrder()
                    .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("*.localhost.local")))
                    .setNotBefore(now)
                    .setNotAfter(now.plus(Duration.ofDays(10))));
            System.out.println("new order: " + order);
        }

        orders = acmeClient.getResource(sckp, account.id(), account.resource().getOrders(), Orders.class);
        var order = acmeClient.getResource(sckp, account.id(), orders.getOrders().getLast(), AcmeResources.Order.class);
        System.out.println("order: " + order);

        for (var authorization : order.getAuthorizations()) {
            var resource = acmeClient.getResource(sckp, account.id(), authorization, Authorization.class);
            System.out.println(resource);
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
