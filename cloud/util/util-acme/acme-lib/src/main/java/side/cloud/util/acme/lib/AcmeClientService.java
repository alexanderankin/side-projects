package side.cloud.util.acme.lib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import side.cloud.util.acme.lib.model.AcmeError;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Account;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.lib.model.AcmeResources.NewAccount;
import side.cloud.util.acme.lib.model.AcmeResources.Order;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Accessors(chain = true)
public class AcmeClientService {
    private RetryConfig retryConfig;
    private RestClient restClient;
    private Config config;
    private JsonMapper jsonMapper;

    public AcmeClientService(Config config) {
        this.config = config;
        restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, config.getUserAgent())
                .build();
        retryConfig = RetryConfig.custom()
                .maxAttempts(10)
                .retryExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .build();
        jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @SneakyThrows
    public Directory directory() {
        return Retry.of("acmeDirectory", retryConfig)
                .executeCallable(() -> {
                    log.debug("acmeDirectory ({})", this);
                    return restClient.get().uri(config.getDirectoryUrl())
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(Directory.class);
                });
    }

    public String newNonce(Directory directory) {
        var entity = restClient.method(HttpMethod.valueOf(config.newNonceHttpMethod.name()))
                .uri(directory.getNewNonce())
                .retrieve()
                .toBodilessEntity();
        return entity.getHeaders().getFirst("Replay-Nonce");
    }

    @SneakyThrows
    public Account newAccount(Directory directory,
                              String newNonce,
                              NewAccount newAccount) {
        var body = config.keyPair.signAndSerialize(
                new AcmeJwsObject()
                        .setHeaders(Map.of("nonce", newNonce, "url", directory.getNewAccount()))
                        .setPayload(jsonMapper.convertValue(newAccount, new TypeReference<>() {
                        }))
        );

        try {
            var response = restClient.post()
                    .uri(directory.getNewAccount())
                    .header(HttpHeaders.CONTENT_TYPE, "application/jose+json")
                    .body(body)
                    .retrieve()
                    .toEntity(Account.class);

            return response.getBody();
        } catch (RestClientResponseException e) {
            Optional.ofNullable(AcmeError.from(e)).ifPresent(AcmeError::doThrow);
            throw new RuntimeException(e.getResponseBodyAsString() + ": " + e.getStatusCode() + "/" + e.getResponseHeaders());
        }
    }

    public Order newOrder(Directory directory, String newNonce, AcmeResources.NewOrder newOrder) {
        var body = config.keyPair.signAndSerialize(
                new AcmeJwsObject()
                        .setHeaders(Map.of("nonce", newNonce, "url", directory.getNewOrder()))
                        .setPayload(jsonMapper.convertValue(newOrder, new TypeReference<>() {
                        }))
        );
        try {
            var response = restClient.post()
                    .uri(directory.getNewOrder())
                    .header(HttpHeaders.CONTENT_TYPE, "application/jose+json")
                    .body(body)
                    .retrieve()
                    .toEntity(Order.class);
            return response.getBody();
        } catch (RestClientResponseException e) {
            Optional.ofNullable(AcmeError.from(e)).ifPresent(AcmeError::doThrow);
            throw new RuntimeException(e.getResponseBodyAsString() + ": " + e.getStatusCode() + "/" + e.getResponseHeaders());
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        @NotNull
        final URI directoryUrl;
        @NotBlank
        String userAgent;
        @NotNull
        NewNonceHttpMethod newNonceHttpMethod = NewNonceHttpMethod.HEAD;
        @NotNull
        @Valid
        SupportedClientKeyPair keyPair;

        public enum NewNonceHttpMethod {
            HEAD, GET
        }
    }

    // static class Protected {
    //     JwsAlgorithm jwsAlgorithm;
    //
    //     enum JwsAlgorithm {
    //         Ed25519, EdDSA,
    //         RS256, RS384, RS512,
    //         ES256, ES384, ES512,
    //     }
    // }

    @Data
    @Accessors(chain = true)
    static class Payload {
        @JsonProperty("protected")
        String protectedField;
        String payload;
        String signature;
    }
}
