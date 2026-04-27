package side.cloud.util.acme.client;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPairAlgorithm;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeRequests;
import side.cloud.util.acme.lib.model.AcmeResources;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AcmeClientITest {
    static JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();
    static InMemoryRetryRegistry retryRegistry = new InMemoryRetryRegistry();
    static AcmeClient.Config.Retry pebbleRetry = new AcmeClient.Config.Retry().setAttempts(5).setInterval(Duration.ofMillis(100));
    static PebbleContainers containers;

    @BeforeAll
    static void setUp() {
        containers = new PebbleContainers();
        containers.start();
    }

    @AfterAll
    static void tearDown() {
        containers.stop();
    }

    @Test
    void createAccount() {
        var restTemplate = new RestTemplateBuilder().requestFactory(this::restTemplateTrustAll).build();
        var config = new AcmeClient.Config()
                .setDirectoryUrl(containers.getPebbleDirectoryUrl())
                .setKeyString(SupportedClientKeyPairAlgorithm.ES256.generate().serialize())
                .setNewAccount(new AcmeResources.NewAccount()
                        .setContact(List.of(URI.create("mailto:example@localhost.localhost")))
                        .setTermsOfServiceAgreed(true))
                .setRetry(pebbleRetry);

        var client = new AcmeClient(restTemplate, jsonMapper, retryRegistry, config);
        assertThat(assertThrows(Exception.class, () -> client.accountId(true)).toString(), containsString("accountDoesNotExist"));

        var accountId = client.accountId(false);
        var account = client.getAccount(accountId);
        assertThat(account, is(notNullValue()));
        assertThat(account.getStatus(), is(AcmeResources.Account.AccountStatus.valid));
        assertThat(account.getContact(), contains(URI.create("mailto:example@localhost.localhost")));
        assertThat(account.getOrders(), is(notNullValue()));
        assertThat(account.getExternalAccountBinding(), is(nullValue()));
    }

    @Test
    void orders() {
        var restTemplate = new RestTemplateBuilder().requestFactory(this::restTemplateTrustAll).build();
        var config = new AcmeClient.Config()
                .setDirectoryUrl(containers.getPebbleDirectoryUrl())
                .setKeyString(SupportedClientKeyPairAlgorithm.ES256.generate().serialize())
                .setNewAccount(new AcmeResources.NewAccount()
                        .setContact(List.of(URI.create("mailto:example@localhost.localhost")))
                        .setTermsOfServiceAgreed(true))
                .setRetry(pebbleRetry);

        var client = new AcmeClient(restTemplate, jsonMapper, retryRegistry, config);
        client.createAccount();
        var orders = client.orders();
        assertThat(orders, notNullValue());
        assertThat(orders, empty());
        var order = client.order(new AcmeResources.NewOrder().setIdentifiers(List.of(new AcmeIdentifier().setType(AcmeIdentifier.AcmeIdentifierType.dns).setValue("example.localhost"))));
        assertThat(order, notNullValue());
        var newOrders = client.orders();
        assertThat(newOrders, hasSize(1));
        var newOrder = client.getOrder(newOrders.getFirst());
        assertThat(newOrder, notNullValue());
        assertThat(newOrder.getNotBefore(), is(nullValue()));
        assertThat(newOrder.getNotAfter(), is(nullValue()));
        assertThat(newOrder.getError(), is(nullValue()));
        assertThat(newOrder.getAuthorizations(), is(not(nullValue())));
        assertThat(newOrder.getAuthorizations(), is(not(empty())));
        assertThat(newOrder.getFinalize(), is(notNullValue()));
        assertThat(newOrder.getCertificate(), is(nullValue()));
    }

    @SneakyThrows
    public ClientHttpRequestFactory restTemplateTrustAll() {
        TrustStrategy trustAllStrategy = (chain, authType) -> true;

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, trustAllStrategy)
                .build();

        DefaultClientTlsStrategy tlsStrategy =
                new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setTlsSocketStrategy(tlsStrategy)
                                .build()
                )
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
