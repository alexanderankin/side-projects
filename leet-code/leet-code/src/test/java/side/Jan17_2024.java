package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
class Jan17_2024 {

    String countAndSay(int n) {
        String result = "1";
        for (int i = 2; i <= n; i++) {
            result = do_countAndSay(result);
        }
        return result;
    }

    private String do_countAndSay(String result) {
        StringBuilder sb = new StringBuilder();

        char[] charArray = result.toCharArray();
        int prevI = 0;
        char prev = charArray[prevI];
        for (int i = 1; i < charArray.length; i++) {
            char next = charArray[i];
            if (next != prev) {
                var diff = i - prevI;
                sb.append(diff);
                sb.append(prev);
                prevI = i;
            }

            prev = next;
        }

        var diff = charArray.length - prevI;
        sb.append(diff);
        sb.append(prev);

        return sb.toString();
    }

    @ParameterizedTest
    @CsvSource({
            "1,1",
            "2,11",
            "3,21",
            "4,1211",
            "5,111221",
            "6,312211",
            "7,13112221",
            "8,1113213211",
            "9,31131211131221",
            "10,13211311123113112211",
            "11,11131221133112132113212221",
            "12,3113112221232112111312211312113211",
    })
    void test(int n, String expect) {
        assertEquals(expect, countAndSay(n));
    }
}
