package org.example;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
class ExampleAlloyObservabilityApp {
    public static void main(String[] args) {
        // simplification for clarity
        new SpringApplicationBuilder(ExampleAlloyObservabilityApp.class)
                .logStartupInfo(BooleanUtils.toBoolean(System.getenv().get("DEBUG")))
                .properties("spring.main.banner-mode=off")
                .properties("logging.level.root=warn")
                .properties("logging.level.org.example=info")
                .properties("management.endpoints.web.exposure.include=health,prometheus")
                .properties("management.observations.annotations.enabled=true")
                .run(args);
        new JsonLoggingListener(null, null).environmentPrepared(null, null);
        log.info("started");
    }

    @RequiredArgsConstructor
    @Slf4j
    @RestController
    static class ExampleController {
        private final MeterRegistry meterRegistry;
        private final ExampleService exampleService;
        private final SecureRandom secureRandom = new SecureRandom();

        @Timed("custom-timer-endpoint-hello")
        @GetMapping
        String hello() {
            log.atInfo().addKeyValue("hello", "world").addKeyValue("id", UUID.randomUUID()).log("hello world");
            return "hello, world!";
        }

        @SneakyThrows
        @Timed("custom-timer-endpoint-variable-sleep")
        @GetMapping("/sleep")
        @ResponseStatus(HttpStatus.OK)
        void sleep(@RequestParam("delay") Optional<Long> delay) {
            long sleep = delay.orElse(100L);
            log.atInfo()
                    .addKeyValue("delay", delay)
                    .addKeyValue("sleep", sleep)
                    .log("sleep endpoint called with {}", delay);
            Thread.sleep(sleep);
        }

        @SneakyThrows
        @Timed("custom-timer-endpoint-random-sleep")
        @GetMapping("/random-sleep")
        @ResponseStatus(HttpStatus.OK)
        void randomSleep() {
            long sleep = secureRandom.nextLong(0, 1000);
            log.atInfo().addKeyValue("sleep", sleep).log();
            meterRegistry.timer("custom-timer-random-sleep").record(sleep, TimeUnit.MILLISECONDS);
            Thread.sleep(sleep);
        }

        @Counted("counter-total")
        @GetMapping("/counter/{name}")
        @ResponseStatus(HttpStatus.OK)
        void counter(@PathVariable("name") String name,
                     @RequestParam("increment") Optional<Integer> increment) {
            log.atInfo()
                    .addKeyValue("name", name)
                    .addKeyValue("increment", increment.orElse(1))
                    .log("counter endpoint called with {}/{}", name, increment);
            meterRegistry.counter("custom-counter-" + name).increment(increment.orElse(1));
        }

        @GetMapping("/tracing")
        void tracingExample() {
            log.info("hello parent trace");
            exampleService.hello();
            log.info("bye parent trace");
        }
    }

    @Service
    static class ExampleService {
        // must be public for new span it seems
        @NewSpan("custom-span")
        public void hello() {
            log.info("hello span trace");
            log.info("bye span trace");
        }
    }
}
