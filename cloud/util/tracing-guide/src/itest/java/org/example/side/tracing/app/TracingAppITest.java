package org.example.side.tracing.app;

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TracingAppITest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void test_traceParentHeader() {
        List<HttpHeaders> httpHeaders = new ArrayList<>();
        DisposableServer server = HttpServer.create()
                .handle((r, s) -> Mono.just(r.requestHeaders())
                        .doOnNext(httpHeaders::add)
                        .then(Mono.from(s.sendString(Mono.just("ok")))))
                .bindNow();

        webTestClient.get().uri(u -> u.queryParam("port", server.port()).build()).exchange();
        server.dispose();

        assertThat(httpHeaders, is(not(nullValue())));
        assertThat(httpHeaders, hasSize(1));
        assertThat(httpHeaders.getFirst().get("traceParent"), is(not(nullValue())));
        var t = httpHeaders.getFirst().get("traceParent");
        var parts = t.split("-");
        assertThat(parts.length, is(4));
        assertThat(parts[0], hasLength(2));
        assertThat(parts[1], hasLength(32));
        assertThat(parts[2], hasLength(16));
        assertThat(parts[3], hasLength(2));
    }
}
