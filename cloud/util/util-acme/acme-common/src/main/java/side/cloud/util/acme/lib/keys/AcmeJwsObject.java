package side.cloud.util.acme.lib.keys;

import com.nimbusds.jose.jwk.JWK;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.Map;

@Data
@Accessors(chain = true)
public sealed abstract class AcmeJwsObject {
    AcmeJwsHeader headers;

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static final class BlankAcmeJwsObject extends AcmeJwsObject {
        public String getPayload() {
            return "";
        }
    }

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static final class JsonAcmeJwsObject extends AcmeJwsObject {
        Map<String, Object> payload;
    }

    @Data
    @Accessors(chain = true)
    public static sealed abstract class AcmeJwsHeader {
        String alg;
        String nonce;
        URI url;

        public Map<String, Object> customParams() {
            // alg, kid, and jwk are not custom params
            return Map.of("nonce", nonce, "url", url);
        }

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class KidAcmeJwsHeader extends AcmeJwsHeader {
            URI kid;

            public KidAcmeJwsHeader setKid(URI kid) {
                this.kid = kid;
                return this;
            }
        }

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class JwkAcmeJwsHeader extends AcmeJwsHeader {
            JWK jwk;
        }
    }
}
