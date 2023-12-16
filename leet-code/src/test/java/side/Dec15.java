package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SuppressWarnings("NewClassNamingConvention")
class Dec15 {
    /**
     * Given an array of integers temperatures represents the daily temperatures,
     * return an array answer such that answer[i] is the
     * number of days you have to wait after the ith day to get a warmer temperature.
     * If there is no future day for which this is possible, keep answer[i] == 0 instead.
     *
     * @param temperatures input
     * @return num of days to wait for each day
     */
    int[] dailyTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length];
        for (int i = 0; i < temperatures.length; i++) {
            for (int j = i; j < temperatures.length; j++) {
                if (temperatures[j] > temperatures[i]) {
                    result[i] = j - i;
                    break;
                }
            }
        }
        return result;
    }

    @ParameterizedTest
    @CsvSource({
            "'73,74,75,71,69,72,76,73', '1,1,4,2,1,1,0,0'",
            "'30,40,50,60', '1,1,1,0'",
            "'30,60,90', '1,1,0'",
    })
    void test_dailyTemperatures(String input, String expected) {
        int[] inputArray = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] expectedArray = Arrays.stream(expected.split(",")).mapToInt(Integer::parseInt).toArray();

        // System.out.println(Arrays.toString(inputArray));
        // System.out.println(Arrays.toString(expectedArray));
        assertArrayEquals(expectedArray, dailyTemperatures(inputArray));
    }

}
