package side.pxe.dhcp4.logic;

import java.nio.ByteBuffer;

public record EthernetAddress(long value) {
    private static final long MAX_VALUE = 0x0000_FFFF_FFFF_FFFFL;

    public EthernetAddress {
        if ((value & ~MAX_VALUE) != 0) {
            throw new IllegalArgumentException("Ethernet address must contain exactly 48 bits");
        }
    }

    public static EthernetAddress parse(String value) {
        var parts = value.split(":", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("expected six colon-separated octets: " + value);
        }
        long result = 0;
        for (var part : parts) {
            if (part.length() != 2) {
                throw new IllegalArgumentException("invalid Ethernet address: " + value);
            }
            result = (result << 8) | Integer.parseInt(part, 16);
        }
        return new EthernetAddress(result);
    }

    public static EthernetAddress read(ByteBuffer source) {
        if (source.remaining() < 6) {
            throw new IllegalArgumentException("Ethernet address requires six bytes");
        }
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result = (result << 8) | Byte.toUnsignedLong(source.get(source.position() + i));
        }
        return new EthernetAddress(result);
    }

    public void write(ByteBuffer destination) {
        for (int i = 5; i >= 0; i--) {
            destination.put((byte) (value >>> (i * 8)));
        }
    }

    @Override
    public String toString() {
        return "%02x:%02x:%02x:%02x:%02x:%02x".formatted(
                (value >>> 40) & 0xff,
                (value >>> 32) & 0xff,
                (value >>> 24) & 0xff,
                (value >>> 16) & 0xff,
                (value >>> 8) & 0xff,
                value & 0xff);
    }
}
