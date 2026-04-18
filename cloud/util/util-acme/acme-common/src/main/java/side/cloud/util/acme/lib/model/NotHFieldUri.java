package side.cloud.util.acme.lib.model;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotHFieldUri.Validator.class)
public @interface NotHFieldUri {
    class Validator implements ConstraintValidator<NotHFieldUri, URI> {
        @SneakyThrows
        @Override
        public boolean isValid(URI value, ConstraintValidatorContext context) {
            if (value == null)
                return true;

            if (!value.getScheme().equals("mailto"))
                return true;

            return value.toURL().getQuery().isEmpty();
        }
    }
}
