package side.cloud.util.acme.lib.challenges;

import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.Repository;

public interface PresentedChallengeRepository extends Repository<PresentedChallengeRepository.PresentedChallenge> {
    record PresentedChallenge(Challenge challenge, Authorization authorization, String key, String value) {
    }
}
