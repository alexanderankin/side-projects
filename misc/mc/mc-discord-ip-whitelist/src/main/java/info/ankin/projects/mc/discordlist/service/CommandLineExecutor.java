package info.ankin.projects.mc.discordlist.service;

import lombok.Data;
import lombok.experimental.Accessors;

public interface CommandLineExecutor {
    CommandResult exec(String... args);

    default CommandResult execUnsafe(String... args) {
        CommandResult result = exec(args);
        if (result.getExitCode() == 0)
            return result;
        throw new ExitCodeException(args[0], result);
    }

    String fullPath(String program);

    @Data
    @Accessors(chain = true)
    class CommandResult {
        String standardOutput;
        String errorOutput;
        int exitCode;
    }

    class ExitCodeException extends RuntimeException {
        public ExitCodeException(String program, CommandResult commandResult) {
            super("could not execute external program '" + program + "', got exit code: " + commandResult.getExitCode());
        }
    }
}
