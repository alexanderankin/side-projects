package info.ankin.projects.tfe4j.client.model;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Getter
public class TerraformClientResponseException extends RuntimeException {
    private static final Predicate<String> DIGITS = Pattern.compile("\\d+").asMatchPredicate();
    private final JsonApiErrors errors;
    @Getter(AccessLevel.NONE)
    private ResponseStatusException asRse;

    /**
     *
     * @param message a custom message to prefix to {@code errors.toString()}
     * @param errors a list of errors from the client
     */
    public TerraformClientResponseException(String message, JsonApiErrors errors) {
        super((Objects.isNull(message) ? "Problem with response from Terraform server" : message) + (Objects.isNull(errors) ? "" : "(errors: '" + errors + "'"));
        this.errors = errors;
    }

    public TerraformClientResponseException(String message) {
        this(message, null);
    }

    public TerraformClientResponseException(JsonApiErrors errors) {
        super("Problem with response from Terraform server");
        this.errors = errors;
    }

    /**
     * often it is useful to pass exceptions back to the client. use with caution.
     */
    public ResponseStatusException asPassThrough() {
        if (asRse == null)
            asRse = new ResponseStatusException(getStatus(), "forwarded Terraform client Exception", this);
        return asRse;
    }

    private HttpStatus getStatus() {
        HttpStatus httpStatus;
        if (errors == null || CollectionUtils.isEmpty(errors.getErrors())) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            httpStatus = errors.getErrors().stream()
                    .map(JsonApiErrors.Error::getStatus)
                    .filter(Objects::nonNull)
                    .filter(DIGITS)
                    .map(Integer::parseInt)
                    .map(HttpStatus::resolve)
                    .filter(Objects::nonNull)
                    .findAny().orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return httpStatus;
    }
}
