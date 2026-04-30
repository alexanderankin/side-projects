package side.cloud.util.acme.lib.keys;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObjectJSON;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.net.URI;

@Data
@Accessors(chain = true)
public class ExternalAccountBindingBuilder {
    SupportedClientKeyPair keyPair;
    ExternalAccountCredential credential;
    URI url;

    @SneakyThrows
    public AcmeResources.Account.ExternalAccountBinding build() {
        var jwsObjectJSON = new JWSObjectJSON(new Payload(keyPair.asJwk().toPublicJWK().toJSONObject()));

        var jwsHeaderBuilder = new JWSHeader.Builder(JWSAlgorithm.parse(credential.macAlgorithm.name()));
        jwsHeaderBuilder.keyID(credential.keyId);
        jwsHeaderBuilder.customParam("url", url.toString());
        JWSHeader jwsHeader = jwsHeaderBuilder.build();

        jwsObjectJSON.sign(jwsHeader, new MACSigner(credential.macKey));
        var goj = jwsObjectJSON.toFlattenedJSONObject();
        return new AcmeResources.Account.ExternalAccountBinding()
                .setProtectedString((String) goj.get("protected"))
                .setPayload((String) goj.get("payload"))
                .setSignature((String) goj.get("signature"));
    }
}
