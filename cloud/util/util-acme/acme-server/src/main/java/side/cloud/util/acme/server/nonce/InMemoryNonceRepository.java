package side.cloud.util.acme.server.nonce;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InMemoryNonceRepository implements NonceRepository {
    private final Map<String, Nonce> nonces = new ConcurrentHashMap<>();

    @Override
    public void createNonce(String nonce, Instant notBefore, Instant notAfter) {
        nonces.put(nonce, new Nonce(nonce, notBefore, notAfter));
    }

    @Override
    public boolean checkNonce(String nonce, Instant now) {
        var nonceResult = nonces.get(nonce);
        return nonceResult != null && valid(nonceResult, now);
    }

    @Override
    public boolean useNonce(String nonce, Instant now) {
        var nonceResult = nonces.remove(nonce);
        return nonceResult != null && valid(nonceResult, now);
    }

    @Override
    public void cleanNonces(Instant now) {
        nonces.values().removeIf(Predicate.not(e -> valid(e, now)));
    }

    private boolean valid(Nonce nonceResult, Instant now) {
        return !nonceResult.notAfter().isAfter(now) && !nonceResult.notBefore().isBefore(now);
    }

    record Nonce(String nonce, Instant notBefore, Instant notAfter) {
    }
}
