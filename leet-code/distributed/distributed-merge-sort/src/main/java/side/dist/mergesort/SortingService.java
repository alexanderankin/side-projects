package side.dist.mergesort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
class SortingService {
    final MergeSortProperties mergeSortProperties;
    final JdbcClient jdbcClient;
    final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);
    RestClient restClient;

    @Autowired
    void setRestClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(mergeSortProperties.getSelfBaseUrl()).build();
    }

    @Scheduled(fixedRateString = "PT15S")
    void pollQueue() {
        if (blockingQueue.remainingCapacity() == 0)
            return;
        // Integer fromDb = null;
        // blockingQueue.put(fromDb);
    }


    public void sort(int id) {
        jdbcClient.sql("insert into")
                .param(id)
                .update();
    }
}
