package side.dist.mergesort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class QueueServiceITest extends DistributedMergeSortITest {

    @Autowired
    QueueService queueService;

    @Autowired
    JdbcClient jdbcClient;

    @SuppressWarnings("SqlWithoutWhere")
    @BeforeEach
    void setup() {
        jdbcClient.sql("delete from job_queue").update();
        jdbcClient.sql("delete from job_in_progress").update();
    }

    @Test
    void test_sendPollAndFinish() {
        var item = queueService.send(Map.of("k", "v"));
        assertThat(item, notNullValue());
        assertThat(item.getId(), notNullValue());

        var polled = queueService.poll();
        assertThat(polled, notNullValue());
        assertThat(polled.getId(), is(item.getId()));
        assertThat(polled.getPayload(), hasEntry("k", "v"));

        assertThat(queueService.poll(), nullValue());

        queueService.finish(polled);

        var remaining = jdbcClient.sql("select count(*) from job_in_progress").query(Integer.class).single();
        assertThat(remaining, is(0));
    }

    @Test
    void test_sendAndFailMovesToDlq() {
        var item = queueService.send(Map.of("fail", true));

        var polled = queueService.poll();
        assertThat(polled.getId(), is(item.getId()));

        queueService.fail(polled, "pow");
        queueService.fail(polled, "boom");

        Map<String, Object> row = jdbcClient
                .sql("select failure_reason from job_in_progress where id = ?")
                .param(polled.getId())
                .query()
                .singleRow();

        assertThat(row.get("failure_reason"), is("boom"));
    }

    @Test
    void test_queryFailures() {
        IntStream.range(0, 20)
                .mapToObj(i -> Map.entry(i, queueService.send(Map.of("id", i))))
                .map(e -> Map.entry(e.getKey(), queueService.poll()))
                .forEach(e -> queueService.fail(e.getValue(), "failure: " + e.getKey()));

        assertThat(queueService.listFailed(Pageable.unpaged(), null).getSize(), is(20));

        {
            var slice = queueService.listFailed(Pageable.ofSize(5), null);
            assertThat(slice.getSize(), is(5));
            assertThat(slice.getContent().getFirst().failureReason(), is("failure: 0"));
            assertThat(slice.getContent().getLast().failureReason(), is("failure: 4"));
        }

        {
            var slice = queueService.listFailed(Pageable.ofSize(5), 5);
            assertThat(slice.getContent().getFirst().failureReason(), is("failure: 5"));
            assertThat(slice.getContent().getLast().failureReason(), is("failure: 9"));
        }

        assertThat(queueService.listFailed(Pageable.ofSize(10), 10).getContent().getFirst().failureReason(),
                is("failure: 10"));
        assertThat(queueService.listFailed(Pageable.ofSize(10), 5).getContent().getLast().failureReason(),
                is("failure: 14"));
    }

    @Test
    void test_concurrentPollClaimsEachJobOnce() throws Exception {
        int jobs = 50;
        for (int i = 0; i < jobs; i++) queueService.send(Map.of("i", i));

        ConcurrentSkipListSet<Integer> claimed = new ConcurrentSkipListSet<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            Callable<Void> worker = () -> {
                while (true) {
                    var item = queueService.poll();
                    if (item == null) break;
                    if (!claimed.add(item.getId()))
                        throw new IllegalStateException("duplicate claim: " + item.getId());
                    queueService.finish(item);
                }
                return null;
            };

            var futures = new java.util.ArrayList<Future<Void>>();
            for (int i = 0; i < 10; i++) futures.add(executor.submit(worker));
            for (var f : futures) f.get(10, TimeUnit.SECONDS);

            executor.shutdown();
        }

        assertThat(claimed.size(), is(jobs));

        var remainingQueue = jdbcClient.sql("select count(*) from job_queue").query(Integer.class).single();
        assertThat(remainingQueue, is(0));

        var remainingProgress = jdbcClient.sql("select count(*) from job_in_progress").query(Integer.class).single();
        assertThat(remainingProgress, is(0));
    }
}
