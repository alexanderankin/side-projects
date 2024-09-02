package side.y2024;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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

    // hackerrank
    void kaprekarNumbers(int p, int q) {
        boolean found = false;
        for (int i = p; i <= q; i++) {
            if (isKap(i)) {
                if (found) System.out.print(" ");
                System.out.print(i);
                found = true;
            }
        }
        if (!found)
            System.out.println("INVALID RANGE");
    }

    boolean isKap(long n) {
        long sq = Math.multiplyExact(n, n);
        String sqs = String.valueOf(sq);
        String rs = sqs.substring(0, sqs.length() / 2);
        int r = rs.isEmpty() ? 0 : Integer.parseInt(rs, 10);
        String ls = sqs.substring(sqs.length() / 2);
        int l = ls.isEmpty() ? 0 : Integer.parseInt(ls, 10);
        return r + l == n;
    }

    @ParameterizedTest
    @CsvSource({
            "5,false",
            "9,true",
            "45,true",
    })
    void test_isKap(int n, boolean expect) {
        assertEquals(expect, isKap(n));
    }

    @ParameterizedTest
    @CsvSource({
            "1,100,'1 9 45 55 99'",
            "400,700,'INVALID RANGE'",
    })
    void test_kaprekarNumbers(int p, int q, String expected) {
        PrintStream out = System.out;
        try {
            var os = new ByteArrayOutputStream();
            System.setOut(new PrintStream(os));
            kaprekarNumbers(p, q);
            assertEquals(expected, os.toString().trim());
        } finally {
            System.setOut(out);
        }
    }

    /**
     * leet code #198
     */
    int houseRobber(int[] houses) {
        if (houses.length <= 2) {
            if (houses.length == 0) return 0;
            if (houses.length == 1) return houses[0];
            return Math.max(houses[0], houses[1]);
        }

        int[] robAmount = new int[houses.length];
        // Arrays.fill(robAmount, 0);

        robAmount[0] = houses[0];
        robAmount[1] = houses[1];
        int max = houses[0];

        for (int i = 2; i < houses.length; i++) {
            // int finalI = i;
            int maxRob = houses[i] + max;
            int notRob = robAmount[i - 1];
            robAmount[i] = Math.max(notRob, maxRob);
            max = Math.max(max, robAmount[i - 1]);
        }

        return Math.max(robAmount[robAmount.length - 1], robAmount[robAmount.length - 2]);
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2,3,1',4",
            "'2,7,9,3,1',12",
            "'2,1,1,2',4",
    })
    void test_houseRobber(String input, int expected) {
        var houses = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
        assertEquals(expected, houseRobber(houses));
    }
}
