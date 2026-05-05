package side.cloud.util.acme.lib.retry;

import io.github.resilience4j.core.IntervalBiFunction;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RetryAfterDelayIntervalFunction<T> implements IntervalBiFunction<T> {
    private final Either<IntervalFunction, IntervalBiFunction<T>> delegate;

    @Override
    public Long apply(Integer attempt, Either<Throwable, T> either) {
        var left = either.getLeft();
        if (left instanceof DelayAware delayAware) {
            var delay = delayAware.delay();
            if (delay != null) {
                log.debug("found delayAware-indicated delay for exception '{}' and waiting for indicated delay: {}", left, delay);
                return delay.toMillis();
            }
        }

        return delegate.fold(
                // support for IntervalFunction kept for backwards compatibility
                i -> i.apply(attempt),
                b -> b.apply(attempt, either)
        );
    }
}
