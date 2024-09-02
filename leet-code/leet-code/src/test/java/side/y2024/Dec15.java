package side.y2024;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SuppressWarnings("NewClassNamingConvention")
class Dec15 {
    /**
     * #739
     * <p>
     * Given an array of integers temperatures represents the daily temperatures,
     * return an array answer such that answer[i] is the
     * number of days you have to wait after the ith day to get a warmer temperature.
     * If there is no future day for which this is possible, keep answer[i] == 0 instead.
     * <p>
     * constraints:
     * 1 <= {@code temperatures.length} <= 10^5
     * 30 <= temperatures[i] <= 100
     *
     * @param temperatures input
     * @return num of days to wait for each day
     */
    int[] dailyTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length];

        // we are storing the number and its index, 2 spaces each "item"
        var stack = new java.util.PriorityQueue<ComparableDailyTemp>();

        for (int i = 0; i < temperatures.length; i++) {
            var thisI = temperatures[i];
            var nextI = i + 1 == temperatures.length ? 0 : temperatures[i + 1];

            if (nextI > thisI) {
                result[i] = 1;

                while (!stack.isEmpty()) {
                    var next = stack.peek();
                    if (next.value >= nextI) break;
                    stack.remove();
                    result[next.i] = i - next.i  + 1;
                }
            } else {
                var thing = new ComparableDailyTemp();
                thing.i = i;
                thing.value = thisI;
                stack.add(thing);
            }
        }
        return result;
    }

    static class ComparableDailyTemp implements Comparable<ComparableDailyTemp> {
        int i;
        int value;

        @Override
        public int compareTo(ComparableDailyTemp o) {
            return Integer.compare(value, o.value);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "'34,80,80,34,34,80,80,80,80,34', '1,0,0,2,1,0,0,0,0,0'",
            "'89,62,70,58,47,47,46,76,100,70', '8,1,5,4,3,2,1,1,0,0'",
            "'73,74,75,71,69,72,76,73', '1,1,4,2,1,1,0,0'",
            "'30,40,50,60', '1,1,1,0'",
            "'30,60,90', '1,1,0'",
    })
    void test_dailyTemperatures(String input, String expected) {
        int[] inputArray = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] expectedArray = Arrays.stream(expected.split(",")).mapToInt(Integer::parseInt).toArray();

        // System.out.println(Arrays.toString(inputArray));
        // System.out.println(Arrays.toString(expectedArray));
        System.out.println(Arrays.toString(dailyTemperatures(inputArray)));
        assertArrayEquals(expectedArray, dailyTemperatures(inputArray));
    }

}
