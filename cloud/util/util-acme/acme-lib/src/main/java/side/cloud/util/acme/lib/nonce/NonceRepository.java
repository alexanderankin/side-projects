package side.cloud.util.acme.lib.nonce;

import java.time.Duration;
import java.time.Instant;

public interface NonceRepository {
    default String newNonce(Duration expiresIn) {
        return newNonce(Instant.now(), expiresIn);
    }

    String newNonce(Instant notBefore, Duration expiresIn);

    boolean isNonceValid(String nonce);

    boolean useNonce(String nonce);

    long cleanExpiredNonces();
}
