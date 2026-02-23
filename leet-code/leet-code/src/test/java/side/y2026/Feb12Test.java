package side.y2026;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Feb12Test {
    @ParameterizedTest
    @CsvSource({
            "1,'One'",
            "12,'Twelve'",
            "123,'One Hundred Twenty Three'",
            "12345,'Twelve Thousand Three Hundred Forty Five'",
            "1234567,'One Million Two Hundred Thirty Four Thousand Five Hundred Sixty Seven'",
    })
    void test_273IntegerToEnglishWords(int input, String expected) {
        assertThat(integerToEnglishWords(input), is(expected));
    }

    String integerToEnglishWords(int input) {
        int[] digits = toDigits(input);
        return integerToEnglishWords(digits, 0, digits.length);
    }

    String integerToEnglishWords(int[] digits, int offset, int length) {
        if (length == 1)
            return Constants.digitNames[digits[offset]];
        if (length == 2) {
            var special = digits[offset + 1] == 1;
            if (special) {
                return Constants.elevenNames[digits[offset]];
            } else {
                return Constants.tensNames[digits[offset + 1]] + " " +
                        integerToEnglishWords(digits, offset, length - 1);
            }
        }
        if (length == 3) {
            return Constants.digitNames[digits[offset + 2]] + " Hundred " +
                    integerToEnglishWords(digits, offset, length - 1);
        }

        var amount = length % 3;
        var type = length / 3;

        if (amount == 0) {
            amount = 3;
            type -= 1;
        }

        return integerToEnglishWords(digits, offset + length - amount, amount) + " " +
                Constants.typeNames[type] + " " +
                integerToEnglishWords(digits, offset, length - amount);
    }

    private int[] toDigits(int input) {
        IntStream.Builder builder = IntStream.builder();
        while (input > 0) {
            builder.add(input % 10);
            input /= 10;
        }

        return builder.build().toArray();
    }

    interface Constants {
        String[] digitNames = "Zero One Two Three Four Five Six Seven Eight Nine".split(" ");
        String[] tensNames = "_ _ Twenty Thirty Forty Fifty Sixty Seventy Eighty Ninety".split(" ");
        String[] elevenNames = "Ten Eleven Twelve Thirteen Fourteen Fifteen Sixteen Seventeen Eighteen Nineteen".split(" ");
        String[] typeNames = "_ Thousand Million Billion Trillion Quadrillion".split(" ");
    }
}
