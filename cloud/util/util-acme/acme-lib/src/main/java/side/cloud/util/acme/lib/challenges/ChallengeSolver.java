package side.cloud.util.acme.lib.challenges;

import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

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
    void dnsChallenge(Challenge challenge, SupportedClientKeyPair keyPair);
}
