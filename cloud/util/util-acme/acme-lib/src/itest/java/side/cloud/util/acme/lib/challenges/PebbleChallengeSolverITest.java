package side.cloud.util.acme.lib.challenges;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.AcmeClient;
import side.cloud.util.acme.lib.AcmeClientTemplate;
import side.cloud.util.acme.lib.AcmeLibBaseITest;
import side.cloud.util.acme.lib.challenges.pebble.PebbleChallengeSolver;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.AcmeResources.NewAccount;
import side.cloud.util.acme.lib.model.AcmeResources.NewOrder;
import side.cloud.util.acme.lib.model.SupportedClientKeyPairAlgorithm;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static side.cloud.util.acme.lib.model.AcmeIdentifier.AcmeIdentifierType.dns;
import static side.cloud.util.acme.lib.model.AcmeResources.Challenge.ChallengeStatus.*;

class PebbleChallengeSolverITest extends AcmeLibBaseITest {
    @SneakyThrows
    @Test
    void test_httpChallenge() {
        var sckp = SupportedClientKeyPairAlgorithm.ES256.generate();
        var acmeClient = new AcmeClient(
                new AcmeClient.Configuration()
                        .setDirectoryUrl(getPebbleDirectoryUrl()),
                new AcmeClientTemplate(modifyToTrustAll(RestClient.builder().build())),
                JsonMapper.builder().findAndAddModules().build()
        );
        var account = acmeClient.newAccount(sckp, new NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        var now = Instant.now();
        var order = acmeClient.newOrder(sckp, account.id(), new NewOrder()
                .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("testnonwildcard.example")))
                .setNotBefore(now)
                .setNotAfter(now.plus(Duration.ofDays(10))));
        Challenge c = null;
        for (var authorization : order.resource().getAuthorizations()) {
            var resource = acmeClient.getResource(sckp, account.id(), authorization, Authorization.class);
            System.out.println("resource: " + resource);
            var challenges = resource.getChallenges();
            challenges.forEach(System.out::println);
            c = challenges.stream().filter(cc -> cc.getType().equals("http-01")).findAny().orElseThrow();
        }
        assertThat(c, is(notNullValue()));

        var pebbleChallengeClient = getPebbleChallengeClient();
        var solver = new PebbleChallengeSolver(new PebbleChallengeSolver.Config().setClient(pebbleChallengeClient));
        solver.httpChallenge(c, sckp);

        assertThat(c.getStatus(), is(pending));
        acmeClient.postResource(sckp, account.id(), c.getUrl(), Map.of(), Challenge.class);

        Challenge finalC = c;
        var challenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
            var latestChallenge = acmeClient.getResource(sckp, account.id(), finalC.getUrl(), Challenge.class);
            Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
            return latestChallenge;
        });

        assertThat("challenge did not reach terminal status before timeout", challenge, is(notNullValue()));
        assertThat("challenge should be valid but was " + challenge, challenge.getStatus(), is(equalTo(valid)));
    }
}
