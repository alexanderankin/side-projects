package info.ankin.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class InMemoryRateLimitPersistence {
    final Config config;
    final Map<Request, ResourceUsage> usageStore = new ConcurrentHashMap<>();

    /**
     * same as {@link #readRateLimitStatus(Request, long)} but uses {@link System#currentTimeMillis()}
     */
    public Response readRateLimitStatus(Request request) {
        return readRateLimitStatus(request, System.currentTimeMillis());
    }

    public Response readRateLimitStatus(Request request, long now) {
        final String resource = request.getResourceName();

        var resourceUsage = usageStore.computeIfAbsent(request, key -> {
            return new ResourceUsage(key.getPrincipal(), config.getDefaultLimit(), 0, now + config.getDefaultWindowSize().toMillis());
        });

        // If current time is beyond resetTimestamp, reset usage
        if (now >= resourceUsage.getResetTimestamp()) {
            resetUsage(resource, resourceUsage, now);
        }

        // Build the response
        return new Response(
                resourceUsage.getLimit(),
                resourceUsage.getLimit() - resourceUsage.getUsed(),
                resourceUsage.getResetTimestamp(),
                resourceUsage.getUsed(),
                resource
        );
    }

    /**
     * same as the other one but uses {@link System#currentTimeMillis()}
     *
     * @see #checkAndUpdateRateLimit(Request, long)
     */
    public Response checkAndUpdateRateLimit(Request request) throws RateLimitException {
        return checkAndUpdateRateLimit(request, System.currentTimeMillis());
    }

    /**
     * Checks if the request is within limits, increments usage if allowed,
     * or throws RateLimitException if exceeded.
     */
    public Response checkAndUpdateRateLimit(Request request, long now) throws RateLimitException {
        final String resource = request.getResourceName();

        var resourceUsage = usageStore.computeIfAbsent(request, key -> {
            return new ResourceUsage(key.getPrincipal(), config.getDefaultLimit(), 0, now + config.defaultWindowSize.toMillis());
        });

        // If current time is beyond resetTimestamp, reset usage
        if (now > resourceUsage.getResetTimestamp()) {
            resetUsage(resource, resourceUsage, now);
        }

        // Check if limit is already reached
        if (resourceUsage.getUsed() >= resourceUsage.getLimit()) {
            throw new RateLimitException("Rate limit exceeded for resource: " + resource);
        }

        // If allowed, increment usage
        resourceUsage.setUsed(resourceUsage.getUsed() + 1);

        // Build and return the updated response
        return new Response(
                resourceUsage.getLimit(),
                resourceUsage.getLimit() - resourceUsage.getUsed(),
                resourceUsage.getResetTimestamp(),
                resourceUsage.getUsed(),
                resource
        );
    }

    /**
     * Resets usage counters if we've passed the current window's reset time.
     */
    private void resetUsage(String resource, ResourceUsage usage, long now) {
        log.info("Resetting usage for resource: {}", resource);
        usage.setUsed(0);
        usage.setResetTimestamp(now + config.getDefaultWindowSize().toMillis());
    }

    /**
     * Holds usage info for a single resource:
     * - limit: max requests in the time window
     * - used: number of requests used so far
     * - resetTimestamp: when usage counters reset (epoch ms)
     */
    @Data
    @AllArgsConstructor
    public static class ResourceUsage {
        String subject;
        int limit;
        int used;
        long resetTimestamp;
    }

    @Data
    public static class Config {
        int defaultLimit = 100;
        Duration defaultWindowSize = Duration.ofMinutes(1);
    }
}
