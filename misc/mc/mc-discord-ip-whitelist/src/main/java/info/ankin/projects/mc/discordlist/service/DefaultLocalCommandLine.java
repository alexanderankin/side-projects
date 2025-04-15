package info.ankin.projects.mc.discordlist.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "mc.discord-list.command-line", name = "default-executor.enabled", havingValue = "true", matchIfMissing = true)
public class DefaultLocalCommandLine implements CommandLineExecutor {
    @SneakyThrows
    private static byte[] readAllBytes(InputStream inputStream) {
        return inputStream.readAllBytes();
    }

    @SneakyThrows
    @Override
    public CommandResult exec(String... args) {
        var pb = new ProcessBuilder(args);
        Process process = pb.start();
        CommandResult commandResult = new CommandResult();

        var readers = List.of(
                new Thread(() -> commandResult.setStandardOutput(new String(readAllBytes(process.getInputStream()), StandardCharsets.UTF_8))),
                new Thread(() -> commandResult.setErrorOutput(new String(readAllBytes(process.getErrorStream()), StandardCharsets.UTF_8))));
        readers.forEach(Thread::start);

        int exitCode = process.waitFor();

        for (Thread reader : readers) {
            reader.join();
        }
        return commandResult.setExitCode(exitCode);
    }

    @SneakyThrows
    @Override
    public String fullPath(String program) {
        for (String path : System.getenv("PATH").split(File.pathSeparator)) {
            File[] files;
            try {
                files = new File(path).listFiles(File::canExecute);
            } catch (Exception ignored) {
                continue;
            }

            if (files == null)
                continue;
            for (File file : files) {
                if (file.getName().equals(program))
                    return file.getPath();
            }
        }
        throw new FileNotFoundException("not found in path: " + program);
    }
}
