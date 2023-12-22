package org.cloud.logging.proxy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

// these two lines turn on the observed application:
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) // turn it on
@TestPropertySource(properties = "server.port=8080") // on port 8080
// now for the test
class LoggingProxyTest {
    static GenericContainer<?> container =
            new GenericContainer<>(
                    new ImageFromDockerfile()
                            .withFileFromClasspath(
                                    "Dockerfile",
                                    "/logging-proxy.Dockerfile")
            )
                    .withExposedPorts(80)
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/nginx.conf"),
                            "/usr/local/openresty/nginx/conf/nginx.conf")
            //
            ;

    /**
     * this is the scheme, host, and port, of the proxy (e.g., {@code http://localhost:8080})
     */
    String proxyBaseUrl;

    WebTestClient webTestClient;

    @BeforeAll
    static void setUp() {
        container.start();
    }

    @BeforeEach
    void setUpEach() {
        proxyBaseUrl = "http://localhost:" + container.getMappedPort(80);
        webTestClient = WebTestClient.bindToServer().baseUrl(proxyBaseUrl).build();
    }

    @Test
    void test() {
        EntityExchangeResult<String> stringEntityExchangeResult = webTestClient.get().uri("/").exchange().expectBody(String.class)
                // .isEqualTo("world")
                .returnResult();

        System.out.println(stringEntityExchangeResult);
    }
}
