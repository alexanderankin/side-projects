package org.cloud.logging.proxy;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

class LoggingProxyTest {
    static final String LOGGED_APP_JAR = Objects.requireNonNull(System.getProperty("loggedAppJar"),
            "missing mandatory property 'loggedAppJar', needed for starting dependencies");

    @SuppressWarnings("resource")
    static final GenericContainer<?> loggedAppContainer =
            new GenericContainer<>("eclipse-temurin:21-jre-alpine")
                    .withNetworkAliases("logged-app")
                    .withExposedPorts(8080)
                    .withEnv("SERVER_PORT", "8080")
                    .withCopyFileToContainer(MountableFile.forHostPath(LOGGED_APP_JAR), "/app.jar")
                    .withCommand("java", "-jar", "-Xmx512m", "-Xms128m", "/app.jar")
                    .waitingFor(Wait.forHttp("/").forPort(8080));

    @SuppressWarnings("resource")
    static final GenericContainer<?> proxy =
            new GenericContainer<>(
                    new ImageFromDockerfile()
                            .withFileFromClasspath(
                                    "Dockerfile",
                                    "/logging-proxy.Dockerfile")
                            .withFileFromClasspath(
                                    "nginx.conf",
                                    "/nginx.conf")
            )
                    .withExposedPorts(80)
                    .dependsOn(loggedAppContainer)
                    .withCopyToContainer(
                            Transferable.of(readNginxConf()
                                    .replace("http://localhost:8080",
                                            "http://logged-app:8080")),
                            "/usr/local/openresty/nginx/conf/nginx.conf")
            //
            ;

    /**
     * this is the scheme, host, and port, of the proxy (e.g., {@code http://localhost:8080})
     */
    String proxyBaseUrl;

    WebTestClient webTestClient;

    @SneakyThrows
    static String readNginxConf() {
        return IOUtils.resourceToString("/nginx.conf", StandardCharsets.UTF_8);
    }

    @BeforeAll
    static void setUp() {
        Network network = Network.newNetwork();
        proxy.withNetwork(network);
        loggedAppContainer.withNetwork(network);
        proxy.start();
    }

    @BeforeEach
    void setUpEach() {
        proxyBaseUrl = "http://localhost:" + proxy.getMappedPort(80);
        webTestClient = WebTestClient.bindToServer().baseUrl(proxyBaseUrl).build();
    }

    @Test
    void test() {
        EntityExchangeResult<String> stringEntityExchangeResult = webTestClient.get().uri("/").exchange().expectBody(String.class)
                .isEqualTo("world")
                .returnResult();

        System.out.println(stringEntityExchangeResult);
    }
}
