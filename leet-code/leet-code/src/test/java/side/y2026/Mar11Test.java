package side.y2026;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Mar11Test {
    static Stream<Arguments> testCases() {
        return Stream.of(
                // smallest case
                Arguments.of(1, "", 1),

                // simple single-digit ranges
                Arguments.of(5, "1235", 4),
                Arguments.of(5, "2345", 1),
                Arguments.of(5, "1234", 5),

                // provided example
                Arguments.of(10, "1098253471", 6),

                // missing in the middle
                Arguments.of(12, "123456789101112", -1), // full sequence

                // missing single digit before transition
                Arguments.of(12, "12356789101112", 4),

                // missing two-digit numbers
                Arguments.of(12, "1234567891112", 10),
                Arguments.of(12, "1234567891012", 11),

                // larger ranges
                Arguments.of(15, "123456789101112131415", -1),
                Arguments.of(15, "1234567891011121415", 13),

                // tricky substring overlap
                Arguments.of(20, "1234567891011121314151617181920", -1),
                Arguments.of(20, "123456789101112131415161718192", 20)
        );
    }

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
    @MethodSource("testCases")
    void test(int n, String input, int expected) {
        assertEquals(expected, findMissing(n, input));
    }

    /**
     * not obvious that it is better, just more clever
     *
     * @see #findMissing(int, String)
     */
    int trieIshFindMissing(int n, String input) {
        char[] charArray = input.toCharArray();

        Map<Character, List<Integer>> starts = HashMap.newHashMap(10);
        for (int i = 0; i < charArray.length; i++) {
            starts.computeIfAbsent(charArray[i], ignored -> new ArrayList<>()).add(i);
        }

        for (int i = 0; i < n; i++) {
            int candidate = i + 1;
            var candidateString = String.valueOf(candidate);
            var candidateChars = candidateString.toCharArray();
            var candidateFirstChar = candidateChars[0];

            boolean candidateFound = false;
            var startsValue = starts.get(candidateFirstChar);
            if (startsValue != null)
                for (var start : startsValue) {
                    boolean startMatch;
                    try {
                        startMatch = Arrays.equals(candidateChars, 0, candidateChars.length, charArray, start, start + candidateChars.length);
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                        startMatch = false;
                    }

                    if (startMatch) {
                        candidateFound = true;
                        break;
                    }
                }

            if (!candidateFound)
                return candidate;
        }
        return -1;
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testTrieIsh(int n, String input, int expected) {
        assertEquals(expected, trieIshFindMissing(n, input));
    }
}
