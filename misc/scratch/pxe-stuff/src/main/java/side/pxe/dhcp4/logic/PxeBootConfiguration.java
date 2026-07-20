package side.pxe.dhcp4.logic;

import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record PxeBootConfiguration(Inet4Address nextServer, String bootFileName) {
    public PxeBootConfiguration {
        Objects.requireNonNull(nextServer, "nextServer");
        Objects.requireNonNull(bootFileName, "bootFileName");
        if (bootFileName.isBlank()) {
            throw new IllegalArgumentException("bootFileName must not be blank");
        }
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(bootFileName)) {
            throw new IllegalArgumentException("bootFileName must contain only US-ASCII characters");
        }
        if (bootFileName.getBytes(StandardCharsets.US_ASCII).length > 255) {
            throw new IllegalArgumentException("bootFileName must fit in one DHCP option");
        }
    }
}
