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
                                "returning *")
                .param(payloadJsonb)
                .query(QueueItemEntity.class)
                .optional()
                .map(this::toDto)
                .orElseThrow();
        log.debug("new queue id {} assigned to queue payload {}", queueItem.getId(), payloadString);
        return queueItem;
    }

    QueueItem poll() {
        QueueItemEntity item = jdbcClient.sql("select * from queue_latest_job()")
                .query(QueueItemEntity.class)
                .optional()
                .orElse(null);
        if (item == null) {
            log.debug("polled queue but it is empty");
            return null;
        }

        log.debug("polled queue to get back queue item {} with payload {}", item.getId(), item.getPayload().getValue());
        return toDto(item);
    }

    void finish(QueueItem progressItem) {
        int rows = jdbcClient.sql("update job_queue set finished_at = now() where id = ?")
                .param(progressItem.getId())
                .update();
        if (1 != rows) {
            // throw new IncorrectResultSizeDataAccessException(1, rows);
            log.warn("expected to finish {} rows but found {} for id {}", 1, rows, progressItem.getId());
        }
        log.debug("marked queue item {} as finished", progressItem.getId());
    }

    void fail(QueueItem progressItem, String failure) {
        int rows = jdbcClient.sql(
                        "UPDATE job_queue " +
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

    Slice<QueueItem> listFailed(Pageable pageable, Integer last) {
        if (pageable.isUnpaged()) {
            log.debug("listing failures without pagination");
            return new SliceImpl<>(
                    jdbcClient.sql("select * from job_queue " +
                                    "where failure_reason is not null " +
                                    "order by id")
                            .query(QueueItemEntity.class)
                            .list())
                    .map(this::toDto);
        }

        int pageSize = pageable.getPageSize();
        List<QueueItem> results;
        if (last != null) {
            log.debug("listing failures with page size {} after {}", pageSize, last);
            results = jdbcClient.sql("select * from job_queue " +
                            "where failure_reason is not null and id > ? " +
                            "order by id " +
                            "limit ?")
                    .params(last, pageSize + 1)
                    .query(QueueItemEntity.class)
                    .stream().map(this::toDto)
                    .toList();

        } else {
            log.debug("listing failures with page size {} from the beginning", pageSize);
            results = jdbcClient.sql("select * from job_queue " +
                            "where failure_reason is not null " +
                            "order by id " +
                            "limit ?")
                    .param(pageSize + 1)
                    .query(QueueItemEntity.class)
                    .stream().map(this::toDto)
                    .toList();
        }
        return new SliceImpl<>(results.subList(0, pageSize), pageable, results.size() > pageSize);
    }

    QueueItem retryFailed(int id) {
        QueueItem queueItem = jdbcClient.sql("select * from retry_failed_job(?)")
                .param(id).query(QueueItemEntity.class).optional().map(this::toDto).orElse(null);
        log.debug("retrying failed id {}, results in {}", id, queueItem);
        return queueItem;
    }

    QueueItem retryLatest() {
        QueueItem queueItem = jdbcClient.sql("select * from retry_latest_job()")
                .query(QueueItemEntity.class).optional().map(this::toDto).orElse(null);
        log.debug("retrying latest job results in {}", queueItem);
        return queueItem;
    }

    int retryAllFailed() {
        return jdbcClient.sql("select count(*) from retry_all_failed_jobs()").query(Integer.class).single();
    }

    @SneakyThrows
    private QueueItem toDto(QueueItemEntity queueItemEntity) {
        return new QueueItem()
                .setId(queueItemEntity.getId())
                .setCreatedAt(queueItemEntity.getCreatedAt())
                .setPayload(objectMapper.readValue(queueItemEntity.getPayload().getValue(), MAP_OF_STRING_TO_OBJECT))
                .setAttempt(queueItemEntity.getAttempt())
                .setOriginalId(queueItemEntity.getOriginalId())
                .setStartedAt(queueItemEntity.getStartedAt())
                .setFinishedAt(queueItemEntity.getFinishedAt())
                .setFailureReason(queueItemEntity.getFailureReason())
                .setRetriedAt(queueItemEntity.getRetriedAt())
                ;
    }

    @Data
    @Accessors(chain = true)
    static class QueueItem {
        Integer id;
        OffsetDateTime createdAt;
        Map<String, Object> payload;
        int attempt;
        Integer originalId;
        OffsetDateTime startedAt;
        OffsetDateTime finishedAt;
        String failureReason;
        OffsetDateTime retriedAt;
    }

    @Data
    @Accessors(chain = true)
    static class QueueItemEntity {
        Integer id;
        OffsetDateTime createdAt;
        PGobject payload;
        int attempt;
        Integer originalId;
        OffsetDateTime startedAt;
        OffsetDateTime finishedAt;
        String failureReason;
        OffsetDateTime retriedAt;
    }
}
