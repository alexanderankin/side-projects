package side.cloud.util.acme.lib.model;

import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Data
public class DefaultInMemoryRepository<T> implements Repository<T> {
    private final Class<T> tClass;
    private final Function<T, String> keyFunction;
    private final Map<String, Repository.ExpiringValue<T>> data;

    @Override
    public String newItem(@Nullable T item, Instant notBefore, Duration expiresIn) {
        var key = keyFunction.apply(item);
        var existing = data.putIfAbsent(key, new ExpiringValue<>(item, notBefore, notBefore.plus(expiresIn)));
        if (existing != null) {
            throw new IllegalArgumentException("item already exists");
        }
        return key;
    }

    @Override
    public T isItemValid(String key) {
        var value = data.get(key);
        if (value == null) {
            return null;
        }
        if (!value.isValidNow(Instant.now())) {
            return null;
        }
        return value.item();
    }

    @Override
    public T useItem(String key) {
        var removed = data.remove(key);
        if (removed == null) {
            return null;
        }
        if (!removed.isValidNow(Instant.now())) {
            return null;
        }
        return removed.item();
    }

    @Override
    public List<T> cleanExpiredItems(Instant now) {
        return new ArrayList<>(data.entrySet()).stream()
                .filter(entry -> !entry.getValue().isValidNow(now))
                .peek(entry -> data.remove(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(ExpiringValue::item)
                .toList();
    }
}
