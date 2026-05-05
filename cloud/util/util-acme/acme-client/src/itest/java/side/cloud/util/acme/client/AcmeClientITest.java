package side.cloud.util.acme.client;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.*;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.containers.pebble.PebbleAcmeServerTestContainer;
import side.cloud.util.acme.lib.containers.pebble.PebbleAcmeServerTestContainer.BuiltInConfig;
import side.cloud.util.acme.lib.keys.CsrBuilder;
import side.cloud.util.acme.lib.keys.ExternalAccountCredential;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPairAlgorithm;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Account;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge.ChallengeStatus;
import side.cloud.util.acme.lib.model.AcmeResources.NewAccount;
import side.cloud.util.acme.lib.model.AcmeResources.NewOrder;
import side.cloud.util.acme.lib.model.AcmeResources.Order.OrderStatus;
import side.cloud.util.acme.lib.model.challenge.ChallengeInput;
import side.cloud.util.acme.lib.model.challenge.ChallengeSolver;
import side.cloud.util.acme.lib.model.challenge.SupportedChallengeType;
import side.cloud.util.acme.lib.model.challenge.persistence.ChallengeSolutionRepository;
import side.cloud.util.acme.lib.model.challenge.presentation.ChallengePresenter;
import side.cloud.util.acme.lib.model.challenge.presentation.ExternalVerifier;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class AcmeClientITest {
    static JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();
    static InMemoryRetryRegistry retryRegistry = new InMemoryRetryRegistry();
    static AcmeClient.Config.Retry pebbleRetry = new AcmeClient.Config.Retry().setAttempts(5).setInterval(Duration.ofMillis(100));
    static PebbleContainers containers;
    AcmeClient.Config config;
    AcmeClient client;

    @BeforeAll
    static void setUp() {
        containers = new PebbleContainers();
        containers.start();
    }

    @AfterAll
    static void tearDown() {
        containers.stop();
    }

    @BeforeEach
    void setUpEach() {
        var restTemplate = new RestTemplateBuilder().requestFactory(this::restTemplateTrustAll).build();
        config = new AcmeClient.Config()
                .setDirectoryUrl(containers.getPebbleDirectoryUrl())
                // .setDirectoryUrl(URI.create("http://127.0.0.1:8080/acme/directory"))
                .setKeyString(SupportedClientKeyPairAlgorithm.ES256.generate().serialize())
                .setNewAccount(new NewAccount()
                        .setContact(List.of(URI.create("mailto:example@localhost.localhost")))
                        .setTermsOfServiceAgreed(true))
                .setRetry(pebbleRetry);
        client = new AcmeClient(restTemplate, jsonMapper, retryRegistry, config);
    }

    @Test
    void createAccount() {
        assertThat(assertThrows(Exception.class, () -> client.accountId(true)).toString(), containsString("accountDoesNotExist"));

        var accountId = client.accountId(false);
        var account = client.getAccount(accountId);
        assertThat(account, is(notNullValue()));
        assertThat(account.getStatus(), is(Account.AccountStatus.valid));
        assertThat(account.getContact(), contains(URI.create("mailto:example@localhost.localhost")));
        assertThat(account.getOrders(), is(notNullValue()));
        assertThat(account.getExternalAccountBinding(), is(nullValue()));
    }

    @Test
    void orders() {
        client.createAccount();
        var orders = client.orders();
        assertThat(orders, notNullValue());
        assertThat(orders, empty());
        var order = client.order(new NewOrder().setIdentifiers(List.of(new AcmeIdentifier().setType(AcmeIdentifier.AcmeIdentifierType.dns).setValue("example.localhost"))));
        assertThat(order, notNullValue());
        var newOrders = client.orders();
        assertThat(newOrders, hasSize(1));
        var newOrder = client.getOrder(newOrders.getFirst());
        assertThat(newOrder, notNullValue());
        assertThat(newOrder.getNotBefore(), is(nullValue()));
        assertThat(newOrder.getNotAfter(), is(nullValue()));
        assertThat(newOrder.getError(), is(nullValue()));
        assertThat(newOrder.getAuthorizations(), is(notNullValue()));
        assertThat(newOrder.getAuthorizations(), is(not(empty())));
        assertThat(newOrder.getFinalize(), is(notNullValue()));
        assertThat(newOrder.getCertificate(), is(nullValue()));
    }

    @SneakyThrows
    @Test
    void authorizationsAndChallenges() {
        client.createAccount();
        var order = client.order(new NewOrder().setIdentifiers(List.of(new AcmeIdentifier().setType(AcmeIdentifier.AcmeIdentifierType.dns).setValue("challenges.localhost"))));

        // get authorization:
        var authorizations = order.getTypedPayload().getAuthorizations().stream().map(client::getAuthorization).toList();

        assertThat(authorizations, is(notNullValue()));
        assertThat(authorizations, is(hasSize(order.getTypedPayload().getIdentifiers().size())));
        assertThat(authorizations.getFirst(), is(notNullValue()));
        assertThat(authorizations.getFirst().getChallenges(), is(notNullValue()));
        assertThat(authorizations.getFirst().getChallenges(), is(not(empty())));
        assertThat(authorizations.getFirst().getChallenges().getFirst(), is(notNullValue()));

        var http01Challenge = authorizations.getFirst().getChallenges().stream()
                .filter(c -> c.getType().equals("http-01"))
                .findAny().orElseThrow();

        var challenge = client.respondToChallenge(http01Challenge.getUrl());

        waitUntilTerminalState(challenge);

        // assert specific terminal state
        assertThat(client.getChallenge(challenge.getUrl()).getStatus(), is(ChallengeStatus.invalid));
        assertThat(client.getOrder(order.getLocation()).getStatus(), is(OrderStatus.invalid));
    }

    @SneakyThrows
    @Test
    void authorizationsAndChallengesSuccess() {
        client.createAccount();
        var order = client.order(new NewOrder().setIdentifiers(List.of(new AcmeIdentifier().setType(AcmeIdentifier.AcmeIdentifierType.dns).setValue("challenges-success.localhost"))));
        var authorization = client.getAuthorizations(order.getTypedPayload()).getFirst();
        var challenge = authorization.getChallenges().stream().filter(t -> t.getType().equals(SupportedChallengeType.ChallengeHTTP01.getRfcName())).findAny().orElseThrow();

        var challengeSolver = new ChallengeSolver(
                new ChallengeSolver.Config(),
                ChallengeSolutionRepository.inMemory(),
                new PebbleChallengeServerPresenter(authorization, containers.challengeServerContainer.baseUrl()),
                ExternalVerifier.noOp());

        log.info("solving challenge");
        var challengeSolution = challengeSolver.solve(new ChallengeInput()
                .setKeyPair(client.supportedClientKeyPair())
                .setAccountUrl(client.accountId(true))
                .setAuthorization(authorization)
                .setChallenge(challenge)
        );
        try {
            log.info("responding to challenge");
            client.respondToChallenge(challenge.getUrl());
            log.info("waiting for challenge status");
            waitUntilTerminalState(challenge);
            assertThat(client.getChallenge(challenge.getUrl()).getStatus(), is(ChallengeStatus.valid));
        } finally {
            log.info("cleaning challenge");
            challengeSolver.clean(challengeSolution);
        }
        log.info("waiting order ready");
        waitUntilReadyStateOfOrder(order.getLocation());
        assertThat(client.getOrder(order.getLocation()).getStatus(), is(OrderStatus.ready));
        log.info("finalizing order");
        var csrKeyPair = SupportedClientKeyPairAlgorithm.RS256.generate();
        client.finalizeOrder(
                order.getTypedPayload(),
                new CsrBuilder()
                        .setOrder(order.getTypedPayload())
                        .build(csrKeyPair)
        );
        log.info("waiting for order status");
        waitUntilTerminalStateOfOrder(order.getLocation());
        assertThat(client.getOrder(order.getLocation()).getStatus(), is(OrderStatus.valid));
        log.info("downloading certificate");
        var certificate = client.downloadCertificate(client.getOrder(order.getLocation()), authorization.getIdentifier());
        log.info("downloaded certificate: {}", certificate);
    }

    private void waitUntilTerminalState(Challenge challenge) {
        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS,
                () -> Set.of(ChallengeStatus.invalid, ChallengeStatus.valid)
                        .contains(client.getChallenge(challenge.getUrl()).getStatus()));
    }

    @SneakyThrows
    private void waitUntilReadyStateOfOrder(URI orderId) {
        try {
            Unreliables.retryUntilTrue(10, TimeUnit.SECONDS,
                    () -> Objects.equals(OrderStatus.ready, client.getOrder(orderId).getStatus()));
        } catch (Exception e) {
            log.error("current order status: {}", client.getOrder(orderId), e);
            throw e;
        }
    }

    private void waitUntilTerminalStateOfOrder(URI orderId) {
        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS,
                () -> Set.of(OrderStatus.invalid, OrderStatus.valid)
                        .contains(client.getOrder(orderId).getStatus()));
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

    private static class PebbleChallengeServerPresenter extends ChallengePresenter.SingleHostCrudPresenter {
        public PebbleChallengeServerPresenter(AcmeResources.Authorization authorization, URI pebbleChallengeServerBaseUrl) {
            super(authorization.getIdentifier().getValue(),
                    new PebbleChallengeServerCrud(pebbleChallengeServerBaseUrl),
                    ChallengePresenter.SingleHostCrudPresenter.Type.http);
        }

        @RequiredArgsConstructor
        private static class PebbleChallengeServerCrud implements Crud {
            private final URI pebbleChallengeServerBaseUrl;

            @Override
            public void create(String key, String value) {
                RestClient.create(pebbleChallengeServerBaseUrl).post().uri("/add-http01").body(Map.of("token", key, "content", value))
                        .retrieve().toBodilessEntity();
            }

            @Override
            public void delete(String key) {
                RestClient.create(pebbleChallengeServerBaseUrl).post().uri("/del-http01").body(Map.of("token", key))
                        .retrieve().toBodilessEntity();
            }
        }
    }

    @Nested
    class ExternalAccountBindingTest {
        static PebbleContainers containers;
        AcmeClient.Config config;
        AcmeClient client;

        @BeforeAll
        static void beforeAll() {
            containers = new PebbleContainers();
            containers.pebbleContainer.withBuiltInConfig(BuiltInConfig.PEBBLE_EXTERNAL_ACCOUNT_BINDINGS);
            containers.start();
        }

        @AfterAll
        static void afterAll() {
            containers.stop();
        }

        @BeforeEach
        void beforeEach() {
            var restTemplate = new RestTemplateBuilder().requestFactory(AcmeClientITest.this::restTemplateTrustAll).build();
            var keyId = PebbleAcmeServerTestContainer.DEFAULT_HMAC_KEYS.keySet().stream().sorted().findFirst().orElseThrow();
            var keyValue = Base64.getUrlDecoder().decode(PebbleAcmeServerTestContainer.DEFAULT_HMAC_KEYS.get(keyId));

            config = new AcmeClient.Config()
                    .setDirectoryUrl(containers.getPebbleDirectoryUrl())
                    // .setDirectoryUrl(URI.create("https://localhost:14000/dir"))
                    .setKeyString(SupportedClientKeyPairAlgorithm.ES256.generate().serialize())
                    .setNewAccount(new NewAccount()
                            .setContact(List.of(URI.create("mailto:example@localhost.localhost")))
                            .setTermsOfServiceAgreed(true))
                    .setExternalAccountCredential(new ExternalAccountCredential()
                            .setMacAlgorithm(MacAlgorithm.HS256)
                            .setKeyId(keyId)
                            .setMacKey(keyValue))
                    .setRetry(pebbleRetry);
            client = new AcmeClient(restTemplate, jsonMapper, retryRegistry, config);
        }

        @Test
        void requestWithoutExternalAccountBindingFailsToCreateAccount() {
            config.setExternalAccountCredential(null);
            assertThat(assertThrows(Exception.class,
                    () -> client.createAccount()).toString(), containsString("External account required but not configured"));
        }

        @Test
        void test() {
            client.createAccount();
            var account = client.getAccount();
            assertThat(account, is(notNullValue()));
            assertThat(account.getExternalAccountBinding(), is(notNullValue()));
            System.out.println(account.getExternalAccountBinding());
        }
    }
}
