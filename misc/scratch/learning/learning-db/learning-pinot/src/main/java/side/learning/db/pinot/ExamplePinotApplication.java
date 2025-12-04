package side.learning.db.pinot;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.simple.JdbcClient;
import side.learning.db.pinot.springdialect.JdbcPinotDialect;

@SpringBootApplication
class ExamplePinotApplication {
    static void main(String[] args) {
        SpringApplication.run(ExamplePinotApplication.class, args);
    }

    @Bean
    Dialect jdbcDialect() {
        return JdbcPinotDialect.INSTANCE;
    }

    @Bean
    ApplicationRunner applicationRunner(JdbcClient jdbcClient) {
        return args -> {
            jdbcClient.sql("select 2 + 2").query().listOfRows().forEach(System.out::println);
        };
    }
}
