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

        assertThat(order.resource().getAuthorizations(), hasSize(1));
        var authorizationId = order.resource().getAuthorizations().getFirst();
        var authorization = acmeClient.getResource(sckp, account.id(), authorizationId, Authorization.class);
        var challenge = authorization.getChallenges().stream()
                .filter(c -> c.getType().equals("http-01"))
                .findAny().orElseThrow();

        var pebbleChallengeClient = getPebbleChallengeClient();
        var solver = new PebbleChallengeSolver((pebbleChallengeClient));
        solver.httpChallenge(challenge, sckp);

        assertThat(challenge.getStatus(), is(pending));
        acmeClient.postResource(sckp, account.id(), challenge.getUrl(), Map.of(), Challenge.class);

        var doneChallenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
            var latestChallenge = acmeClient.getResource(sckp, account.id(), challenge.getUrl(), Challenge.class);
            Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
            return latestChallenge;
        });

        assertThat("doneChallenge did not reach terminal status before timeout", doneChallenge, is(notNullValue()));
        assertThat("doneChallenge should be valid but was " + doneChallenge, doneChallenge.getStatus(), is(equalTo(valid)));
    }

    @SneakyThrows
    @Test
    void test_dnsChallenge() {
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

        var dnsHost = "testdnschallenge.localhost";
        for (var identifier : List.of("*." + dnsHost, dnsHost)) {
            var now = Instant.now();
            var order = acmeClient.newOrder(sckp, account.id(), new NewOrder()
                    .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue(identifier)))
                    .setNotBefore(now)
                    .setNotAfter(now.plus(Duration.ofDays(10))));

            assertThat(order.resource().getAuthorizations(), hasSize(1));
            var authorizationId = order.resource().getAuthorizations().getFirst();
            var authorization = acmeClient.getResource(sckp, account.id(), authorizationId, Authorization.class);
            var challenge = authorization.getChallenges().stream()
                    .filter(c -> c.getType().equals("dns-01"))
                    .findAny().orElseThrow();

            var pebbleChallengeClient = getPebbleChallengeClient();
            var solver = new PebbleChallengeSolver(pebbleChallengeClient);
            solver.dnsChallenge(challenge, sckp, authorization);

            assertThat(challenge.getStatus(), is(pending));
            acmeClient.postResource(sckp, account.id(), challenge.getUrl(), Map.of(), Challenge.class);

            var doneChallenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
                var latestChallenge = acmeClient.getResource(sckp, account.id(), challenge.getUrl(), Challenge.class);
                Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
                return latestChallenge;
            });

            assertThat("doneChallenge did not reach terminal status before timeout", doneChallenge, is(notNullValue()));
            assertThat("doneChallenge should be valid but was " + doneChallenge, doneChallenge.getStatus(), is(equalTo(valid)));
        }
    }

    @SneakyThrows
    @Test
    void test_tlsAlpnChallenge() {
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
                .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("testnonwildcard.localhost")))
                .setNotBefore(now)
                .setNotAfter(now.plus(Duration.ofDays(10))));

        assertThat(order.resource().getAuthorizations(), hasSize(1));
        var authorizationId = order.resource().getAuthorizations().getFirst();
        var authorization = acmeClient.getResource(sckp, account.id(), authorizationId, Authorization.class);
        var challenge = authorization.getChallenges().stream()
                .filter(c -> c.getType().equals("tls-alpn-01"))
                .findAny().orElseThrow();

        var pebbleChallengeClient = getPebbleChallengeClient();
        var solver = new PebbleChallengeSolver((pebbleChallengeClient));
        solver.tlsAlpnChallenge(challenge, sckp, authorization);

        assertThat(challenge.getStatus(), is(pending));
        acmeClient.postResource(sckp, account.id(), challenge.getUrl(), Map.of(), Challenge.class);

        var doneChallenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
            var latestChallenge = acmeClient.getResource(sckp, account.id(), challenge.getUrl(), Challenge.class);
            Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
            return latestChallenge;
        });

        assertThat("doneChallenge did not reach terminal status before timeout", doneChallenge, is(notNullValue()));
        assertThat("doneChallenge should be valid but was " + doneChallenge, doneChallenge.getStatus(), is(equalTo(valid)));
    }

    @SneakyThrows
    @Test
    void test_dnsAccountChallenge() {
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
                .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("testdnsaccount.localhost")))
                .setNotBefore(now)
                .setNotAfter(now.plus(Duration.ofDays(10))));

        assertThat(order.resource().getAuthorizations(), hasSize(1));
        var authorizationId = order.resource().getAuthorizations().getFirst();
        var authorization = acmeClient.getResource(sckp, account.id(), authorizationId, Authorization.class);
        var challenge = authorization.getChallenges().stream()
                .filter(c -> c.getType().equals("dns-account-01"))
                .findAny().orElseThrow();

        var pebbleChallengeClient = getPebbleChallengeClient();
        var solver = new PebbleChallengeSolver((pebbleChallengeClient));
        solver.dnsAccountChallenge(challenge, sckp, authorization, account.id());

        assertThat(challenge.getStatus(), is(pending));
        acmeClient.postResource(sckp, account.id(), challenge.getUrl(), Map.of(), Challenge.class);

        var doneChallenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
            var latestChallenge = acmeClient.getResource(sckp, account.id(), challenge.getUrl(), Challenge.class);
            Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
            return latestChallenge;
        });

        assertThat("doneChallenge did not reach terminal status before timeout", doneChallenge, is(notNullValue()));
        assertThat("doneChallenge should be valid but was " + doneChallenge, doneChallenge.getStatus(), is(equalTo(valid)));
    }

    @SneakyThrows
    @Test
    void test_dnsPersistChallenge() {
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
                .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("*.testdnspersist.localhost")))
                .setNotBefore(now)
                .setNotAfter(now.plus(Duration.ofDays(10))));

        assertThat(order.resource().getAuthorizations(), hasSize(1));
        var authorizationId = order.resource().getAuthorizations().getFirst();
        var authorization = acmeClient.getResource(sckp, account.id(), authorizationId, Authorization.class);
        var challenge = authorization.getChallenges().stream()
                .filter(c -> c.getType().equals("dns-persist-01"))
                .findAny().orElseThrow();

        var pebbleChallengeClient = getPebbleChallengeClient();
        var solver = new PebbleChallengeSolver((pebbleChallengeClient));
        solver.dnsPersistChallenge(challenge, authorization, account.id());

        assertThat(challenge.getStatus(), is(pending));
        acmeClient.postResource(sckp, account.id(), challenge.getUrl(), Map.of(), Challenge.class);

        var doneChallenge = Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> {
            var latestChallenge = acmeClient.getResource(sckp, account.id(), challenge.getUrl(), Challenge.class);
            Assert.isTrue(Set.of(valid, invalid).contains(latestChallenge.getStatus()), "status be terminal");
            return latestChallenge;
        });

        assertThat("doneChallenge did not reach terminal status before timeout", doneChallenge, is(notNullValue()));
        assertThat("doneChallenge should be valid but was " + doneChallenge, doneChallenge.getStatus(), is(equalTo(valid)));
    }
}
