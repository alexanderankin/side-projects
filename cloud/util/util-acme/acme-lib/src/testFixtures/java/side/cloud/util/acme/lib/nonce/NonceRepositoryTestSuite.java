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

        String nonce = repo.newNonce(Instant.now(), Duration.ofMinutes(5));

        assertThat(repo.isNonceValid(nonce), is(true));
        assertThat(repo.cleanExpiredNonces(), is(0L));

        assertThat(repo.useNonce(nonce), is(true));
        assertThat(repo.useNonce(nonce), is(false));

        assertThat(repo.isNonceValid(nonce), is(false));

        assertThat(repo.cleanExpiredNonces(), greaterThanOrEqualTo(0L));
        assertThat(repo.cleanExpiredNonces(), is(0L));
    }

    @Test
    void testExpirationAcrossDrivers() {
        var repo = createRepository();
        String nonce = repo.newNonce(Instant.now().minusSeconds(60), Duration.ofSeconds(1));
        assertThat(repo.isNonceValid(nonce), is(false));
        assertThat(repo.cleanExpiredNonces(), greaterThanOrEqualTo(1L));
    }
}
