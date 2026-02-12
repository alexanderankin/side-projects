package side.dist.mergesort;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "input_task")
class MergeSortEntity {
    @Id
    @GeneratedValue
    Integer id;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "finished_at")
    Instant finishedAt;
}
