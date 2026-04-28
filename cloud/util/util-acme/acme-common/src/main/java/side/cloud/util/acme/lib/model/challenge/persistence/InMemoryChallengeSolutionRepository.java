package side.cloud.util.acme.lib.model.challenge.persistence;

import side.cloud.util.acme.lib.model.challenge.ChallengeSolution;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryChallengeSolutionRepository implements ChallengeSolutionRepository {
    private final ConcurrentHashMap<ChallengeSolution, DataItem> challengeSolutions = new ConcurrentHashMap<>();

    @Override
    public void add(ChallengeSolution challengeSolution, Duration duration) {
        var dataItem = challengeSolutions.putIfAbsent(challengeSolution, new DataItem(challengeSolution, Instant.now(), duration));
        if (dataItem != null) {
            throw new IllegalArgumentException("Duplicate challenge solution");
        }
    }

    @Override
    public void remove(ChallengeSolution challengeSolution) {
        challengeSolutions.remove(challengeSolution);
    }

    @Override
    public List<ChallengeSolution> listExpired(Instant now) {
        return challengeSolutions.values().stream()
                .filter(new ExpirationChecker(now)::expired)
                .map(DataItem::challengeSolution)
                .toList();
    }

    record DataItem(ChallengeSolution challengeSolution, Instant added, Duration duration) {
    }

    record ExpirationChecker(Instant now) {
        boolean expired(DataItem dataItem) {
            return now.isAfter(dataItem.added.plus(dataItem.duration));
        }
    }
}
