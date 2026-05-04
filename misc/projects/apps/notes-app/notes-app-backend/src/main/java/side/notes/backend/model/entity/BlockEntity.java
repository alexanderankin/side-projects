package side.notes.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;
import side.notes.backend.model.validation.HasId;

import java.util.Comparator;
import java.util.SortedSet;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "block")
public class BlockEntity extends BaseEntity implements Comparable<BlockEntity> {
    private static final Comparator<BlockEntity> blockEntityComparator = Comparator.nullsFirst(Comparator.comparing(BlockEntity::getOrdinal));

    @JsonView(Views.Default.class)
    @Column(nullable = false, updatable = false)
    @NaturalId
    String ordinal;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @NaturalId
    @JoinColumn(name = "note_id", nullable = false, updatable = false)
    NoteEntity note;

    @NotNull
    @JsonView(Views.Default.class)
    String content;

    @JsonView(Views.Default.Partial.class)
    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "block_tag",
            joinColumns = @JoinColumn(name = "block_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    SortedSet<@NotNull @HasId TagEntity> tags;

    @Override
    public int compareTo(BlockEntity o) {
        return blockEntityComparator.compare(this, o);
    }
}
