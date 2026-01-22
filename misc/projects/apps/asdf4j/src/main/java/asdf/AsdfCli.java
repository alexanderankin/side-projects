package asdf;

import asdf.model.AsdfVersionProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@CommandLine.Command(
        name = "asdf4j",
        description = "asdf but java and cross platform",
        versionProvider = AsdfVersionProvider.class,
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                AsdfCli.PluginCli.class,
                AsdfCli.Current.class,
                AsdfCli.Install.class,
                AsdfCli.SetCommand.class,
                AsdfCli.Latest.class,
                AsdfCli.Info.class,
                AsdfCli.ReShim.class,
                AsdfCli.ShimVersions.class,
                AsdfCli.Exec.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class AsdfCli {
    static void main(String[] args) {
        System.exit(new CommandLine(AsdfCli.class)
                .setUnmatchedOptionsArePositionalParams(true)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args));
    }

    @Data
    @Command(name = "current", description = "Display current versions of all packages")
    static class Current implements Runnable {
        @Parameters(arity = "0..1", description = "Display version of this package")
        String name;

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "install", description = "Install all the package versions listed in the nearest parent directory .tool-versions file")
    static class Install implements Runnable {
        @Parameters(arity = "0..1", index = "0", description = "Install this package from .tool-versions file")
        String name;
        @Parameters(arity = "0..1", index = "1", description = "Install this version of package")
        String version;

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "set", description = "Set a version in a [~/].tool-versions file")
    static class SetCommand implements Runnable {
        @Parameters(arity = "1", index = "0", description = "Name of the package to set version of")
        String name;
        @Parameters(arity = "1", index = "1", description = "version to set in the pacakge")
        String version;
        @Option(names = {"-u", "--user", "--home"}, description = "Set in ~/.tool-versions", defaultValue = "false")
        boolean user;
        @Option(names = {"-p", "--parent"}, description = "Set in nearest parent directory", defaultValue = "false")
        boolean parent;

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "latest", description = "See latest versions of packages listed in nearest parent directory .tool-versions file")
    static class Latest implements Runnable {
        @Parameters(arity = "0..1", index = "0", description = "See latest of this package")
        String name;

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "info", description = "Display diagnostic info")
    static class Info implements Runnable {
        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "reshim", description = "recreate shims")
    static class ReShim implements Runnable {
        @Parameters(arity = "0..1", index = "0", description = "Reshim this package")
        String name;

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "shimversions", description = "List the plugins and versions that provide a command")
    static class ShimVersions implements Runnable {
        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "exec", description = "Executes the command shim for current version")
    static class Exec implements Runnable {
        @Parameters(arity = "1", index = "0")
        String command;
        // @Parameters(index = "1..", parameterConsumer = LiteralParameterConsumer.class, preprocessor = LiteralPreProcessor.class)
        @Parameters(index = "1..")
        List<String> parameters = new ArrayList<>();

        @Override
        public void run() {
        }
    }

    @Data
    @Command(name = "plugin", description = "manage plugins", subcommands = {
            PluginCli.Add.class,
            PluginCli.Remove.class,
            PluginCli.List.class,
            PluginCli.Update.class,
    })
    static class PluginCli {
        @Data
        @Command(name = "add", description = "Add a plugin via git url"/*" or from the plugin repo"*/)
        static class Add implements Runnable {
            @Parameters(arity = "1", index = "0")
            String name;
            @Parameters(arity = "0..1", index = "1")
            String gitUrl;

            @Override
            public void run() {
            }
        }

        @Data
        @Command(name = "remove", description = "Remove a plugin and package versions")
        static class Remove implements Runnable {
            @Parameters(arity = "1")
            String name;

            @Override
            public void run() {
            }
        }


        @Data
        @Command(name = "list")
        static class List implements Runnable {
            @Override
            public void run() {
            }

            @Command(name = "all")
            void listAll() {
            }
        }

        @Data
        @Command(name = "update", description = "Update a plugin to latest commit on default branch or a particular git-ref")
        static class Update implements Runnable {
            @Override
            public void run() {
            }

            @Command(name = "all", description = "Update all plugins to latest commit on default branch")
            void updateAll() {
            }
        }
    }
}
