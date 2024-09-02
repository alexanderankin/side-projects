package side.y2024;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("NewClassNamingConvention")
public class Jul10_2024 {
    @SneakyThrows
    @Test
    void test() {
        Path input = Files.createTempFile(getClass().getSimpleName(), ".txt");
        Path output = Files.createTempFile(getClass().getSimpleName(), ".txt");
        input.toFile().deleteOnExit();
        output.toFile().deleteOnExit();

        Files.writeString(input, """
                1
                4
                1
                3
                4
                10
                5
                """);

        Solution.main(input.toString(), output.toString());

        String s = Files.readString(output);
        assertThat(s.trim(), is("1 3 4 5 10"));
    }
}

// @formatter:off
@SuppressWarnings("ALL")
class Solution {

    static class DoublyLinkedListNode {
        public int data;
        public DoublyLinkedListNode next;
        public DoublyLinkedListNode prev;

        public DoublyLinkedListNode(int nodeData) {
            this.data = nodeData;
            this.next = null;
            this.prev = null;
        }
    }

    static class DoublyLinkedList {
        public DoublyLinkedListNode head;
        public DoublyLinkedListNode tail;

        public DoublyLinkedList() {
            this.head = null;
            this.tail = null;
        }

        public void insertNode(int nodeData) {
            DoublyLinkedListNode node = new DoublyLinkedListNode(nodeData);

            if (this.head == null) {
                this.head = node;
            } else {
                this.tail.next = node;
                node.prev = this.tail;
            }

            this.tail = node;
        }
    }

    public static void printDoublyLinkedList(DoublyLinkedListNode node, String sep, BufferedWriter bufferedWriter) throws IOException {
        while (node != null) {
            bufferedWriter.write(String.valueOf(node.data));

            node = node.next;

            if (node != null) {
                bufferedWriter.write(sep);
            }
        }
    }



    /*
     * Complete the 'sortedInsert' function below.
     *
     * The function is expected to return an INTEGER_DOUBLY_LINKED_LIST.
     * The function accepts following parameters:
     *  1. INTEGER_DOUBLY_LINKED_LIST llist
     *  2. INTEGER data
     */

    /*
     * For your reference:
     *
     * DoublyLinkedListNode {
     *     int data;
     *     DoublyLinkedListNode next;
     *     DoublyLinkedListNode prev;
     * }
     *
     */

    public static DoublyLinkedListNode sortedInsert(DoublyLinkedListNode llist, int data) {
        if (data < llist.data) {
            return insertFirst(llist, data);
        }
        DoublyLinkedListNode pointer = llist;
        while (pointer.next != null && data > pointer.data) {
            pointer=pointer.next;
        }
        if (data < pointer.data)
            pointer = pointer.prev;
        return insertAfter(llist, pointer, data);
    }
    static DoublyLinkedListNode insertAfter(DoublyLinkedListNode llist ,DoublyLinkedListNode pointer, int data) {
        DoublyLinkedListNode node = new DoublyLinkedListNode(data);

        DoublyLinkedListNode temp = pointer.next;
        if (temp != null) temp.prev = node;
        pointer.next = node;
        node.prev = pointer;
        node.next = temp;

        return llist;
    }

    static DoublyLinkedListNode insertFirst(DoublyLinkedListNode llist ,int data) {
        DoublyLinkedListNode result = new DoublyLinkedListNode(data);
        result.next = llist;
        llist.prev = result;
        return result;
    }


    // public static void main(String[] args) throws IOException {
    //     BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));
    public static void main(String inputPath, String outputPath) throws IOException {
        Scanner scanner = new Scanner(new FileInputStream(inputPath));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));

        int t = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int tItr = 0; tItr < t; tItr++) {
            DoublyLinkedList llist = new DoublyLinkedList();

            int llistCount = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            for (int i = 0; i < llistCount; i++) {
                int llistItem = scanner.nextInt();
                scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

                llist.insertNode(llistItem);
            }

            int data = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            DoublyLinkedListNode llist1 = sortedInsert(llist.head, data);

            printDoublyLinkedList(llist1, " ", bufferedWriter);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        scanner.close();
    }
}
// @formatter:on
