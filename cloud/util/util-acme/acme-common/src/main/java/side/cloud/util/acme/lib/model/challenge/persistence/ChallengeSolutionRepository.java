package side.cloud.util.acme.lib.model.challenge.persistence;

import side.cloud.util.acme.lib.model.challenge.ChallengeSolution;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface ChallengeSolutionRepository {
    static ChallengeSolutionRepository inMemory() {
        return new InMemoryChallengeSolutionRepository();
    }

    void add(ChallengeSolution challengeSolution, Duration duration);

    void remove(ChallengeSolution challengeSolution);

    List<ChallengeSolution> listExpired(Instant now);
}
