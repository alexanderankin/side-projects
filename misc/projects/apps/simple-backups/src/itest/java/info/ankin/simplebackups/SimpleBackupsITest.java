package info.ankin.simplebackups;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import io.floci.testcontainers.FlociContainer;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(Resources.SYSTEM_PROPERTIES)
public abstract class SimpleBackupsITest {
    @TempDir
    protected static Path containerFiles;
    protected static TestcontainersConfig testcontainersConfig;
    private static String previousEndpoint;

    @BeforeAll
    static void beforeAll() {
        previousEndpoint = System.getProperty("aws.endpointUrl");
        testcontainersConfig = new TestcontainersConfig(containerFiles);
        // Exercise the production S3 client factory against Floci, without mocking or changing it.
        System.setProperty("aws.endpointUrl", testcontainersConfig.getS3Endpoint().toString());
    }

    @AfterAll
    static void afterAll() {
        try {
            if (testcontainersConfig != null)
                testcontainersConfig.close();
        } finally {
            if (previousEndpoint != null) {
                System.setProperty("aws.endpointUrl", previousEndpoint);
            } else {
                System.clearProperty("aws.endpointUrl");
            }
        }
    }

    @Getter
    protected static class TestcontainersConfig implements AutoCloseable {
        private final GenericContainer<?> sftpContainer;
        private final FlociContainer flociContainer;
        private final Path sshPrivateKey;
        private final Credentials sftpContainerCredentials = new Credentials("user", "pass");

        @SuppressWarnings("resource")
        @SneakyThrows
        TestcontainersConfig(Path directory) {
            sshPrivateKey = directory.resolve("client-key");
            Path publicKey = directory.resolve("client-key.pub");
            var key = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, 2048);
            try {
                key.writePrivateKey(sshPrivateKey.toString());
                key.writePublicKey(publicKey.toString(), "simple-backups-integration-test");
                FilesSupport.privatePermissions(sshPrivateKey, false);
            } finally {
                key.dispose();
            }
            sftpContainer = new GenericContainer<>(DockerImageName.parse("atmoz/sftp"))
                    .withExposedPorts(22)
                    .waitingFor(Wait.forListeningPorts(22))
                    .withCopyFileToContainer(MountableFile.forHostPath(publicKey), "/home/user/.ssh/keys/test.pub")
                    .withCommand("user:pass:::upload");
            flociContainer = new FlociContainer();
            try {
                sftpContainer.start();
                flociContainer.start();
            } catch (RuntimeException failure) {
                close();
                throw failure;
            }
        }

        @SneakyThrows
        public URI getSftpContainerUri() {
            return UriComponentsBuilder.newInstance()
                    .scheme("sftp")
                    .host(sftpContainer.getHost())
                    .port(sftpContainer.getMappedPort(22))
                    .build()
                    .toUri();
        }

        @SneakyThrows
        public URI getS3Endpoint() {
            // An IP endpoint makes the SDK use path-style buckets without special production settings.
            String address = InetAddress.getByName(flociContainer.getHost()).getHostAddress();
            return UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host(address)
                    .port(flociContainer.getMappedPort(FlociContainer.PORT))
                    .build().toUri();
        }

        @Override
        public void close() {
            try {
                flociContainer.stop();
            } finally {
                sftpContainer.stop();
            }
        }

        @Data
        @Accessors(fluent = true)
        public static final class Credentials {
            private final String username;
            @ToString.Exclude
            private final String password;
        }
    }
}
