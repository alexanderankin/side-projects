package side.cloud.util.acme.lib.nonce;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryNonceRepository implements NonceRepository {
    private final Map<String, ExpiringValue<String>> nonces = new ConcurrentHashMap<>();

    @Override
    public String newItem(String nonce, Instant notBefore, Duration expiresIn) {
        Instant expiresAt = notBefore.plus(expiresIn);
        nonces.put(nonce, new ExpiringValue<>(nonce, notBefore, expiresAt));
        return nonce;
    }

    @Override
    public String isItemValid(String nonce) {
        ExpiringValue<String> entry = nonces.get(nonce);
        return entry != null && entry.isValidNow(Instant.now()) ? nonce : null;
    }

    @Override
    public String useItem(String nonce) {
        ExpiringValue<String> entry = nonces.remove(nonce);
        return entry != null && entry.isValidNow(Instant.now()) ? nonce : null;
    }

    @Override
    public List<String> cleanExpiredItems(Instant now) {
        var list = new ArrayList<String>();
        for (Map.Entry<String, ExpiringValue<String>> e : nonces.entrySet()) {
            if (!e.getValue().isValidNow(now)) {
                nonces.remove(e.getKey());
                list.add(e.getKey());
            }
        }

        return list;
    }
}
