package org.cloud.logging.proxy;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@SpringBootApplication
public class LoggedApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggedApplication.class, args);
    }

    @RestController
    static class Ctrl {
        @GetMapping
        String hello() {
            return "world";
        }

        @SneakyThrows
        @RequestMapping(path = "/sleep", method = {RequestMethod.GET, RequestMethod.POST})
        Sleep sleep(@RequestParam("ms") Optional<Long> time) {
            String start = Instant.now().toString();
            Thread.sleep(time.orElse(0L));
            return new Sleep(start, Instant.now().toString());
        }

        record Sleep(String start, String end) {
        }

        @SneakyThrows
        @RequestMapping(path = "/repeat", method = {RequestMethod.GET, RequestMethod.POST})
        String repeat(
                @RequestParam(name = "text", defaultValue = "lorem ipsum ") String text,
                @RequestParam(name = "times", defaultValue = "10") int times
        ) {
            return text.repeat(times);
        }

        @SneakyThrows
        @RequestMapping(path = "/slow-repeat", method = {RequestMethod.GET, RequestMethod.POST})
        StreamingResponseBody slowRepeat(
                @RequestParam(name = "text", defaultValue = "lorem ipsum ") String text,
                @RequestParam(name = "times", defaultValue = "10") int times,
                @RequestParam(name = "repeat", defaultValue = "10") int repeat,
                @RequestParam(name = "delay", defaultValue = "500") int delay
        ) {
            return outputStream -> {
                var repeated = text.repeat(times);
                var writer = new PrintStream(outputStream,
                        // autoFlush
                        true,
                        StandardCharsets.UTF_8);

                int count = repeat;
                while (count-- > 0) {
                    // sleep first, simulate actually problematic server, not the one which is easy to debug
                    LoggedApplication.sleep(delay);
                    writer.println(repeated);
                }
            };
        }
    }

    @SneakyThrows
    private static void sleep(int delay) {
        Thread.sleep(delay);
    }
}
