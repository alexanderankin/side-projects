package side.scratch.passkeys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import side.scratch.passkeys.qr.QrUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.cbor.CBORMapper;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

@Slf4j
public class ReadQr {
    @SneakyThrows
    static void main() {
        var imageBytes = Files.readAllBytes(Path.of(System.getProperty("user.home"), "Pictures/Screenshots/Screenshot From 2026-08-16 07-13-47.png"));
        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        var fidoString = QrUtils.parseQrText(image);
        log.info(fidoString);

        byte[] cbor = FidoQr.decode(fidoString);
        var qr = CBORMapper.shared().<Stuff>readValue(cbor, new TypeReference<>() {
        });

        byte[] publicKey = qr.getPublicKey();
        byte[] qrSecret = qr.getQrSecret();
        var flow = qr.getFlow();

        log.info("P256 key: " + HexFormat.of().formatHex(publicKey));
        log.info("QR secret: " + HexFormat.of().formatHex(qrSecret));
        log.info("flow: " + flow);
        log.info("qr: " + qr);
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stuff {
        @JsonProperty("0")
        byte[] publicKey;
        @JsonProperty("1")
        byte[] qrSecret;
        @JsonProperty("2")
        Number domains;
        @JsonProperty("3")
        Object timestamp;
        @JsonProperty("4")
        Object stateAssisted;
        @JsonProperty("5")
        Flow flow;

        public enum Flow {
            @JsonProperty("ga") GET_ASSERTION,
            @JsonProperty("mc") MAKE_CREDENTIAL,
        }
    }
}
