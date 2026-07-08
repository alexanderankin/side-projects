package side.pkg.gradle2maven;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import info.ankin.projects.picocli.logback.verbosity.LogbackVerbosityMixin;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

@CommandLine.Command(
        name = "gradle2maven",
        description = "convert gradle to maven folder structure",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        subcommands = {
                GradleToMavenCli.SubCommand.Convert.class,
        }
)
public class GradleToMavenCli {
    public static void main(String[] args) {
        System.exit(new CommandLine(GradleToMavenCli.class).execute(args));
    }

    @CommandLine.Command(mixinStandardHelpOptions = true)
    static abstract sealed class SubCommand {
        private final ch.qos.logback.classic.Logger rootLogger;
        @CommandLine.Option(names = {"-n", "--dry-run"}, description = "simulate operations where possible")
        boolean dryRun;

        protected SubCommand() {
            rootLogger = LoggerFactory.getILoggerFactory() instanceof LoggerContext lc
                    ? lc.getLogger(Logger.ROOT_LOGGER_NAME)
                    : null;

            if (rootLogger != null)
                rootLogger.setLevel(Level.WARN);
        }

        @CommandLine.Mixin
        LogbackVerbosityMixin logbackVerbosityMixin;

        @Slf4j
        @CommandLine.Command(
                name = "convert",
                description = "convert a directory from one to the other",
                mixinStandardHelpOptions = true
        )
        public static final class Convert extends SubCommand implements Runnable {
            @CommandLine.Option(names = {"-i", "--input"}, required = false)
            Path input;

            @CommandLine.Option(names = {"-o", "--output"}, required = true)
            Path output;

            @Override
            public void run() {
                RepoDirConverter repoDirConverter = new RepoDirConverter();

                if (input == null) {
                    input = repoDirConverter.getDefaultRepoDir();
                    log.warn("using default repo dir: {}", input);
                }

                log.info("converting from {} to {}", input, output);

                if (dryRun)
                    repoDirConverter.convertDryRun(input, output);
                else
                    repoDirConverter.convert(input, output);
            }
        }

    }
}
