package side.cloud.util.acme.lib;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import side.cloud.util.acme.lib.containers.PebbleAcmeServerTestContainer;

@Slf4j
class PebbleTestContainerTest {
    @Test
    void containerStarts() {
        try (var pebble = new PebbleAcmeServerTestContainer()) {
            pebble.start();
            log.info("directory is: {}", pebble.directory());
        }
    }
}
