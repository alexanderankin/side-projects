package side.ufw.web.exec;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static side.ufw.web.exec.ExecUtils.combineExecutableAndArgs;

@Service
public class ExecExecService implements ExecService {
    Exec exec;

    @Autowired
    void setExec(Optional<Exec> exec) {
        this.exec = exec.orElse(Exec.INSTANCE);
    }

    @Override
    public ExecProperties.ExecType execType() {
        return ExecProperties.ExecType.local;
    }

    @Override
    public Result execute(Config config) {
        var mappedConfig = Exec.Config.builder()
                .commands(combineExecutableAndArgs(config.executable(), config.arguments()))
                .workingDirectory(Optional.ofNullable(config.workingDirectory()).map(Path::toFile).orElse(null))
                .environment(Optional.ofNullable(config.environment()).orElse(System.getenv()))
                .timeout(Duration.ofSeconds(10))
                .build();

        return mapResult(exec.exec(mappedConfig));
    }

    @SneakyThrows
    private Result mapResult(Exec.Result exec) {
        return new Result(exec.getCode(), exec.getOut().readAllBytes(), exec.getErr().readAllBytes());
    }
}
