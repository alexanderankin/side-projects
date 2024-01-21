package totp4j;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import javax.crypto.Mac;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

public class OtpGenerator {
    private final Base32 base32;

    public OtpGenerator() {
        this(new Base32());
    }

    public OtpGenerator(Base32 base32) {
        this.base32 = base32;
    }

    public Password generate(String token, Instant instant, int length) {
        long unix = instant.getEpochSecond();
        double unixF = (double) unix;

        long timer = (long) Math.floor(unixF / (double) OtpConstants.timeSplitInSeconds);
        long remainingTime = OtpConstants.timeSplitInSeconds - unix % OtpConstants.timeSplitInSeconds;

        token = token
                .replaceAll(" ", "")
                .toUpperCase();

        byte[] secretBytes = base32.decode(token);
        Mac mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1, secretBytes);

        // byte[] buf = new byte[OtpConstants.sumByteLength];
        ByteBuffer buf = ByteBuffer.allocate(OtpConstants.sumByteLength);
        buf.order(ByteOrder.BIG_ENDIAN).putLong(timer);
        byte[] sum = mac.doFinal(buf.array());

        // http://tools.ietf.org/html/rfc4226#section-5.4
        int offset = sum[sum.length - 1] & OtpConstants.mask1;
        long value = (long) ((((int) (sum[offset])) & OtpConstants.mask2) << OtpConstants.shift24) |
                ((((int) (sum[offset + 1]) & OtpConstants.mask3)) << OtpConstants.shift16) |
                ((((int) (sum[offset + 2]) & OtpConstants.mask3)) << OtpConstants.shift8) |
                (((int) (sum[offset + 3])) & OtpConstants.mask3);

        long modulo = (int) value % (long) (Math.pow(10, (int) (length)));

        String format = String.format("%%0%dd", length);

        return new Password()
                .setCode(String.format(format, modulo))
                .setRemainingTime(remainingTime);
    }

    interface OtpConstants {
        int mask1 = 0xf;
        int mask2 = 0x7f;
        int mask3 = 0xff;
        int timeSplitInSeconds = 30;
        int shift24 = 24;
        int shift16 = 16;
        int shift8 = 8;
        int sumByteLength = 8;
        int passwordHashLength = 32;
    }

    @Data
    @Accessors(chain = true)
    public static class Password {
        String code;
        long remainingTime;
    }
}
