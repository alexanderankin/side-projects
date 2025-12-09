package side.cloud.util.packer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class JumpHostInitializer {
    static final String COMMENT = "# PackerToolCli@";
    private final JSch jSch;

    public static JSch getJSch() {
        return getJSch(null);
    }

    @SneakyThrows
    public static JSch getJSch(String privateKey) {
        JSch jsch = new JSch();
        if (privateKey != null) {
            log.info("adding extra private key: {}", privateKey);
            jsch.addIdentity(privateKey);
        }

        var dotSshList = new File(System.getProperty("user.home"), ".ssh").listFiles();
        if (dotSshList == null) {
            log.warn("no ~/.ssh directory exists");
        } else {
            var sshIds = Arrays.stream(dotSshList)
                    .filter(path -> path.getName().startsWith("id_") &&
                            path.getName().endsWith(".pub") &&
                            new File(path.getParent(), path.getName().replace(".pub", "")).exists())
                    .map(path -> new File(path.getParent(), path.getName().replace(".pub", "")))
                    .toList();
            log.info("adding ids from ~/.ssh/id*.pub: {}", sshIds);
            for (var id : sshIds) {
                jsch.addIdentity(id.getPath());
            }
        }
        return jsch;
    }

    @SneakyThrows
    public void add(String user, String host, Path key) {
        var pubKey = Files.readString(key).trim();
        add(user, host, pubKey);
    }

    public void add(String user, String host, String pubKey) {
        if (pubKey.contains("\n"))
            throw new IllegalArgumentException("public key has new lines in it");

        var previousKeys = runCommand(user, host, "cat ~/.ssh/authorized_keys")
                .failIfNot0("get authorized_keys")
                .standardOutput().toString(StandardCharsets.UTF_8);

        if (previousKeys.contains(pubKey)) {
            log.info("key already on jump host");
            return;
        }

        String cmd = "bash -c \"echo '" + pubKey + " " + COMMENT + System.currentTimeMillis() + "'\" >> ~/.ssh/authorized_keys";
        runCommand(user, host, cmd)
                .failIfNot0("add authorized key");
    }

    // Duration.of(timeout, timeUnit.toChronoUnit())
    public void clean(String host, String user, Duration timeout) {
        var previousKeys = runCommand(user, host, "cat ~/.ssh/authorized_keys")
                .failIfNot0("get authorized_keys")
                .standardOutput().toString(StandardCharsets.UTF_8);

        Instant beforeCutoff = Instant.now().minus(timeout);
        LocalDateTime beforeLdt = LocalDateTime.ofInstant(beforeCutoff, ZoneId.systemDefault());

        var badTimestamps = previousKeys.lines()
                .filter(line -> line.contains(COMMENT))
                .map(line -> line.split(COMMENT)[1])
                .filter(line -> Instant.ofEpochMilli(Long.parseLong(line)).isBefore(beforeCutoff))
                .toList();

        log.info("cleaning timestamps {} before {}", badTimestamps, beforeLdt);
        if (badTimestamps.isEmpty()) {
            log.info("no timestamps found");
        } else {
            var cmd = "bash -c \"sed " +
                    String.join(" ", badTimestamps.stream().map(t -> "-e '/" + COMMENT + t + "$/d'").toList()) +
                    " -i ~/.ssh/authorized_keys\"";

            log.info("cleaning timestamps with {}", cmd);
            runCommand(user, host, cmd)
                    .failIfNot0("clean authorized_keys");
            log.info("cleaned timestamps");
        }
    }

    @SneakyThrows
    private SshCommandResult runCommand(String user, String host, String command) {
        Session session = jSch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("UserKnownHostsFile", "/dev/null");
        session.connect(10_000);
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setCommand(command);
        var standardOutput = new ByteArrayOutputStream();
        var standardError = new ByteArrayOutputStream();
        exec.setOutputStream(standardOutput);
        exec.setErrStream(standardError);
        exec.connect(10_000);
        Retry.of("execDone", RetryConfig.custom().retryOnResult(Predicate.isEqual(false)).intervalFunction(IntervalFunction.of(500)).maxAttempts(20).build())
                .executeCallable(exec::isClosed);
        exec.disconnect();
        session.disconnect();
        return new SshCommandResult(exec, standardOutput, standardError);
    }

    private record SshCommandResult(
            ChannelExec exec,
            ByteArrayOutputStream standardOutput,
            ByteArrayOutputStream standardError
    ) {
        String outputSummary() {
            return "out: " + standardOutput.toString(StandardCharsets.UTF_8) + ", err: " + standardError.toString(StandardCharsets.UTF_8);
        }

        SshCommandResult failIfNot0(String failureMessageAction) {
            if (this.exec().getExitStatus() != 0) {
                throw new IllegalStateException("command to " + failureMessageAction + " failed with exit code: " +
                        this.exec().getExitStatus() + "; " +
                        this.outputSummary());
            }
            return this;
        }
    }
}
