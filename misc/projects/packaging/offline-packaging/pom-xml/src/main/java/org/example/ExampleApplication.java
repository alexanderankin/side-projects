package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@SpringBootApplication
public class ExampleApplication {
    static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @RestController
    static class HomeController {
        @GetMapping
        String hello(@RequestParam(name = "name", required = false) String name) {
            return "hello, " + Objects.requireNonNullElse(name, "world") + "!\n";
        }
    }
}
