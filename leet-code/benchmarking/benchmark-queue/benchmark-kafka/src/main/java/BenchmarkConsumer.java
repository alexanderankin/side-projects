import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class BenchmarkConsumer {

    private static final int MESSAGE_TARGET = 1_000_000;

    public static void main(String[] args) {
        ((LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory())
                .getLogger(Logger.ROOT_LOGGER_NAME)
                .setLevel(Level.INFO);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "benchmark-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Benchmark tuning
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "65536");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "50");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("benchmark"));

        long count = 0;

        // Wait for assignment
        while (consumer.assignment().isEmpty()) {
            consumer.poll(Duration.ofMillis(100));
        }

        long start = System.nanoTime();

        while (count < MESSAGE_TARGET) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));

            count += records.count();
        }

        long end = System.nanoTime();
        consumer.commitSync();
        consumer.close();

        double seconds = (end - start) / 1_000_000_000.0;
        double throughput = MESSAGE_TARGET / seconds;

        System.out.println("Total consumed: " + MESSAGE_TARGET);
        System.out.println("Total time: " + seconds + " sec");
        System.out.println("Throughput: " + throughput + " msg/sec");
    }
}
