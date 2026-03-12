package side.cloud.util.acme.lib.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AcmeJwsObject {
    Map<String, Object> headers;
    Map<String, Object> payload;
}
