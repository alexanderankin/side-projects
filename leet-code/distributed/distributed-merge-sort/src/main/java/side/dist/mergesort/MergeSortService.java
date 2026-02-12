package side.dist.mergesort;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
class MergeSortService {
    private static final String INSERT_INTO_INPUT_TASK_ITEM =
            "INSERT INTO input_task_item(id, parent_id, value) " +
                    "VALUES (nextval('input_task_item_seq'), ?, ?)";
    private final MergeSortRepository mergeSortRepository;
    private final MergeSortProperties mergeSortProperties;
    private final EntityManager entityManager;
    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional
    public MergeSortEntity createMergeSort(InputStream inputStream) {
        try (var file = new TempFile(mergeSortProperties.getUploadDirectory().resolve(UUID.randomUUID().toString()).toFile())) {
            Files.copy(inputStream, file.file().toPath());
            log.debug("copied new task inputs into {}", file);

            var entity = mergeSortRepository.save(new MergeSortEntity().setCreatedAt(Instant.now()));
            var entityId = entity.getId();
            entityManager.flush();
            log.debug("created new db entry for task inputs: {}", entityId);

            try (MappingIterator<Integer> it =
                         objectMapper.readerFor(Integer.class).readValues(file.file())) {
                var batched = new BatchIterator<>(it, mergeSortProperties.getDbBatchSize());
                while (batched.hasNext()) {
                    var batch = batched.next();
                    insertItems(entityId, batch);
                    log.trace("created {} more items for input: {}", batch.size(), entityId);
                }
            }
            log.debug("created items for task input: {}", entityId);
            return entity;
        }
    }

    private void insertItems(int parentId, List<Integer> values) {
        jdbcTemplate.batchUpdate(INSERT_INTO_INPUT_TASK_ITEM, values, mergeSortProperties.getDbBatchSize(),
                (ps, value) -> {
                    ps.setInt(1, parentId);
                    ps.setInt(2, value);
                });
    }

    public StreamingResponseBody getMergeSortInput(int id) {
        return out -> {
            var ps = new PrintStream(out);
            jdbcClient.sql("select value from input_task_item where parent_id = ? order by id asc")
                    .param(id)
                    .query(Integer.class)
                    .stream()
                    .forEach(ps::println);
        };
    }

    private record TempFile(File file) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            Files.deleteIfExists(file.toPath());
        }
    }

    @RequiredArgsConstructor
    static class BatchIterator<T> implements Iterator<List<T>> {
        private final Iterator<T> source;
        private final int batchSize;

        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public List<T> next() {
            List<T> batch = new ArrayList<>(batchSize);
            int count = 0;

            while (count < batchSize && source.hasNext()) {
                batch.add(source.next());
                count++;
            }

            return batch;
        }
    }

}
