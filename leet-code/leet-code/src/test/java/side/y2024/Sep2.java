package side.y2024;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sep2Test {
    static int findMaxLength(int[] nums) {
        /*
            0101111010101010111111111101010101010
        */

        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        // minus one because numbers start with 0 (difference as length = compare with -1)
        map.put(0, -1);

        // "prefix-sum" concept - reduce to a count
        int count = 0;
        int max = 0;
        for (int i = 0; i < nums.length; i++) {
            int num = nums[i];
            int diff = num == 0 ? -1 : 1;
            count += diff;

            // don't track a count twice.
            if (!map.containsKey(count)) {
                // if it is the first time, track it
                map.put(count, i);
            } else {
                // max length of this is difference from the last time we saw it (tracked it)
                max = Math.max(max, i - map.get(count));
            }
        }
        return max;
    }

    @ParameterizedTest
    @CsvSource({
            "0,0",
            "1,0",
            "01,2",
            "010,2",
            "000,0",
            "111,0",
            "0101111010101010111111111101010101010,12",
    })
    void test(String inputString, int expected) {
        assertEquals(expected, findMaxLength(Arrays.stream(inputString.split("")).mapToInt(Integer::parseInt).toArray()));
    }
}
