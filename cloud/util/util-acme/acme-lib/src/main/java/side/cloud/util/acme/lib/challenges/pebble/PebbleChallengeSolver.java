package side.cloud.util.acme.lib.challenges.pebble;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.challenges.ChallengeSolver;
import side.cloud.util.acme.lib.challenges.ChallengeSupport;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

@Data
public class PebbleChallengeSolver implements ChallengeSolver {
    private final Config config;

    @Override
    public void httpChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair) {
        config.client.addHttp01(challenge.getToken(), ChallengeSupport.keyAuthorization(keyPair, challenge.getToken()));
    }

    @Override
    public void dnsChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair) {

    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        PebbleChallengeServerClient client;
    }
}
