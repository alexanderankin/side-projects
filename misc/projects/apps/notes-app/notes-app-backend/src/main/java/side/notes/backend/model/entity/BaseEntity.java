package side.notes.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(chain = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @JsonView(Views.Default.class)
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    UUID id;

    @JsonView(Views.Default.class)
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime created;

    @JsonView(Views.Default.class)
    @LastModifiedDate
    LocalDateTime updated;

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    @Data
    @Accessors(chain = true)
    @MappedSuperclass
    public static abstract class NamedEntity extends BaseEntity {
        @JsonView(Views.Default.class)
        @NotNull
        @EqualsAndHashCode.Include
        @NaturalId
        @Column(nullable = false, updatable = false)
        @Length(max = 255)
        String name;
    }
}
