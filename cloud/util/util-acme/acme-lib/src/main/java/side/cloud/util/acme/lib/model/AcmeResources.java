package side.cloud.util.acme.lib.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import side.cloud.util.acme.lib.AcmeServer;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p><a href="https://datatracker.ietf.org/doc/html/rfc8555#section-7.1">from rfc8555 7.1</a>:</p>
 * <p>
 * The following diagram illustrates the relations between resources on
 * an ACME server.  For the most part, these relations are expressed by
 * URLs provided as strings in the resources' JSON representations.
 * Lines with labels in quotes indicate HTTP link relations.
 * </p>
 * <pre>
 *
 *                                   directory
 *                                       |
 *                                       +--> newNonce
 *                                       |
 *           +----------+----------+-----+-----+------------+
 *           |          |          |           |            |
 *           |          |          |           |            |
 *           V          V          V           V            V
 *      newAccount   newAuthz   newOrder   revokeCert   keyChange
 *           |          |          |
 *           |          |          |
 *           V          |          V
 *        account       |        order --+--> finalize
 *                      |          |     |
 *                      |          |     +--> cert
 *                      |          V
 *                      +---> authorization
 *                                | ^
 *                                | | "up"
 *                                V |
 *                              challenge
 * </pre>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555#section-7.1">rfc8555: 7.1 - resources</a>
 */
@SuppressWarnings({"unused", "GrazieInspection"})
public interface AcmeResources {

    @Dto
    @Data
    @Accessors(chain = true)
    class Account {
        AccountStatus status;
        /**
         * <p>
         * An array of URLs that the
         * server can use to contact the client for issues related to this
         * account.  For example, the server may wish to notify the client
         * about server-initiated revocation or certificate expiration.  For
         * information on supported URL schemes, see Section 7.3.
         * </p>
         *
         * @see AcmeServer.Config#getContactSupportedSchemes()
         */
        List<@NotNull @NotHFieldUri URI> contact;
        Boolean termsOfServiceAgreed;
        ExternalAccountBinding externalAccountBinding;
        /**
         * <p>
         * Each account object includes an "orders" URL from which a list of
         * orders created by the account can be fetched via POST-as-GET request.
         * The result of the request MUST be a JSON object whose "orders" field
         * is an array of URLs, each identifying an order belonging to the
         * account.
         * </p>
         * <p>
         * The server SHOULD include pending orders and SHOULD NOT
         * include orders that are invalid in the array of URLs.
         * </p>
         */
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<URI> orders;

        /**
         * <pre>
         *                      valid
         *                        |
         *                        |
         *            +-----------+-----------+
         *     Client |                Server |
         *    deactiv.|                revoke |
         *            V                       V
         *       deactivated               revoked
         *
         *                    State Transitions for Account Objects
         * </pre>
         * <p>
         * Account objects are created in the "valid" state, since no further
         * action is required to create an account after a successful newAccount
         * request.  If the account is deactivated by the client or revoked by
         * the server, it moves to the corresponding state.
         * </p>
         */
        public enum AccountStatus {
            valid, deactivated, revoked
        }

        /**
         * todo review if this is accurate
         */
        @Data
        @Accessors(chain = true)
        public static class ExternalAccountBinding {
            @NotNull
            MacAlgorithm alg;
            String kid;
            @NotNull
            URI url;
        }
    }

    /**
     * An ACME order object represents a client's request for a certificate
     * and is used to track the progress of that order through to issuance.
     * Thus, the object contains information about the requested
     * certificate, the authorizations that the server requires the client
     * to complete, and any certificates that have resulted from this order.
     */
    @Dto
    @Data
    @Accessors(chain = true)
    class Order {
        OrderStatus status;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant expires;
        List<AcmeIdentifier> identifiers;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant notBefore;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant notAfter;
        /**
         * The error that occurred while processing
         * the order, if any.  This field is structured as a problem document
         * [RFC7807].
         */
        ProblemDetail error;
        /**
         * For pending orders, the
         * authorizations that the client needs to complete before the
         * requested certificate can be issued (see Section 7.5), including
         * unexpired authorizations that the client has completed in the past
         * for identifiers specified in the order.  The authorizations
         * required are dictated by server policy; there may not be a 1:1
         * relationship between the order identifiers and the authorizations
         * required.  For final orders (in the "valid" or "invalid" state),
         * the authorizations that were completed.  Each entry is a URL from
         * which an authorization can be fetched with a POST-as-GET request.
         */
        List<String> authorizations;
        /**
         * A URL that a CSR must be POSTed to once
         * all the order's authorizations are satisfied to finalize the
         * order.  The result of a successful finalization will be the
         * population of the certificate URL for the order.
         */
        String finalize;
        /**
         * A URL for the certificate that has
         * been issued in response to this order.
         */
        String certificate;

        /**
         * <pre>
         *     pending --------------+
         *        |                  |
         *        | All authz        |
         *        | "valid"          |
         *        V                  |
         *      ready ---------------+
         *        |                  |
         *        | Receive          |
         *        | finalize         |
         *        | request          |
         *        V                  |
         *    processing ------------+
         *        |                  |
         *        | Certificate      | Error or
         *        | issued           | Authorization failure
         *        V                  V
         *      valid             invalid
         *
         *                     State Transitions for Order Objects
         * </pre>
         * <p>
         * Order objects are created in the "pending" state.  Once all of the
         * authorizations listed in the order object are in the "valid" state,
         * the order transitions to the "ready" state.  The order moves to the
         * "processing" state after the client submits a request to the order's
         * "finalize" URL and the CA begins the issuance process for the
         * certificate.  Once the certificate is issued, the order enters the
         * "valid" state.  If an error occurs at any of these stages, the order
         * moves to the "invalid" state.  The order also moves to the "invalid"
         * state if it expires or one of its authorizations enters a final state
         * other than "valid" ("expired", "revoked", or "deactivated").
         * </p>
         */
        public enum OrderStatus {
            pending, ready, processing, valid, invalid
        }
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class Authorization {
        /**
         * what you are authorized to represent
         */
        AcmeIdentifier identifier;
        AuthorizationStatus status;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant expires;
        List<Map<String, Object>> challenges;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        boolean wildcard;

        /**
         * <p>
         * Authorization objects are created in the "pending" state.  If one of
         * the challenges listed in the authorization transitions to the "valid"
         * state, then the authorization also changes to the "valid" state.  If
         * the client attempts to fulfill a challenge and fails, or if there is
         * an error while the authorization is still pending, then the
         * authorization transitions to the "invalid" state.  Once the
         * authorization is in the "valid" state, it can expire ("expired"), be
         * deactivated by the client ("deactivated", see Section 7.5.2), or
         * revoked by the server ("revoked").
         * </p>
         * <pre>
         *                       pending --------------------+
         *                          |                        |
         *        Challenge failure |                        |
         *               or         |                        |
         *              Error       |  Challenge valid       |
         *                +---------+---------+              |
         *                |                   |              |
         *                V                   V              |
         *             invalid              valid            |
         *                                    |              |
         *                                    |              |
         *                                    |              |
         *                     +--------------+--------------+
         *                     |              |              |
         *                     |              |              |
         *              Server |       Client |   Time after |
         *              revoke |   deactivate |    "expires" |
         *                     V              V              V
         *                  revoked      deactivated      expired
         *
         *                 State Transitions for Authorization Objects
         * </pre>
         */
        public enum AuthorizationStatus {
            pending, valid, invalid, deactivated, expired, revoked
        }
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class Challenge {
        ChallengeStatus status;

        /**
         * <p>
         * Challenge objects are created in the "pending" state.  They
         * transition to the "processing" state when the client responds to the
         * challenge (see Section 7.5.1) and the server begins attempting to
         * validate that the client has completed the challenge.  Note that
         * within the "processing" state, the server may attempt to validate the
         * challenge multiple times (see Section 8.2).  Likewise, client
         * requests for retries do not cause a state change.  If validation is
         * successful, the challenge moves to the "valid" state; if there is an
         * error, the challenge moves to the "invalid" state.
         * </p>
         * <pre>
         *             pending
         *                |
         *                | Receive
         *                | response
         *                V
         *            processing <-+
         *                |   |    | Server retry or
         *                |   |    | client retry request
         *                |   +----+
         *                |
         *                |
         *    Successful  |   Failed
         *    validation  |   validation
         *      +---------+---------+
         *      |                   |
         *      V                   V
         *    valid              invalid
         *
         *                   State Transitions for Challenge Objects
         * </pre>
         */
        public enum ChallengeStatus {
            pending, processing, valid, invalid
        }
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class Certificate {
    }

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8555#section-7.1.1">rfc8555: 7.1.1 - Directory</a>
     */
    @Dto
    @Data
    @Accessors(chain = true)
    class Directory {
        Meta meta;
        URI keyChange;
        URI newAccount;
        /**
         * If the ACME server does not implement pre-authorization
         * (Section 7.4.1), it MUST omit the "newAuthz" field of the directory.
         */
        URI newAuthz;
        URI newNonce;
        URI newOrder;
        URI renewalInfo;
        URI revokeCert;

        /**
         * <p>
         * The object MAY additionally contain a "meta" field.  If present, it
         * MUST be a JSON object; each field in the object is an item of
         * metadata relating to the service provided by the ACME server.
         * </p>
         *
         * <p>
         * The following metadata items are defined (Section 9.7.6), all of
         * which are OPTIONAL:
         * </p>
         *
         * <p>
         * termsOfService (optional, string):  A URL identifying the current
         * terms of service.
         * </p>
         *
         * <p>
         * website (optional, string):  An HTTP or HTTPS URL locating a website
         * providing more information about the ACME server.
         * </p>
         *
         * <p>
         * {@code caaIdentities} (optional, array of string):  The hostnames that the
         * ACME server recognizes as referring to itself for the purposes of
         * CAA record validation as defined in [RFC6844].  Each string MUST
         * represent the same sequence of ASCII code points that the server
         * will expect to see as the "Issuer Domain Name" in a CAA issue or
         * {@code issuewild} property tag.  This allows clients to determine the
         * correct issuer domain name to use when configuring CAA records.
         * </p>
         *
         * <p>
         * externalAccountRequired (optional, boolean):  If this field is
         * present and set to "true", then the CA requires that all
         * newAccount requests include an "externalAccountBinding" field
         * associating the new account with an external account.
         * </p>
         *
         */
        @Dto
        @Data
        @Accessors(chain = true)
        public static class Meta {
            URI termsOfService;
            URI website;
            List<String> caaIdentities;
            Boolean externalAccountRequired;
            LinkedHashMap<String, String> profiles = new LinkedHashMap<>();
        }
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class NewNonce {
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class NewAccount {
        /**
         * @see Account#contact
         */
        List<@NotNull @NotHFieldUri URI> contact;
        /**
         * @see Account#termsOfServiceAgreed
         */
        Boolean termsOfServiceAgreed;
        /**
         * If this field is present
         * with the value "true", then the server MUST NOT create a new
         * account if one does not already exist.  This allows a client to
         * look up an account URL based on an account key (see
         * Section 7.3.1).
         */
        Boolean onlyReturnExisting;
        /**
         * @see Account#externalAccountBinding
         */
        Account.ExternalAccountBinding externalAccountBinding;
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class NewOrder {
        /**
         * @see Order#identifiers
         */
        List<AcmeIdentifier> identifiers;
        /**
         * @see Order#notBefore
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant notBefore;
        /**
         * @see Order#notAfter
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant notAfter;
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class RevokeCert {
    }

    @Dto
    @Data
    @Accessors(chain = true)
    class KeyChange {
    }
}
