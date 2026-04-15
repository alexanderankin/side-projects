package side.cloud.util.acme.lib.nonce;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DefaultNonceService implements NonceService {
    private final SecureRandom RANDOM = new SecureRandom();
    private final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final Config config;
    private final NonceRepository nonceRepository;
    private volatile Thread cleanupThread;

    @Override
    public String generateNonce() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    @Override
    public String newNonce() {
        var newNonce = nonceRepository.newItem(generateNonce(), Instant.now(), config.getTtl());
        log.debug("generated newNonce: {}", newNonce);
        return newNonce;
    }

    @Override
    public boolean nonceValid(String nonce) {
        var nonceValid = nonceRepository.isItemValid(nonce) != null;
        log.debug("nonceValid found that {} is valid: {}", nonce, nonceValid);
        return nonceValid;
    }

    @Override
    public boolean useNonce(String nonce) {
        var usedNonce = nonceRepository.useItem(nonce) != null;
        log.debug("useNonce used nonce {} successfully: {}", nonce, usedNonce);
        return usedNonce;
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

            cleanupThread = Thread.ofVirtual().name("DefaultNonceService.cleanup").start(this::loopCleanup);
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

                var cleanedExpiredNonces = (long) nonceRepository.cleanExpiredItems(Instant.now()).size();
                log.debug("loopCleanup: waited {}, cleaned {} expiredNonces", cleanInterval, cleanedExpiredNonces);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        /**
         * how long does nonce last
         */
        @NotNull
        Duration ttl;

        /**
         * how long to wait until clean up expired nonces
         */
        Duration cleanInterval;

        @Min(1)
        public long getTtlInMillis() {
            return ttl.toMinutes();
        }
    }
}
