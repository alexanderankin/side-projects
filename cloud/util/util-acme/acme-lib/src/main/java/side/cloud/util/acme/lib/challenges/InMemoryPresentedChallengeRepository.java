package side.cloud.util.acme.lib.challenges;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPresentedChallengeRepository implements PresentedChallengeRepository {
    private final Map<String, ExpiringValue<PresentedChallenge>> items = new ConcurrentHashMap<>();

    @SuppressWarnings("NullableProblems")
    @Override
    public String newItem(@NonNull PresentedChallenge item, Instant notBefore, Duration expiresIn) {
        var old = items.putIfAbsent(item.key(), new ExpiringValue<>(item, notBefore, notBefore.plus(expiresIn)));
        if (old != null) {
            throw new IllegalArgumentException("item already exists");
        }
        return item.key();
    }

    @Override
    public PresentedChallenge isItemValid(String key) {
        var value = items.get(key);
        if (value == null)
            return null;
        if (!value.isValidNow(Instant.now())) {
            items.remove(key);
            return null;
        }
        return value.item();
    }

    @Override
    public PresentedChallenge useItem(String key) {
        var removed = items.remove(key);
        if (removed == null) return null;
        return removed.item();
    }

    @Override
    public List<PresentedChallenge> cleanExpiredItems(Instant now) {
        return items.entrySet().stream()
                .filter(entry -> entry.getValue().isValidNow(now))
                .peek(entry -> items.remove(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(ExpiringValue::item)
                .toList();
    }
}
