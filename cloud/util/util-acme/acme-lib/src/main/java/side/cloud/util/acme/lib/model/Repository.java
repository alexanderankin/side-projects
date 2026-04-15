package side.cloud.util.acme.lib.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public interface Repository<T> {
    Function<T, String> getKeyFunction();

    /**
     * @return key
     */
    String newItem(T item, Instant notBefore, Duration expiresIn);

    T isItemValid(String key);

    T useItem(String key);

    List<T> cleanExpiredItems(Instant now);

    // this is awkward when you already have it in a field
    default Function<T, String> getFunction(Class<?> clazz) {
        if (IntoId.class.isAssignableFrom(clazz)) {
            return e -> ((IntoId) e).intoId();
        }

        return getKeyFunction();
    }

    interface IntoId {
        String intoId();

    }

    record ExpiringValue<T>(T item, Instant notBefore, Instant expiresAt) {
        public boolean isValidNow(Instant now) {
            var beforeOk = notBefore == null || !now.isBefore(notBefore);
            var afterOk = expiresAt == null || now.isBefore(expiresAt);
            return beforeOk && afterOk;
        }
    }
}
