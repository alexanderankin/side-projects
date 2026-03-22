package side.cloud.util.acme.lib;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class PebbleTestContainerTest {
    @Test
    void containerStarts() {
        try (var pebble = new PebbleAcmeTestContainer()) {
            pebble.start();
            log.info("directory is: {}", pebble.directory());
        }
    }
}
