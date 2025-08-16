package info.ankin.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class InMemoryRateLimitPersistenceTest {
    InMemoryRateLimitPersistence persistence = new InMemoryRateLimitPersistence(new InMemoryRateLimitPersistence.Config());

    @Test
    void test() {
        var prefix = "test";
        Request request = new Request().setPrincipal(prefix + ".1").setResourceName("resource");
        Response response1 = persistence.readRateLimitStatus(request, 10);
        assertEquals(0, response1.getXRateLimitUsed());
        Response use1 = persistence.checkAndUpdateRateLimit(request, 10);
        assertEquals(1, use1.getXRateLimitUsed());
        Response response2 = persistence.readRateLimitStatus(request, 10);
        assertEquals(1, response2.getXRateLimitUsed());
        Response response3 = persistence.readRateLimitStatus(request, 10 + persistence.config.getDefaultWindowSize().toMillis());
        assertEquals(0, response3.getXRateLimitUsed());

        log.info("{}, {}, {}, {}", response1, use1, response2, response3);
    }
}
