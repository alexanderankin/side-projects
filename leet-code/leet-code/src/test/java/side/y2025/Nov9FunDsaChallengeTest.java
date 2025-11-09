package side.y2025;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Nov9FunDsaChallengeTest {
    private static void assertDsWorks(String opsString, String expectedString, Ds ds) {
        for (String opString : opsString.split(",")) {
            var opParts = opString.trim().split(" ");
            switch (opParts[0].trim()) {
                case "s" -> ds.set(Integer.parseInt(opParts[1]), Boolean.parseBoolean(opParts[2]));
                case "c" -> ds.clear();
                default -> throw new UnsupportedOperationException(opString);
            }
        }
        var expectedBooleanList = Arrays.stream(expectedString.trim().split(",")).map(String::trim).map(Boolean::parseBoolean).toList();
        var expectedBooleanArray = new boolean[expectedBooleanList.size()];
        for (int i = 0; i < expectedBooleanList.size(); i++) {
            expectedBooleanArray[i] = expectedBooleanList.get(i);
        }
        assertThat(expectedBooleanArray, equalTo(ds.getAllValues()));
    }

    @ParameterizedTest
    @CsvSource({
            "2, 's 0 true, s 1 true, c, s 0 true', 'true, false'",
    })
    void test(int size, String opsString, String expectedString) {
        var ds = Ds.init(size);
        assertDsWorks(opsString, expectedString, ds);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 's 0 true, s 1 true, c, s 0 true', 'true, false'",
    })
    void testAdvanced(int size, String opsString, String expectedString) {
        var ds = AdvancedDs.init(size);
        assertDsWorks(opsString, expectedString, ds);
    }

    /**
     * Fun DSA challenge.
     * <p>
     * Create a data structure with the following operations:
     * <p>
     * init(n): Create, O(n)
     * <p>
     * set(i, b): Set the value at index ‘i’ to a Boolean value ‘b’, i < n, O(1)
     * <p>
     * get(i): Get the Boolean value at index ‘i’, i < n, O(1)
     * <p>
     * clear(): Set all values to ‘false’, O(1)
     */
    @Data
    @Accessors(chain = true)
    static class Ds {
        @ToString.Exclude
        final boolean[] rawValues;
        @ToString.Exclude
        final long[] rawValuesSetTimes;
        long lastCleared;

        static Ds init(int n) {
            return new Ds(new boolean[n], new long[n])
                    .setLastCleared(System.nanoTime());
        }

        @ToString.Include
        int valuesSize() {
            return rawValues == null ? 0 : rawValues.length;
        }

        void set(int i, boolean b) {
            rawValues[i] = b;
            rawValuesSetTimes[i] = System.nanoTime();
        }

        boolean get(int i) {
            if (rawValuesSetTimes[i] <= lastCleared) {
                return false;
            }
            return rawValues[i];
        }

        boolean[] getAllValues() {
            boolean[] result = new boolean[rawValues.length];
            for (int i = 0; i < rawValues.length; i++) {
                result[i] = get(i);
            }
            return result;
        }

        void clear() {
            lastCleared = System.nanoTime();
        }
    }


    /**
     * Fun DSA challenge.
     * <p>
     * Create a data structure with the following operations:
     * <p>
     * init(n): Create, O(n)
     * <p>
     * set(i, b): Set the value at index ‘i’ to a Boolean value ‘b’, i < n, O(1)
     * <p>
     * get(i): Get the Boolean value at index ‘i’, i < n, O(1)
     * <p>
     * clear(): Set all values to ‘false’, O(1)
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    static class AdvancedDs extends Ds {
        @ToString.Exclude
        final boolean[] rawValues;
        @ToString.Exclude
        final long[] rawValuesSetTimes;
        final AtomicLong lastClearedAtomic = new AtomicLong();

        private AdvancedDs(boolean[] rawValues, long[] rawValuesSetTimes) {
            super(null, null);
            this.rawValues = rawValues;
            this.rawValuesSetTimes = rawValuesSetTimes;
        }

        static AdvancedDs init(int n) {
            return new AdvancedDs(new boolean[n], new long[n]);
        }

        @ToString.Include
        int valuesSize() {
            return rawValues == null ? 0 : rawValues.length;
        }

        void set(int i, boolean b) {
            rawValues[i] = b;
            rawValuesSetTimes[i] = this.lastClearedAtomic.get();
        }

        boolean get(int i) {
            if (rawValuesSetTimes[i] < lastClearedAtomic.get()) {
                return false;
            }
            return rawValues[i];
        }

        boolean[] getAllValues() {
            long lastCleared = this.lastClearedAtomic.get();
            boolean[] result = new boolean[rawValues.length];
            for (int i = 0; i < rawValues.length; i++) {
                if (rawValuesSetTimes[i] >= lastCleared) {
                    result[i] = rawValues[i];
                }
            }
            return result;
        }

        void clear() {
            lastClearedAtomic.getAndUpdate(prev -> {
                long next = prev + 1;

                if (next < prev) {
                    Arrays.fill(rawValues, false);
                    Arrays.fill(rawValuesSetTimes, 0);
                    return 0;
                }

                return next;
            });
        }
    }
}
