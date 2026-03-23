package side.cloud.util.acme.lib.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @see <a href=https://datatracker.ietf.org/doc/html/rfc8555#section-6.7>rfc8555: 6.7: Errors</a>
 */
public class AcmeError extends RuntimeException {
    public AcmeError(String message) {
        super(message);
    }

    public AcmeError(String message, Exception cause) {
        super(message, cause);
    }

    public static AcmeError from(RestClientResponseException exception) {
        try {
            var problemDetail = Objects.requireNonNull(exception.getResponseBodyAs(ProblemDetail.class));
            var type = Optional.ofNullable(problemDetail.getType()).map(URI::toString).orElse(null);
            var knownAcmeErrorType = KnownAcmeError.KnownAcmeErrorType.fromType(type);
            if (knownAcmeErrorType != null) {
                return new KnownAcmeError(
                        problemDetail.getDetail(),
                        new KnownAcmeError.KnownAcmeErrorDto().setKnownAcmeErrorType(knownAcmeErrorType),
                        exception);
            } else {
                return new UnknownAcmeError(
                        problemDetail.getDetail(),
                        new UnknownAcmeError.UnknownAcmeErrorDto()
                                .setType(problemDetail.getType())
                                .setDetail(problemDetail.getDetail()),
                        exception);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public void doThrow() {
        throw this;
    }

    @Getter
    public static class KnownAcmeError extends AcmeError {
        private final KnownAcmeErrorDto error;

        public KnownAcmeError(String message, KnownAcmeErrorDto error) {
            super(message);
            this.error = error;
        }

        public KnownAcmeError(String message, KnownAcmeErrorDto error, Exception cause) {
            super(message, cause);
            this.error = error;
        }

        @Getter
        @ToString
        public enum KnownAcmeErrorType {
            accountDoesNotExist("The request specified an account that does not exist"),
            alreadyRevoked("The request specified a certificate to be revoked that has already been revoked"),
            badCSR("The CSR is unacceptable (e.g., due to a short key)"),
            badNonce("The client sent an unacceptable anti-replay nonce"),
            badPublicKey("The JWS was signed by a public key the server does not support"),
            badRevocationReason("The revocation reason provided is not allowed by the server"),
            badSignatureAlgorithm("The JWS was signed with an algorithm the server does not support"),
            caa("Certification Authority Authorization (CAA) records forbid the CA from issuing a certificate"),
            compound("Specific error conditions are indicated in the \"subproblems\" array"),
            connection("The server could not connect to validation target"),
            dns("There was a problem with a DNS query during identifier validation"),
            externalAccountRequired("The request must include a value for the \"externalAccountBinding\" field"),
            incorrectResponse("Response received didn't match the challenge's requirements"),
            invalidContact("A contact URL for an account was invalid"),
            malformed("The request message was malformed"),
            orderNotReady("The request attempted to finalize an order that is not ready to be finalized"),
            rateLimited("The request exceeds a rate limit"),
            rejectedIdentifier("The server will not issue certificates for the identifier"),
            serverInternal("The server experienced an internal error"),
            tls("The server received a TLS error during validation"),
            unauthorized("The client lacks sufficient authorization"),
            unsupportedContact("A contact URL for an account used an unsupported protocol scheme"),
            unsupportedIdentifier("An identifier is of an unsupported type"),
            userActionRequired("Visit the \"instance\" URL and take actions specified there"),
            ;

            public static final Set<KnownAcmeErrorType> NON_TRANSIENT = Set.of(
                    accountDoesNotExist,
                    alreadyRevoked,
                    badCSR,
                    badPublicKey,
                    badRevocationReason,
                    badSignatureAlgorithm,
                    caa,
                    externalAccountRequired,
                    invalidContact,
                    malformed,
                    rejectedIdentifier,
                    unauthorized,
                    unsupportedContact,
                    unsupportedIdentifier
            );
            private static final Map<String, KnownAcmeErrorType> TYPE_TO_ERROR_TYPE = Arrays.stream(KnownAcmeErrorType.values())
                    .collect(Collectors.toMap(KnownAcmeErrorType::getType, Function.identity()));
            private final String detail;
            private final String type;

            KnownAcmeErrorType(String detail) {
                this.detail = detail;
                type = "urn:ietf:params:acme:error:" + name();
            }

            public static KnownAcmeErrorType fromType(String type) {
                return TYPE_TO_ERROR_TYPE.get(type);
            }
        }

        @Dto
        @Data
        @Accessors(chain = true)
        public static class KnownAcmeErrorDto {
            @JsonUnwrapped
            KnownAcmeErrorType knownAcmeErrorType;
            List<KnownAcmeErrorSubProblemDto> subproblems;
        }

        @Dto
        @Data
        @Accessors(chain = true)
        public static class KnownAcmeErrorSubProblemDto {
            @JsonUnwrapped
            KnownAcmeErrorType knownAcmeErrorType;
            AcmeIdentifier identifier;
        }
    }

    @Getter
    public static class UnknownAcmeError extends AcmeError {
        private final UnknownAcmeErrorDto error;

        public UnknownAcmeError(String message, UnknownAcmeErrorDto error) {
            super(message);
            this.error = error;
        }

        public UnknownAcmeError(String message, UnknownAcmeErrorDto error, Exception cause) {
            super(message, cause);
            this.error = error;
        }

        @Dto
        @Data
        @Accessors(chain = true)
        public static class UnknownAcmeErrorDto {
            URI type;
            String detail;
            List<UnknownAcmeErrorSubProblemDto> subproblems;
        }

        @Dto
        @Data
        @Accessors(chain = true)
        public static class UnknownAcmeErrorSubProblemDto {
            URI type;
            String detail;
            AcmeIdentifier identifier;
        }
    }
}
