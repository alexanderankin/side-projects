package side.cloud.util.acme.lib.challenges;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.util.Assert;
import side.cloud.util.acme.lib.challenges.PresentedChallengeRepository.PresentedChallenge;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.Repository;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import static org.slf4j.helpers.MessageFormatter.basicArrayFormat;

@Slf4j
@RequiredArgsConstructor
public class ChallengeTemplate implements ChallengeOperations {
    private final Config config;
    private final Presenter challengePresenter;
    private final Repository<PresentedChallenge> presentedChallengeRepository;
    private volatile Thread cleanupThread;

    private static Presenter.Type typeOfChallenge(PresentedChallenge item) {
        var presentType = Presenter.Type.valueOfAcmeNameOrNull(item.challenge().getType());
        if (presentType == null)
            throw new IllegalArgumentException("Unsupported challenge type: " + item.challenge().getType());
        return presentType;
    }

    protected String create(PresentedChallenge item) {
        var type = typeOfChallenge(item);

        var now = Instant.now();

        Instant authExpires;
        if (item.authorization() != null) {
            authExpires = item.authorization().getExpires();
        } else {
            authExpires = null;
        }

        Duration expiresIn;
        if (authExpires != null) {
            expiresIn = now.until(authExpires);
        } else {
            expiresIn = config.getTtl();
        }

        // gen key
        var key = presentedChallengeRepository.newItem(item, now, expiresIn);
        // present it
        try {
            challengePresenter.add(type, item.key(), item.value());
        } catch (Exception e) {
            log.error("could not present challenge {} of type {}: {}", key, type, item, e);
            presentedChallengeRepository.useItem(key);
            throw new RuntimeException(basicArrayFormat("could not present challenge {} of type {}: {}", new Object[]{key, type, item}), e);
        }
        return key;
    }

    @Override
    public String httpChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization) {
        var keyAuthorization = Utils.keyAuthorization(keyPair, challenge.getToken());
        return create(new PresentedChallenge(challenge, authorization, challenge.getToken(), keyAuthorization));
    }

    @Override
    public String dnsChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization) {
        var dnsName = "_acme-challenge." + Utils.authorizationIdentifierDomain(authorization);
        var keyAuthorizationDigest = Utils.dns01KeyAuthorizationDigest(keyPair, challenge.getToken());
        return create(new PresentedChallenge(challenge, authorization, dnsName, keyAuthorizationDigest));
    }

    @Override
    public String tlsAlpnChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization) {
        var host = Utils.authorizationIdentifierDomain(authorization);
        var keyAuthorization = Utils.keyAuthorization(keyPair, challenge.getToken());
        return create(new PresentedChallenge(challenge, authorization, host, keyAuthorization));
    }

    @Override
    public String dnsAccountChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization, URI accountUrl) {
        var dnsName = Utils.dnsAccount01ValidationDomainName(authorization, accountUrl);
        var keyAuthorizationDigest = Utils.dns01KeyAuthorizationDigest(keyPair, challenge.getToken());
        return create(new PresentedChallenge(challenge, authorization, dnsName, keyAuthorizationDigest));
    }

    @Override
    public String dnsPersistChallenge(Challenge challenge, Authorization authorization, URI accountUrl) {
        var dnsName = "_validation-persist." + Utils.authorizationIdentifierDomain(authorization);
        var issueValue = Utils.dnsPersist01IssueValue(challenge, authorization, accountUrl);
        return create(new PresentedChallenge(challenge, authorization, dnsName, issueValue));
    }

    @Override
    public boolean cleanup(String challengeId) {
        var ch = presentedChallengeRepository.useItem(challengeId);
        var found = ch != null;
        if (found) {
            var type = typeOfChallenge(ch);
            try {
                challengePresenter.delete(type, ch.key());
            } catch (Exception e) {
                log.warn("Failed to delete challenge with id {} and type {}", challengeId, type, e);
            }
        }
        return found;
    }

    @Override
    public void startCleanup() {
        if (cleanupThread != null) {
            log.debug("already started thread {} for background cleanup", cleanupThread.getName());
            return;
        }

        synchronized (this) {
            if (cleanupThread != null) {
                log.debug("while waiting to start thread for background cleanup, {} was started", cleanupThread.getName());
                return;
            }

            cleanupThread = Thread.ofVirtual().name("ChallengeTemplate.cleanup").start(this::loopCleanup);
            log.info("started cleanup thread: {}", cleanupThread.getName());
        }
    }

    private void loopCleanup() {
        var cleanInterval = Optional.ofNullable(config.getCleanInterval())
                .or(() -> Optional.ofNullable(config.getTtl()))
                .orElseThrow();

        while (true) {
            try {
                log.trace("loopCleanup: waiting {}", cleanInterval);
                Thread.sleep(cleanInterval);

                var cleanedExpiredChallenges = presentedChallengeRepository.cleanExpiredItems(Instant.now());
                log.debug("loopCleanup: waited {}, cleaned {} expiredChallenges", cleanInterval, cleanedExpiredChallenges);
                for (var challenge : cleanedExpiredChallenges) {
                    var type = typeOfChallenge(challenge);
                    try {
                        challengePresenter.delete(type, challenge.key());
                    } catch (Exception e) {
                        log.error("could not clean expired challenge {}: of type {}: {}", challenge.key(), type, challenge);
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

    static class Utils {
        private static final Base32 BASE_32_ENCODER = Base32.builder().setPadding((byte) 0).get();

        static String keyAuthorization(SupportedClientKeyPair keyPair, String token) {
            return token + "." + keyPair.jwkPublicKeySha256Thumbprint();
        }

        @SneakyThrows
        static String dns01KeyAuthorizationDigest(SupportedClientKeyPair keyPair, String token) {
            var keyAuthorization = keyAuthorization(keyPair, token);
            var digest = MessageDigest.getInstance("SHA-256").digest(keyAuthorization.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        }

        static String authorizationIdentifierDomain(Authorization authorization) {
            Assert.notNull(authorization, "authorization must not be null");
            Assert.notNull(authorization.getIdentifier(), "authorization.identifier must not be null");
            Assert.hasText(authorization.getIdentifier().getValue(), "authorization.identifier.value must not be blank");
            var value = authorization.getIdentifier().getValue();
            return value.startsWith("*.") ? value.substring(2) : value;
        }

        @SneakyThrows
        static String dnsAccount01ValidationDomainName(Authorization authorization, URI accountUrl) {
            Assert.notNull(accountUrl, "accountUrl must not be null");
            var digest = MessageDigest.getInstance("SHA-256").digest(accountUrl.toString().getBytes(StandardCharsets.US_ASCII));
            var first10 = Arrays.copyOf(digest, 10);
            return "_" + BASE_32_ENCODER.encodeToString(first10).toLowerCase() + "._acme-challenge." + authorizationIdentifierDomain(authorization);
        }

        static String dnsPersist01IssueValue(Challenge challenge, Authorization authorization, URI accountUrl) {
            Assert.notNull(challenge, "challenge must not be null");
            Assert.notNull(authorization, "authorization must not be null");
            Assert.notNull(accountUrl, "accountUrl must not be null");
            var issuerDomainNames = challenge.getIssuerDomainNames();
            Assert.notEmpty(issuerDomainNames, "challenge.issuerDomainNames must not be empty");
            var issuerDomain = issuerDomainNames.getFirst();
            var policy = authorization.isWildcard() ? "; policy=wildcard" : "";
            return issuerDomain + "; accounturi=" + accountUrl + policy;
        }
    }


    @Data
    @Accessors(chain = true)
    public static class Config {
        @NotNull
        Duration ttl = Duration.ofHours(5);
        Duration cleanInterval = Duration.ofHours(1);

        @Min(1)
        public long getTtlInMillis() {
            return ttl.toMinutes();
        }
    }
}
