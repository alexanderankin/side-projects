package onebrc;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class CalculateAverage_alexanderankin {
    public static void main(String[] args) {
        // new CalculateAverage_alexanderankin().calculate(Path.of("measurements.txt")).forEach(System.out::println);
        new CalculateAverage_alexanderankin().calculate(Path.of("/home/toor/IdeaProjects/side-projects/leet-code/one-billion-row-challenge/src/test/resources/example.txt")).forEach(System.out::println);
    }

    @SneakyThrows
    List<String> calculate(Path path) {
        var file = path.toFile();
        // long length = file.length();

        var data = new HashMap<Integer, int[]>();
        var order = new TreeMap<String, Integer>();
        var toOrder = new HashMap<Integer, String>();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            boolean eof = false;
            byte[] buf = new byte[32768]; // https://stackoverflow.com/a/56091135

            eof:
            while (!eof) {
                int read = fileInputStream.read(buf);
                if (read != buf.length) eof = true;

                int i = 0;
                // skip over the line?
                // while (buf[i] != '\n') i++;

                for (; i < read; i++) {
                    if (buf[i] == -1) break eof;

                    // read the name
                    int start = i;
                    while (buf[i] != ';') i++;

                    // hash the name
                    int hash = hashCode(buf, start, i);

                    // with hash, we can do record keeping
                    int[] dataElement = data.computeIfAbsent(hash, k -> new int[4]);

                    int end = i;
                    boolean[] called = {false};
                    var s = toOrder.computeIfAbsent(hash, k -> {
                        called[0] = true;
                        return new String(buf, start, end, StandardCharsets.UTF_8);
                    });
                    if (called[0])
                        order.put(s, hash);

                    // count a new occurrence
                    dataElement[0]++;

                    // read the number
                    int number = i + 1;
                    while (buf[i] != '\n') i++;

                    // convert number to integer, multiplied by 10
                    int numberValue = parseNumber(buf, number, i);

                    dataElement[1] = Math.addExact(dataElement[1], numberValue);

                    dataElement[2] = Math.min(dataElement[2], numberValue);
                    dataElement[3] = Math.max(dataElement[3], numberValue);
                }
            }
        }

        List<String> output = new ArrayList<>(order.size());
        for (Map.Entry<String, Integer> orderEntry : order.entrySet()) {
            int[] dataValue = data.get(orderEntry.getValue());
            double average = (dataValue[1] / (double) dataValue[0]) / 10.0;
            double min = dataValue[2] / 10.0;
            double max = dataValue[3] / 10.0;
            output.add(orderEntry.getKey() + "=" + min + "/" + average + "/" + max);
        }
        return output;
    }

    int parseNumber(byte[] buf, int number, int end) {
        boolean negative = number < buf.length && buf[number] == '-';
        if (negative) number++;

        boolean decimal = false;

        int result = 0;

        for (int i = number; i < end; i++) {
            if (buf[i] == '.') {
                decimal = true;
            } else {
                result *= 10;
                result += (buf[i] - '0');
            }
        }

        if (!decimal) result *= 10;

        return negative ? -result : result;
    }

    /**
     * @see java.util.Arrays#hashCode(int[])
     * @see Integer#hashCode(int)
     */
    public int hashCode(byte[] a, int start, int endExclusive) {
        if (a == null)
            return 0;

        int result = 1;

        for (int i = start; i < endExclusive; i++) {
            int element = a[i];
            result = 31 * result + element;
        }

        return result;
    }
}
