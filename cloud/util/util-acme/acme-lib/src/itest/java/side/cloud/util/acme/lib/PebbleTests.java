package side.cloud.util.acme.lib;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PebbleTests {
    static PebbleAcmeTestContainer pebbleAcmeTestContainer;

    @BeforeAll
    static void beforeAll() {
        pebbleAcmeTestContainer = new PebbleAcmeTestContainer();
        pebbleAcmeTestContainer.start();
    }

    @Test
    void test() {

    }
}
