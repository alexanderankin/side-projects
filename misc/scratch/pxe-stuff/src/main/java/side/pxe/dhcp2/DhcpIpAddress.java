package side.pxe.dhcp2;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.StringJoiner;

public interface DhcpIpAddress extends Comparable<DhcpIpAddress> {
    static DhcpIpAddress of(String s) {
        return new StringDhcpIpAddress(s);
    }

    static DhcpIpAddress of(int i) {
        return new IntDhcpIpAddress(i);
    }

    static DhcpIpAddress of(byte[] b) {
        return new ByteArrayDhcpIpAddress(b);
    }

    static DhcpIpAddress of(ByteBuffer byteBuffer) {
        return new ByteBufferDhcpIpAddress(byteBuffer);
    }

    String humanReadableRepresentation();

    int intRepresentation();

    byte[] byteArrayRepresentation();

    ByteBuffer byteBufferRepresentation();

    default DhcpIpAddress intBased() {
        return of(intRepresentation());
    }

    default DhcpIpAddress humanReadableBased() {
        return of(humanReadableRepresentation());
    }

    default DhcpIpAddress byteArrayBased() {
        return of(byteArrayRepresentation());
    }

    default DhcpIpAddress byteBufferBased() {
        return of(byteBufferRepresentation());
    }

    @Override
    default int compareTo(DhcpIpAddress o) {
        return Integer.compareUnsigned(this.intRepresentation(), o.intRepresentation());
    }

    default DhcpIpAddress next() {
        return of(intRepresentation() + 1);
    }

    @Data
    abstract class BaseIpAddress implements DhcpIpAddress {
        @ToString.Include
        public String humanReadableRepresentation() {
            var j = new StringJoiner(".");
            for (var b : byteArrayRepresentation()) {
                j.add(String.valueOf(Byte.toUnsignedInt(b)));
            }
            return j.toString();
        }

        @ToString.Include
        public int intRepresentation() {
            return byteBufferRepresentation().getInt(0);
        }

        @Override
        public ByteBuffer byteBufferRepresentation() {
            return ByteBuffer.wrap(byteArrayRepresentation());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BaseIpAddress b))
                return false;
            return intRepresentation() == b.intRepresentation();
        }

        @Override
        public int hashCode() {
            return intRepresentation();
        }
    }

    @ToString(callSuper = true)
    @Data
    @Accessors(chain = true)
    class StringDhcpIpAddress extends BaseIpAddress {
        private final String ipAddress;

        public StringDhcpIpAddress(String ipAddress) {
            if (ipAddress == null)
                throw new IllegalArgumentException();
            var parts = ipAddress.split("\\.");
            if (parts.length != 4)
                throw new IllegalArgumentException();
            if (!Arrays.stream(parts).map(Integer::parseInt).allMatch(e -> e >= 0 && e <= 255))
                throw new IllegalArgumentException();
            this.ipAddress = ipAddress;
        }

        @Override
        public String humanReadableRepresentation() {
            return ipAddress;
        }

        @Override
        public byte[] byteArrayRepresentation() {
            var bytes = new byte[4];
            var parts = ipAddress.split("\\.");
            for (var i = 0; i < parts.length; i++) {
                bytes[i] = (byte) Integer.parseInt(parts[i]);
            }
            return bytes;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @ToString(callSuper = true)
    @Data
    @Accessors(chain = true)
    class IntDhcpIpAddress extends BaseIpAddress {
        private final int ipAddress;

        public IntDhcpIpAddress(int ipAddress) {
            this.ipAddress = ipAddress;
        }

        @Override
        public int intRepresentation() {
            return ipAddress;
        }

        @Override
        public byte[] byteArrayRepresentation() {
            return byteBufferRepresentation().array();
        }

        @Override
        public ByteBuffer byteBufferRepresentation() {
            return ByteBuffer.allocate(4).putInt(ipAddress).position(0);
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @ToString(callSuper = true)
    @Data
    @Accessors(chain = true)
    class ByteArrayDhcpIpAddress extends BaseIpAddress {
        private final byte[] ipAddress;

        public ByteArrayDhcpIpAddress(byte[] ipAddress) {
            if (ipAddress == null || ipAddress.length != 4)
                throw new IllegalArgumentException();
            this.ipAddress = ipAddress;
        }

        @Override
        public byte[] byteArrayRepresentation() {
            return ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @ToString(callSuper = true)
    @Data
    @Accessors(chain = true)
    class ByteBufferDhcpIpAddress extends BaseIpAddress {
        private final ByteBuffer ipAddress;

        public ByteBufferDhcpIpAddress(ByteBuffer ipAddress) {
            if (ipAddress == null || ipAddress.remaining() != 4)
                throw new IllegalArgumentException();
            this.ipAddress = ipAddress;
        }

        @Override
        public byte[] byteArrayRepresentation() {
            byte[] result = new byte[4];
            ipAddress.get(0, result, 0, 4);
            return result;
        }

        @Override
        public ByteBuffer byteBufferRepresentation() {
            return ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
