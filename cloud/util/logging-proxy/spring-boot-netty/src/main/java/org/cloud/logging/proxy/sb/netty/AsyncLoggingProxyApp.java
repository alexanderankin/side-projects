package org.cloud.logging.proxy.sb.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.web.filter.reactive.ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE;

@SpringBootApplication
class AsyncLoggingProxyApp {
    public static void main(String[] args) {
        // silence non request logging for prod by LOGGING_LEVEL_ROOT=OFF
        // System.setProperty("logging.level.ROOT", "OFF");
        // System.setProperty("spring.main.banner-mode", "OFF");
        // todo configure allowing to change log levels externally

        // test config: test app + default port not to conflict with the test app
        System.setProperty("server.port", "8082");
        System.setProperty("server.netty.connection-timeout", "0");
        System.setProperty("logged-routes.default-route.base-url", "http://localhost:8080/");

        SpringApplication.run(AsyncLoggingProxyApp.class, args);
    }

    @Data
    @Accessors(chain = true)
    @Validated
    @Component
    @ConfigurationProperties("logged-routes")
    static class LoggedRoutes {
        /**
         * just take all requests and proxy them to here by default
         */
        @NotNull
        @Valid
        Route defaultRoute;

        // Map<String, Route> routes = new HashMap<>();

        @Data
        @Accessors(chain = true)
        static class Route {
            @NotNull
            String baseUrl;
            // boolean stripPrefix;
        }
    }

    @Configuration
    static class Config {
        @Bean
        WebClient webClient(WebClient.Builder builder) {
            return builder.build();
        }
    }

    @Component
    @ConditionalOnMissingBean(RequestLogger.class)
    static class RequestLogger {
        void logRequest(String request) {
            System.out.println(request);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    @Component
    static class Filter implements WebFilter {
        final LoggedRoutes loggedRoutes;
        final WebClient webClient;
        final ObjectMapper objectMapper;
        final RequestLogger requestLogger;

        @SuppressWarnings("deprecation") // spring boot people are just wrong about .exchange
        @Override
        @NonNull
        public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                                 @NonNull WebFilterChain ignored) {

            var target = UriComponentsBuilder.fromHttpUrl(loggedRoutes.getDefaultRoute().getBaseUrl())
                    .path(exchange.getRequest().getURI().getPath())
                    .query(exchange.getRequest().getURI().getQuery())
                    .toUriString();

            var originalBody = exchange.getRequest().getBody();
            var bufferedBodyData = new Buffer();
            var bufferedBody = originalBody.doOnNext(bufferedBodyData::add);

            var bufferedResponse = new Buffer();

            ContextView context = Context.of(
                    EXCHANGE_CONTEXT_ATTRIBUTE, exchange,
                    "bufferedBodyData", bufferedBodyData,
                    "bufferedResponse", bufferedResponse,
                    "target", target
            );


            return webClient.method(exchange.getRequest().getMethod())
                    .uri(target)
                    .body(bufferedBody, DataBuffer.class)
                    .headers(h -> h.addAll(exchange.getRequest().getHeaders()))
                    .exchange()
                    .flatMap(clientResponse ->
                            this.handleUpstreamResponse(clientResponse,
                                    bufferedResponse.wrap(clientResponse.body(BodyExtractors.toDataBuffers()))))
                    .then(log()/* .contextWrite(context) */)
                    .contextWrite(context)
                    .then();

        }

        Mono<Void> log() {
            return Mono.deferContextual(Mono::just).doOnNext(contextView -> {
                ServerWebExchange exchange = contextView.get(EXCHANGE_CONTEXT_ATTRIBUTE);
                var target = contextView.get("target");
                Buffer bufferedBodyData = contextView.get("bufferedBodyData");
                Buffer bufferedResponse = contextView.get("bufferedResponse");
                var r = exchange.getResponse();
                var httpHeaders = exchange.getRequest().getHeaders();

                var map = new LinkedHashMap<>();
                map.put("client",
                        Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                                .map(InetSocketAddress::toString)
                                .orElse(null));
                map.put("method", exchange.getRequest().getMethod().name());
                // slight difference, this one is nullable
                if (r.getStatusCode() != null) map.put("status", r.getStatusCode().value());
                map.put("url", exchange.getRequest().getURI().toString());
                map.put("url_target", target);
                map.put("request_body", maybeToString(bufferedBodyData.toByteArray()));
                map.put("request_headers", filterHeaders(httpHeaders.toSingleValueMap()));
                map.put("response_body", maybeToString(bufferedResponse.toByteArray()));
                map.put("response_headers", filterHeaders(r.getHeaders().toSingleValueMap()));
                requestLogger.logRequest(writeValueAsString(map));
            }).then();
        }

        @SneakyThrows
        public String writeValueAsString(Object value) {
            return objectMapper.writeValueAsString(value);
        }

        private Mono<Void> handleUpstreamResponse(ClientResponse clientResponse, Flux<DataBuffer> body) {
            Flux<DataBuffer> actualBody = body != null ? body : clientResponse.body(BodyExtractors.toDataBuffers());

            return Mono.deferContextual(Mono::just)
                    .flatMap(contextView -> {
                        ServerWebExchange exchange = contextView.get(EXCHANGE_CONTEXT_ATTRIBUTE);
                        var response = exchange.getResponse();

                        response.setStatusCode(clientResponse.statusCode());
                        response.getHeaders().addAll(clientResponse.headers().asHttpHeaders());
                        return response.writeWith(actualBody);
                    })
                    .then(clientResponse.releaseBody().onErrorComplete())
                    .onErrorResume(e -> clientResponse.releaseBody().onErrorComplete())
                    .then();
        }

        //<editor-fold desc="tried and true from previous attempt ('spring-boot')">
        Map<String, String> filterHeaders(Map<String, String> input) {
            // optimize a datastructure for linear reading and writing
            List<String> toRemove = new ArrayList<>(input.size());
            for (String key : input.keySet()) {
                String lower = key.toLowerCase();
                if (lower.contains("auth") || lower.contains("key")) {
                    toRemove.add(key);
                }
            }

            try {
                toRemove.forEach(input::remove);
                return input;
            } catch (UnsupportedOperationException ignored) {
                // well, we tried to play nice, now we create more garbage
                var hashMap = new HashMap<String, String>((int) Math.ceil(input.size() / 0.75));
                input.forEach((k, v) -> {
                    // this is okay because we expect at most 1 or 2 headers
                    if (toRemove.contains(k)) return;
                    hashMap.put(k, v);
                });
                return hashMap;
            }
        }

        private Object maybeToString(byte[] responseBodyCopy) {
            try {
                return new String(responseBodyCopy, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return (responseBodyCopy);
            }
        }
        //</editor-fold>

        /**
         * this class is, to reactive, what ByteArrayOutputStream is to mvc
         */
        @Data
        static class Buffer {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            @SuppressWarnings("resource")
            @SneakyThrows
            public void add(DataBuffer dataBuffer) {
                var before = dataBuffer.readPosition();
                var all = dataBuffer.asInputStream().readAllBytes();
                byteArrayOutputStream.write(all);
                dataBuffer.readPosition(before);
            }

            public byte[] toByteArray() {
                return byteArrayOutputStream.toByteArray();
            }

            public Flux<DataBuffer> wrap(Flux<DataBuffer> body) {
                return body.doOnNext(this::add);
            }
        }
    }
}
