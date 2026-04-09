package side.cloud.util.acme.lib.challenges;

import org.jspecify.annotations.Nullable;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;

public interface ChallengeOperations {
    @Nullable
    String httpChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization);

    @Nullable
    String dnsChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization);

    @Nullable
    String tlsAlpnChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization);

    @Nullable
    String dnsAccountChallenge(Challenge challenge, SupportedClientKeyPair keyPair, Authorization authorization, URI accountUrl);

    @Nullable
    String dnsPersistChallenge(Challenge challenge, Authorization authorization, URI accountUrl);

    boolean cleanup(String challengeId);

    void startCleanup();
}
