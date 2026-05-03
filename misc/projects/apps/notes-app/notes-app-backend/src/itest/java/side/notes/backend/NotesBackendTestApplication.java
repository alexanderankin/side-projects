package side.notes.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootApplication
class NotesBackendTestApplication {
    static void main(String[] args) {
        SpringApplication.run(NotesBackendTestApplication.class, args);
    }

    @TestConfiguration
    static class TestcontainersConfiguration {
        @Bean
        @ServiceConnection
        JdbcDatabaseContainer<?> dbContainer() {
            if (2 > 1)
                return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
            return new MySQLContainer<>(DockerImageName.parse("mysql:8.4-oracle"));
        }
    }
}
