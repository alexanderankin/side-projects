package side.notes.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.SortedSet;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "tag")
public class TagEntity extends BaseEntity.NamedEntity implements Comparable<TagEntity> {
    static final Comparator<TagEntity> COMPARATOR = Comparator.nullsFirst(Comparator.comparing(NamedEntity::getName));

    @JsonView(Views.Default.class)
    String description;

    @ToString.Exclude
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    SortedSet<BlockEntity> notes;

    @Override
    public int compareTo(@NonNull TagEntity other) {
        return COMPARATOR.compare(this, other);
    }
}
