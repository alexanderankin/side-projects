package side.cloud.util.acme.lib.challenges.pebble;

import lombok.Data;
import side.cloud.util.acme.lib.challenges.ChallengeSolver;
import side.cloud.util.acme.lib.challenges.ChallengeSupport;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;

@Data
public class PebbleChallengeSolver implements ChallengeSolver {
    private final PebbleChallengeServerClient client;

    @Override
    public void httpChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair) {
        client.addHttp01(challenge.getToken(), ChallengeSupport.keyAuthorization(keyPair, challenge.getToken()));
    }

    @Override
    public void dnsChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair, AcmeResources.Authorization authorization) {
        client.addDnsTxt(
                ChallengeSupport.dns01TxtRecordName(authorization),
                ChallengeSupport.dns01KeyAuthorizationHash(keyPair, challenge.getToken()));
    }

    @Override
    public void tlsAlpnChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair, AcmeResources.Authorization authorization) {
        client.addTlsAlpn01(
                ChallengeSupport.authorizationIdentifierDomain(authorization),
                ChallengeSupport.keyAuthorization(keyPair, challenge.getToken()));
    }

    @Override
    public void dnsAccountChallenge(AcmeResources.Challenge challenge, SupportedClientKeyPair keyPair, AcmeResources.Authorization authorization, URI accountUrl) {
        client.addDnsTxt(
                ChallengeSupport.dnsAccount01TxtRecordName(authorization, accountUrl),
                ChallengeSupport.dns01KeyAuthorizationHash(keyPair, challenge.getToken()));
    }

    @Override
    public void dnsPersistChallenge(AcmeResources.Challenge challenge, AcmeResources.Authorization authorization, URI accountUrl) {
        client.addDnsTxt(
                ChallengeSupport.dnsPersist01TxtRecordName(authorization),
                ChallengeSupport.dnsPersist01TxtRecordValue(challenge, authorization, accountUrl));
    }
}
