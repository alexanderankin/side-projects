package side.ufw.web.exec;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshExecService implements ExecService {
    private static final AtomicBoolean NO_THREAD_WARNING = new AtomicBoolean(false);
    private final ExecProperties props;

    @Override
    public ExecProperties.ExecType execType() {
        return ExecProperties.ExecType.ssh;
    }

    @Override
    @SneakyThrows
    public Result execute(Config config) {
        var session = getSession();

        String command = buildCommand(config);
        log.debug("buildCommand returned {}", command);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        channel.setOutputStream(out);
        channel.setErrStream(err);

        var timeoutDuration = props.getTimeout();
        channel.connect(Math.toIntExact(timeoutDuration.toMillis()));

        var thread = extractThread(channel);
        if (thread == null) {
            if (NO_THREAD_WARNING.compareAndExchange(false, true))
                log.warn("could not find thread in ChannelExec, busy looping - this message printed only once");
            var start = System.nanoTime();
            while (!channel.isClosed() && Duration.ofNanos(System.nanoTime() - start).compareTo(timeoutDuration) < 0) {
                // noinspection BusyWait
                Thread.sleep(250);
            }
        } else {
            thread.join(timeoutDuration);
        }

        if (!channel.isClosed())
            throw new TimeoutException("channel is not closed in time");

        int exit = channel.getExitStatus();

        channel.disconnect();
        session.disconnect();

        var result = new Result(exit, out.toByteArray(), err.toByteArray());
        log.debug("command resulted in {}", result);
        return result;
    }

    private Thread extractThread(ChannelExec channelExec) {
        Field threadField = ReflectionUtils.findField(ChannelExec.class, "thread");
        if (threadField == null)
            return null;
        ReflectionUtils.makeAccessible(threadField);
        return (Thread) ReflectionUtils.getField(threadField, channelExec);
    }

    private Session getSession() throws JSchException {
        var ssh = props.getSsh();
        var auth = ssh.getAuth();

        JSch jsch = new JSch();

        if (auth.getSshKey() != null) {
            log.debug("getting session with getSshKey - ssh props: {}", ssh);
            jsch.addIdentity(auth.getSshKey().toAbsolutePath().toString());
        }

        if (auth.isUseIdentity()) {
            log.debug("getting session with isUseIdentity - ssh props: {}", ssh);
            loadDefaultIdentities(jsch);
        }

        Session session = jsch.getSession(ssh.getUser(), ssh.getHost(), ssh.getPort());

        if (auth.getPassword() != null) {
            log.debug("getting session with getPassword - ssh props: {}", ssh);
            session.setPassword(auth.getPassword());
        }

        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("UserKnownHostsFile", "/dev/null");
        session.connect(Math.toIntExact(props.getTimeout().toMillis()));
        return session;
    }

    private void loadDefaultIdentities(JSch jsch) {
        Path sshDir = Path.of(System.getProperty("user.home"), ".ssh");

        String[] keys = {
                "id_rsa",
                "id_ecdsa",
                "id_ed25519",
                "id_dsa"
        };

        for (String key : keys) {
            Path keyPath = sshDir.resolve(key);
            if (java.nio.file.Files.exists(keyPath)) {
                try {
                    jsch.addIdentity(keyPath.toAbsolutePath().toString());
                } catch (Exception ignored) {
                    // skip invalid/unreadable keys
                }
            }
        }
    }

    private String buildCommand(Config config) {
        StringBuilder inner = new StringBuilder();

        // --- env vars ---
        if (config.environment() != null && !config.environment().isEmpty()) {
            config.environment().forEach((k, v) -> {
                inner.append(k)
                        .append("=")
                        .append(shellEscape(v))
                        .append(" ");
            });
        }

        // --- working directory ---
        if (config.workingDirectory() != null) {
            inner.append("cd ")
                    .append(shellEscape(config.workingDirectory().toString()))
                    .append(" && ");
        }

        // --- executable ---
        inner.append(shellEscape(config.executable()));

        // --- args ---
        if (config.arguments() != null) {
            for (String arg : config.arguments()) {
                inner.append(" ").append(shellEscape(arg));
            }
        }

        // wrap once for bash -c
        return "bash -c " + shellEscape(inner.toString());
    }

    private String shellEscape(String s) {
        if (s == null || s.isEmpty()) {
            return "''";
        }
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }
}
