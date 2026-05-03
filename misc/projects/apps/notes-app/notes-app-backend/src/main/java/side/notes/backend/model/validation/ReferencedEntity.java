package side.notes.backend.model.validation;

import jakarta.validation.groups.Default;
import side.notes.backend.model.entity.BaseEntity;

/**
 * validation group which makes sure that referenced entities have id
 *
 * @see BaseEntity#id
 */
public interface ReferencedEntity extends Default {
}
