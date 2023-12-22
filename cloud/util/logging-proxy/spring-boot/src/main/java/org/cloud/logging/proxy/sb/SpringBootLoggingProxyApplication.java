package org.cloud.logging.proxy.sb;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
class SpringBootLoggingProxyApplication {
    @SneakyThrows
    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        System.setProperty("logged-routes.routes.test.base-url", "http://localhost:8080/");
        SpringApplication.run(SpringBootLoggingProxyApplication.class, args);
        // uses com.fasterxml.jackson.core.Base64Variants#MIME_NO_LINEFEEDS
        // System.out.println(new ObjectMapper().writeValueAsString("abc".getBytes()));
    }

    @Data
    @Accessors(chain = true)
    @Component
    @ConfigurationProperties("logged-routes")
    static class LoggedRoutes {
        // todo implement this
        /*
            WHERE I LEFT OFF
         */
        boolean stripPrefix = true;
        Map<String, Route> routes = new HashMap<>();

        @Data
        @Accessors(chain = true)
        static class Route {
            String baseUrl;
            boolean stripPrefix;
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
        static final ResponseEntity<StreamingResponseBody> NOT_FOUND = ResponseEntity.notFound().build();
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
        @RequestMapping(path = "/**", method = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
        ResponseEntity<StreamingResponseBody> proxy(HttpMethod method,
                                                    HttpServletRequest servletRequest,
                                                    @RequestBody(required = false) InputStream requestBody,
                                                    @RequestHeader HttpHeaders httpHeaders) {
            var requestBodyCopy = new ByteArrayOutputStream();
            if (requestBody != null) {
                requestBody = new TeeInputStream(requestBody, requestBodyCopy);
            } else {
                requestBody = NullInputStream.INSTANCE;
            }

            var url = servletRequest.getRequestURI();
            if (url.charAt(0) == '/')
                url = url.substring(1);
            var split = url.split("/");
            if (split.length == 0) return NOT_FOUND;
            var prefix = split[0];
            var baseUrl = loggedRoutes.getRoutes().get(prefix);
            if (baseUrl == null) return NOT_FOUND;

            var target = UriComponentsBuilder.fromHttpUrl(baseUrl.getBaseUrl())
                    .path(url)
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
            map.put("method", method.name());
            map.put("status", r.getStatusCode().value());
            map.put("url", url);
            map.put("url_target", target);
            map.put("request_body", maybeToString(requestBodyCopy.toByteArray()));
            map.put("request_headers", httpHeaders.toSingleValueMap());
            map.put("response_headers", r.getHeaders().toSingleValueMap());

            return ResponseEntity.status(r.getStatusCode())
                    .headers(r.getHeaders())
                    .body(new MyStreamingResponseBody(responseBody,
                            responseBodyCopy,
                            (responseBA) -> {
                                map.put("response_body", maybeToString(responseBodyCopy.toByteArray()));
                                System.out.println(objectMapper.writeValueAsString(map));
                            }));
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
            private final ThrowingConsumer<byte[]> callback;

            @Override
            public void writeTo(@NonNull OutputStream out) throws IOException {
                responseBody.transferTo(out);
                callback.accept(responseBodyCopy.toByteArray());
            }
        }
    }
}
