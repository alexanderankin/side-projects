package side.notes.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;

import java.util.SortedSet;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "block")
public class BlockEntity extends BaseEntity {
    @Column(nullable = false, updatable = false)
    @NaturalId
    Integer ordinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @NaturalId
    @JoinColumn(name = "note_id", nullable = false, updatable = false)
    NoteEntity note;

    @NotNull
    String content;

    @ManyToMany
    @JoinTable(name = "block_tag",
            joinColumns = @JoinColumn(name = "block_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    SortedSet<@NotNull @HasId TagEntity> tags;
}
