package side.cloud.util.acme.lib;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;

import java.net.URI;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public interface AcmeClientOperations {
    String newNonce(Directory directory);

    Directory directory();

    ResponseEntity<String> post(URI uri, JwsHeader header, Map<String, Object> payload);

    ResponseEntity<String> postAsGet(URI uri, JwsHeader header);

    @EqualsAndHashCode(callSuper = false)
    @Data
    @Accessors(chain = true)
    sealed abstract class JwsHeader extends AbstractMap<String, Object> {
        String alg;
        String nonce;
        URI url;

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class KidJwsHeader extends JwsHeader {
            URI kid;

            @Nonnull
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return Map.<String, Object>of("alg", alg, "nonce", nonce, "url", url, "kid", kid).entrySet();
            }
        }

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class JwkJwsHeader extends JwsHeader {
            Map<String, Object> jwk;

            @Nonnull
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return Map.of("alg", alg, "nonce", nonce, "url", url, "jwk", jwk).entrySet();
            }
        }
    }
}
