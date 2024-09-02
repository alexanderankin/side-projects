package side.y2024;

import org.junit.jupiter.api.Test;

import java.util.*;

class Apr13_2024 {
    @Test
    void test() {
        // PriorityQueue<Integer> integers = new PriorityQueue<>(Comparator.comparingInt(i -> -i));
        // integers.add(20);
        // integers.add(26);
        //
        // System.out.println(integers.peek());

        String instructions = """
                1 97
                2
                1 20
                2
                1 26
                1 20
                2
                3
                1 91
                3""";
        var result = getMax(Arrays.asList(instructions.split("\r?\n")));
        System.out.println(result);
    }

    public static List<Integer> getMax(List<String> operations) {
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(i -> -i));
        PriorityQueue<Integer> priorityQueue1 = new PriorityQueue<>();
        List<Integer> result = new ArrayList<>();
        for (String o : operations) {
            String[] parts = o.split(" ");
            switch(parts[0]) {
                case "1": {
                    priorityQueue.add(Integer.valueOf(parts[1]));
                    priorityQueue1.add(Integer.valueOf(parts[1]));
                    break;
                }
                case "2": {
                    priorityQueue.poll();
                    priorityQueue1.poll();
                    break;
                }
                case "3": {
                    Integer peek = priorityQueue.peek();
                    result.add(peek);
                    break;
                }
            }
        }
        return result;

    }
}
