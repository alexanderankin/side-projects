package dsa;

import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// https://medium.com/techie-delight/binary-search-tree-bst-practice-problems-and-interview-questions-ea13a6731098
class BstCrashCourseTest {
    static List<List<Node>> insertKeyIntoBst_cases() {
        return List.of(
                Arrays.asList(
                        null,
                        new Node().setData(1),
                        new Node().setData(1)
                ),
                Arrays.asList(
                        new Node().setData(15)
                                .setLeft(new Node().setData(10)
                                        .setLeft(new Node().setData(8))
                                        .setRight(new Node().setData(12)))
                                .setRight(new Node().setData(20)
                                        .setRight(new Node().setData(25))),
                        new Node().setData(16),
                        new Node().setData(15)
                                .setLeft(new Node().setData(10)
                                        .setLeft(new Node().setData(8))
                                        .setRight(new Node().setData(12)))
                                .setRight(new Node().setData(20)
                                        .setLeft(new Node().setData(16))
                                        .setRight(new Node().setData(25)))
                )
        );
    }

    static List<List<Node>> searchKeyInBst_cases() {
        return List.of(
                Arrays.asList(
                        new Node().setData(15)
                                .setLeft(new Node().setData(10)
                                        .setLeft(new Node().setData(8))
                                        .setRight(new Node().setData(12)))
                                .setRight(new Node().setData(20)
                                        .setLeft(new Node().setData(16))
                                        .setRight(new Node().setData(25))),
                        new Node().setData(25),
                        new Node().setData(1)
                ),
                Arrays.asList(
                        new Node().setData(15)
                                .setLeft(new Node().setData(10)
                                        .setLeft(new Node().setData(8))
                                        .setRight(new Node().setData(12)))
                                .setRight(new Node().setData(20)
                                        .setLeft(new Node().setData(16))
                                        .setRight(new Node().setData(25))),
                        new Node().setData(5),
                        new Node().setData(0)
                )
        );
    }

    /**
     * Given the root of a binary search tree (BST) and an integer k, insert k into the
     * BST. The solution should not rearrange the existing tree nodes and insert a new
     * node with the given key at its correct position in BST.
     *
     * <pre>
     * Input: Below BST, k = 16
     *
     *           15
     *         /    \
     *        /      \
     *       /        \
     *      10        20
     *     /  \         \
     *    /    \         \
     *   8     12        25
     *
     * Output:
     *
     *           15
     *         /    \
     *        /      \
     *       /        \
     *      10        20
     *     /  \      /  \
     *    /    \    /    \
     *   8     12  16    25
     * </pre>
     * You may assume that the key does not exist in the BST.
     */
    Node insertKeyIntoBst(Node root, int k) {
        if (root == null) return new Node(k);
        Node prev = null, tmp = root;
        while (tmp != null) {
            prev = tmp;
            if (tmp.data > k) {
                tmp = tmp.left;
            } else {
                tmp = tmp.right;
            }
        }

        Node newNode = new Node(k);
        if (prev.data < k) prev.right = newNode;
        else prev.left = newNode;

        return root;
    }

    @ParameterizedTest
    @MethodSource("insertKeyIntoBst_cases")
    void test(List<Node> testCase) {
        assertThat(insertKeyIntoBst(testCase.getFirst(), testCase.get(1).data), is(testCase.getLast()));
    }

    /**
     * Given the root of a binary search tree (BST) and a key, search for the node with
     * that key in the BST.
     * <p>
     * For example, consider the following BST.
     *
     * <pre>
     *           15
     *         /    \
     *        /      \
     *       /        \
     *      10        20
     *     /  \      /  \
     *    /    \    /    \
     *   8     12  16    25
     * </pre>
     * Input: key = 25
     * Output: true
     * <p>
     * Input: key = 5
     * Output: false
     */
    boolean searchKeyInBst(Node root, int key) {
        while (root != null) {
            if (root.data == key) {
                return true;
            } else if (root.data > key) {
                root = root.left;
            } else /* if (root.data < key) */ {
                root = root.right;
            }
        }
        return false;
    }

    @ParameterizedTest
    @MethodSource("searchKeyInBst_cases")
    void test_searchKeyInBst(List<Node> testCase) {
        assertThat(searchKeyInBst(testCase.getFirst(), testCase.get(1).data), is(testCase.getLast().data != 0));
    }

    @Data
    @Accessors(chain = true)
    static class Node {
        int data;         // data field
        Node left, right; // pointer to the left and right child

        public Node(int k) {
            data = k;
        }

        public Node() {
        }
    }

    @Nested
    class UtilTests {
        static List<PrintTestCase> print_cases() {
            return List.of(
                    new PrintTestCase(
                            new Node().setData(1),
                            """
                                    1
                                    """
                    ),
                    new PrintTestCase(
                            new Node().setData(2).setLeft(new Node().setData(1)),
                            """
                                       2
                                      /
                                     /
                                    1
                                    """
                    ),
                    new PrintTestCase(
                            new Node().setData(15)
                                    .setLeft(new Node().setData(10)
                                            .setLeft(new Node().setData(8))
                                            .setRight(new Node().setData(12)))
                                    .setRight(new Node().setData(20)
                                            .setLeft(new Node().setData(16))
                                            .setRight(new Node().setData(25))),
                            """
                                            15
                                          /    \\
                                         /      \\
                                        /        \\
                                       10        20
                                      /  \\      /  \\
                                     /    \\    /    \\
                                    8     12  16    25
                                    """
                    ),
                    new PrintTestCase(
                            new Node().setData(15)
                                    .setLeft(new Node().setData(10)
                                            .setLeft(new Node().setData(8))
                                            .setRight(new Node().setData(12)))
                                    .setRight(new Node().setData(20)
                                            .setLeft(new Node().setData(16))
                                            .setRight(new Node().setData(25))),
                            """
                                            15
                                          /    \\
                                         /      \\
                                        /        \\
                                       10        20
                                      /  \\      /  \\
                                     /    \\    /    \\
                                    8     12  16    25
                                    """
                    )
            );
        }

        int depth(Node node) {
            int maxDepth = 0;
            Node cur = node;
            Deque<Node> stack = new ArrayDeque<>();
            Deque<Integer> depths = new ArrayDeque<>();
            int depth = 0;
            while (cur != null || !stack.isEmpty()) {
                while (cur != null) {
                    stack.addLast(cur);
                    depths.add(depth);
                    maxDepth = Math.max(maxDepth, depth);
                    cur = cur.left;
                    depth++;
                }
                cur = stack.removeLast();
                depth = depths.removeLast();
                assert !Integer.TYPE.isInstance(cur.data);
                cur = cur.right;
                depth++;
            }
            return maxDepth;
        }

        int depthToHeight(int depth) {
            return -1;
        }

        @Disabled("unclear purpose")
        @ParameterizedTest
        @CsvSource({
                "0,1",
                "1,4",
                "2,8",
        })
        void test_depthToHeight(int depth, int ex) {
            assertThat(depthToHeight(depth), is(ex));
        }

        @ParameterizedTest
        @MethodSource("print_cases")
        void test_printing(PrintTestCase tc) {
            System.out.println(tc.string.split("\n").length + ", " + depth(tc.node));
        }

        record PrintTestCase(Node node, String string) {
        }
    }
}
