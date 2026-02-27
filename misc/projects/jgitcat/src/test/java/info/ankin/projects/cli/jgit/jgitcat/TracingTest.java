package info.ankin.projects.cli.jgit.jgitcat;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class TracingTest {
    @Test
    void traceIt() {
        String repo = "https://github.com/msangel/promisified-resource-loader";
        new CommandLine(new JGitCat()).execute("-r", repo, "-b", "master");
    }
}
