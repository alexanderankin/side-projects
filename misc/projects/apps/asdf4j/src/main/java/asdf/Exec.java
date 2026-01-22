package asdf;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Exec {
    public static final Exec INSTANCE = new Exec();

    @SneakyThrows
    public void execWithInheritedIo(Config config) {
        var pb = new ProcessBuilder();
        List<String> commands = config.getCommands();
        if (commands != null)
            pb.command(commands);

        pb.directory(config.workingDirectory);

        if (!config.isInheritEnv())
            pb.environment().clear();
        var env = config.getEnvironment();
        if (env != null)
            pb.environment().putAll(env);

        pb.inheritIO();

        var process = pb.start();
        var exitCode = process.waitFor();
        if (exitCode != 0)
            throw new ExitCodeException(config, exitCode);
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
    }

    public static class ExitCodeException extends RuntimeException {
        public ExitCodeException(Config config, int code) {
            super("process exited with code: " + code);
        }
    }
}
