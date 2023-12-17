package side;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;

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
}
