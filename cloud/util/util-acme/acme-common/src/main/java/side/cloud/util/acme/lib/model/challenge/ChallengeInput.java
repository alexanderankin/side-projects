package side.cloud.util.acme.lib.model.challenge;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;

import java.net.URI;

@Data
@Accessors(chain = true)
public class ChallengeInput {
    /**
     * client's authentication
     */
    SupportedClientKeyPair keyPair;
    /**
     * the account that the client's auth is for
     */
    URI accountUrl;
    /**
     * order's identifier
     */
    Authorization authorization;
    /**
     * server challenge of the input
     */
    Challenge challenge;
}
