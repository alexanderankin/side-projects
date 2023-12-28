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

    /**
     * Each new term in the Fibonacci sequence is generated by adding the previous two terms.
     * By starting with 1 and 2, the first 10 terms will be:1,2,3,5,8,13,21,34,55,89,...
     * <p>
     * By considering the terms in the Fibonacci sequence whose values do not exceed four million,
     * find the sum of the even-valued terms.
     */
    long problemTwo(int upTo) {
        long result = 0;
        int fib0 = 0;
        int fib1 = 1;
        while (true) {
            int fib = fib0 + fib1;
            if (fib > upTo) break;
            if (fib % 2 == 0)
                result += fib;
            fib0 = fib1;
            fib1 = fib;
        }
        return result;
    }

    @ParameterizedTest
    @CsvSource({
            "10,44",
            "4000000,4613732",
    })
    void test_problemTwo(int upTo, long expected) {
        assertEquals(expected, problemTwo(upTo));
    }

    long problemTwo(long input) {
        return primeFactors(input).stream().mapToLong(Long::longValue).reduce(0, Math::max);
    }

    @ParameterizedTest
    @CsvSource({
            "13195,29",
            "600851475143,6857",
    })
    void test_problemThree(long input, long expected) {
        assertEquals(expected, problemTwo(input));
    }

    // https://www.geeksforgeeks.org/java-program-for-efficiently-print-all-prime-factors-of-a-given-number/
    public List<Long> primeFactors(long n) {
        List<Long> primes = new ArrayList<>();
        // Print the number of 2s that divide n
        while (n % 2 == 0) {
            // System.out.print(2 + " ");
            primes.add((long) 2);
            n /= 2;
        }

        // n must be odd at this point.  So we can
        // skip one element (Note i = i +2)
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            // While i divides n, print i and divide n
            while (n % i == 0) {
                // System.out.print(i + " ");
                primes.add((long) i);
                n /= i;
            }
        }

        // This condition is to handle the case when
        // n is a prime number greater than 2
        if (n > 2)
            primes.add(n);
        return primes;
    }

    /**
     * A palindromic number reads the same both ways. The largest palindrome made from the product of two 2-digit
     * numbers is 9009=91×99.Find the largest palindrome made from the product of two 3-digit numbers.
     */
    long problemFour(int nDigits) {
        // equation: x1 * 10^4 + x2 * 10^3 + x2 * 10^2 + x1 * 10^1
        // sum x{1..n} = xi * 10^i + xi * 10^(n - i)
        // sum x{1..n} = xi(10^i + 10^(n - i))
        // sum = y * z
        // ok, naive solution:
        long start = (long) Math.pow(10, nDigits - 1);
        long end = 0;
        for (int i = 0; i < nDigits; i++) {
            end *= 10;
            end += 9;
        }

        long max = 0;

        long a = end;
        while (a >= start) {
            long b = end;
            while (b >= a) {
                long product = a * b;
                if (isPalindrome(product) && product > max)
                    max = product;
                b--;
            }
            a--;
        }
        return max;

        // we can apparently also optimize by using the 11 multiple fact

        // back to the good stuff, the incomprehensible euler stuff:
        // xy
        // P=1000x+100y+10y+x
        // P=1001x+110y
        // P=(91x+10y)11
        // xyz
        // P=100000x10000y1000z100z10yx
        // P=100001x10010y1100z
        // P=119091x910y100z
        // wxyz
        // P=10000000w+1000000x+100000y+10000z+1000z+100y+10x+w
        // P=10000001w+1000010x+100100y+11000z
        // P=(909091w+90910x+9100y+1000z)11
    }

    boolean isPalindrome(long num) {
        long reverse = 0;
        long copy = num;
        while (copy > 0) {
            reverse *= 10;
            reverse += copy % 10;
            copy /= 10;
        }
        return num == reverse;
    }

    @ParameterizedTest
    @CsvSource({
            "1,9",
            "2,9009",
            "3,906609",
            // "4,99000099",
            // "5,9966006699",
    })
    void test_problemFour(int size, long expected) {
        assertEquals(expected, problemFour(size));
    }

    @ParameterizedTest
    @CsvSource({
            "true,4",
            "true,44",
            "false,445",
            "true,5445",
            "false,1234",
    })
    void test_isPalindrome(boolean expected, int input) {
        assertEquals(expected, isPalindrome(input));
    }

    /**
     * smallest multiple
     */
    int problem5(int upTo) {
        int next = upTo * upTo;

        int limit = Integer.MAX_VALUE;
        while (limit-- > 0) {
            boolean even = true;
            for (int i = 1; i < upTo; i++) {
                if (next % i != 0) {
                    even = false;
                    break;
                }
            }

            if (even) return next;
            next += upTo;
        }
        return -1;
    }

    @ParameterizedTest
    @CsvSource({
            "10,2520",
            "20,232792560",
    })
    void test_problem5(int upTo, int expected) {
        assertEquals(expected, problem5(upTo));
    }
}
