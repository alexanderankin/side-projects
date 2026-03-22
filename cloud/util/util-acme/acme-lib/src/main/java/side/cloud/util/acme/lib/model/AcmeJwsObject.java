package side.cloud.util.acme.lib.model;

import lombok.Data;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.AcmeClientOperations;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AcmeJwsObject {
    AcmeClientOperations.JwsHeader headers;
    Map<String, Object> payload;
}
