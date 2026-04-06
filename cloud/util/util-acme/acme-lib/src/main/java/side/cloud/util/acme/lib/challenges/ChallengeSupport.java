package side.cloud.util.acme.lib.challenges;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
public class ChallengeSupport {
    /**
     * @see ChallengeSolver#dnsChallenge(Challenge, SupportedClientKeyPair)
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555/#section-8.4">rfc8555 8.4 dns-01</a>
     */
    @SneakyThrows
    public static String hashOfTokenAndKey(SupportedClientKeyPair keyPair, String token) {
        var eka = token + "." + keyPair.jwkPublicKeySha256Thumbprint();
        var authorizedKeysDigest = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(eka.getBytes()));
        log.trace("hashOfTokenAndKey: {} from key pair {} and token {}", authorizedKeysDigest, keyPair, token);
        return authorizedKeysDigest;
    }
}
