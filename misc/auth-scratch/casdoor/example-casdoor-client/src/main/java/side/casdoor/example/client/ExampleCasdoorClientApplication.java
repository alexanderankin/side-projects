package side.casdoor.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

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

    @Configuration
    static class InitializeDbConfiguration {
        @Bean
        public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("org/springframework/session/jdbc/schema-h2.sql"));
            populator.setContinueOnError(true); // for non-production code

            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(populator);
            initializer.setEnabled(true);
            return initializer;
        }
    }
}
