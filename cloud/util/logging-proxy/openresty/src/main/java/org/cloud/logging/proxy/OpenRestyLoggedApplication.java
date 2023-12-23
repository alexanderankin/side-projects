package org.cloud.logging.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(org.cloud.logging.proxy.logged.application.LoggedApplication.Ctrl.class)
public class OpenRestyLoggedApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenRestyLoggedApplication.class, args);
    }
}
