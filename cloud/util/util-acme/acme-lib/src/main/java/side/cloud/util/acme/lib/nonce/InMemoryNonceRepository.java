package side.cloud.util.acme.lib.nonce;

import lombok.Value;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryNonceRepository implements NonceRepository {
    private final SecureRandom RANDOM = new SecureRandom();
    private final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final Map<String, NonceEntry> nonces = new ConcurrentHashMap<>();

    private String generateNonce() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    @Override
    public String newNonce(Instant notBefore, Duration expiresIn) {
        String nonce;
        Instant expiresAt = notBefore.plus(expiresIn);

        do {
            nonce = generateNonce();
        } while (nonces.containsKey(nonce));

        nonces.put(nonce, new NonceEntry(notBefore, expiresAt));
        return nonce;
    }

    @Override
    public boolean isNonceValid(String nonce) {
        NonceEntry entry = nonces.get(nonce);
        return entry != null && entry.isValidNow(Instant.now());
    }

    @Override
    public boolean useNonce(String nonce) {
        NonceEntry entry = nonces.remove(nonce);
        return entry != null && entry.isValidNow(Instant.now());
    }

    @Override
    public long cleanExpiredNonces() {
        long removed = 0;
        Instant now = Instant.now();

        for (Map.Entry<String, NonceEntry> e : nonces.entrySet()) {
            if (e.getValue().isExpired(now)) {
                if (nonces.remove(e.getKey(), e.getValue())) {
                    removed++;
                }
            }
        }

        return removed;
    }

    @Value
    private static class NonceEntry {
        Instant notBefore;
        Instant expiresAt;

        boolean isValidNow(Instant now) {
            return !now.isBefore(notBefore) && now.isBefore(expiresAt);
        }

        boolean isExpired(Instant now) {
            return now.isAfter(expiresAt);
        }
    }
}
