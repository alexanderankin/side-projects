package side.cloud.util.acme.lib.model;

import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface Repository<T> {
    /**
     * @return key
     */
    String newItem(@Nullable T item, Instant notBefore, Duration expiresIn);

    T isItemValid(String key);

    T useItem(String key);

    List<T> cleanExpiredItems(Instant now);

    record ExpiringValue<T>(T item, Instant notBefore, Instant expiresAt) {
        public boolean isValidNow(Instant now) {
            return !now.isBefore(notBefore) && now.isBefore(expiresAt);
        }
    }
}
