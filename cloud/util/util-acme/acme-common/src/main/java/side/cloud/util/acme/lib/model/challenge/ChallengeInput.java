package side.cloud.util.acme.lib.model.challenge;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;

@Data
@Accessors(chain = true)
public class ChallengeInput {
    SupportedClientKeyPair keyPair;
    Challenge challenge;
    SupportedChallengeType challengeType;
}
