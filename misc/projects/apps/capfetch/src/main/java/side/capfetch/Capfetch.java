package side.capfetch;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Capfetch {
    @SneakyThrows
    static void main() {
        var jsonMapper = JsonMapper.builder().build();
        var writer = jsonMapper.writer().withDefaultPrettyPrinter();
        var jsonString = writer.writeValueAsString(getStats());
        System.out.println(jsonString);
    }

    public static Stats getStats() {
        var current = SupportedOperatingSystem.current();
        return switch (current) {
            case LINUX -> {
                var kbRamOutput = run(new ProcessBuilder("awk", "/MemTotal/ {print $2}", "/proc/meminfo"))
                        .throwOnError()
                        .outAsString();

                var gbRam = new BigDecimal(kbRamOutput.trim())
                        .multiply(BigDecimal.valueOf(1024))
                        .divide(BigDecimal.valueOf(1_000_000_000L), 3, RoundingMode.HALF_UP);

                var kbDiskOutput = run(new ProcessBuilder("df", "-k", "--output=size", "/"))
                        .throwOnError()
                        .outAsString().lines().toList().getLast();

                var gbDisk = new BigDecimal(kbDiskOutput.trim())
                        .multiply(BigDecimal.valueOf(1024))
                        .divide(BigDecimal.valueOf(1_000_000_000L), 3, RoundingMode.HALF_UP);

                yield new Stats()
                        .setRamGb(gbRam)
                        .setDiskGb(gbDisk)
                        .setNproc(Runtime.getRuntime().availableProcessors())
                        .setCpu(run(new ProcessBuilder("grep", "-m1", "^model name", "/proc/cpuinfo")).throwOnError().outAsString().split(":")[1].strip());

            }
            case null, default ->
                    throw new UnsupportedOperationException("operating system '" + current + "' is not supported");
        };
    }

    @SneakyThrows
    public static RunResult run(ProcessBuilder processBuilder) {
        var p = processBuilder.start();

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            var out = CompletableFuture.supplyAsync(() -> getBytes(p.getInputStream()), exec);
            var err = CompletableFuture.supplyAsync(() -> getBytes(p.getErrorStream()), exec);

            CompletableFuture.allOf(out, err).join();
            if (!p.waitFor(10, TimeUnit.SECONDS)) {
                p.destroy();
                throw new TimeoutException();
            }

            return new RunResult(processBuilder, out.get(), err.get(), p.exitValue());
        }
    }

    @SneakyThrows
    private static byte[] getBytes(InputStream inputStream) {
        return inputStream.readAllBytes();
    }

    public enum SupportedOperatingSystem {
        WINDOWS, MACOS, LINUX,
        ;

        public static SupportedOperatingSystem current() {
            var osName = System.getProperty("os.name");

            if (osName.toLowerCase().contains("windows"))
                return WINDOWS;
            if (osName.toLowerCase().contains("mac"))
                return MACOS;
            if (osName.toLowerCase().contains("linux"))
                return LINUX;

            return null;
        }
    }

    public record RunResult(ProcessBuilder pb, byte[] out, byte[] err, int code) {
        public String outAsString() {
            return new String(out, StandardCharsets.UTF_8);
        }

        public String errAsString() {
            return new String(err, StandardCharsets.UTF_8);
        }

        public RunResult throwOnError() {
            if (code != 0)
                throw new IllegalStateException("process '" + pb.command() + "' exited with code: " + code + ", output: " + outAsString() + ", error output: " + errAsString());
            return this;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Stats {
        BigDecimal ramGb;
        BigDecimal diskGb;
        String cpu;
        int nproc;
    }
}
