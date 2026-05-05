package side.cloud.util.acme.presenter.sftp.jsch;

import com.jcraft.jsch.*;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.challenge.presentation.ChallengePresenter;
import side.cloud.util.acme.presenter.sftp.jsch.JschSftpChallengePresenter.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class JschSftpChallengePresenterCrud implements ChallengePresenter.SingleHostCrudPresenter.Crud {
    @Getter
    private final Config config;
    private final Retry retry;

    public JschSftpChallengePresenterCrud(Config config, RetryRegistry retryRegistry) {
        this.config = config;
        this.retry = retryRegistry.retry("acme-sftp",
                RetryConfig.custom()
                        .maxAttempts(config.getMaxRetry())
                        .waitDuration(Duration.ofMillis(200))
                        .build()
        );
        retry.getEventPublisher().onIgnoredError(e -> log.warn("onIgnoredError: {}", e, e.getLastThrowable()));
    }

    @SneakyThrows
    @Override
    public void create(String key, String value) {
        retry.executeCallable(() -> {
            try (var session = session()) {
                try (var sftp = connectSftp(session.session())) {
                    mkdirs(sftp.channel, config.getHostPath());
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

                    log.debug("SFTP put host={} path={} size={}", config.getHost(), remotePath(key), bytes.length);

                    try (InputStream in = new ByteArrayInputStream(bytes)) {
                        sftp.channel.put(in, remotePath(key));
                    }
                }
            }
            return null;
        });
    }

    @SneakyThrows
    @Override
    public String read(String key) {
        return retry.executeCallable(() -> {
            try (var session = session()) {
                try (var sftp = connectSftp(session.session())) {
                    log.debug("SFTP get host={} path={}", config.getHost(), remotePath(key));

                    var out = new ByteArrayOutputStream();
                    sftp.channel.get(remotePath(key), out);
                    return out.toString(StandardCharsets.UTF_8);
                } catch (SftpException sftpException) {
                    if (sftpException.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        return null;
                    }
                    throw sftpException;
                }
            }
        });
    }

    @SneakyThrows
    @Override
    public void delete(String key) {
        retry.executeCallable(() -> {
            try (var session = session()) {
                try (var sftp = connectSftp(session.session())) {
                    try {
                        log.debug("SFTP rm host={} path={}", config.getHost(), remotePath(key));
                        sftp.channel.rm(remotePath(key));
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            log.debug("SFTP rm skipped (not found) path={}", remotePath(key));
                            return null;
                        }
                        throw e;
                    }
                }
            }
            return null;
        });
    }

    private void mkdirs(ChannelSftp sftp, String path) throws SftpException {
        String original = sftp.pwd();

        try {
            if (path.startsWith("/")) {
                sftp.cd("/");
            }

            for (String part : path.split("/")) {
                if (part.isBlank()) continue;

                try {
                    sftp.cd(part);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        // doesn't exist → create then cd
                        sftp.mkdir(part);
                        sftp.cd(part);
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            sftp.cd(original);
        }
    }

    private String remotePath(String key) {
        if (key.contains("/") || key.contains("\\") || key.equals(".") || key.equals("..")) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return config.getHostPath().replaceAll("/+$", "") + "/" + key;
    }

    private CloseableSession session() throws JSchException {
        JSch jsch = new JSch();
        Config.Auth auth = config.getAuth();

        if (auth.getIdentityFile() != null) {
            jsch.addIdentity(auth.getIdentityFile().toString());
        } else if (auth.getIdentityFileContent() != null) {
            var idBytes = auth.getIdentityFileContent().getBytes(StandardCharsets.UTF_8);
            jsch.addIdentity("inline", idBytes, null, null);
        }

        Session session = jsch.getSession(auth.getUsername(), config.getHost(), config.getPort());

        if (auth.getPassword() != null) {
            session.setPassword(auth.getPassword());
        }

        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("UserKnownHostsFile", "/dev/null");
        session.setTimeout(timeoutMs());
        session.connect();

        return new CloseableSession(session);
    }

    private CloseableChannel<ChannelSftp> connectSftp(Session session) throws JSchException {
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect(timeoutMs());
        return new CloseableChannel<>(sftp);
    }


    private int timeoutMs() {
        return Math.toIntExact(config.getTimeout().toMillis());
    }

    private record CloseableSession(Session session) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            try {
                session.disconnect();
            } catch (Exception e) {
                log.error("error while closing session", e);
            }
        }
    }

    private record CloseableChannel<T extends Channel>(T channel) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            try {
                channel.disconnect();
            } catch (Exception e) {
                log.error("error while closing channel", e);
            }
        }
    }
}
