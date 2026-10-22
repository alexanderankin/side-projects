package side.scratch.ssl.security;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.nginx.NginxContainer;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SecureSslClientITest {
    static TestContext testContext;
    SecureSslClient secureSslClient;

    @BeforeAll
    static void setUpAll() {
        testContext = new TestContext();
    }

    @BeforeEach
    void setUp() {
        secureSslClient = new SecureSslClient(testContext.dnsResolver);
    }

    @Test
    void testMakeGoodRequest() {
        assertDoesNotThrow(() -> {
            assertEquals(200, secureSslClient.makeRequest(URI.create("https://ankin.info")));
        });
    }

    @Test
    void testMakeBadRequest() {
        assertThrows(Exception.class, () -> {
            var code = secureSslClient.makeRequest(testContext.getMitmProxyBaseUri());
            log.error("able to fetch bad uri, got back code: {}", code);
        });
    }

    @SneakyThrows
    @Test
    void testMakeBadRequestWorkAnyway() {
        var restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory(
                HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDnsResolver(testContext.dnsResolver)
                        .setTlsSocketStrategy(new DefaultClientTlsStrategy(
                            SSLContextBuilder.create()
                                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                                .build(),
                            HostnameVerificationPolicy.BUILTIN,
                            null
                        ))
                        .build())
                    .build()
            ))
            .build();

        assertDoesNotThrow(() -> {
            var code = secureSslClient.makeRequest(testContext.getMitmProxyBaseUri(), restClient);
            log.error("able to fetch bad uri anyway, got back code: {}", code);
        });
    }

    @Getter
    static class TestContext {
        private static final Logger nginxLog = LoggerFactory.getLogger(TestContext.class.getName() + ".nginx");
        private static final Logger mitmLog = LoggerFactory.getLogger(TestContext.class.getName() + ".mitm");

        final Network network;
        final NginxContainer nginxContainer;
        final GenericContainer<?> genericContainer;
        final URI mitmProxyBaseUri;
        final DnsResolver dnsResolver;

        @SuppressWarnings("resource")
        TestContext() {
            network = Network.newNetwork();
            nginxContainer = new NginxContainer("nginx:alpine-slim")
                .withNetwork(network)
                .withNetworkAliases("nginx")
                .withLogConsumer(new Slf4jLogConsumer(nginxLog));
            genericContainer = new GenericContainer<>("mitmproxy/mitmproxy:12.3.0")
                .withNetwork(network)
                .withExposedPorts(8080)
                .withCreateContainerCmdModifier(c -> c.withCmd(
                    "mitmdump --emudhra --mode reverse:http://nginx:80 -p 8080".split(" ")
                ))
                .waitingFor(Wait.forHttps("/").forPort(8080).usingTls().allowInsecure())
                .withLogConsumer(new Slf4jLogConsumer(mitmLog))
                .dependsOn(nginxContainer);

            genericContainer.start();
            mitmProxyBaseUri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("nginx")
                .port(genericContainer.getMappedPort(8080))
                .build()
                .toUri();

            dnsResolver = new DnsResolver() {
                final DnsResolver systemResolver = SystemDefaultDnsResolver.INSTANCE;

                @Override
                public InetAddress[] resolve(String host) throws UnknownHostException {
                    if ("nginx".equalsIgnoreCase(host)) {
                        return new InetAddress[]{InetAddress.getLoopbackAddress()};
                    }

                    return systemResolver.resolve(host);
                }

                @Override
                public String resolveCanonicalHostname(String host) throws UnknownHostException {
                    if ("nginx".equalsIgnoreCase(host)) {
                        return host;
                    }

                    return systemResolver.resolveCanonicalHostname(host);
                }
            };
        }
    }

}
