package side.cloud.util.acme.lib.model.challenge;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.model.AcmeIdentifier;

@Data
@Accessors(chain = true)
public class ChallengeSolution {
    SupportedChallengeType type;

    /**
     * for http - path, for dns - relative path (e.g. "_acme-challenge")
     */
    String key;

    /**
     * for http - content returned by the path, for dns - content of TXT record
     */
    String value;

    /**
     * for http - server that serves files, for dns - rest of the dns name
     */
    AcmeIdentifier identifier;
}
