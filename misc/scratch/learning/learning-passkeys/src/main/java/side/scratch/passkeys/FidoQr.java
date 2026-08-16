package side.scratch.passkeys;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public final class FidoQr {

    private static final String PREFIX = "FIDO:/";

    private FidoQr() {}

    /**
     * Decode the decimal payload from a FIDO hybrid QR code.
     * <p>
     * Returns the raw CBOR bytes.
     */
    public static byte[] decode(String uri) {
        if (uri == null || !uri.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Not a FIDO:/ URI");
        }

        String digits = uri.substring(PREFIX.length());

        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(
                "FIDO payload must contain decimal digits only");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int pos = 0;

        // Every complete 7-byte chunk is represented by 17 decimal digits.
        while (digits.length() - pos >= 17) {
            decodeChunk(digits.substring(pos, pos + 17), 7, out);
            pos += 17;
        }

        int remaining = digits.length() - pos;

        if (remaining != 0) {
            int bytes = switch (remaining) {
                case 3  -> 1;
                case 5  -> 2;
                case 8  -> 3;
                case 10 -> 4;
                case 13 -> 5;
                case 15 -> 6;
                default -> throw new IllegalArgumentException(
                    "Invalid trailing FIDO digit count: " + remaining);
            };

            decodeChunk(digits.substring(pos), bytes, out);
        }

        return out.toByteArray();
    }

    /**
     * Decimal integer -> little-endian byte sequence.
     */
    private static void decodeChunk(
            String decimal,
            int byteCount,
            ByteArrayOutputStream out) {

        BigInteger n = new BigInteger(decimal);

        BigInteger limit = BigInteger.ONE.shiftLeft(byteCount * 8);

        if (n.signum() < 0 || n.compareTo(limit) >= 0) {
            throw new IllegalArgumentException(
                "FIDO chunk does not fit in " + byteCount + " bytes");
        }

        // FIDO specifies little-endian integer encoding.
        for (int i = 0; i < byteCount; i++) {
            out.write(n.and(BigInteger.valueOf(0xff)).intValue());
            n = n.shiftRight(8);
        }
    }
}
