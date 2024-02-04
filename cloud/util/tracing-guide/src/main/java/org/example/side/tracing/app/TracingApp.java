package org.example.side.tracing.app;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
        System.setProperty("server.port", "8090");
        SpringApplication.run(TracingApp.class, args);
    }

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
            return webclient.get().uri("http://localhost:" + port.orElse(8081)).retrieve().toEntity(String.class);
        }
    }
}
