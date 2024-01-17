package jdba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class JDBAApplication {
    public static void main(String[] args) {
        SpringApplication.run(JDBAApplication.class, args);
    }

    @RestController
    @RequestMapping("/api")
    static class ApiController {

    }

    static class Configuration {

    }
}
