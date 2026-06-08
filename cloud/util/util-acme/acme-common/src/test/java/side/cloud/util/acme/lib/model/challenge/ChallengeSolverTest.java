package side.cloud.util.acme.lib.model.challenge;

import tools.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPairAlgorithm;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.challenge.ChallengeSolver.Config;
import side.cloud.util.acme.lib.model.challenge.persistence.ChallengeSolutionRepository;
import side.cloud.util.acme.lib.model.challenge.presentation.ChallengePresenter;
import side.cloud.util.acme.lib.model.challenge.presentation.ExternalVerifier;

import static org.mockito.Mockito.mock;
import static side.cloud.util.acme.lib.model.AcmeIdentifier.AcmeIdentifierType.dns;

class ChallengeSolverTest {

    ChallengeSolver challengeSolver;

    @BeforeEach
    void setUp() {
        challengeSolver = new ChallengeSolver(
                new Config(),
                ChallengeSolutionRepository.inMemory(),
                mock(ChallengePresenter.class),
                mock(ExternalVerifier.class));
    }

    @SneakyThrows
    @Test
    void test() {
        var challengeInput = new ChallengeInput()
                .setKeyPair(SupportedClientKeyPairAlgorithm.ES256.generate())
                .setAccountUrl(null)
                .setAuthorization(new AcmeResources.Authorization()
                        .setIdentifier(new AcmeIdentifier().setType(dns).setValue("example.localhost")))
                .setChallenge(new AcmeResources.Challenge()
                        .setType(SupportedChallengeType.ChallengeDNS01.getRfcName())
                        .setToken("challenge-token"));
        System.out.println(JsonMapper.builder().build().writeValueAsString(challengeSolver.solve(challengeInput)));
        challengeInput.getChallenge().setType(SupportedChallengeType.ChallengeHTTP01.getRfcName());
        System.out.println(JsonMapper.builder().build().writeValueAsString(challengeSolver.solve(challengeInput)));
    }

}
