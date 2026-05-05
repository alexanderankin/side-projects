package side.cloud.util.acme.lib.retry;

import org.jspecify.annotations.Nullable;

import java.time.Duration;

public interface DelayAware {
    @Nullable
    Duration delay();
}
