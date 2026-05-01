package side.notes.backend.model.entity;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE_USE)
@Retention(RUNTIME)
@Constraint(validatedBy = {HasId.BaseEntityHasId.class})
public @interface HasId {
    class BaseEntityHasId implements ConstraintValidator<HasId, BaseEntity> {
        @Override
        public boolean isValid(BaseEntity value, ConstraintValidatorContext context) {
            return value == null || value.getId() != null;
        }
    }
}
