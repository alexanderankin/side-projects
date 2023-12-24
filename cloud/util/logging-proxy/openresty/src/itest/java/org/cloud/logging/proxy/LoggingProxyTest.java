package org.cloud.logging.proxy;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
                    .withLogConsumer(BufferingConsumer.PROXY_LOG_CONSUMER)
            //
            ;

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

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

    @SneakyThrows
    Map<String, Object> remove() {
        return Optional.of(BufferingConsumer.PROXY_LOG_CONSUMER.output.take()).map(this::parseMap).orElse(null);
    }

    @SneakyThrows
    Map<String, Object> parseMap(String input) {
        return OBJECT_MAPPER.readValue(input, MAP_TYPE_REFERENCE);
    }

    @BeforeEach
    void setUpEach() {
        proxyBaseUrl = "http://localhost:" + proxy.getMappedPort(80);
        webTestClient = WebTestClient.bindToServer().baseUrl(proxyBaseUrl).build();
    }

    /*
        example output:

        {
          "req": {
            "headers": {
              "accept-encoding": "gzip",
              "user-agent": "ReactorNetty/1.1.13",
              "host": "localhost:32909",
              "accept": "*\/*",
              "webtestclient-request-id": 1
            },
            "body": false,
            "method": "GET",
            "uri": "/"
          },
          "resp": {
            "headers": {
              "connection": "close",
              "content-type": "text/plain;charset: UTF-8",
              "content-length": 5
            },
            "status": 200,
            "body": "world"
          },
          "remote_addr": "172.31.0.1",
          "timing": {
            "time_iso8601": "2023-12-24T21:33:57+00:00",
            "time_millis": 1703453637113,
            "response_millis": 4
          }
        }

     */
    @Test
    @Timeout(10)
    void test() {
        EntityExchangeResult<String> stringEntityExchangeResult = webTestClient.get().uri("/").exchange().expectBody(String.class)
                .isEqualTo("world")
                .returnResult();

        System.out.println(stringEntityExchangeResult);
        var logged = remove();
        System.out.println(logged);
        assertThat(logged.get("remote_addr"), is(not(nullValue())));
        // assertThat("client is null during test", logged, hasEntry("client", null));

        assertThat(logged, allOf(hasKey("req"), hasKey("resp"), hasKey("timing")));
        var req = logged.get("req");
        var resp = logged.get("resp");
        var timing = logged.get("timing");
        assertThat(Arrays.asList(req, resp, timing), everyItem(is(not(nullValue()))));
        assertThat(Arrays.asList(req, resp, timing), everyItem(is(instanceOf(Map.class))));

        @SuppressWarnings("unchecked")
        var reqMap = (Map<String, Object>) req;
        @SuppressWarnings("unchecked")
        var respMap = (Map<String, Object>) resp;
        @SuppressWarnings("unchecked")
        var timingMap = (Map<String, Object>) timing;

        assertThat(reqMap, hasKey("headers"));
        assertThat(reqMap.get("headers"), is(notNullValue()));
        assertThat(reqMap.get("body"), is(Boolean.FALSE));
        assertThat(reqMap.get("method"), is("GET"));
        assertThat(reqMap.get("uri"), is("/"));

        assertThat(respMap, hasKey("headers"));
        assertThat(respMap.get("headers"), is(instanceOf(Map.class)));
        assertThat((Map<?, ?>) respMap.get("headers"), allOf(
                hasKey("connection"),
                hasKey("content-type"),
                hasKey("content-length")
        ));
        // in this case it is known
        assertThat(respMap.get("headers"), is(Map.ofEntries(
                Map.entry("connection", "close"),
                Map.entry("content-type", "text/plain;charset=UTF-8"),
                Map.entry("content-length", "5")
        )));
        assertThat(respMap.get("status"), is(200));
        assertThat(respMap.get("body"), is("world"));
        assertThat(timingMap, allOf(hasKey("time_iso8601"), hasKey("time_millis"), hasKey("response_millis")));
    }

    public static class BufferingConsumer implements Consumer<OutputFrame> {
        static final BufferingConsumer PROXY_LOG_CONSUMER = new BufferingConsumer();
        ArrayBlockingQueue<String> output = new ArrayBlockingQueue<>(10);

        @Override
        public void accept(OutputFrame outputFrame) {
            if (output.remainingCapacity() == 0) output.remove();
            output.add(outputFrame.getUtf8String());
        }
    }
}
