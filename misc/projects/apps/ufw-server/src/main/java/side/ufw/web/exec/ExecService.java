package side.ufw.web.exec;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ExecService {
    ExecProperties.ExecType execType();

    Result execute(Config config);

    record Config(
            String executable,
            List<String> arguments,
            Map<String, String> environment,
            Path workingDirectory
    ) {
        public Config(List<String> command) {
            this(command.getFirst(), command.subList(1, command.size()), Map.of(), null);
        }
    }

    record Result(int result, byte[] out, byte[] err) {
    }
}
