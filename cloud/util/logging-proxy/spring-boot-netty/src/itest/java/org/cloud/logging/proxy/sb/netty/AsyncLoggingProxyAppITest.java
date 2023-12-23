package org.cloud.logging.proxy.sb.netty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    static String baseUrl;
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BufferringRequestLogger requestLogger;

    @DynamicPropertySource
    static void loggedApplicationProperties(DynamicPropertyRegistry registry) {
        loggedAppContainer.start();
        baseUrl = "http://localhost:" + loggedAppContainer.getMappedPort(8080);
        registry.add("logged-routes.default-route.base-url", () -> baseUrl);
    }

    Map<String, Object> remove() {
        return Optional.ofNullable(requestLogger.queue.remove()).map(this::parseMap).orElse(null);
    }

    @SneakyThrows
    Map<String, Object> parseMap(String input) {
        return objectMapper.readValue(input, MAP_TYPE_REFERENCE);
    }

    @Test
    void test_home() {
        webTestClient.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("world")
                .returnResult();
        var logged = remove();
        assertThat(logged.get("client"), is(nullValue()));
        assertThat("client is null during test", logged, hasEntry("client", null));
        assertThat(logged, hasEntry("method", "GET"));
        assertThat(logged, hasEntry("status", 200));
        assertThat(logged, hasEntry("url", "/"));
        assertThat(logged, hasKey("url_target"));
        assertThat((String) logged.get("url_target"), startsWith(baseUrl));
        assertThat((String) logged.get("url_target"), is(baseUrl + "/"));
        assertThat(logged, hasEntry("request_body", ""));
        assertThat("nothing to really assert here in test",
                logged, hasKey("request_headers"));
        assertThat(logged, hasEntry("response_body", "world"));
        assertThat(logged, hasKey("response_headers"));
        assertThat(((Map<?, ?>) logged.get("response_headers")).keySet().stream()
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .toList(),
                containsInAnyOrder(
                        "content-type",
                        "content-length",
                        "date"
                ));
    }

    @Test
    void test_sleep() {
        webTestClient.get().uri("/sleep").exchange()
                .expectStatus().isOk()
                .expectBody(Sleep.class).value(s -> assertThat(s.isPopulated(), is(true)))
                .returnResult();
        var logged = remove();
        assertThat(logged.get("client"), is(nullValue()));
        assertThat("client is null during test", logged, hasEntry("client", null));
        assertThat(logged, hasEntry("method", "GET"));
        assertThat(logged, hasEntry("status", 200));
        assertThat(logged, hasEntry("url", "/sleep"));
        assertThat(logged, hasKey("url_target"));
        assertThat((String) logged.get("url_target"), startsWith(baseUrl));
        assertThat((String) logged.get("url_target"), is(baseUrl + "/sleep"));
        assertThat(logged, hasEntry("request_body", ""));
        assertThat("nothing to really assert here in test",
                logged, hasKey("request_headers"));
        assertThat(logged, hasKey("response_body"));
        assertThat(logged.get("response_body"), is(not(nullValue())));
        assertThat(logged.get("response_body").toString(), matchesRegex("\\{\"start\":\"[^\"]+\",\"end\":\"[^\"]+\"}"));
        assertThat(logged, hasKey("response_headers"));
        assertThat(((Map<?, ?>) logged.get("response_headers")).keySet().stream()
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .toList(),
                containsInAnyOrder(
                        "content-type",
                        "transfer-encoding",
                        "date"
                ));
    }

    @Test
    void test_repeat() {
        long l0 = System.nanoTime();

        webTestClient.get().uri("/slow-repeat?delay=100&repeat=10").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();

        assertThat("response should have taken about a second",
                Duration.ofNanos(System.nanoTime() - l0).toMillis() / 1000.0,
                Matchers.closeTo(1.0, 0.5 /* generous margin */));
        var logged = remove();
        assertThat(logged.get("client"), is(nullValue()));
        assertThat("client is null during test", logged, hasEntry("client", null));
        assertThat(logged, hasEntry("method", "GET"));
        assertThat(logged, hasEntry("status", 200));
        assertThat(logged, hasEntry("url", "/slow-repeat?delay=100&repeat=10"));
        assertThat(logged, hasKey("url_target"));
        assertThat((String) logged.get("url_target"), startsWith(baseUrl));
        assertThat((String) logged.get("url_target"), is(baseUrl + "/slow-repeat?delay=100&repeat=10"));
        assertThat(logged, hasEntry("request_body", ""));
        assertThat("nothing to really assert here in test",
                logged, hasKey("request_headers"));
        assertThat(logged, hasKey("response_body"));
        assertThat(logged.get("response_body"), is(not(nullValue())));
        assertThat(logged.get("response_body").toString(), matchesRegex("^(lorem ipsum ){10}\n$"));
        assertThat(logged, hasKey("response_headers"));
        assertThat(((Map<?, ?>) logged.get("response_headers")).keySet().stream()
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .toList(),
                containsInAnyOrder(
                        "transfer-encoding",
                        "date"
                ));
    }

    @Test
    void test_repeatWithPost() {
        long l0 = System.nanoTime();

        webTestClient.post().uri("/slow-repeat")
                .body(BodyInserters.fromFormData("delay", "100").with("repeat", "10"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();

        assertThat("response should have taken about a second",
                Duration.ofNanos(System.nanoTime() - l0).toMillis() / 1000.0,
                Matchers.closeTo(1.0, 0.5 /* generous margin */));
        var logged = remove();
        assertThat(logged.get("client"), is(nullValue()));
        assertThat("client is null during test", logged, hasEntry("client", null));
        assertThat(logged, hasEntry("method", "POST"));
        assertThat(logged, hasEntry("status", 200));
        assertThat(logged, hasEntry("url", "/slow-repeat"));
        assertThat(logged, hasKey("url_target"));
        assertThat((String) logged.get("url_target"), startsWith(baseUrl));
        assertThat((String) logged.get("url_target"), is(baseUrl + "/slow-repeat"));
        assertThat(logged, hasEntry("request_body", "delay=100&repeat=10"));
        assertThat("nothing to really assert here in test",
                logged, hasKey("request_headers"));
        assertThat(logged, hasKey("response_body"));
        assertThat(logged.get("response_body"), is(not(nullValue())));
        assertThat(logged.get("response_body").toString(), matchesRegex("^(lorem ipsum ){10}\n$"));
        assertThat(logged, hasKey("response_headers"));
        assertThat(((Map<?, ?>) logged.get("response_headers")).keySet().stream()
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .toList(),
                containsInAnyOrder(
                        "transfer-encoding",
                        "date"
                ));
    }

    record Sleep(String start, String end) {
        boolean isPopulated() {
            return start != null && end != null;
        }
    }
}
