package side.cloud.util.acme.lib.model.challenge;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.challenge.persistence.ChallengeSolutionRepository;
import side.cloud.util.acme.lib.model.challenge.presentation.ChallengePresenter;
import side.cloud.util.acme.lib.model.challenge.presentation.ExternalVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static side.cloud.util.acme.lib.model.challenge.ChallengeCrypto.*;

@Slf4j
@RequiredArgsConstructor
public class ChallengeSolver {
    private final Config config;
    private final ChallengeSolutionRepository solutionRepository;
    private final ChallengePresenter challengePresenter;
    private final ExternalVerifier externalVerifier;
    private volatile Thread cleanupThread;

    public ChallengeSolution solve(ChallengeInput challengeInput) {
        Instant now = Instant.now();
        var solution = solution(challengeInput);

        var authExpiration = challengeInput.getAuthorization().getExpires();
        Duration duration = authExpiration == null ? config.defaultSolutionDuration : Duration.between(now, authExpiration);
        solutionRepository.add(solution, duration);
        challengePresenter.present(solution);

        return solution;
    }

    @SneakyThrows
    public void waitFor(ChallengeSolution solution, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (challengePresenter.verify(solution)) {
                return;
            }
            if (externallyVerify(solution))
                return;
            try {
                Thread.sleep(config.challengePollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting", e);
            }
        }

        throw new RuntimeException("Timed out waiting for solution");
    }

    private boolean externallyVerify(ChallengeSolution solution) {
        return switch (solution.getType()) {
            case ChallengeHTTP01 -> externalVerifier.verifyHttp(null, null);
            case ChallengeDNS01 -> externalVerifier.verifyDns(null, null);
            case ChallengeDNSAccount01 -> externalVerifier.verifyDns(null, null);
            case ChallengeDNSPersist01 -> externalVerifier.verifyDns(null, null);
            case null, default -> throw new UnsupportedOperationException();
        };
    }

    public void clean(ChallengeSolution solution) {
        challengePresenter.remove(solution);
        solutionRepository.remove(solution);
    }

    public void startCleaning() {
        if (cleanupThread != null) {
            log.debug("already started thread {} for background cleanup", cleanupThread.getName());
            return;
        }

        synchronized (this) {
            if (cleanupThread != null) {
                log.debug("while waiting to start thread for background cleanup, {} was started", cleanupThread.getName());
                return;
            }

            cleanupThread = Thread.ofVirtual().name(config.solutionCleanupThreadName).start(this::loopCleanup);
            log.info("started cleanup thread: {}", cleanupThread.getName());
        }
    }

    private void loopCleanup() {
        while (true) {
            try {
                log.trace("loopCleanup: waiting {}", config.cleanInterval);
                Thread.sleep(config.cleanInterval);

                var cleanedExpiredChallenges = solutionRepository.listExpired(Instant.now());
                log.debug("loopCleanup: waited {}, cleaned {} expiredChallenges", config.cleanInterval, cleanedExpiredChallenges);
                for (ChallengeSolution challenge : cleanedExpiredChallenges) {
                    var type = challenge.getType();
                    try {
                        clean(challenge);
                    } catch (Exception e) {
                        log.error("could not clean expired challenge {}: of type {}: {}", challenge, type, challenge);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                log.warn("loopCleanup: cleanup failed", e);
            }
        }
    }

    ChallengeSolution solution(ChallengeInput challengeInput) {
        var result = new ChallengeSolution();
        var identifier = challengeInput.getAuthorization().getIdentifier();
        result.setIdentifier(identifier);

        var keyPair = challengeInput.getKeyPair();
        var challenge = challengeInput.getChallenge();
        var authorization = challengeInput.getAuthorization();
        var accountUrl = challengeInput.getAccountUrl();
        var chType = SupportedChallengeType.valueOfRfcName(challenge.getType());
        result.setType(chType);
        Map.Entry<String, String> kv = switch (chType) {
            case ChallengeHTTP01 -> Map.entry(
                    challenge.getToken(),
                    keyAuthorization(keyPair, challenge.getToken())
            );
            case ChallengeTLSALPN01 -> Map.entry(
                    authorizationIdentifierDomain(identifier),
                    keyAuthorization(keyPair, challenge.getToken())
            );
            case ChallengeDNS01 -> Map.entry(
                    "_acme-challenge." + authorizationIdentifierDomain(identifier),
                    dns01KeyAuthorizationDigest(keyPair, challenge.getToken())
            );
            case ChallengeDNSAccount01 -> Map.entry(
                    dnsAccount01ValidationDomainName(identifier, accountUrl),
                    dns01KeyAuthorizationDigest(keyPair, challenge.getToken())
            );
            case ChallengeDNSPersist01 -> Map.entry(
                    "_validation-persist." + authorizationIdentifierDomain(identifier),
                    dnsPersist01IssueValue(challenge, authorization, accountUrl)
            );
        };

        result.setKey(kv.getKey());
        result.setValue(kv.getValue());
        return result;
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        private Duration defaultSolutionDuration = Duration.ofDays(1);
        private String solutionCleanupThreadName = "ChallengeSolver.cleanup";
        private Duration cleanInterval = Duration.ofHours(1);
        private Duration challengePollInterval = Duration.ofSeconds(30);
    }
}
