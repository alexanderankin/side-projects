package side.pxe.dhcp2;

import java.nio.ByteBuffer;

public class Util {
    public static byte[] byteBufferToByteArray(ByteBuffer b) {
        byte[] result = new byte[b.remaining()];
        b.get(b.position(), result, 0, result.length);
        return result;
    }

    public static byte[] concatArray(byte[] ...a) {
        int totalLength = 0;
        for (var bytes : a) {
            totalLength += bytes.length;
        }

        int last = 0;
        byte[] result = new byte[totalLength];
        for (var bytes : a) {
            System.arraycopy(bytes, 0, result, last, a.length);
            last += a.length;
        }
        return result;
    }
}
