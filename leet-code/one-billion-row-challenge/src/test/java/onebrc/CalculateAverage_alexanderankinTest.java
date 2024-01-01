package onebrc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CalculateAverage_alexanderankinTest {

    @ParameterizedTest
    @CsvSource({
            "10,100",
            "1,10",
            "0,0",
            "'',0",
            "-0,0",
            "-0,0",
            "1.5,15",
            "1.0,10",
            "-1.7,-17",
    })
    void test(String inputString, int expected) {
        byte[] input = inputString.getBytes();
        int i = new CalculateAverage_alexanderankin().parseNumber(input, 0, input.length);
        assertEquals(expected, i);
    }

}
