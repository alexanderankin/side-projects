package side.notes.backend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
    String description;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    SortedSet<BlockEntity> notes;
}
