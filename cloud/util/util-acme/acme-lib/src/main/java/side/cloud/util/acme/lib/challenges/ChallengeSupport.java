package side.cloud.util.acme.lib.challenges;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
public class ChallengeSupport {
    /**
     * @see ChallengeSolver#httpChallenge(Challenge, SupportedClientKeyPair)
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555/#section-8.1">rfc8555 8.1 key authorization</a>
     */
    public static String keyAuthorization(SupportedClientKeyPair keyPair, String token) {
        return token + "." + keyPair.jwkPublicKeySha256Thumbprint();
    }

    /**
     * @see ChallengeSolver#dnsChallenge(Challenge, SupportedClientKeyPair, Authorization)
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555/#section-8.4">rfc8555 8.4 dns-01</a>
     */
    @SneakyThrows
    public static String hashOfTokenAndKey(SupportedClientKeyPair keyPair, String token) {
        var keyAuthorization = keyAuthorization(keyPair, token);
        var digest = MessageDigest.getInstance("SHA-256").digest(keyAuthorization.getBytes(StandardCharsets.US_ASCII));
        var authorizedKeysDigest = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        log.trace("hashOfTokenAndKey: {} from key pair {} and token {}", authorizedKeysDigest, keyPair, token);
        return authorizedKeysDigest;
    }
}
