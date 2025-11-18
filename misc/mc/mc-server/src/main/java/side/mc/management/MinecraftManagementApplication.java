package side.mc.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
class MinecraftManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinecraftManagementApplication.class, args);
    }

    @RestController
    static class ApiController {
    }
}
