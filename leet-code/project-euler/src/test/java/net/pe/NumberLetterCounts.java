package net.pe;

public class NumberLetterCounts {

    public static void main(String[] args) {
        int result = calculateLetterCounts();
        System.out.println(result);
    }

    private static int calculateLetterCounts() {
        int sum = 0;

        // Count for numbers 1 to 9
        sum += "onetwothreefourfivesixseveneightnine".length();

        // Count for numbers 10 to 19
        sum += "teneleventwelvethirteenfourteenfifteensixteenseventeeneighteennineteen".length();

        // Count for multiples of 10 (20, 30, ..., 90)
        sum += "twentythirtyfortyfiftysixtyseventyeightyninety".length() * 10;

        // Count for hundreds (100, 200, ..., 900)
        sum += "onehundredtwohundredthreehundredfourhundredfivehundredsixhundredsevenhundredeighthundredninehundred".length() * 100;

        // Count for "and" in numbers greater than 100
        sum += "and".length() * 99 * 9;

        // Count for "onethousand"
        sum += "onethousand".length();

        return sum;
    }
}
