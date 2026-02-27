package org.example.side.tracing.app;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@SpringBootApplication
public class TracingApp {
    public static void main(String[] args) {
        SpringApplication.run(TracingApp.class, args);
    }

    @Slf4j
    @RestController
    @RequiredArgsConstructor
    static class Ctrl {
        final WebClient.Builder builder;
        WebClient webclient;

        @PostConstruct
        void init() {
            webclient = builder.build();
        }

        @GetMapping
        Mono<?> hello(@RequestParam(name = "port") Optional<Integer> port) {
            log.info("hello from hello endpoint");
            return webclient.get().uri("http://localhost:" + port.orElse(8081)).retrieve().toEntity(String.class);
        }
    }
}
