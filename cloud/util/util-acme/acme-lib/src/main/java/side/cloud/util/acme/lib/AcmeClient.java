package side.cloud.util.acme.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import side.cloud.util.acme.lib.model.AcmeError;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources.*;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Accessors(chain = true)
public class AcmeClient {
    private final AcmeClientService acmeClientService;
    @Getter(AccessLevel.NONE)
    private volatile Directory acmeDirectory;

    AcmeClient(AcmeClientService acmeClientService) {
        this.acmeClientService = acmeClientService;
    }

    @SuppressWarnings("unused")
    public static AcmeClient create(Config config) {
        try (var acmeFactory = new AcmeFactory()) {
            return acmeFactory.acmeClient(config);
        }
    }

    @SneakyThrows
    public Directory acmeDirectory() {
        if (acmeDirectory != null) {
            return acmeDirectory;
        }
        synchronized (this) {
            if (acmeDirectory != null) {
                return acmeDirectory;
            }
            return acmeDirectory = acmeClientService.directory();
        }
    }

    public String newNonce() {
        return acmeClientService.newNonce(acmeDirectory());
    }

    public Account newAccount(NewAccount newAccount) {
        Directory directory = acmeDirectory();
        String newNonce = newNonce();
        var body = acmeClientService.getConfig().keyPair.signAndSerialize(
                new AcmeJwsObject()
                        .setHeaders(Map.of("nonce", newNonce, "url", directory.getNewAccount()))
                        .setPayload(acmeClientService.getJsonMapper().convertValue(newAccount, new TypeReference<>() {
                        }))
        );

        try {
            var response = acmeClientService.getRestClient().post()
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

    public Account fetchAccount(NewAccount newAccount) {
        newAccount.setOnlyReturnExisting(true);
        return newAccount(newAccount);
    }

    public void keyChange(Account account) {
        // but interesting
        throw new UnsupportedOperationException();
    }

    public void deactivateAccount(Account account) {
        throw new UnsupportedOperationException();
    }

    public Order newOrder(NewOrder newOrder) {
        Directory directory = acmeDirectory();
        String newNonce = newNonce();
        var body = acmeClientService.getConfig().keyPair.signAndSerialize(
                new AcmeJwsObject()
                        .setHeaders(Map.of("nonce", newNonce, "url", directory.getNewOrder()))
                        .setPayload(acmeClientService.getJsonMapper().convertValue(newOrder, new TypeReference<>() {
                        }))
        );
        try {
            var response = acmeClientService.getRestClient().post()
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
        @Valid
        final SupportedClientKeyPair keyPair;
        @NotNull
        final URI directoryUrl;
        @NotBlank
        String userAgent;
    }
}
