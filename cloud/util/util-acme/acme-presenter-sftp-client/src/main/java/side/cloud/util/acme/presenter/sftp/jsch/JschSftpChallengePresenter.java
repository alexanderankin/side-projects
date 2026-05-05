package side.cloud.util.acme.presenter.sftp.jsch;

import io.github.resilience4j.retry.RetryRegistry;
import jakarta.validation.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.model.challenge.presentation.ChallengePresenter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class JschSftpChallengePresenter extends ChallengePresenter.SingleHostCrudPresenter {
    @Getter
    private final JschSftpChallengePresenter.Config config;

    public JschSftpChallengePresenter(Config config, RetryRegistry retryRegistry) {
        super(config.host, new JschSftpChallengePresenterCrud(config, retryRegistry), Type.http);
        this.config = config;
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        /**
         * host to connect to
         */
        @NotBlank
        String host;

        /**
         * port on host to connect to
         */
        @Positive
        int port = 22;

        /**
         * where do files go on the server
         */
        @NotBlank
        String hostPath;
        /**
         * timeout for operations
         */
        Duration timeout = Duration.ofSeconds(10);
        /**
         * number of times to try an operation
         */
        @Positive
        int maxRetry = 5;

        @Valid
        @NotNull
        Auth auth = new Auth();

        @Data
        @Accessors(chain = true)
        public static class Auth {
            @NotBlank
            String username;
            String password;
            String identityFileContent;
            @Exists
            Path identityFile;

            @AssertTrue
            public boolean isHasExactlyOneAuthMethod() {
                int count = 0;
                if (password != null)
                    count += 1;
                if (identityFileContent != null)
                    count += 1;
                if (identityFile != null)
                    count += 1;
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
