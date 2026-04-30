package side.notes.backend.model.entity;

import jakarta.validation.groups.Default;

/**
 * validation group which makes sure that referenced entities have id
 *
 * @see BaseEntity#id
 */
public interface ReferencedEntity extends Default {
}
