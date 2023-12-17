package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NewClassNamingConvention")
class Dec17 {
    /**
     * #901
     * <p>
     * Design an algorithm that collects daily price quotes for some stock and returns the span of that stock's price
     * for the current day.
     * <p>
     * The span of the stock's price in one day is the maximum number of consecutive days (starting from that day and
     * going backward) for which the stock price was less than or equal to the price of that day.
     * <p>
     * For example, if the prices of the stock in the last four days is [7,2,1,2] and the price of the stock today is 2,
     * then the span of today is 4 because starting from today, the price of the stock was less than or equal 2 for 4
     * consecutive days. Also, if the prices of the stock in the last four days is [7,34,1,2] and the price of the stock
     * today is 8, then the span of today is 3 because starting from today, the price of the stock was less than or
     * equal 8 for 3 consecutive days. Implement the StockSpanner class:
     * <p>
     * StockSpanner() Initializes the object of the class.
     * int next(int price) Returns the span of the stock's price given that today's price is price.
     */
    static class StockSpanner {
        ArrayList<Integer> data = new ArrayList<>();

        public StockSpanner() {
        }

        public int next(int price) {
            data.add(price);
            int j = 0;
            for (int i = data.size() - 1; i >= 0; i--) {
                Integer d = data.get(i);
                if (d > price) break;
                j += 1;
            }
            return j;
        }
    }

    @ParameterizedTest
    @CsvSource({
            "'7,2,1,2,2', '1,1,1,3,4'",
            "'7,34,1,2,8', '1,2,1,2,3'",
            "'100,80,60,70,60,75,85', '1,1,1,2,1,4,6'",
    })
    void test_stockSpanner(String input, String expected) {
        int[] inputArray = Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] expectedArray = Arrays.stream(expected.split(",")).mapToInt(Integer::parseInt).toArray();

        var spanner = new StockSpanner();
        for (int i = 0; i < inputArray.length; i++) {
            int ii = inputArray[i];
            assertEquals(expectedArray[i], spanner.next(ii));
        }
    }

    /**
     * #496
     * <p>
     * The next greater element of some element {@code x} in an array is the first greater element that is to the right
     * of {@code x} in the same array.
     * <p>
     * You are given two distinct 0-indexed integer arrays {@code nums1} and {@code nums2}, where {@code nums1} is a
     * subset of {@code nums2}.
     * <p>
     * For each {@code 0 <= i < nums1.length}, find the index {@code j} such that {@code nums1[i] == nums2[j]} and
     * determine the next greater element of {@code nums2[j]} in {@code nums2}. If there is no next greater element,
     * then the answer for this query is {@code -1}.
     * <p>
     * Return an array {@code ans} of length {@code nums1.length} such that {@code ans[i]} is the next greater element
     * as described above.
     */
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        int[] result = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            int n1 = nums1[i];

            boolean found = false;
            int j = 0;
            for (; j < nums2.length; j++) {
                int n2 = nums2[j];
                if (n2 == n1) break;
            }
            for (; j < nums2.length; j++) {
                int n2 = nums2[j];
                if (n2 > n1) {
                    found = true;
                    result[i] = n2;
                    break;
                }
            }
            if (!found) {
                result[i] = -1;
            }
        }
        return result;
    }

    @ParameterizedTest
    @CsvSource({
            "'4,1,2', '1,3,4,2', '-1,3,-1'",
            "'2,4', '1,2,3,4', '3,-1'",
    })
    void test_nextGreaterElement(String nums1String, String nums2String, String expectedString) {
        int[] nums1 = Arrays.stream(nums1String.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] nums2 = Arrays.stream(nums2String.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] expected = Arrays.stream(expectedString.split(",")).mapToInt(Integer::parseInt).toArray();

        System.out.println(Arrays.toString(nextGreaterElement(nums1, nums2)));
        assertArrayEquals(expected, nextGreaterElement(nums1, nums2));
    }

    // # 14
    String longestCommonPrefix(String[] strs) {
        // too memory hungry for top scoring?
        // int minLength = Arrays.stream(strs).mapToInt(String::length).min().orElseThrow();
        int minLength = Integer.MAX_VALUE;
        for (String str : strs) {
            if (str.length() < minLength) {
                minLength = str.length();
            }
        }

        int i = 0;
        outer:
        for (; i < minLength; i++) {
            char ref = strs[0].charAt(i);
            for (String str : strs) {
                if (str.charAt(i) != ref) break outer;
            }
        }
        return strs[0].substring(0, i);
    }

    @ParameterizedTest
    @CsvSource({
            "fl, 'flower,flow,flight'",
            "'', 'dog,racecar,car'",
    })
    void test_longestCommonPrefix(String expected, String example) {
        String[] input = example.split(",");
        System.out.println(Arrays.toString(input));
        assertEquals(expected, longestCommonPrefix(input));
    }

    /**
     * #15
     * <p>
     * Given an integer array nums,
     * return all the triplets [nums[i], nums[j], nums[k]]
     * such that i != j, i != k, and j != k,
     * and nums[i] + nums[j] + nums[k] == 0.
     * <p>
     * Notice that the solution set must not contain duplicate triplets.
     */
    List<List<Integer>> threeSum(int[] nums) {
        return new AbstractList<List<Integer>>() {
            List<List<Integer>> data;

            @Override
            public List<Integer> get(int index) {
                init();
                return data.get(index);
            }

            @Override
            public int size() {
                init();
                return data.size();
            }

            void init() {
                if (data != null) return;
                data = Dec17.this.calculateThreeSum(nums);
            }
        };
    }

    List<List<Integer>> calculateThreeSum(int[] nums) {
        Arrays.sort(nums);
        var ans = new ArrayList<List<Integer>>();

        for (int i = 0; i < nums.length - 2; i++) {
            // remove the need for set:
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int k = nums.length - 1;
            for (int j = i + 1; j < k; ) {
                var sum = nums[i] + nums[j] + nums[k];
                if (sum < 0) {
                    j++;
                } else if (sum > 0) {
                    k--;
                } else {
                    ans.add(List.of(nums[i], nums[j], nums[k]));
                    j++;
                    k--;

                    // remove the need for set:
                    while (k > j && nums[k] == nums[k + 1]) k--;
                    while (k > j && nums[j] == nums[j - 1]) j++;
                }
            }
        }

        return ans;
    }

    @ParameterizedTest
    @CsvSource({
            "'-1,0,1,2,-1,-4','[[-1,-1,2],[-1,0,1]]'",
            "'0,1,1','[]'",
            "'0,0,0','[[0,0,0]]'",
            "'-2,0,0,2,2','[[-2,0,2]]'",
    })
    void test_threeSum(String inputString, String expectedString) {
        int[] nums = Arrays.stream(inputString.split(",")).mapToInt(Integer::parseInt).toArray();

        expectedString = expectedString
                .replaceAll("^\\[\\[?", "")
                .replaceAll("]?]$", "");
        var expected = Arrays.stream(expectedString.split("],\\["))
                .map(s -> Arrays.stream(s.split(","))
                        .filter(Predicate.not(String::isEmpty))
                        .map(Integer::parseInt)
                        .toList())
                .filter(Predicate.not(List::isEmpty))
                .toList();

        assertEquals(expected, threeSum(nums));
    }

    /**
     * #16
     */
    int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int closest = Integer.MAX_VALUE + target;
        for (int i = 0; i < nums.length; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int j = i + 1, k = nums.length - 1;
            while (j < k) {
                var sum = nums[i] + nums[j] + nums[k];
                if (sum < target) {
                    var diff = Math.abs(target - sum);
                    if (diff < Math.abs(target - closest))
                        closest = sum;
                    j++;
                } else if (sum > target) {
                    var diff = Math.abs(target - sum);
                    if (diff < Math.abs(target - closest))
                        closest = sum;
                    k--;
                } else {
                    return sum;
                }
            }
        }
        return closest;
    }

    @ParameterizedTest
    @CsvSource({
            "'-1,2,1,-4', 1, 2",
            "'0,0,0', 1, 0",
    })
    void test_threeSumClosest(String numsString, int target, int expected) {
        int[] nums = Arrays.stream(numsString.split(",")).mapToInt(Integer::parseInt).toArray();

        assertEquals(expected, threeSumClosest(nums, target));

    }


}
