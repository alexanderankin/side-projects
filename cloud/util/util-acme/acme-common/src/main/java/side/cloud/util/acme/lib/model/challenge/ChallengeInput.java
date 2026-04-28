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
    SupportedClientKeyPair keyPair;
    URI accountUrl;
    Authorization authorization;
    Challenge challenge;
}
