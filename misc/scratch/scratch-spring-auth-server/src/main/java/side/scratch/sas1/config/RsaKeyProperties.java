package side.scratch.sas1.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "auth-server.keys")
@Validated
public class RsaKeyProperties {
    @NotEmpty
    Map<String, @Valid RsaKey> keys;

    @Data
    @Accessors(chain = true)
    public static class RsaKey {
        @NotBlank
        String publicKey;
        @NotBlank
        String privateKey;
        @NotNull
        UUID keyId;
    }
}
