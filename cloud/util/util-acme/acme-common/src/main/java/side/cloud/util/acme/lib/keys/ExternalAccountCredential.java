package side.cloud.util.acme.lib.keys;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;

@Data
@Accessors(chain = true)
public class ExternalAccountCredential {
    @NotEmpty
    String keyId;
    @NotNull
    MacAlgorithm macAlgorithm;
    @NotEmpty
    byte[] macKey;

    public static ExternalAccountCredential generate(
            String kid,
            MacAlgorithm macAlgorithm,
            SecureRandom secureRandom
    ) {
        int keyLengthBytes = switch (macAlgorithm) {
            case HS256 -> 32;
            case HS384 -> 48;
            case HS512 -> 64;
            case null -> throw new IllegalArgumentException("Unsupported MAC algorithm: " + macAlgorithm);
        };

        byte[] macKey = new byte[keyLengthBytes];
        secureRandom.nextBytes(macKey);

        return new ExternalAccountCredential()
                .setKeyId(kid)
                .setMacAlgorithm(macAlgorithm)
                .setMacKey(macKey);
    }

    public String asMacKeyBase64() {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(macKey);
    }

    public AcmeResources.Account.ExternalAccountBinding sign(SupportedClientKeyPair keyPair, URI url) {
        return new ExternalAccountBindingBuilder()
                .setKeyPair(keyPair)
                .setCredential(this)
                .setUrl(url)
                .build();
    }
}
