package side.notes.backend.model;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.data.domain.Pageable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UpToOneSort.PageableValidator.class)
public @interface UpToOneSort {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class PageableValidator implements ConstraintValidator<UpToOneSort, Pageable> {
        @Override
        public boolean isValid(Pageable value, ConstraintValidatorContext context) {
            return value == null || value.getSort().stream().count() <= 1;
        }
    }
}
