package side.pkg.gradle2maven;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
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

        /**
         * @see <a href="https://picocli.info/#_repeated_boolean_options">Picocli manual - 6.1.3. Repeated Boolean Options</a>
         */
        @CommandLine.Option(
                names = {"-v", "--verbose"},
                description = "Increase verbosity. Specify multiple times to increase (-vvv)."
        )
        private void verbosity(boolean[] verbosity) {
            // default is warn, for every other one, set it
            int delta = 0;
            for (boolean b : verbosity) {
                delta += b ? 1 : -1;
            }

            List<Level> levels = List.of(Level.OFF, Level.ERROR, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL);
            int defaultLevel = 1;
            int newLevel = defaultLevel + delta;
            int legalNewLevel = newLevel < 0
                    ? 0
                    : newLevel >= levels.size()
                    ? levels.size() - 1
                    : newLevel;

            rootLogger.setLevel(levels.get(legalNewLevel));
        }

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
