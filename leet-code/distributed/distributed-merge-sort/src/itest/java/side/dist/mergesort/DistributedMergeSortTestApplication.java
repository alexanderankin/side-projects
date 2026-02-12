package side.dist.mergesort;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class DistributedMergeSortTestApplication {
    static void main(String[] args) {
        SpringApplication.from(DistributedMergeSortApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

    @TestConfiguration
    static class TestcontainersConfiguration {
        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        }
    }
}
