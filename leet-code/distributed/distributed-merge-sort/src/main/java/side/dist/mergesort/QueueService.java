package side.dist.mergesort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
class QueueService {
    static final TypeReference<Map<String, Object>> MAP_OF_STRING_TO_OBJECT = new TypeReference<>() {
    };
    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    QueueItem send(Map<String, Object> payload) {
        String payloadString = objectMapper.writeValueAsString(payload);
        PGobject payloadJsonb = new PGobject();
        payloadJsonb.setType("jsonb");
        payloadJsonb.setValue(payloadString);
        QueueItem queueItem = jdbcClient.sql(
                        "insert into job_queue(payload) " +
                                "values (?) " +
                                "returning id, payload, created_at")
                .param(payloadJsonb)
                .query(QueueItemEntity.class)
                .optional()
                .map(this::toDto)
                .orElseThrow();
        log.debug("new queue id {} assigned to queue payload {}", queueItem.getId(), payloadString);
        return queueItem;
    }

    ProgressItem poll() {
        ProgressItemEntity item = jdbcClient.sql("""
                        WITH moved AS (
                            DELETE FROM job_queue
                                WHERE id = (SELECT id
                                            FROM job_queue
                                            ORDER BY id desc
                                                FOR UPDATE SKIP LOCKED
                                            LIMIT 1)
                                RETURNING id, payload, created_at)
                        INSERT
                        INTO job_in_progress (id, payload, job_created_at)
                        SELECT id, payload, created_at
                        FROM moved
                        RETURNING id, payload, created_at;
                        """)
                .query(ProgressItemEntity.class)
                .optional()
                .orElse(null);
        if (item == null) {
            log.debug("polled queue but it is empty");
            return null;
        }

        log.debug("polled queue to get back queue item {} with payload {}", item.getId(), item.getPayload().getValue());
        return toDto(item);
    }

    void finish(ProgressItem progressItem) {
        int rows = jdbcClient.sql("delete from job_in_progress where id = ?")
                .param(progressItem.getId())
                .update();
        if (1 != rows) {
            // throw new IncorrectResultSizeDataAccessException(1, rows);
            log.warn("expected to finish {} rows but found {} for id {}", 1, rows, progressItem.getId());
        }
        log.debug("marked queue item {} as finished", progressItem.getId());
    }

    void fail(ProgressItem progressItem, String failure) {
        int rows = jdbcClient.sql(
                        "UPDATE job_in_progress " +
                                "SET failure_reason = ?, finished_at = now() " +
                                "WHERE id = ?")
                .params(failure, progressItem.getId())
                .update();
        if (1 != rows) {
            // throw new IncorrectResultSizeDataAccessException(1, rows);
            log.warn("expected to fail {} rows but found {} for id {}", 1, rows, progressItem.getId());
        }
        log.debug("marked queue item {} as failed wth reason {}", progressItem.getId(), failure);

    }

    Slice<FailureListItem> listFailed(Pageable pageable, Integer last) {
        if (pageable.isUnpaged()) {
            log.debug("listing failures without pagination");
            return new SliceImpl<>(
                    jdbcClient.sql("select id, job_created_at, finished_at, failure_reason " +
                                    "from job_in_progress " +
                                    "where failure_reason is not null " +
                                    "order by id")
                            .query(FailureListItem.class)
                            .list());
        }

        int pageSize = pageable.getPageSize();
        List<FailureListItem> results;
        if (last != null) {
            log.debug("listing failures with page size {} after {}", pageSize, last);
            results = jdbcClient.sql("select id, job_created_at, finished_at, failure_reason " +
                            "from job_in_progress " +
                            "where failure_reason is not null and id > ? " +
                            "order by id " +
                            "limit ?")
                    .params(last, pageSize + 1)
                    .query(FailureListItem.class)
                    .list();

        } else {
            log.debug("listing failures with page size {} from the beginning", pageSize);
            results = jdbcClient.sql("select id, job_created_at, finished_at, failure_reason " +
                            "from job_in_progress " +
                            "where failure_reason is not null " +
                            "order by id " +
                            "limit ?")
                    .param(pageSize + 1)
                    .query(FailureListItem.class)
                    .list();
        }
        return new SliceImpl<>(results.subList(0, pageSize), pageable, results.size() > pageSize);
    }

    @SneakyThrows
    private QueueItem toDto(QueueItemEntity queueItemEntity) {
        return new QueueItem()
                .setId(queueItemEntity.getId())
                .setPayload(objectMapper.readValue(queueItemEntity.getPayload().getValue(), MAP_OF_STRING_TO_OBJECT))
                .setCreatedAt(queueItemEntity.getCreatedAt());
    }

    @SneakyThrows
    private ProgressItem toDto(ProgressItemEntity queueItemEntity) {
        var parent = toDto(new QueueItemEntity()
                .setId(queueItemEntity.getId())
                .setPayload(queueItemEntity.getPayload())
                .setCreatedAt(queueItemEntity.getCreatedAt()));
        ProgressItem progressItem = new ProgressItem()
                .setFinishedAt(queueItemEntity.getFinishedAt())
                .setFailureReason(queueItemEntity.getFailureReason());
        progressItem.setId(parent.getId())
                .setPayload(parent.getPayload())
                .setCreatedAt(parent.getCreatedAt());
        return progressItem;
    }

    record FailureListItem(Integer id, OffsetDateTime jobCreatedAt, OffsetDateTime finishedAt, String failureReason) {
    }

    @Data
    @Accessors(chain = true)
    static class QueueItem {
        Integer id;
        Map<String, Object> payload;
        OffsetDateTime createdAt;
    }

    @Data
    @Accessors(chain = true)
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    static class ProgressItem extends QueueItem {
        OffsetDateTime finishedAt;
        String failureReason;
    }

    @Data
    @Accessors(chain = true)
    static class QueueItemEntity {
        Integer id;
        PGobject payload;
        OffsetDateTime createdAt;
    }

    @Data
    @Accessors(chain = true)
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    static class ProgressItemEntity extends QueueItemEntity {
        OffsetDateTime finishedAt;
        String failureReason;
    }
}
