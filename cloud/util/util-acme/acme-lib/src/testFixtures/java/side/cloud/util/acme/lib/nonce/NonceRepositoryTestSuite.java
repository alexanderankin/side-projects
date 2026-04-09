package side.cloud.util.acme.lib.nonce;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public abstract class NonceRepositoryTestSuite {
    protected abstract NonceRepository createRepository();

    @Test
    void nonceLifecycle() {
        NonceRepository repo = createRepository();

        String nonce = repo.newItem(null, Instant.now(), Duration.ofMinutes(5));

        assertThat(repo.isNonceValid(nonce), is(true));
        assertThat((long) repo.cleanExpiredItems(Instant.now()).size(), is(0L));

        assertThat(repo.useNonce(nonce), is(true));
        assertThat(repo.useNonce(nonce), is(false));

        assertThat(repo.isNonceValid(nonce), is(false));

        assertThat((long) repo.cleanExpiredItems(Instant.now()).size(), greaterThanOrEqualTo(0L));
        assertThat((long) repo.cleanExpiredItems(Instant.now()).size(), is(0L));
    }

    @Test
    void testExpirationAcrossDrivers() {
        var repo = createRepository();
        String nonce = repo.newItem(null, Instant.now().minusSeconds(60), Duration.ofSeconds(1));
        assertThat(repo.isNonceValid(nonce), is(false));
        assertThat((long) repo.cleanExpiredItems(Instant.now()).size(), greaterThanOrEqualTo(1L));
    }
}
