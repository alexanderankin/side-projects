package side.mc.management;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ProcessRunner {
    @SneakyThrows
    public RunResult run(String command) {
        log.trace("running command {}", command);
        var pb = new ProcessBuilder(command.split("\\s+"));
        var p = pb.start();
        var exit = p.waitFor();
        var dataInput = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        var dataError = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        RunResult runResult = new RunResult(dataInput, dataError, exit);
        log.trace("running command {} resulted in {}", command, runResult);
        if (exit != 0) {
            throw new RunResultError().setRunResult(runResult);
        }
        return runResult;
    }


    public record RunResult(String output, String error, int code) {
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    @Accessors(chain = true)
    public static class RunResultError extends RuntimeException {
        RunResult runResult;
    }
}
