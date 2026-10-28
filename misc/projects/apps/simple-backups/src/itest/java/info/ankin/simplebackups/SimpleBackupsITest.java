package info.ankin.simplebackups;

import io.floci.testcontainers.FlociContainer;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

public abstract class SimpleBackupsITest {
    protected static TestcontainersConfig testcontainersConfig;

    @BeforeAll
    static void beforeAll() {
        testcontainersConfig = new TestcontainersConfig();
    }

    @Getter
    static protected class TestcontainersConfig {
        GenericContainer<?> sftpContainer;
        FlociContainer flociContainer;

        @SuppressWarnings("resource")
        TestcontainersConfig() {
            sftpContainer = new GenericContainer<>(DockerImageName.parse("atmoz/sftp"))
                    .withExposedPorts(22)
                    .waitingFor(Wait.forListeningPorts(22))
                    .withCommand("%s:%s:::upload".formatted("user", "pass"));
            sftpContainer.start();

            flociContainer = new FlociContainer();
            flociContainer.start();
        }

        public URI getSftpContainerUri() {
            return UriComponentsBuilder.newInstance()
                    .scheme("sftp")
                    .host(sftpContainer.getHost())
                    .port(sftpContainer.getMappedPort(22))
                    .build()
                    .toUri();
        }

        public Credentials getSftpContainerCredentials() {
            return new Credentials("user", "pass");
        }

        public record Credentials(String username, String password) {
        }
    }
}
