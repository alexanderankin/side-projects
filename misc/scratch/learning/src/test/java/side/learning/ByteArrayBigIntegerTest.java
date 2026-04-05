package side.learning;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ByteArrayBigIntegerTest {

    @ParameterizedTest
    @CsvSource({
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
    })
    void test_extendsNumber(int value) {
        assertThat(ByteArrayBigInteger.of(value).intValue(), is(value));
        assertThat(ByteArrayBigInteger.of(value).longValue(), is((long) value));
        assertThat(ByteArrayBigInteger.of(value).floatValue(), is((float) value));
        assertThat(ByteArrayBigInteger.of(value).doubleValue(), is((double) value));
    }

    @ParameterizedTest
    @CsvSource({
            "'30064771079', 7",
            "'30064771080', 8",
            "'133143986207', 31",
    })
    void test_largeIntValue(String decimalValue, int intValue) {
        assertThat(ByteArrayBigInteger.of(decimalValue, 10).intValue(), is(intValue));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 2", "0, 8", "0, 10", "0, 16",
            "1, 2", "1, 8", "1, 10", "1, 16",
            "2, 2", "2, 8", "2, 10", "2, 16",
            "3, 2", "3, 8", "3, 10", "3, 16",
            "4, 2", "4, 8", "4, 10", "4, 16",
            "5, 2", "5, 8", "5, 10", "5, 16",
            "6, 2", "6, 8", "6, 10", "6, 16",
            "7, 2", "7, 8", "7, 10", "7, 16",
            "8, 2", "8, 8", "8, 10", "8, 16",
            "9, 2", "9, 8", "9, 10", "9, 16",
            "10, 2", "10, 8", "10, 10", "10, 16",
            "11, 2", "11, 8", "11, 10", "11, 16",
            "12, 2", "12, 8", "12, 10", "12, 16",
            "13, 2", "13, 8", "13, 10", "13, 16",
            "14, 2", "14, 8", "14, 10", "14, 16",
            "15, 2", "15, 8", "15, 10", "15, 16",
            "16, 2", "16, 8", "16, 10", "16, 16",
            "17, 2", "17, 8", "17, 10", "17, 16",
            "18, 2", "18, 8", "18, 10", "18, 16",
            "19, 2", "19, 8", "19, 10", "19, 16",
    })
    void test_parseSerializeStringRadix(int testCase, int radix) {
        assertThat(ByteArrayBigInteger.of(testCase).toString(radix), is(Integer.toString(testCase, radix)));
    }

    @ParameterizedTest
    @CsvSource({
            "30064771079, '0000000000000000000000000000011100000000000000000000000000000111'",
            "133143986207, '0000000000000000000000000001111100000000000000000000000000011111'",
            "4393751544831, '0000000000000000000000111111111100000000000000000000001111111111'",
            "140733193420799, '0000000000000000011111111111111100000000000000000111111111111111'",
            "281470681808895, '0000000000000000111111111111111100000000000000001111111111111111'",
            "18446744043644780536, '1111111111111111111111111111100011111111111111111111111111111000'",
            "18446743940565565408, '1111111111111111111111111110000011111111111111111111111111100000'",
            "18446739679958006784, '1111111111111111111111000000000011111111111111111111110000000000'",
            "18446603340516130816, '1111111111111111100000000000000011111111111111111000000000000000'",
            "18446462603027742720, '1111111111111111000000000000000011111111111111110000000000000000'",
    })
    void test_parseSerializeStringRadixLarge(String decimalString, String binaryString) {
        assertThat(ByteArrayBigInteger.of(binaryString, 2).toString(10), is(decimalString));
    }
}
