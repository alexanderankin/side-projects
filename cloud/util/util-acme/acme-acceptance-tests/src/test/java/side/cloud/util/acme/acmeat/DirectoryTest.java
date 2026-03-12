package side.cloud.util.acme.acmeat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import side.cloud.util.acme.lib.AcmeClient;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static side.cloud.util.acme.lib.model.AcmeIdentifier.AcmeIdentifierType.dns;

@Slf4j
class DirectoryTest extends BaseAcmeAcceptanceTest {
    @Test
    void test_baseUrlIsDirectoryEndpoint() {
        var acmeClient = AcmeClient.create(new AcmeClient.Config(
                exampleKey,
                URI.create(acmeServerBaseUrl)
        ).setUserAgent("UserAgent"));
        acmeClient.getAcmeClientService().setRestClient(modifyToTrustAll(acmeClient.getAcmeClientService().getRestClient()));
        var directory = acmeClient.acmeDirectory();
        log.info("directory: {}", directory);
        assertThat(directory, is(notNullValue()));
    }

    /**
     * <p>from <a href=https://datatracker.ietf.org/doc/html/rfc8555#section-7.1>rfc8555 7.1</a>:</p>
     * <pre>
     *     +-------------------+--------------------------------+--------------+
     *     | Action            | Request                        | Response     |
     *     +-------------------+--------------------------------+--------------+
     *     | Get directory     | GET  directory                 | 200          |
     *     |                   |                                |              |
     *     | Get nonce         | HEAD newNonce                  | 200          |
     *     |                   |                                |              |
     *     | Create account    | POST newAccount                | 201 ->       |
     *     |                   |                                | account      |
     *     |                   |                                |              |
     *     | Submit order      | POST newOrder                  | 201 -> order |
     *     |                   |                                |              |
     *     | Fetch challenges  | POST-as-GET order's            | 200          |
     *     |                   | authorization urls             |              |
     *     |                   |                                |              |
     *     | Respond to        | POST authorization challenge   | 200          |
     *     | challenges        | urls                           |              |
     *     |                   |                                |              |
     *     | Poll for status   | POST-as-GET order              | 200          |
     *     |                   |                                |              |
     *     | Finalize order    | POST order's finalize url      | 200          |
     *     |                   |                                |              |
     *     | Poll for status   | POST-as-GET order              | 200          |
     *     |                   |                                |              |
     *     | Download          | POST-as-GET order's            | 200          |
     *     | certificate       | certificate url                |              |
     *     +-------------------+--------------------------------+--------------+
     * </pre>
     */
    @Test
    void test_happyPath() {
        var acmeClient = AcmeClient.create(new AcmeClient.Config(
                exampleKey,
                URI.create(acmeServerBaseUrl)
        )
                .setUserAgent("UserAgent"));
        acmeClient.getAcmeClientService().setRestClient(modifyToTrustAll(acmeClient.getAcmeClientService().getRestClient()));
        var account = acmeClient.newAccount(new AcmeResources.NewAccount()
                .setContact(List.of(URI.create("mailto:example@localhost.local")))
                .setTermsOfServiceAgreed(
                        Optional.of(acmeClient.acmeDirectory())
                                .map(AcmeResources.Directory::getMeta)
                                .map(AcmeResources.Directory.Meta::getTermsOfService).isPresent()));
        var order = acmeClient.newOrder(new AcmeResources.NewOrder()
                .setIdentifiers(List.of(new AcmeIdentifier().setType(dns).setValue("https://example.com")))
                .setNotBefore(Instant.now())
                .setNotAfter(Instant.now().plus(Duration.ofDays(8))));
        // var order = acmeClient
    }

    /*
    @Test
    void test_newNonce() {
    }

    @Test
    void test_newOrder() {
    }

    @Test
    void test_renewalInfo() {
    }

    @Test
    void test_revokeCert() {
    }

    @Test
    void test_keyChange() {
    }
    */
}
