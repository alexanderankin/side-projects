package side.pki.ca;

import lombok.extern.slf4j.Slf4j;
import picocli.AutoComplete;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
        name = "self-signed-ca-tool",
        description = "",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                AutoComplete.GenerateCompletion.class,
        }
)
public class SelfSignedCaTool {
    @CommandLine.Command(name = "simple")
    public static class Simple {
    }
}
