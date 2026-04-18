package side.cloud.util.acme.lib.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;

public class AcmeRequests {
    @Dto
    @Data
    @Accessors(chain = true)
    public static class AcmeRequest {
        URI url;

        @NotEmpty
        String nonce;

        @Nullable
        URI accountId;

        @Nullable
        Map<String, Object> payload;

        /**
         * request must either be for a new account (has payload), or for an existing account (has account id)
         */
        @AssertTrue
        public boolean isHasPayloadIfNoAccountId() {
            return payload != null || accountId == null;
        }
    }

    @Dto
    @Data
    @Accessors(chain = true)
    public static class AcmeResponse {
        URI location;

        URI next;

        @Nullable
        Map<String, Object> payload;

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Dto
        @Data
        @Accessors(chain = true)
        public static class TypedAcmeResponse<T> extends AcmeResponse {
            T typedPayload;
        }
    }
}
