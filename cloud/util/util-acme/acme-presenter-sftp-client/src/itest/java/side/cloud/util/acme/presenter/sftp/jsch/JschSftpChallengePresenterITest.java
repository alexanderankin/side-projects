package side.cloud.util.acme.presenter.sftp.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.SftpException;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import side.cloud.util.acme.presenter.sftp.jsch.JschSftpChallengePresenter.Config;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JschSftpChallengePresenterITest {
    static GenericContainer<?> sftpContainer;
    static String sshPrivateKey;
    JschSftpChallengePresenter presenter;

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        sftpContainer = new GenericContainer<>(DockerImageName.parse("serversideup/docker-ssh:v3.2.0"));
        sftpContainer.addExposedPort(2222);
        // generate key
        var kp = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, 2048);
        // private key
        var baos = new ByteArrayOutputStream();
        kp.writePrivateKey(baos);
        sshPrivateKey = baos.toString(StandardCharsets.UTF_8);
        // public key
        baos.reset();
        kp.writePublicKey(baos, null);
        var sshPublicKey = baos.toString(StandardCharsets.UTF_8);
        sftpContainer.withEnv("AUTHORIZED_KEYS", sshPublicKey);
        sftpContainer.start();
    }

    @BeforeEach
    void beforeEach() {
        presenter = new JschSftpChallengePresenter(
                new Config()
                        .setMaxRetry(1)
                        .setHost(sftpContainer.getHost())
                        .setPort(sftpContainer.getMappedPort(2222))
                        .setHostPath(".well-known/acme-challenge")
                        .setAuth(new Config.Auth()
                                // https://hub.docker.com/r/serversideup/docker-ssh
                                .setUsername("tunnel")
                                .setIdentityFileContent(sshPrivateKey)),
                new InMemoryRetryRegistry()
        );
    }

    @Test
    void testCrud_readDne() {
        var result = presenter.getCrud().read("test");
        assertThat(result, is(nullValue()));
    }

    @Test
    void testCrud_createPermissionDenied() {
        presenter.getConfig().setHostPath("/var/www/html/.well-known/acme-challenge");
        var se = assertThrows(SftpException.class, () -> presenter.getCrud().create("key", "value"));
        assertThat(se.id, is(ChannelSftp.SSH_FX_PERMISSION_DENIED));
    }

    @Test
    void testCrud_create() {
        presenter.getCrud().create("key", "value");
    }

    @Test
    void testCrud_createReadDeleteRead() {
        var value = "testCrud_createReadDeleteRead";
        presenter.getCrud().create("key", value);
        assertThat(presenter.getCrud().read("key"), is(value));
        presenter.getCrud().delete("key");
        assertThat(presenter.getCrud().read("key"), is(nullValue()));
        presenter.getCrud().delete("key");
        assertThat(presenter.getCrud().read("key"), is(nullValue()));
    }
}
