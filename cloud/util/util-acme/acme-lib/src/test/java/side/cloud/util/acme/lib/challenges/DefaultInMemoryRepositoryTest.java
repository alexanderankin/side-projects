package side.cloud.util.acme.lib.challenges;

import org.junit.jupiter.api.Test;
import side.cloud.util.acme.lib.challenges.PresentedChallengeRepository.PresentedChallenge;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.DefaultInMemoryRepository;
import side.cloud.util.acme.lib.model.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultInMemoryRepositoryTest {
    private final Repository<PresentedChallenge> repository = new DefaultInMemoryRepository<>(PresentedChallenge.class, PresentedChallenge::key, new ConcurrentHashMap<>());

    @Test
    void cleanupChallenge() {
        var challengeId = repository.newItem(
                new PresentedChallenge(new Challenge().setType("dns-01").setToken("token"),
                        new Authorization(),
                        "key",
                        "value"),
                Instant.now(), Duration.ofSeconds(30));

        assertThat(repository.isItemValid(challengeId), is(notNullValue()));
        var removed = repository.useItem(challengeId);
        assertThat(repository.isItemValid(challengeId), is(nullValue()));

        assertThat(removed, is(notNullValue()));
        assertThat(repository.isItemValid(challengeId), is(nullValue()));
    }

    @Test
    void cleanExpiredChallenges() {
        repository.newItem(new PresentedChallenge(new Challenge().setType("dns-01").setToken("token"),
                        new Authorization(), "_acme-challenge.example.com", "v"),
                Instant.now().minusSeconds(120),
                Duration.ofMinutes(1));
        repository.newItem(new PresentedChallenge(new Challenge().setType("dns-01").setToken("token"),
                        new Authorization(), "k2", "v"),
                Instant.now().minusSeconds(120),
                Duration.ofMinutes(10));

        var removed = repository.cleanExpiredItems(Instant.now()).size();

        assertThat(removed, is(1));
        assertThat(repository.cleanExpiredItems(Instant.now()).size(), is(0));
    }
}
