import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class BenchmarkProducer {

    private static final int MESSAGE_COUNT = 1_000_000;

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Benchmark tuning
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 64_000);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);

        String payload = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // small message (~32B)

        long start = System.nanoTime();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("benchmark", Integer.toString(i), payload);

            producer.send(record, (metadata, exception) -> {
                if (exception != null)
                    exception.printStackTrace();
                latch.countDown();
            });
        }

        latch.await();
        producer.flush();
        producer.close();

        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        double throughput = MESSAGE_COUNT / seconds;

        System.out.println("Total time: " + seconds + " sec");
        System.out.println("Throughput: " + throughput + " msg/sec");
    }
}
