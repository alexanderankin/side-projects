package side.cloud.util.acme.lib;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import side.cloud.util.acme.lib.model.AcmeError;
import side.cloud.util.acme.lib.model.AcmeError.KnownAcmeError;
import side.cloud.util.acme.lib.model.AcmeError.KnownAcmeError.KnownAcmeErrorType;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AcmeClientTemplate implements AcmeClientOperations {
    private final RestClient restClient;
    private final RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .retryOnException(AcmeClientTemplate::shouldRethrow)
            .build();
    final Retry retry = Retry.of("post", retryConfig);

    {
        retry.getEventPublisher().onRetry(e -> log.debug("retrying: {}", e));
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean shouldRethrow(Throwable throwable) {
        if (throwable instanceof HttpClientErrorException) {
            return false;
        }
        if (throwable instanceof KnownAcmeError knownAcmeError) {
            if (KnownAcmeErrorType.NON_TRANSIENT.contains(knownAcmeError.getError().getKnownAcmeErrorType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Directory directory(URI directoryUrl) {
        return restClient.get().uri(directoryUrl).retrieve().body(Directory.class);
    }

    @Override
    public String newNonce(Directory directory) {
        return restClient.get().uri(directory.getNewNonce()).retrieve().toBodilessEntity().getHeaders().getFirst("Replay-Nonce");
    }

    @SneakyThrows
    @Override
    public <T> ResponseEntity<T> post(URI uri, SupportedClientKeyPair keyPair, AcmeJwsObject jwsObject, Class<T> tClass, Directory directory) {
        return retry.executeCallable(() -> {
            try {
                jwsObject.getHeaders().setNonce(newNonce(directory));
                var body = keyPair.signAndSerialize(jwsObject);
                return restClient.post().uri(uri).header(HttpHeaders.CONTENT_TYPE, "application/jose+json").body(body).retrieve().toEntity(tClass);
            } catch (RestClientResponseException e) {
                Optional.ofNullable(AcmeError.from(e)).ifPresent(AcmeError::doThrow);
                throw e;
            }
        });

    }
}
