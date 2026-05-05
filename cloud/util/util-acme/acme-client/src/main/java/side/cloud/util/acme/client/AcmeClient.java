package side.cloud.util.acme.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import side.cloud.util.acme.lib.keys.CsrBuilder;
import side.cloud.util.acme.lib.keys.ExternalAccountCredential;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeRequests;
import side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse.TypedAcmeResponse;
import side.cloud.util.acme.lib.model.AcmeResources.*;
import side.cloud.util.acme.lib.retry.DelayAware;
import side.cloud.util.acme.lib.retry.RetryAfterDelayIntervalFunction;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.REPLAY_NONCE;

@RequiredArgsConstructor
public class AcmeClient {
    static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    private final RetryRegistry retryRegistry;
    private final Config config;
    private volatile Directory directory;
    private volatile URI accountId;
    // little harm in parsing twice
    private SupportedClientKeyPair supportedClientKeyPair;

    public Directory directory() {
        if (directory == null) {
            synchronized (this) {
                if (directory == null) {
                    directory = doDirectory();
                }
            }
        }
        return directory;
    }

    private Directory doDirectory() {
        return restTemplate.getForObject(config.getDirectoryUrl(), Directory.class);
    }

    public String nonce() {
        return restTemplate.headForHeaders(directory().getNewNonce()).getFirst(REPLAY_NONCE);
    }

    public SupportedClientKeyPair supportedClientKeyPair() {
        if (supportedClientKeyPair == null) {
            supportedClientKeyPair = SupportedClientKeyPair.deserialize(config.getKeyString());
        }
        return supportedClientKeyPair;
    }

    @SuppressWarnings("UnusedReturnValue")
    public URI createAccount() {
        return accountId(false);
    }

    @SneakyThrows
    public URI accountId(boolean onlyReturnExisting) {
        if (accountId == null) {
            synchronized (this) {
                var configuredAccountId = config.getAccountId();
                if (configuredAccountId != null) {
                    accountId = configuredAccountId;
                } else if (accountId == null) {
                    var eabRequired = Optional.of(directory())
                            .map(Directory::getMeta).map(Directory.Meta::getExternalAccountRequired).orElse(false);

                    if (eabRequired && config.externalAccountCredential == null) {
                        throw new IllegalStateException("External account required but not configured");
                    }

                    var configuredNewAccountParameters = config.getNewAccount();
                    var lookup = jsonMapper.convertValue(configuredNewAccountParameters, NewAccount.class);
                    lookup.setOnlyReturnExisting(onlyReturnExisting);

                    if (eabRequired) {
                        lookup.setExternalAccountBinding(
                                config.externalAccountCredential.sign(supportedClientKeyPair(), directory().getNewAccount()));
                    }

                    var retry = retryRegistry.retry(getClass().getSimpleName(), config.getRetry().asRetryConfig());
                    var entity = retry.executeCallable(() -> {
                        var req = AcmeRequestSerDe.serialize(new AcmeRequestSerDe.RequestAndKeyPair(
                                new AcmeRequests.AcmeRequest()
                                        .setUrl(directory().getNewAccount())
                                        .setNonce(nonce())
                                        .setPayload(jsonMapper.convertValue(lookup, MAP_TYPE_REFERENCE)),
                                supportedClientKeyPair()
                        ));
                        try {
                            return restTemplate.exchange(req, Account.class);
                        } catch (RestClientResponseException e) {
                            RuntimeException r;
                            if ((r = DelayRestClientResponseException.of(e)) != null) throw r;
                            throw e;
                        }
                    });
                    accountId = entity.getHeaders().getLocation();
                }
            }
        }
        return accountId;
    }

    public <T> TypedAcmeResponse<T> get(URI uri, Class<T> responseType) {
        return post(uri, null, responseType);
    }

    @SneakyThrows
    public <T> TypedAcmeResponse<T> post(URI uri, Object body, Class<T> responseType) {
        var response = postWithRetry(uri, body);

        var link = response.getHeaders().getFirst(HttpHeaders.LINK);
        URI next = link == null ? null : Links.parse(link).getLink("next").map(Link::getHref).map(URI::create).orElse(null);

        var result = new TypedAcmeResponse<T>();
        result
                .setTypedPayload(jsonMapper.readValue(response.getBody(), responseType))
                .setPayload(jsonMapper.readValue(response.getBody(), MAP_TYPE_REFERENCE))
                .setLocation(response.getHeaders().getLocation())
                .setNext(next);
        return result;
    }

    @SneakyThrows
    private ResponseEntity<String> postWithRetry(URI uri, Object body) {
        ResponseEntity<String> response;
        {
            var retry = retryRegistry.retry(getClass().getSimpleName(), config.getRetry().asRetryConfig());
            response = retry.executeCallable(() -> doPost(uri, body));
        }
        return response;
    }

    private ResponseEntity<String> doPost(URI uri, Object body) {
        try {
            return restTemplate.exchange(
                    AcmeRequestSerDe.serialize(
                            new AcmeRequestSerDe.RequestAndKeyPair(
                                    new AcmeRequests.AcmeRequest()
                                            .setUrl(uri)
                                            .setNonce(nonce())
                                            .setAccountId(accountId(true))
                                            .setPayload(body == null ? null : jsonMapper.convertValue(body, MAP_TYPE_REFERENCE)),
                                    supportedClientKeyPair()
                            )
                    ),
                    String.class
            );
        } catch (RestClientResponseException e) {
            RuntimeException r;
            if ((r = DelayRestClientResponseException.of(e)) != null) throw r;
            throw e;
        }
    }

    public TypedAcmeResponse<Order> order(NewOrder order) {
        return post(directory().getNewOrder(), order, Order.class);
    }

    public Account getAccount() {
        return getAccount(accountId(true));
    }

    public Account getAccount(URI uri) {
        return get(uri, Account.class).getTypedPayload();
    }

    public Order getOrder(URI uri) {
        return get(uri, Order.class).getTypedPayload();
    }

    public List<URI> orders() {
        var account = getAccount();
        var ordersUrl = account.getOrders();
        return get(ordersUrl, Orders.class).getTypedPayload().getOrders();
    }

    public Authorization getAuthorization(URI uri) {
        return get(uri, Authorization.class).getTypedPayload();
    }

    public List<Authorization> getAuthorizations(Order order) {
        return order.getAuthorizations().stream().map(this::getAuthorization).toList();
    }

    public Challenge respondToChallenge(URI challengeUri) {
        return post(challengeUri, Map.of(), Challenge.class).getTypedPayload();
    }

    public Challenge getChallenge(URI uri) {
        return get(uri, Challenge.class).getTypedPayload();
    }

    public Challenge.ChallengeStatus getChallengeStatus(URI uri) {
        return getChallenge(uri).getStatus();
    }

    public Order finalizeOrder(Order order, CsrBuilder.Csr csr) {
        var finalizeUrl = order.getFinalize();
        return post(finalizeUrl, Map.of("csr", csr.asCsrValue()), Order.class).getTypedPayload();
    }

    public String downloadCertificate(Order order, AcmeIdentifier identifier) {
        var certificateUrl = order.getCertificate();
        if (certificateUrl == null) {
            return null;
        }
        var certResponse = postWithRetry(certificateUrl, null);
        return certResponse.getBody();
    }

    private static class DelayRestClientResponseException extends RestClientResponseException implements DelayAware {
        @Getter
        @Accessors(fluent = true)
        private final Duration delay;

        public DelayRestClientResponseException(Duration delay, RestClientResponseException e) {
            this(delay, e.getMessage(), e.getStatusCode(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
            initCause(e);
        }

        public DelayRestClientResponseException(Duration delay,
                                                String message,
                                                HttpStatusCode statusCode,
                                                String statusText,
                                                @Nullable HttpHeaders headers,
                                                byte @Nullable [] responseBody,
                                                @Nullable Charset responseCharset) {
            super(message, statusCode, statusText, headers, responseBody, responseCharset);
            this.delay = delay;
        }

        public static DelayRestClientResponseException of(RestClientResponseException e) {
            Duration delay = delayOf(e);
            if (delay != null) {
                return new DelayRestClientResponseException(delay, e);
            }
            return null;
        }

        public static Duration delayOf(RestClientResponseException e) {
            var responseHeaders = e.getResponseHeaders();
            if (responseHeaders == null)
                return null;
            var retryAfter = responseHeaders.getFirst(HttpHeaders.RETRY_AFTER);
            if (retryAfter == null)
                return null;
            if (retryAfter.matches("^\\d{1,20}$")) {
                return Duration.ofSeconds(Integer.parseInt(retryAfter));
            }
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME);
                return Duration.between(Instant.now(), zdt);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    @Data
    @Accessors(chain = true)
    @Validated
    public static class Config {
        @NotNull
        URI directoryUrl;

        @NotNull
        String keyString;

        /**
         * this can be recovered from the server,
         * if you don't trust the server, just fill it in.
         */
        @Nullable
        URI accountId;

        /**
         * yes it is a bit over the top to require,
         * but it DOES need to be there to bootstrap things
         */
        @NotNull
        @Valid
        NewAccount newAccount;

        @Valid
        ExternalAccountCredential externalAccountCredential;

        Retry retry = new Retry();

        @AssertTrue
        public boolean isKeyStringCanParseSupportedClientKeyPair() {
            try {
                SupportedClientKeyPair.deserialize(keyString);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Data
        @Accessors(chain = true)
        public static class Retry {
            // disable retries in development
            int attempts = 1;
            Duration interval = Duration.ofSeconds(5);

            RetryConfig asRetryConfig() {
                var retryConfig = RetryConfig.custom()
                        .maxAttempts(attempts)
                        .waitDuration(interval)
                        .build();

                return RetryConfig.from(retryConfig)
                        .intervalBiFunction(new RetryAfterDelayIntervalFunction<>(Either.right(retryConfig.getIntervalBiFunction())))
                        .build();
            }
        }
    }
}
