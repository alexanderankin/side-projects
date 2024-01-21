package org.example.jmh;

import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 0)
@Measurement(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TestBenchmark {
    @SneakyThrows
    public static void main(String[] args) {
        // org.openjdk.jmh.Main.main(new String[]{"-h"});
        org.openjdk.jmh.Main.main(new String[]{MethodHandles.lookup().lookupClass().getName() + ".*"});
        // System.out.println(MethodHandles.lookup().lookupClass().getName());
    }

    @Benchmark
    public void repeat(Blackhole blackhole) {
        for (int i = 0; i < 100_000; i++) {
            blackhole.consume("abc".repeat(50));
        }
    }

    @SneakyThrows
    @Benchmark
    public void joiner(Blackhole blackhole) {
        StringJoiner stringJoiner = new StringJoiner("");
        Field size = StringJoiner.class.getDeclaredField("size");
        size.setAccessible(true);
        for (int i = 0; i < 100_000; i++) {
            for (int j = 0; j < 50; j++) {
                stringJoiner.add("abc");
            }
            if (blackhole != null) blackhole.consume(stringJoiner.toString());
            size.set(stringJoiner, 0);
        }
    }
}
