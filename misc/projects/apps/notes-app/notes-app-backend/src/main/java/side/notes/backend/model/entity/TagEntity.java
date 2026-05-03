package side.notes.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.SortedSet;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "tag")
public class TagEntity extends BaseEntity.NamedEntity {
    @JsonView(Views.Default.class)
    String description;

    @ToString.Exclude
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    SortedSet<BlockEntity> notes;
}
