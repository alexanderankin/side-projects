package org.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(
        name = "example-program",
        description = "example for illustrating jar trick",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
        }
)
public class Program implements Runnable {
    public static void main(String[] args) {
        System.exit(new CommandLine(Program.class).execute(args));
    }

    @Override
    public void run() {
        System.out.println("running...");
    }
}
