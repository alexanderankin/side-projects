package side.cloud.util.acme.lib.challenges;

import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;

public interface ChallengeSolver {
    /**
     * place the token at <code>http://{domain}/.well-known/acme-challenge/{token}</code>
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555/#section-8.3">rfc8555 8.3 http-01</a>
     */
    void httpChallenge(Challenge challenge, SupportedClientKeyPair keyPair);

    /**
     * place base64(sha256(token, base64(sha256Thumbprint(account)))) in _acme-challenge.{domain} TXT
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555/#section-8.4">rfc8555 8.4 dns-01</a>
     */
    void dnsChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization);

    /**
     * Present acme-tls/1 and key authorization digest for validation on the identifier host.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8737#section-3">rfc8737 3 tls-alpn-01</a>
     */
    void tlsAlpnChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization);

    /**
     * place base64url(sha256(keyAuthorization)) in _{lower(base32(sha256(accountURL)[:10]))}._acme-challenge.{domain} TXT
     */
    void dnsAccountChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization, URI accountUrl);

    /**
     * place "{issuer-domain}; accounturi={accountUrl}[; policy=wildcard]" in _validation-persist.{domain} TXT
     */
    void dnsPersistChallenge(Challenge challenge, Authorization authorization, URI accountUrl);
}
