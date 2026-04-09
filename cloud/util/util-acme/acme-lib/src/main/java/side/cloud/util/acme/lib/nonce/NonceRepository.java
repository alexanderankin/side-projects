package side.cloud.util.acme.lib.nonce;

import side.cloud.util.acme.lib.model.Repository;

import java.time.Duration;
import java.time.Instant;

public interface NonceRepository extends Repository<String> {
    @Override
    default String newItem(String ignored, Instant notBefore, Duration expiresIn) {
        return newNonce(notBefore, expiresIn);
    }

    @Override
    default String isItemValid(String key) {
        return isNonceValid(key) ? key : null;
    }

    @Override
    default String useItem(String key) {
        return useNonce(key) ? key : null;
    }

    default String newNonce(Instant notBefore, Duration expiresIn) {
        throw new UnsupportedOperationException();
    }

    boolean isNonceValid(String nonce);

    boolean useNonce(String nonce);
}
