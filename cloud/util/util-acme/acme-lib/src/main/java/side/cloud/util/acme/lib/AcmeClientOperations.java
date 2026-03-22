package side.cloud.util.acme.lib;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;

import java.net.URI;
import java.util.Map;

public interface AcmeClientOperations {
    String newNonce(Directory directory);

    Directory directory();

    ResponseEntity<String> post(URI uri, JwsHeader header, Map<String, Object> payload);

    ResponseEntity<String> postAsGet(URI uri, JwsHeader header);

    @Data
    @Accessors(chain = true)
    sealed abstract class JwsHeader {
        String alg;
        String nonce;
        URI url;

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class KidJwsHeader extends JwsHeader {
            String kid;
        }

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class JwkJwsHeader extends JwsHeader {
            Map<String, Object> jwk;
        }
    }
}
