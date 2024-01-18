package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
class Jan18_2024 {

    /**
     * Greater Is Better (Hackerrank)
     * <p>
     * Given a word, create a new word by swapping some or all of its characters.
     * This new word must meet two criteria:
     * <p>
     * It must be greater than the original word
     * <p>
     * It must be the smallest word that meets the first condition
     */
    String gb(String s) {
        var chars = s.toCharArray();
        var last = chars[chars.length - 1];
        for (int i = chars.length - 2; i >= 0; i--) {
            char c = chars[i];

            if (last > c) {
                return new String(swap(chars, i));
            }

            last = c;
        }
        return "no answer";
    }

    private char[] swap(char[] chars, int i) {
        int minIndex = i + 1;
        char c = chars[minIndex];
        for (int j = minIndex + 1; j < chars.length; j++) {
            char cc = chars[j];
            if (cc < c && cc > chars[i]) {
                c = cc;
                minIndex = j;
            }
        }

        int ii = minIndex;
        char tmp = chars[i];
        chars[i] = chars[ii];
        chars[ii] = tmp;
        Arrays.sort(chars, i + 1, chars.length);
        return chars;
    }

    @ParameterizedTest
    @CsvSource({
            // case 0
            "ab,ba",
            "bb,no answer",
            "hefg,hegf",
            "dhck,dhkc",
            "dkhc,hcdk",

            // case 1
            "lmno,lmon",
            "dcba,no answer",
            "dcbb,no answer",
            "abdc,acbd",
            "abcd,abdc",
            "fedcbabcd,fedcbabdc",
    })
    void test_gb(String input, String expected) {
        assertEquals(expected, gb(input));
    }
}
