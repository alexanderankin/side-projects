package side.learning;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Arrays;

@SuppressWarnings("Lombok")
@Data
@Accessors(chain = true)
public class ByteArrayBigInteger extends Number implements Comparable<ByteArrayBigInteger> {
    private static final byte[] DEFAULT_DATA = new byte[0];
    // private static final int DEFAULT_LENGTH = 1;

    @ToString.Exclude
    byte[] data = DEFAULT_DATA;
    // int length = DEFAULT_LENGTH;
    // int lastBitLength;

    public static ByteArrayBigInteger of(int i) {
        if (i == 0) return new ByteArrayBigInteger();

        byte[] tmp = new byte[4];
        int len = 0;

        while (i != 0) {
            tmp[len++] = (byte) i;
            i >>>= 8;
        }

        return ofLsb(Arrays.copyOf(tmp, len));
    }

    public static ByteArrayBigInteger ofLsb(byte[] data) {
        return new ByteArrayBigInteger().setData(data);
    }

    public static ByteArrayBigInteger ofMsb(byte[] data, boolean inPlace) {
        if (!inPlace)
            data = Arrays.copyOf(data, data.length);
        ByteArrayOperations.reverse(data);
        return ofLsb(data);
    }

    public static ByteArrayBigInteger of(String str, int radix) {
        if (str == null || str.isEmpty())
            throw new NumberFormatException("Empty input");

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("Invalid radix: " + radix);

        if (radix == 2)
            return ofBinaryString(str);

        byte[] result = new byte[1]; // starts at 0

        for (int i = 0; i < str.length(); i++) {
            int digit = Character.digit(str.charAt(i), radix);
            if (digit < 0)
                throw new NumberFormatException("Invalid digit: " + str.charAt(i));

            // result = result * radix
            int carry = 0;
            for (int j = 0; j < result.length; j++) {
                int value = (result[j] & 0xFF) * radix + carry;
                result[j] = (byte) value;
                carry = value >>> 8;
            }
            if (carry != 0) {
                result = Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = (byte) carry;
            }

            // result = result + digit
            carry = digit;
            for (int j = 0; j < result.length; j++) {
                int value = (result[j] & 0xFF) + carry;
                result[j] = (byte) value;
                carry = value >>> 8;
                if (carry == 0) break;
            }
            if (carry != 0) {
                result = Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = (byte) carry;
            }
        }

        return ofLsb(result);
    }

    public static ByteArrayBigInteger ofBinaryString(String str) {
        int bitLen = str.length();
        if (bitLen == 0)
            throw new NumberFormatException("No digits");

        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];

        int bitIndex = 0; // LSB position

        // iterate from right (LSB) to left (MSB)
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (c != '0' && c != '1')
                throw new NumberFormatException("Invalid binary digit: " + c);

            if (c == '1') {
                int byteIndex = bitIndex >>> 3;
                int bitOffset = bitIndex & 7;
                result[byteIndex] |= (byte) (1 << bitOffset);
            }
            bitIndex++;
        }


        return ofLsb(result);
    }

    public int length() {
        return data.length;
    }

    @Override
    public int intValue() {
        int len = Math.min(4, data.length);
        int result = 0;
        for (int i = 0; i < len; i++) {
            result |= (data[i] & 0xFF) << (8 * i);
        }
        return result;
    }

    @Override
    public long longValue() {
        int len = Math.min(8, data.length);
        long result = 0;
        for (int i = 0; i < len; i++) {
            result |= ((long) data[i] & 0xFF) << (8 * i);
        }
        return result;
    }

    @Override
    public float floatValue() {
        return longValue();
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    @Override
    public int compareTo(@NonNull ByteArrayBigInteger o) {
        return Arrays.compare(this.data, 0, length(), o.data, 0, o.length());
    }

    public String toString(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new IllegalArgumentException("Invalid radix: " + radix);

        if (data.length == 0)
            return "0";

        // working copy in MSB-first order
        byte[] tmp = Arrays.copyOf(data, data.length);
        ByteArrayOperations.reverse(tmp);

        StringBuilder sb = new StringBuilder();

        int start = 0;

        while (start < tmp.length) {
            int remainder = 0;

            // divide in-place, respecting logical start
            for (int i = start; i < tmp.length; i++) {
                int value = (remainder << 8) | (tmp[i] & 0xFF);
                tmp[i] = (byte) (value / radix);
                remainder = value % radix;
            }

            sb.append(Character.forDigit(remainder, radix));

            // advance start past leading zeros (no copying)
            while (start < tmp.length && tmp[start] == 0) {
                start++;
            }
        }

        return sb.isEmpty() ? "0" : sb.reverse().toString();
    }

    @ToString.Include
    public String toDecimalString() {
        return toString(10);
    }

    public void reverseInPlace(ByteArrayBigInteger other) {
        ByteArrayOperations.reverse(data);
    }

    public ByteArrayBigInteger reverse(ByteArrayBigInteger other) {
        var copy = copy();
        copy.reverseInPlace(other);
        return copy;
    }

    public void andInPlace(ByteArrayBigInteger other) {
        andInPlace(other, false);
    }

    public void andInPlace(ByteArrayBigInteger other, boolean strictLengthCheck) {
        if (strictLengthCheck && length() != other.length()) throw new AssertionError();
        else if (other.length() > length())
            throw new IllegalStateException("andInPlace fails when destination is smaller");

        // ByteArrayOperations.and(data, length(), other.data, other.length());
        var otherLength = other.length();
        for (int i = 0; i < otherLength; i++) {
            data[i] &= other.data[i];
        }
    }

    public ByteArrayBigInteger and(ByteArrayBigInteger other) {
        var copy = copy(Math.max(length(), other.length()));
        copy.andInPlace(other);
        return copy;
    }

    public void orInPlace(ByteArrayBigInteger other) {
        orInPlace(other, false);
    }

    public void orInPlace(ByteArrayBigInteger other, boolean strictLengthCheck) {
        if (strictLengthCheck && length() != other.length()) throw new AssertionError();
        else if (other.length() > length())
            throw new IllegalStateException("andInPlace fails when destination is smaller");

        var otherLength = other.length();
        for (int i = 0; i < otherLength; i++) {
            data[i] |= other.data[i];
        }
    }

    public ByteArrayBigInteger or(ByteArrayBigInteger other) {
        var copy = copy(Math.max(length(), other.length()));
        copy.orInPlace(other);
        return copy;
    }

    public void invertInPlace() {
        for (int i = 0; i < length(); i++) {
            data[i] = (byte) ~data[i];
        }
    }

    public ByteArrayBigInteger invert() {
        var copy = copy();
        copy.invertInPlace();
        return copy;
    }

    public ByteArrayBigInteger copy() {
        return copy(length());
    }

    public ByteArrayBigInteger copy(int length) {
        return new ByteArrayBigInteger().setData(Arrays.copyOf(data, length));
    }

}
