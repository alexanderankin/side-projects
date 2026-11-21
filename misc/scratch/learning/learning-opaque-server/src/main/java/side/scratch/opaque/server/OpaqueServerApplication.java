package side.scratch.opaque.server;

import com.codeheadsystems.hofmann.springboot.controller.OpaqueController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
// Version 3.0 of the starter supplies the protocol beans but does not register its controller.
@Import(OpaqueController.class)
public class OpaqueServerApplication {

    public static void main(String[] args) {
        log.info("Starting local OPAQUE server");
        SpringApplication.run(OpaqueServerApplication.class, args);
    }
}
