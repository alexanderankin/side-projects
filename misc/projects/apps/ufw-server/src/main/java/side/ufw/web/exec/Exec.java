package side.ufw.web.exec;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

// from asdf/Exec.java
@Slf4j
public class Exec {
    public static final Exec INSTANCE = new Exec();

    private static void configCommands(Config config, ProcessBuilder pb) {
        List<String> commands = config.getCommands();
        if (commands != null)
            pb.command(commands);
    }

    private static void configWd(Config config, ProcessBuilder pb) {
        pb.directory(config.workingDirectory);
    }

    private static void configEnv(Config config, ProcessBuilder pb) {
        if (!config.isInheritEnv())
            pb.environment().clear();
        var env = config.getEnvironment();
        if (env != null)
            pb.environment().putAll(env);
    }

    @SneakyThrows
    public void execWithInheritedIo(Config config) {
        var pb = new ProcessBuilder();
        configCommands(config, pb);
        configWd(config, pb);
        configEnv(config, pb);

        pb.inheritIO();

        var process = pb.start();
        var exitCode = process.waitFor();
        if (exitCode != 0)
            throw new ExitCodeException(config, exitCode);
    }

    @SneakyThrows
    public Result exec(Config config) {
        var process = start(config);
        var resultBuilder = new ResultBuilder(process);
        resultBuilder.start();

        if (config.getTimeout() == null)
            process.waitFor();
        else
            process.waitFor(config.getTimeout());
        resultBuilder.stop();

        var result = resultBuilder.build();
        if (result.getCode() != 0)
            throw new ExitCodeException(config, result);
        return result;
    }

    @SneakyThrows
    public ProcessAndResult launch(Config config) {
        var process = start(config);
        var out = new PipedOutputStream();
        var err = new PipedOutputStream();

        Thread.ofVirtual().name("exec-launch-piped-out")
                .start(new SafeTranferRunnable(process.getInputStream(), out));

        Thread.ofVirtual().name("exec-launch-piped-err")
                .start(new SafeTranferRunnable(process.getErrorStream(), err));

        return new ProcessAndResult(process, new LazyCodeResult(new PipedInputStream(out), new PipedInputStream(err), process::waitFor));
    }

    @SneakyThrows
    public Process start(Config config) {
        var pb = new ProcessBuilder();
        configCommands(config, pb);
        configWd(config, pb);
        configEnv(config, pb);
        return pb.start();
    }

    @Builder
    @Data
    public static class Config {
        @Singular
        List<String> commands;
        File workingDirectory;
        Map<String, String> environment;
        @Builder.Default
        boolean inheritEnv = true;
        Duration timeout;
    }

    @Getter
    public static class ExitCodeException extends RuntimeException {
        @NonNull
        private final Config config;
        @NonNull
        private final Result result;

        public ExitCodeException(@NonNull Config config, int code) {
            this(config, new Result(null, null, code));
        }

        public ExitCodeException(@NonNull Config config, @NonNull Result result) {
            super("process exited with code: " + result.getCode());
            this.config = config;
            this.result = result;
        }
    }

    public record ProcessAndResult(Process process, Result result) {
    }

    @Data
    public static class Result {
        final InputStream out;
        final InputStream err;
        final int code;
    }

    public static class LazyCodeResult extends Result {
        final Callable<Integer> code;

        public LazyCodeResult(InputStream out, InputStream err, Callable<Integer> code) {
            super(out, err, -1);
            this.code = code;
        }

        @SneakyThrows
        @Override
        public int getCode() {
            return Objects.requireNonNull(code.call());
        }
    }

    @Data
    private static class ResultBuilder {
        final Process process;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        private Thread outThread;
        private Thread errThread;

        void start() {
            outThread = Thread.ofVirtual().name("exec-stdout").start(this::stdoutRunnable);
            errThread = Thread.ofVirtual().name("exec-stderr").start(this::stderrRunnable);
        }

        @SneakyThrows
        void stop() {
            if (!outThread.join(Duration.ofMillis(1)))
                outThread.interrupt();
            if (!errThread.join(Duration.ofMillis(1)))
                errThread.interrupt();
        }

        @SneakyThrows
        void stdoutRunnable() {
            process.getInputStream().transferTo(out);
        }

        @SneakyThrows
        void stderrRunnable() {
            process.getErrorStream().transferTo(err);
        }

        Result build() {
            return new Result(
                    new ByteArrayInputStream(out.toByteArray()),
                    new ByteArrayInputStream(err.toByteArray()),
                    process.exitValue()
            );
        }
    }

    private record SafeTranferRunnable(InputStream errStream, PipedOutputStream err) implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            errStream.transferTo(err);
        }
    }
}
