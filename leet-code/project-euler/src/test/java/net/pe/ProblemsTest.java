package net.pe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemsTest {
    /**
     * If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3,5,6 and 9. The sum of these multiples is 23.
     * <p>
     * Find the sum of all the multiples of 3 or 5 below 1000.
     */
    int problemOne_naive(int upTo) {
        int result = 0;
        for (int i = 0; i < upTo; i++) {
            if (i % 5 == 0 || i % 3 == 0)
                result += i;
        }
        return result;
    }

    /**
     * @see #problemOne_naive(int)
     */
    int problemOne(int upTo) {
        var nums5 = (upTo - 1) / 5;
        var nums3 = (upTo - 1) / 3;
        var nums15 = (upTo - 1) / 15;

        var sum3 = (nums3 * (nums3 + 1) / 2) * 3;
        var sum5 = (nums5 * (nums5 + 1) / 2) * 5;
        var sum15 = (nums15 * (nums15 + 1) / 2) * 15;
        return sum3 + sum5 - sum15;
    }

    @ParameterizedTest
    @CsvSource({
            "10,23",
            "0,0",
            "3,0",
            "4,3",
            "6,8",
            "20,78",
            "1000,233168",
    })
    void test_problemOne(int upTo, int expected) {
        assertEquals(expected, problemOne_naive(upTo));
        assertEquals(expected, problemOne(upTo));
    }

    List<Integer> problemOneIntermediate_naive(int upTo) {
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i < upTo; i++) {
            if (i % 5 == 0 || i % 3 == 0)
                result.add(i);
        }
        return result;
    }

    List<Integer> problemOneIntermediate(int upTo) {
        var nums5 = (upTo - 1) / 5;
        var nums3 = (upTo - 1) / 3;

        var result3 = IntStream.rangeClosed(1, nums3).map(i -> i * 3).boxed().toList();
        var result5 = IntStream.rangeClosed(1, nums5).map(i -> i * 5).boxed().toList();
        return Stream.concat(result3.stream(), result5.stream()).distinct().sorted().toList();
    }

    @ParameterizedTest
    @CsvSource({
            "0,''",
            "3,''",
            "4,'3'",
            "6,'3,5'",
            "10,'3,5,6,9'",
            "20,'3,5,6,9,10,12,15,18'",
    })
    void test_p1_intermediate(int upTo, String expectedString) {
        List<Integer> expected = Arrays.stream(expectedString.split(",")).filter(Predicate.not(String::isEmpty)).map(Integer::parseInt).toList();

        assertEquals(expected, problemOneIntermediate_naive(upTo), "naive");
        assertEquals(expected, problemOneIntermediate(upTo), "efficient");
    }
}
