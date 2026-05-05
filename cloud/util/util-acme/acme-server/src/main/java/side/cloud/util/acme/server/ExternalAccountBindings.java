package side.cloud.util.acme.server;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObjectJSON;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.JWK;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.util.Assert;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPairAlgorithm;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Account.ExternalAccountBinding;

import java.net.URI;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;

public class ExternalAccountBindings {

    public static ExternalAccountBindingVerifier verify(ExternalAccountBindingParser parser, byte[] macKey) {
        return new ExternalAccountBindingVerifier(parser, macKey);
    }

    public static ExternalAccountBindingParser parse(ExternalAccountBinding externalAccountBinding) {
        return new ExternalAccountBindingParser(externalAccountBinding);
    }

    @Data
    @Accessors(chain = true)
    public static class ExternalAccountBindingVerifier {
        final ExternalAccountBindingParser parser;
        final byte[] macKey;
        URI url;
        Set<MacAlgorithm> enabledAlgorithms;
        SupportedClientKeyPair keyPair;

        /**
         * verifies that:
         * <ul>
         * <li>eab payload is jwk public key</li>
         * <li>eab using HS256/HS384/HS512</li>
         * <li>eab has url of newAccount</li>
         * <li>eab signed with credential secret as MAC key</li>
         * </ul>
         */
        @SneakyThrows
        public boolean verify() {
            if (!enabledAlgorithms.contains(parser.macAlgorithm()))
                return false;

            var payloadJwk = parser.payloadJwk();
            var payloadJwkAlg = SupportedClientKeyPairAlgorithm.valueOf(payloadJwk.getAlgorithm().getName());
            var fromPayload = new SupportedClientKeyPair()
                    .setKeyPair(new KeyPair(payloadJwkAlg.extractPublic(payloadJwk), null))
                    .setAlgorithm(payloadJwkAlg);

            if (!fromPayload.equals(keyPair) || !MessageDigest.isEqual(fromPayload.getKeyPairPublicEncoded(), keyPair.getKeyPairPublicEncoded()))
                return false;

            if (!MessageDigest.isEqual(parser.url().toString().getBytes(), url.toString().getBytes()))
                return false;

            try {
                if (!parser.getJwsJson().getSignatures().getFirst().verify(new MACVerifier(macKey)))
                    return false;
            } catch (JOSEException e) {
                return false;
            }

            return true;
        }
    }


    @Data
    @Accessors(chain = true)
    public static class ExternalAccountBindingParser {
        final AcmeResources.Account.ExternalAccountBinding externalAccountBinding;
        final JWSObjectJSON jwsJson;

        @SneakyThrows
        public ExternalAccountBindingParser(ExternalAccountBinding externalAccountBinding) {
            this.externalAccountBinding = externalAccountBinding;
            jwsJson = JWSObjectJSON.parse(Map.of(
                    "protected", externalAccountBinding.getProtectedString(),
                    "payload", externalAccountBinding.getPayload(),
                    "signature", externalAccountBinding.getSignature()
            ));

            Assert.isTrue(jwsJson.getSignatures().size() == 1, "must have exactly one signature");
        }

        @SneakyThrows
        public URI url() {
            return jwsJson.getSignatures().getFirst().getHeader().getCustomParam("url") instanceof String s ? URI.create(s) : null;
        }

        public MacAlgorithm macAlgorithm() {
            return MacAlgorithm.valueOf(jwsJson.getSignatures().getFirst().getHeader().getAlgorithm().getName());
        }

        @SneakyThrows
        public JWK payloadJwk() {
            return JWK.parse(jwsJson.getPayload().toJSONObject());
        }

        public String kid() {
            return jwsJson.getSignatures().getFirst().getHeader().getKeyID();
        }
    }

}
