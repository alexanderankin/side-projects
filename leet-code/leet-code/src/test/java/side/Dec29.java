package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
class Dec29 {
    /**
     * Given an array of distinct integers candidates and a target integer target, return
     * a list of all unique combinations of candidates where the chosen numbers sum to
     * target. You may return the combinations in any order.
     * <p>
     * The same number may be chosen from candidates an unlimited number of times. Two
     * combinations are unique if the frequency of at least one of the chosen numbers is
     * different.
     * <p>
     * The test cases are generated such that the number of unique combinations that sum
     * up to target is less than 150 combinations for the given input.
     */
    List<List<Integer>> combinationSum(int[] candidates, int target) {
        ArrayList<List<Integer>> results = new ArrayList<>();
        doCombinationSum(candidates, target, results, new ArrayList<>(), 0, 0);
        return results;
    }

    void doCombinationSum(int[] candidates,
                          int target,
                          List<List<Integer>> results, List<Integer> current,
                          int currentSum,
                          int currentIndex) {

        if (currentSum > target) return;
        if (currentSum == target) {
            results.add(new ArrayList<>(current));
            return;
        }

        for (int i = currentIndex; i < candidates.length; i++) {
            int candidate = candidates[i];
            current.add(candidate);
            currentSum += candidate;
            doCombinationSum(candidates, target, results, current, currentSum, i);
            current.removeLast();
            currentSum -= candidate;
        }
    }

    @ParameterizedTest
    @CsvSource({
            "'2,3,6,7',7,'[[2,2,3],[7]]'",
            "'2,3,5',8,'[[2,2,2,2],[2,3,3],[3,5]]'",
            "'2',1,'[]'",
    })
    void test_combinationSum(String candidatesStr, int target, String expectedString) {
        int[] candidates = Arrays.stream(candidatesStr.split(",")).mapToInt(Integer::parseInt).toArray();

        expectedString = expectedString
                .replaceAll("^\\[\\[?", "")
                .replaceAll("]?]$", "");
        List<List<Integer>> expected = Arrays.stream(expectedString.split("],\\["))
                .map(s -> Arrays.stream(s.split(","))
                        .filter(Predicate.not(String::isEmpty))
                        .map(Integer::parseInt)
                        .toList())
                .filter(Predicate.not(List::isEmpty))
                .toList();

        assertEquals(expected, combinationSum(candidates, target));
    }
}
