package org.cloud.logging.proxy.sb.netty;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT24H")
@ActiveProfiles("itest")
class AsyncLoggingProxyAppITest {
    static final String LOGGED_APP_JAR = Objects.requireNonNull(System.getProperty("loggedAppJar"),
            "missing mandatory property 'loggedAppJar', needed for starting dependencies");

    @SuppressWarnings("resource")
    static final GenericContainer<?> loggedAppContainer =
            new GenericContainer<>("eclipse-temurin:21-jre-alpine")
                    .withExposedPorts(8080)
                    .withEnv("SERVER_PORT", "8080")
                    .withCopyFileToContainer(MountableFile.forHostPath(LOGGED_APP_JAR), "/app.jar")
                    .withCommand("java", "-jar", "-Xmx512m", "-Xms128m", "/app.jar")
                    .waitingFor(Wait.forHttp("/").forPort(8080));

    static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    @Autowired
    WebTestClient webTestClient;

    @DynamicPropertySource
    static void loggedApplicationProperties(DynamicPropertyRegistry registry) {
        loggedAppContainer.start();
        String baseUrl = "http://localhost:" + loggedAppContainer.getMappedPort(8080);
        registry.add("logged-routes.default-route.base-url", () -> baseUrl);
    }

    @Test
    void test_home() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("world")
                .returnResult();
    }

    @Test
    void test_sleep() {
        webTestClient.get().uri("/sleep").exchange()
                .expectStatus().isOk()
                .expectBody(Sleep.class).value(s -> assertThat(s.isPopulated(), is(true)))
                .returnResult();
    }

    @Test
    void test_repeat() {
        long l0 = System.nanoTime();

        webTestClient.get().uri("/slow-repeat?delay=100&repeat=10").exchange().expectBody(String.class).value(System.out::println);

        assertThat("response should have taken about a second",
                Duration.ofNanos(System.nanoTime() - l0).toMillis() / 1000.0,
                Matchers.closeTo(1.0, 0.5 /* generous margin */));
    }

    record Sleep(String start, String end) {
        boolean isPopulated() {
            return start != null && end != null;
        }
    }
}
