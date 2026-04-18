package side.cloud.util.acme.server.nonce;

import java.time.Instant;

public interface NonceRepository {
    void createNonce(String nonce, Instant notBefore, Instant notAfter);

    boolean checkNonce(String nonce, Instant now);

    boolean useNonce(String nonce, Instant now);

    void cleanNonces(Instant now);
}
