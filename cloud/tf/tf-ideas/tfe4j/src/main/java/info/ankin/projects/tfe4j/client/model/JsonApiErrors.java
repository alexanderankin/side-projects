package info.ankin.projects.tfe4j.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <ul>
 *     <li>{@code id}: a unique identifier for this particular occurrence of the problem.</li>
 *     <li>{@code links}: a {@code links object} that MAY contain the following members:
 *         <ul>
 *             <li>{@code about}: a {@code link} that leads to further details about this
 *                 particular occurrence of the problem. When de-referenced, this URI SHOULD
 *                 return a human-readable description of the error.
 *             </li>
 *             <li>{@code type}: a {@code link} that identifies the type of error that this
 *                 particular error is an instance of. This URI SHOULD be de-referencable to
 *                 a human-readable explanation of the general error.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>{@code status}: the HTTP status code applicable to this problem, expressed as a
 *         string value. This SHOULD be provided.
 *     </li>
 *     <li>{@code code}: an application-specific error code, expressed as a string value.</li>
 *     <li>{@code title}: a short, human-readable summary of the problem that SHOULD NOT
 *         change from occurrence to occurrence of the problem, except for purposes of
 *         localization.
 *     </li>
 *     <li>{@code detail}: a human-readable explanation specific to this occurrence of the
 *         problem. Like {@code title}, this field’s value can be localized.
 *     </li>
 *     <li>{@code source}: an object containing references to the primary source of the error.
 *         It SHOULD include one of the following members or be omitted:
 *         <ul>
 *             <li>{@code pointer}: a JSON Pointer [<a href="https://tools.ietf.org/html/rfc6901">RFC6901</a>]
 *                 to the value in the request document that caused the error [e.g. {@code "}</code>
 *                 for a primary data object, or {@code "}attributes/title"</code> for a specific
 *                 attribute]. This MUST point to a value in the request document that
 *                 exists; if it doesn’t, the client SHOULD simply ignore the pointer.
 *             </li>
 *             <li>{@code parameter}: a string indicating which URI query parameter caused
 *                 the error.
 *             </li>
 *             <li>{@code header}: a string indicating the name of a single request header which
 *                 caused the error.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>{@code meta}: a {@code meta object} containing non-standard meta-information about the
 *         error.
 *     </li>
 * </ul>
 *
 * @see <a href="https://jsonapi.org/format/#error-objects">jsonapi.org: format, #error-objects</a>
 */
@Data
@Accessors(chain = true)
public class JsonApiErrors {
    List<Error> errors;

    @Data
    @Accessors(chain = true)
    static class Error {
        String id;
        ErrorLinks links;
        String status;
        String code;
        String title;
        String details;
        ErrorSource source;

        @Data
        @Accessors(chain = true)
        public static class ErrorLinks {
            String about;
            String type;
        }

        @Data
        @Accessors(chain = true)
        public static class ErrorSource {
            String pointer;
            String parameter;
            String header;
        }
    }
}
