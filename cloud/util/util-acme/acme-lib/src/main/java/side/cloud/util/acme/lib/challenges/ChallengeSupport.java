package side.cloud.util.acme.lib.challenges;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.util.Assert;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
public class ChallengeSupport {
    private static final Base32 BASE_32_ENCODER = Base32.builder().setPadding((byte) 0).get();


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

    public static String dnsHost(Authorization authorization) {
        Assert.notNull(authorization, "authorization must not be null");
        Assert.notNull(authorization.getIdentifier(), "authorization.identifier must not be null");
        Assert.hasText(authorization.getIdentifier().getValue(), "authorization.identifier.value must not be blank");
        var value = authorization.getIdentifier().getValue();
        return value.startsWith("*.") ? value.substring(2) : value;
    }

    public static String dns01TxtRecordName(Authorization authorization) {
        return "_acme-challenge." + dnsHost(authorization);
    }

    @SneakyThrows
    public static String dnsAccount01Label(URI accountUrl) {
        Assert.notNull(accountUrl, "accountUrl must not be null");
        var digest = MessageDigest.getInstance("SHA-256").digest(accountUrl.toString().getBytes(StandardCharsets.US_ASCII));
        var first10 = Arrays.copyOf(digest, 10);
        return BASE_32_ENCODER.encodeToString(first10).toLowerCase();
    }

    public static String dnsAccount01TxtRecordName(Authorization authorization, URI accountUrl) {
        return "_" + dnsAccount01Label(accountUrl) + "._acme-challenge." + dnsHost(authorization);
    }

    public static String dnsPersist01TxtRecordName(Authorization authorization) {
        return "_validation-persist." + dnsHost(authorization);
    }

    public static String dnsPersist01TxtRecordValue(Challenge challenge, Authorization authorization, URI accountUrl) {
        Assert.notNull(challenge, "challenge must not be null");
        Assert.notNull(authorization, "authorization must not be null");
        Assert.notNull(accountUrl, "accountUrl must not be null");
        var issuerDomain = firstIssuerDomainName(challenge.getIssuerDomainNames());
        var policy = authorization.isWildcard() ? "; policy=wildcard" : "";
        return issuerDomain + "; accounturi=" + accountUrl + policy;
    }

    private static String firstIssuerDomainName(List<String> issuerDomainNames) {
        Assert.notEmpty(issuerDomainNames, "challenge.issuerDomainNames must not be empty");
        return issuerDomainNames.getFirst();
    }

}
