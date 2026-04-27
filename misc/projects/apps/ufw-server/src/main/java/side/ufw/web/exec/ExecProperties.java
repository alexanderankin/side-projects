package side.ufw.web.exec;

import jakarta.validation.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "ufw-exec")
@Component
@Validated
public class ExecProperties {
    @NotNull
    ExecType type = ExecType.local;

    @Valid
    SshConfig ssh;

    @NotNull
    Duration timeout = Duration.ofSeconds(10);

    public enum ExecType {
        local,
        ssh,
    }

    @Data
    @Accessors(chain = true)
    public static class SshConfig {
        @NotBlank
        String host;
        @NotBlank
        String user;
        @Positive
        int port = 22;
        @Valid
        @NotNull
        Auth auth = new Auth();

        @Data
        @Accessors(chain = true)
        public static class Auth {
            @ToString.Exclude
            String password;
            @Exists
            Path sshKey;
            boolean useIdentity;

            @ToString.Include
            String passwordStars() {
                // noinspection SuspiciousRegexArgument
                return password == null ? null : password.replaceAll(".", "*");
            }

            @AssertTrue
            public boolean isExactlyOneMethod() {
                int count = 0;
                if (StringUtils.hasText(password)) count++;
                if (sshKey != null) count++;
                if (useIdentity) count++;
                return count == 1;
            }

            @Retention(RetentionPolicy.RUNTIME)
            @Constraint(validatedBy = Exists.Validator.class)
            public @interface Exists {
                String message() default "file does not exist";

                Class<?>[] groups() default {};

                Class<? extends Payload>[] payload() default {};


                class Validator implements ConstraintValidator<Exists, Path> {
                    @Override
                    public boolean isValid(Path value, ConstraintValidatorContext context) {
                        return value == null || Files.exists(value);
                    }
                }
            }
        }
    }
}
