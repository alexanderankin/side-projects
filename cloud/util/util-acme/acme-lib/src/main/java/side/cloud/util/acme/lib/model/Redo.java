package side.cloud.util.acme.lib.model;

import lombok.SneakyThrows;
import org.springframework.util.Assert;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.AcmeResources.Order;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface Redo {
    record OrderAuthorizationChallenge(Order order, Authorization authorization, Challenge challenge) {
    }

    abstract class Solver {
        abstract UUID createSolution(SupportedClientKeyPair keyPair, OrderAuthorizationChallenge challenge);

        abstract void deleteSolution(UUID solutionId);
    }

    abstract class SolutionRepository {
        abstract UUID createSolution(OrderAuthorizationChallenge challenge, String key, String value);
    }

    abstract class InMemorySolutionRepository extends SolutionRepository {
        Map<UUID, Solution> solutions = new ConcurrentHashMap<>();

        @Override
        UUID createSolution(OrderAuthorizationChallenge challenge, String key, String value) {
            var id = UUID.randomUUID();
            Assert.isNull(solutions.putIfAbsent(id, new Solution(challenge, key, value)), "uuid is repeated");
            return id;
        }

        record Solution(OrderAuthorizationChallenge challenge, String key, String value) {
            boolean isExpired(Instant now) {
                if (challenge.order.getNotAfter() != null &&
                        challenge.order.getNotAfter().isAfter(now)) {
                    return false;
                }

                if (challenge.order.getNotBefore() != null &&
                        challenge.order.getNotBefore().isBefore(now)) {
                    return false;
                }

                if (challenge.authorization.getExpires() != null &&
                        challenge.authorization.getExpires().isBefore(now)) {
                    return false;
                }

                return true;
            }
        }
    }

    abstract class ChallengeSolutionManager {
        Solver solver;
        SolutionRepository solutionRepository;

        UUID solveChallenge(SupportedClientKeyPair keyPair, OrderAuthorizationChallenge challenge) {
            return solver.createSolution(keyPair, challenge);
        }

        void startCleaning() {
            Thread.ofVirtual().name(getClass().getSimpleName()).start(this::clean);
        }

        @SneakyThrows
        void clean() {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(Duration.ofMinutes(1));
            }
        }
    }
}
