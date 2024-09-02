package side.y2024;

import lombok.ToString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * todo learn whatever im supposed to learn here
 * <p>
 * maybe <a href="https://www.geeksforgeeks.org/interval-tree/">https://www.geeksforgeeks.org/interval-tree/</a> ?
 */
@SuppressWarnings("NewClassNamingConvention")
class Jan19_2024 {

    long arrayManipulation(int n, List<List<Integer>> queries) {
        long[] array = new long[n];

        for (List<Integer> query : queries) {
            for (int i = query.get(0) - 1; i < query.get(1); i++) {
                array[i] += query.get(2);
            }
        }

        return LongStream.of(array).max().getAsLong();
    }

    @ParameterizedTest
    @CsvSource({
            "10,10,'1,5,3;4,8,7;6,9,1'",
    })
    void test_arrayManipulation(long expect, int size, String queriesString) {
        var queries = Arrays.stream(queriesString.split(";"))
                .map(s -> Arrays.stream(s.split(",")).map(Integer::parseInt).toList())
                .toList();

        assertEquals(expect, arrayManipulation(size, queries));
    }

    long arrayManipulation2(int n, List<List<Integer>> queries) {
        Node start = new Node();
        start.start = 0;
        start.end = n;
        start.value = 0;
        for (List<Integer> query : queries) {
            Node next = new Node();
            next.start = query.get(0);
            next.end = query.get(1);

            start.insert(next);
        }

        throw new UnsupportedOperationException();
    }

    @Disabled("not implemented yet")
    @ParameterizedTest
    @CsvSource({
            "10,10,'1,5,3;4,8,7;6,9,1'",
    })
    void test_arrayManipulation2(long expect, int size, String queriesString) {
        var queries = Arrays.stream(queriesString.split(";"))
                .map(s -> Arrays.stream(s.split(",")).map(Integer::parseInt).toList())
                .toList();

        assertEquals(expect, arrayManipulation2(size, queries));
    }

    @SuppressWarnings("JUnitMalformedDeclaration")
    @ToString
    static class Node {
        int start, end;
        long value;
        Node left, right;

        public void insert(Node next) {
            Node left = this;
            while (next.end > left.end && left.right != null) {
                left = left.right;
            }

            // left.right = next;

            Node right = this;
            while (next.start < right.start && right.left != null) {
                right = right.left;
            }

            // right.right = next;

            if (left == right) {
                //noinspection RedundantExplicitVariableType,UnnecessaryLocalVariable
                Node found = left;
                if (found.start < next.start) {
                    found.right = next;
                } else {
                    found.left = next;
                }
            }

            System.out.println();
        }

        @Test
        void test_insert() {
            var n = new Node();
            n.start = 3;
            n.end = 5;

            var next = new Node();
            next.start = 8;
            next.end = 10;
            n.insert(next);

            assertSame(n.right, next);
        }

        @Test
        void test_insert1() {
            var n = new Node();
            n.start = 8;
            n.end = 10;

            var next = new Node();
            next.start = 3;
            next.end = 5;
            n.insert(next);

            assertSame(n.left, next);
        }

    }
}
