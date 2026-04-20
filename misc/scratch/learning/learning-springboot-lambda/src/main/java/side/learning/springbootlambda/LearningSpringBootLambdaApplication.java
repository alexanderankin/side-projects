package side.learning.springbootlambda;

import side.learning.springbootlambda.controller.PingController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
// We use direct @Import instead of @ComponentScan to speed up cold starts
// @ComponentScan(basePackages = "side.learning.springbootlambda.controller")
@Import({PingController.class})
public class LearningSpringBootLambdaApplication {

    static void main(String[] args) {
        SpringApplication.run(LearningSpringBootLambdaApplication.class, args);
    }
}
