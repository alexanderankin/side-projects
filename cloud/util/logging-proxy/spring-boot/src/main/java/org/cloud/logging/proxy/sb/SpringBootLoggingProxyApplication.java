package org.cloud.logging.proxy.sb;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootApplication
class SpringBootLoggingProxyApplication {
    @SneakyThrows
    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        System.setProperty("logged-routes.default-route.base-url", "http://localhost:8080/");
        SpringApplication.run(SpringBootLoggingProxyApplication.class, args);
        // uses com.fasterxml.jackson.core.Base64Variants#MIME_NO_LINEFEEDS
        // System.out.println(new ObjectMapper().writeValueAsString("abc".getBytes()));
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

        /**
         * 10kb default by default
         */
        int bufferSize = 10_000;

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
        RestClient restClient(RestClient.Builder builder) {
            return builder.build();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @RestController
    static class Ctrl {
        // static final ResponseEntity<StreamingResponseBody> NOT_FOUND = ResponseEntity.notFound().build();
        final RestClient restClient;
        final LoggedRoutes loggedRoutes;
        final ObjectMapper objectMapper;

        @GetMapping("/_admin")
        String admin() {
            return "hello";
        }

        @PostConstruct
        void init() {
            log.atInfo().setMessage("found routes: {}").addArgument(loggedRoutes).addKeyValue("routes", loggedRoutes).log();
        }

        @SneakyThrows
        @RequestMapping("/**")
        ResponseEntity<StreamingResponseBody> proxy(HttpMethod method,
                                                    HttpServletRequest servletRequest,
                                                    @Nullable InputStream requestBody,
                                                    @RequestHeader HttpHeaders httpHeaders) {
            var requestBodyCopy = new ByteArrayOutputStream();
            if (requestBody != null) {
                requestBody = new TeeInputStream(requestBody, requestBodyCopy);
            } else {
                requestBody = NullInputStream.INSTANCE;
            }

            var reqUrl = servletRequest.getRequestURI();
            var baseUrl = loggedRoutes.defaultRoute.getBaseUrl();
            var target = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path(reqUrl)
                    .query(servletRequest.getQueryString())
                    .toUriString();

            @SuppressWarnings("resource")
            var r = restClient.method(method)
                    .uri(target)
                    .headers(h -> h.addAll(httpHeaders))
                    .body(requestBody::transferTo)
                    .exchange((request, response) -> response, false);

            var responseBody = r.getBody();
            var responseBodyCopy = new ByteArrayOutputStream();
            responseBody = new TeeInputStream(responseBody, responseBodyCopy);


            var map = new LinkedHashMap<>();
            map.put("client", servletRequest.getRemoteAddr());
            map.put("method", method.name());
            map.put("status", r.getStatusCode().value());
            map.put("url", reqUrl);
            map.put("url_target", target);
            map.put("request_body", maybeToString(requestBodyCopy.toByteArray()));
            map.put("request_headers", filterHeaders(httpHeaders.toSingleValueMap()));
            map.put("response_headers", filterHeaders(r.getHeaders().toSingleValueMap()));

            return ResponseEntity.status(r.getStatusCode())
                    .headers(r.getHeaders())
                    .body(new MyStreamingResponseBody(responseBody,
                            responseBodyCopy,
                            loggedRoutes.getBufferSize(),
                            (responseBA) -> {
                                map.put("response_body", maybeToString(responseBodyCopy.toByteArray()));
                                System.out.println(objectMapper.writeValueAsString(map));
                            }));
        }

        Map<String, String> filterHeaders(Map<String, String> input) {
            List<String> toRemove = new ArrayList<>(input.size());
            for (String key : input.keySet()) {
                String lower = key.toLowerCase();
                if (lower.contains("auth") || lower.contains("key")) {
                    toRemove.add(key);
                }
            }
            toRemove.forEach(input::remove);
            return input;
        }

        private Object maybeToString(byte[] responseBodyCopy) {
            try {
                return new String(responseBodyCopy, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return (responseBodyCopy);
            }
        }

        @RequiredArgsConstructor
        private static class MyStreamingResponseBody implements StreamingResponseBody {
            private final InputStream responseBody;
            private final ByteArrayOutputStream responseBodyCopy;
            private final int bufferSize;
            private final ThrowingConsumer<byte[]> callback;

            @Override
            public void writeTo(@NonNull OutputStream out) throws IOException {
                transferTo(out);
                callback.accept(responseBodyCopy.toByteArray());
            }

            private void transferTo(OutputStream out) throws IOException {
                Objects.requireNonNull(out, "out");
                byte[] buffer = new byte[bufferSize];
                int read;
                while ((read = responseBody.read(buffer, 0, bufferSize)) >= 0) {
                    out.write(buffer, 0, read);
                    out.flush();
                }
            }
        }
    }
}
