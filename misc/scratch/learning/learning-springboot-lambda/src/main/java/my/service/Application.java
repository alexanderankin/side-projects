package my.service;

import my.service.controller.PingController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
// We use direct @Import instead of @ComponentScan to speed up cold starts
// @ComponentScan(basePackages = "my.service.controller")
@Import({PingController.class})
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
