package side.notes.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(chain = true)
@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    UUID id;

    @CreatedDate
    OffsetDateTime created;

    @LastModifiedDate
    OffsetDateTime updated;

    @MappedSuperclass
    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    static abstract class NamedEntity extends BaseEntity {
        @NotNull
        @EqualsAndHashCode.Include
        @NaturalId
        @Column(nullable = false, updatable = false)
        String name;
    }
}
