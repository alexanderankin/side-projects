package side.capfetch;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import javax.management.*;
import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Capfetch {
    @SneakyThrows
    static void main() {
        var stats = getStats();
        System.out.printf("""
                        {
                          "ramGb" : %s,
                          "diskGb" : %s,
                          "cpu" : "%s",
                          "nproc" : %s
                        }
                        """, stats.getRamGb(),
                stats.getDiskGb(),
                stats.getCpu(),
                stats.getNproc());
    }

    @SneakyThrows
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
                        .setCpu(getCpu(current));

            }
            case null, default -> new Stats()
                    .setRamGb(getRam())
                    .setDiskGb(new BigDecimal(new File("/").getTotalSpace())
                            .divide(BigDecimal.valueOf(1_000_000_000L), 3, RoundingMode.HALF_UP))
                    .setCpu(getCpu(current))
                    .setNproc(Runtime.getRuntime().availableProcessors());
        };
    }

    @SneakyThrows
    private static BigDecimal getRam() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        var os = new ObjectName("java.lang", "type", "OperatingSystem");
        var tPMS = Arrays.stream(mBeanServer.getMBeanInfo(os).getAttributes())
                .filter(s -> s.getName().equals("TotalPhysicalMemorySize"))
                .findAny().orElse(null);

        if (tPMS == null)
            throw new UnsupportedOperationException();
        if (!tPMS.getType().equals("long"))
            throw new UnsupportedOperationException();

        long tPMSValue = (long) mBeanServer.getAttribute(os, tPMS.getName());
        return new BigDecimal(tPMSValue)
                .divide(BigDecimal.valueOf(1_000_000_000L), 3, RoundingMode.HALF_UP);
    }

    public static String getCpu(SupportedOperatingSystem current) {
        return switch (current) {
            // needs testing
            case WINDOWS -> run(new ProcessBuilder("wmic", "cpu", "get", "Name"))
                    .throwOnError().outAsString()
                    .lines().skip(1).findFirst().orElse("").strip();
            case MACOS -> run(new ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string"))
                    .throwOnError().outAsString().strip();
            case LINUX -> run(new ProcessBuilder("grep", "-m1", "^model name", "/proc/cpuinfo"))
                    .throwOnError().outAsString().split(":")[1].strip();
            case null, default -> null;
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
            var override = System.getenv("CAPFETCH_OVERRIDE_OS");
            switch (override) {
                case "WINDOWS", "MACOS", "LINUX":
                    return SupportedOperatingSystem.valueOf(override);
                case "null":
                    return null;
                case null, default:
            }

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
