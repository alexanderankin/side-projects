package side.cloud.util.acme.lib.challenges.pebble;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
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
    public void dnsChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair, AcmeResources.Authorization authorization) {
        var value = authorization.getIdentifier().getValue();
        var dnsHost = value.startsWith("*.") ? value.substring(2) : value;
        var txtName = "_acme-challenge." + dnsHost;
        config.client.addDnsTxt(txtName, ChallengeSupport.hashOfTokenAndKey(keyPair, challenge.getToken()));
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        PebbleChallengeServerClient client;
    }
}
