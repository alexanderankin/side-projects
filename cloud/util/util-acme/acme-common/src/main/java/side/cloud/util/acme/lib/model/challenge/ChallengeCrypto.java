package side.cloud.util.acme.lib.model.challenge;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base32;
import org.springframework.util.Assert;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class ChallengeCrypto {
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

    static String authorizationIdentifierDomain(AcmeIdentifier identifier) {
        var value = identifier.getValue();
        return value.startsWith("*.") ? value.substring(2) : value;
    }

    @SneakyThrows
    static String dnsAccount01ValidationDomainName(AcmeIdentifier authorization, URI accountUrl) {
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
