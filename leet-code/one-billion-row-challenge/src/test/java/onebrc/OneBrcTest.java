package onebrc;

import lombok.SneakyThrows;
import onebrc.ri.TestableCalculateAverage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.function.Supplier;

import static onebrc.OneBrcAssertions.assertSequencedMapEquals;
import static onebrc.OneBrcAssertions.assertSequencedMapNotEquals;

class OneBrcTest {
    @TempDir
    static Path tempDir;

    @SneakyThrows
    @BeforeAll
    static void populateTempDir() {
        extracted("/example.txt", "example.txt");
        extracted("/example-homepage.txt", "example-homepage.txt");
    }

    private static void extracted(String name, String file) throws IOException {
        Files.copy(Objects.requireNonNull(OneBrcTest.class.getResourceAsStream(name)), tempDir.resolve(file));
    }

    @Test
    void acceptanceTest() {

    }

    static class MapBuilder<K, V> implements Supplier<SequencedMap<K, V>> {
        private final SequencedMap<K, V> map;

        public MapBuilder(Supplier<SequencedMap<K, V>> ctor) {
            map = ctor.get();
        }

        public MapBuilder() {
            this(LinkedHashMap::new);
        }

        public MapBuilder<K, V> add(K k, V v) {
            map.put(k, v);
            return this;
        }

        public SequencedMap<K, V> build() {
            return map;
        }

        @Override
        public SequencedMap<K, V> get() {
            return build();
        }
    }

    @Nested
    class ReferenceImplementationTests {
        @SneakyThrows
        @Test
        void test_homePage() {
            Map<String, TestableCalculateAverage.ResultRow> actual =
                    TestableCalculateAverage.calculate(tempDir.resolve("example-homepage.txt").toString());

            System.out.println(actual);

            Map<String, TestableCalculateAverage.ResultRow> expected =
                    new MapBuilder<String, TestableCalculateAverage.ResultRow>()
                            .add("Abha", new TestableCalculateAverage.ResultRow(5.0, 18.0, 27.4))
                            .add("Abidjan", new TestableCalculateAverage.ResultRow(15.7, 26.0, 34.1))
                            .add("Abéché", new TestableCalculateAverage.ResultRow(12.1, 29.4, 35.6))
                            .add("Accra", new TestableCalculateAverage.ResultRow(14.7, 26.4, 33.1))
                            .add("Addis Ababa", new TestableCalculateAverage.ResultRow(2.1, 16.0, 24.3))
                            .add("Adelaide", new TestableCalculateAverage.ResultRow(4.1, 17.3, 29.7))
                            .add("Bulawayo", new TestableCalculateAverage.ResultRow(8.9, 8.9, 8.9))
                            .add("Cracow", new TestableCalculateAverage.ResultRow(12.6, 12.6, 12.6))
                            .add("Hamburg", new TestableCalculateAverage.ResultRow(12.0, 12.0, 12.0))
                            .add("Palembang", new TestableCalculateAverage.ResultRow(38.8, 38.8, 38.8))
                            .add("St. John's", new TestableCalculateAverage.ResultRow(15.2, 15.2, 15.2))
                            .build();

            assertSequencedMapEquals(expected, actual);
        }

        @SneakyThrows
        @Test
        void test_alphabetic() {
            assertSequencedMapEquals(new MapBuilder<String, TestableCalculateAverage.ResultRow>()
                            .add("A", new TestableCalculateAverage.ResultRow(1.0, 2.0, 3.0))
                            .add("B", new TestableCalculateAverage.ResultRow(2.0, 3.0, 4.0))
                            .add("C", new TestableCalculateAverage.ResultRow(5.0, 10.0, 15.0))
                            .build(),
                    TestableCalculateAverage.calculate(tempDir.resolve("example.txt").toString()));
            assertSequencedMapNotEquals(new MapBuilder<String, TestableCalculateAverage.ResultRow>(LinkedHashMap::new)
                            .add("A", new TestableCalculateAverage.ResultRow(1.0, 2.0, 3.0))
                            .add("C", new TestableCalculateAverage.ResultRow(5.0, 10.0, 15.0))
                            .add("B", new TestableCalculateAverage.ResultRow(2.0, 3.0, 4.0))
                            .build(),
                    TestableCalculateAverage.calculate(tempDir.resolve("example.txt").toString()));
        }
    }
}
