package side.cloud.util.acme.lib.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Dto
@Data
@Accessors(chain = true)
public class AcmeIdentifier {
    AcmeIdentifierType type;
    String value;

    public enum AcmeIdentifierType {
        dns,
    }
}
