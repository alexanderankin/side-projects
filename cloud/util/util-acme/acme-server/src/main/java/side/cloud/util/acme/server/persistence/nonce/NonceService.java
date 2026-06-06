package side.cloud.util.acme.server.persistence.nonce;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@RequiredArgsConstructor
public class NonceService {
    private final NonceRepository nonceRepository;
    private final SecureRandom secureRandom;
    private final Config config;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private volatile Thread cleanupThread;

    public String genNonce() {
        byte[] nonce = new byte[config.getRandomBytes()];
        secureRandom.nextBytes(nonce);
        return encoder.encodeToString(nonce);
    }

    public Instant now() {
        return Instant.now();
    }

    public String newNonce() {
        var nonce = genNonce();
        var now = now();
        var notAfter = now.plus(config.getTtl());
        nonceRepository.createNonce(nonce, now, notAfter);
        return nonce;
    }

    public boolean checkNonce(String nonce) {
        return nonceRepository.checkNonce(nonce, now());
    }

    public boolean useNonce(String nonce) {
        return nonceRepository.useNonce(nonce, now());
    }

    public void startCleanup() {
        if (cleanupThread == null) {
            synchronized (this) {
                if (cleanupThread == null) {
                    cleanupThread = Thread.ofPlatform().name(getClass().getSimpleName() + ".cleanup").start(this::cleanupLoop);
                }
            }
        }
    }

    @SneakyThrows
    private void cleanupLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            nonceRepository.cleanNonces(now());
            Thread.sleep(config.cleanupInterval);
        }
    }

    // todo validation
    @Data
    @Accessors(chain = true)
    @Validated
    public static class Config {
        int randomBytes = 40;
        Duration ttl = Duration.ofMinutes(10);
        Duration cleanupInterval = Duration.ofMinutes(5);
    }
}
