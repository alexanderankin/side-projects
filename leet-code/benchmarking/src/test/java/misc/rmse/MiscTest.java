package misc.rmse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static misc.rmse.Misc.rmseSimd;
import static misc.rmse.Misc.rootMeanSquaredError;

class MiscTest {
    @ParameterizedTest
    @CsvSource({
            "'1,2,3,4,5', '1.6,2.5,2.9,3,4.1', 0.697", // https://www.askpython.com/python/examples/rmse-root-mean-square-error
            "'0,0,0,0', '0,0,0,0', 0.0",
            "'2,8', '0,10', 2.0",
            "'3,3,3', '0,0,0', 3.0",
            "'2,4,6,8', '1,2,3,4', 2.738",
            "'-1,-2,-3,-4', '1,2,3,4', 5.477",
            "'1.1,2.2,3.3,4.4', '1.1,2.2,3.3,4.4', 0.0",
    })
    void test_rootMeanSquaredError(String actualNums, String expectedNums, double expected) {
        Assertions.assertEquals(expected, rootMeanSquaredError(parseList(actualNums), parseList(expectedNums)), 0.1);
    }

    double[] parseList(String input) {
        return Arrays.stream(input.split(",")).mapToDouble(Double::parseDouble).toArray();
    }

    @ParameterizedTest
    @CsvSource({
            "'1,2,3,4,5', '1.6,2.5,2.9,3,4.1', 0.697", // https://www.askpython.com/python/examples/rmse-root-mean-square-error
            "'0,0,0,0', '0,0,0,0', 0.0",
            "'2,8,2,12,2,8,2,12', '0,10,0,10,0,10,0,10', 2.0",
    })
    void test_rmseSimd(String actualNums, String expectedNums, double expected) {
        Assertions.assertEquals(expected, rmseSimd(parseList(actualNums), parseList(expectedNums)), 0.1);
    }
}
