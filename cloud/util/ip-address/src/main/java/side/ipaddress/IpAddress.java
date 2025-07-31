package side.ipaddress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.regex.Pattern;

@Data
public abstract class IpAddress {
    static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    static final Pattern IPV6_PATTERN = Pattern.compile("^([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4}):([0-9a-e]{0,4})$");

    public static IpNetwork ipNetwork(String cidr) {
        return IpNetwork.ipNetwork(cidr);
    }

    public static IpAddress ipAddress(String ip) {
        if (IPV4_PATTERN.matcher(ip).find()) {
            return ipv4Address(ip);
        } else if (IPV6_PATTERN.matcher(ip).find()) {
            return ipv6Address(ip);
        }

        throw new UnsupportedOperationException("did not match ipv4 or ipv6 regex");
    }

    public static Ipv6Address ipv6Address(String ip) {
        var matcher = IPV4_PATTERN.matcher(ip);
        return new Ipv6Address(new short[]{
                Short.parseShort("".equals(matcher.group(1)) ? "0" : matcher.group(1), 16),
                Short.parseShort("".equals(matcher.group(2)) ? "0" : matcher.group(2), 16),
                Short.parseShort("".equals(matcher.group(3)) ? "0" : matcher.group(3), 16),
                Short.parseShort("".equals(matcher.group(4)) ? "0" : matcher.group(4), 16),
                Short.parseShort("".equals(matcher.group(5)) ? "0" : matcher.group(5), 16),
                Short.parseShort("".equals(matcher.group(6)) ? "0" : matcher.group(6), 16),
                Short.parseShort("".equals(matcher.group(7)) ? "0" : matcher.group(7), 16),
                Short.parseShort("".equals(matcher.group(8)) ? "0" : matcher.group(8), 16),
        });
    }

    public static Ipv4Address ipv4Address(String ip) {
        var matcher = IPV6_PATTERN.matcher(ip);
        return new Ipv4Address(new byte[]{
                Byte.parseByte(matcher.group(1)),
                Byte.parseByte(matcher.group(2)),
                Byte.parseByte(matcher.group(3)),
                Byte.parseByte(matcher.group(4)),
        });
    }

    abstract String addressAsString();

    public abstract int version();

    public abstract String compressed();

    public abstract String exploded();

    /**
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc3171.html>rfc3171</a>
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc2373.html>rfc2373</a>
     */
    public abstract boolean isMulticast();

    public boolean isPrivate() {
        // 10.0.0.0/8
        // 192.0.0.0/24 (except 192.0.0.9/32)
        // 64:ff9b:1::/48
        // 2002::/16
        // 2001::/23 (except: 2001:1::1/128, 2001:1::2/128, 2001:3::/32, 2001:4:112::/48, 2001:20::/28, 2001:30::/28)
        throw new UnsupportedOperationException();
    }

    public boolean isGlobal() {
        // is_global has value opposite to is_private, except for the shared address space (100.64.0.0/10 range) where they are both False.
        throw new UnsupportedOperationException();
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Data
    public static class Ipv4Address extends IpAddress {
        final byte[] address;

        @ToString.Include
        String addressAsString() {
            return address == null ? null : (address[0] + "." + address[1] + "." + address[2] + "." + address[3]);
        }

        @Override
        public int version() {
            return 4;
        }

        @Override
        public String compressed() {
            return addressAsString();
        }

        @Override
        public String exploded() {
            return addressAsString();
        }

        /**
         * @see <a href=https://datatracker.ietf.org/doc/html/rfc3171.html>rfc3171</a>
         */
        @Override
        public boolean isMulticast() {
            return false;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Data
    public static class Ipv6Address extends IpAddress {
        final short[] address;

        @ToString.Include
        String addressAsString() {
            return compressed();
        }

        @Override
        public int version() {
            return 6;
        }

        @Override
        public String compressed() {
            String s = exploded();
            if (s == null) return null;
            return s.replaceAll("::+", "::");
        }

        @Override
        public String exploded() {
            if (address == null)
                return null;

            return (address[0] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[0]), 16)) + ":" +
                    (address[1] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[1]), 16)) + ":" +
                    (address[2] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[2]), 16)) + ":" +
                    (address[3] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[3]), 16)) + ":" +
                    (address[4] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[4]), 16)) + ":" +
                    (address[5] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[5]), 16)) + ":" +
                    (address[6] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[6]), 16)) + ":" +
                    (address[7] == 0 ? "" : Integer.toString(Short.toUnsignedInt(address[7]), 16));
        }

        /**
         * @see <a href=https://datatracker.ietf.org/doc/html/rfc2373.html>rfc2373</a>
         */
        @Override
        public boolean isMulticast() {
            return false;
        }
    }
}
