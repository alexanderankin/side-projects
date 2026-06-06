package side.cloud.util.acme.server.persistence.os;

import side.cloud.util.acme.server.persistence.nonce.NonceRepository;

import java.time.Instant;

public class ObjectStorageNonceRepository implements NonceRepository {
    @Override
    public void createNonce(String nonce, Instant notBefore, Instant notAfter) {

    }

    @Override
    public boolean checkNonce(String nonce, Instant now) {
        return false;
    }

    @Override
    public boolean useNonce(String nonce, Instant now) {
        return false;
    }

    @Override
    public void cleanNonces(Instant now) {

    }
}
