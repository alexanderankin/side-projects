package side.cloud.util.acme.lib;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Orders;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static side.cloud.util.acme.lib.model.AcmeIdentifier.AcmeIdentifierType.dns;

public class AcmeClientTest extends AcmeLibBaseITest {
    // SupportedClientKeyPair sckp = SupportedClientKeyPairAlgorithm.ES256.generate();
    SupportedClientKeyPair sckp = SupportedClientKeyPair.deserialize("v1:ES256:MFkwEwYHKoZIzj0CAQYIKo" +
            "ZIzj0DAQcDQgAEUDPFEhGFNAmgFheD_cDyofsqTgbkBAx4YCt" +
            "P56aU35pb4TzDqpVFV0QcycypvDrWdFAyG2muh7nCeGGKoZY6Ig" +
            ":MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQg_V" +
            "uL02p_VzsiBYeUpKa7HLMYsIquYRR1j7OBIHb_W1agCgYIKoZIz" +
            "j0DAQehRANCAARQM8USEYU0CaAWF4P9wPKh-ypOBuQEDHhgK0_n" +
            "ppTfmlvhPMOqlUVXRBzJzKm8OtZ0UDIbaa6HucJ4YYqhljoi");
    AcmeClient acmeClient;

    @BeforeEach
    void setUp() {
        acmeClient = new AcmeClient(
                new AcmeClient.Configuration()
                        .setDirectoryUrl(getPebbleDirectoryUrl()),
                new AcmeClientTemplate(modifyToTrustAll(RestClient.builder().build())),
                JsonMapper.builder().findAndAddModules().build()
        );
    }

    @Test
    void test() {
        var account = acmeClient.newAccount(sckp, new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        System.out.println(account);

        var orders = acmeClient.getResource(sckp, account.id(), account.resource().getOrders(), Orders.class);
        System.out.println("orders: " + orders);
        if (orders.getOrders().isEmpty()) {
            Instant now = Instant.now();
            var order = acmeClient.newOrder(sckp, account.id(), new AcmeResources.NewOrder()
                    .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("*.localhost.local")))
                    .setNotBefore(now)
                    .setNotAfter(now.plus(Duration.ofDays(10))));
            System.out.println("new order: " + order);
        }

        orders = acmeClient.getResource(sckp, account.id(), account.resource().getOrders(), Orders.class);
        var order = acmeClient.getResource(sckp, account.id(), orders.getOrders().getLast(), AcmeResources.Order.class);
        System.out.println("order: " + order);

        for (var authorization : order.getAuthorizations()) {
            System.out.println("authorization: " + authorization);
            var resource = acmeClient.getResource(sckp, account.id(), authorization, Authorization.class);
            System.out.println("resource: " + resource);
        }
    }

    @Test
    void test_dnsChallenge() {
        var account = acmeClient.newAccount(sckp, new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        var now = Instant.now();
        var order = acmeClient.newOrder(sckp, account.id(), new AcmeResources.NewOrder()
                    .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("*.testdnschallenge.localhost")))
                    .setNotBefore(now)
                    .setNotAfter(now.plus(Duration.ofDays(10))));
        for (var authorization : order.resource().getAuthorizations()) {
            var resource = acmeClient.getResource(sckp, account.id(), authorization, Authorization.class);
            System.out.println("resource: " + resource);
            /*
            resource: AcmeResources.Authorization(
                identifier=AcmeIdentifier(type=dns, value=testdnschallenge.localhost),
                status=pending,
                expires=2026-04-06T15:49:45Z,
                challenges=[
                    AcmeResources.Challenge(
                        type=dns-account-01,
                        url=https://localhost:63942/chalZ/XBE..., token=LPE... status=pending, issuerDomainNames=null, validated=null, error=null),
                    AcmeResources.Challenge(
                        type=dns-persist-01,
                        url=https://localhost:63942/chalZ/TaX..., token=null, status=pending, issuerDomainNames=[pebble.letsencrypt.org], validated=null, error=null),
                    AcmeResources.Challenge(type=dns-01,
                        url=https://localhost:63942/chalZ/8FC..., token=BUF..., status=pending, issuerDomainNames=null, validated=null, error=null)],
                wildcard=true)
             */
        }
    }

    @Test
    void test_nonWildCardChallenge() {
        var account = acmeClient.newAccount(sckp, new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@example.com")))
                .setTermsOfServiceAgreed(true));
        var now = Instant.now();
        var order = acmeClient.newOrder(sckp, account.id(), new AcmeResources.NewOrder()
                    .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("testnonwildcard.localhost")))
                    .setNotBefore(now)
                    .setNotAfter(now.plus(Duration.ofDays(10))));
        for (var authorization : order.resource().getAuthorizations()) {
            var resource = acmeClient.getResource(sckp, account.id(), authorization, Authorization.class);
            System.out.println("resource: " + resource);
            resource.getChallenges().forEach(System.out::println);
            /*
            resource: AcmeResources.Authorization(identifier=AcmeIdentifier(type=dns, value=testnonwildcard.localhost),
                status=pending, expires=2026-04-06T15:53:44Z, challenges=[...], wildcard=false)
            AcmeResources.Challenge(url=https://localhost:65006/chalZ/A0n..., token=..., type=http-01,
                status=pending, issuerDomainNames=null, validated=null, error=null)
            AcmeResources.Challenge(url=https://localhost:65006/chalZ/lXl..., token=..., type=dns-account-01,
                status=pending, issuerDomainNames=null, validated=null, error=null)
            AcmeResources.Challenge(url=https://localhost:65006/chalZ/1Dw..., token=..., type=tls-alpn-01,
                status=pending, issuerDomainNames=null, validated=null, error=null)
            AcmeResources.Challenge(url=https://localhost:65006/chalZ/IPH..., token=..., type=dns-01,
                status=pending, issuerDomainNames=null, validated=null, error=null)
            AcmeResources.Challenge(url=https://localhost:65006/chalZ/kUd, token=null, type=dns-persist-01,
                status=pending, issuerDomainNames=[pebble.letsencrypt.org], validated=null, error=null)
             */
        }
    }
}
