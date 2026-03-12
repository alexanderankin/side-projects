package side.y2026;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Mar11Test {
    /**
     * given an input string of (all but one) numbers 1 to n,
     * stringified and concatenated,
     * find which one is missing.
     *
     * @param n     maximum number in the concatenation
     * @param input concatenated numbers 1 to n (inclusive)
     * @return the missing number
     */
    int findMissing(int n, String input) {
        for (int i = 0; i < n; i++) {
            int candidate = i + 1;
            var candidateString = String.valueOf(candidate);
            if (!input.contains(candidateString))
                return candidate;
        }
        return -1;
    }

    @ParameterizedTest
    @CsvSource({
            "10, 1098253471, 6",
    })
    void test(int n, String input, int expected) {
        assertEquals(expected, findMissing(n, input));
    }
}
