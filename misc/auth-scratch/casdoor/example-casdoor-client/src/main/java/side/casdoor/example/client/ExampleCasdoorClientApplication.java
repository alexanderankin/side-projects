package side.casdoor.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ExampleCasdoorClientApplication {
    static void main(String[] args) {
        SpringApplication.run(ExampleCasdoorClientApplication.class, args);
    }

    @RestController
    @RequestMapping(path = "/api")
    static class ApiController {
        @GetMapping(path = "/whoami")
        String getWhoAmI() {
            return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }
    }
}
