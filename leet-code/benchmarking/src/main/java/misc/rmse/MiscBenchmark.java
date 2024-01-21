package misc.rmse;

import lombok.SneakyThrows;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static misc.rmse.Misc.rmseSimd;
import static misc.rmse.Misc.rootMeanSquaredError;

@Fork(value = 1, warmups = 0)
@Measurement(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class MiscBenchmark {
    double[] expected;
    double[] actual;

    @SneakyThrows
    public static void main(String[] args) {
        Main.main(new String[]{MethodHandles.lookup().lookupClass().getName() + ".*"});
    }

    @Setup
    public void setup() {
        Random random = new Random();
        expected = random.doubles().limit(12345).toArray();
        actual = Arrays.stream(expected).map(d -> d + random.nextDouble(0, 5)).toArray();
    }

    @Benchmark
    public void rmseBench(Blackhole blackhole) {
        blackhole.consume(rootMeanSquaredError(actual, expected));
    }

    @Benchmark
    public void rmseSimdBench(Blackhole blackhole) {
        blackhole.consume(rmseSimd(actual, expected));
    }
}
