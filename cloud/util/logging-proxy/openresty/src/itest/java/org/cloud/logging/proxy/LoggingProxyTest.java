package org.cloud.logging.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
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
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.MountableFile;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

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
    @SneakyThrows
    @Test
    @Timeout(10)
    void test() {
        EntityExchangeResult<String> stringEntityExchangeResult = webTestClient.get().uri("/").exchange().expectBody(String.class)
                .isEqualTo("world")
                .returnResult();

        System.out.println(stringEntityExchangeResult);
        var logEntry = OBJECT_MAPPER.readValue(BufferingConsumer.PROXY_LOG_CONSUMER.output.take(), LogByLuaLogEntry.class);
        System.out.println(logEntry);

        if (NetworkInterface.networkInterfaces().flatMap(NetworkInterface::inetAddresses).map(InetAddress::getHostAddress).noneMatch(Predicate.isEqual(logEntry.getClient())))
            System.err.println("warning - the client is not known to this machine");

        assertThat(logEntry, is(not(nullValue())));
        assertThat(logEntry.getClient(), is(not(emptyOrNullString())));
        assertThat(logEntry.getReq(), is(not(nullValue())));
        assertThat(logEntry.getReq().getMethod(), is("GET"));
        assertThat(logEntry.getReq().getUri(), is("/"));
        assertThat(logEntry.getReq().getHeaders(), is(not(nullValue())));
        assertThat(logEntry.getReq().getHeaders(), hasKey("host"));

        assertThat(logEntry.getResp(), is(not(nullValue())));
        assertThat(logEntry.getResp().getStatus(), is(200));
        assertThat(logEntry.getResp().getHeaders(), is(Map.ofEntries(
                Map.entry("content-length", "5"),
                Map.entry("content-type", "text/plain;charset=UTF-8"),
                Map.entry("connection", "close")
        )));
        assertThat(logEntry.getResp().getBody(), is("world"));

        assertThat(logEntry.getTiming(), is(not(nullValue())));
        assertThat(logEntry.getTiming().getIso8601(), is(not(nullValue())));
        assertThat(logEntry.getTiming().getIso8601().isAfter(Instant.now().minusSeconds(5)), is(true));
        assertThat((double) logEntry.getTiming().getCurrentTime(), is(closeTo(System.currentTimeMillis(), 5_000)));
        assertThat((double) logEntry.getTiming().getResponseMillis(), is(closeTo(0, 200)));
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

    @Data
    @Accessors(chain = true)
    static class LogByLuaLogEntry {
        @JsonProperty("remote_addr")
        String client;
        Req req;
        Resp resp;
        Timing timing;

        @Data
        @Accessors(chain = true)
        static class Req {
            String method;
            String uri;
            Map<String, Object> headers;
            String body;
        }

        @Data
        @Accessors(chain = true)
        static class Resp {
            int status;
            Map<String, Object> headers;
            String body;
        }

        @Data
        @Accessors(chain = true)
        static class Timing {
            @JsonProperty("time_iso8601")
            Instant iso8601;
            @JsonProperty("time_millis")
            long currentTime;
            @JsonProperty("response_millis")
            long responseMillis;
        }
    }
}
