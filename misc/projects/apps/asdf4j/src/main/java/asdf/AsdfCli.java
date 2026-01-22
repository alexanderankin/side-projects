package asdf;

import lombok.extern.slf4j.Slf4j;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@CommandLine.Command(
        name = "asdf4j",
        description = "asdf but java and cross platform",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                AutoComplete.GenerateCompletion.class,
        }
)
public class AsdfCli {
}
