package side.ipaddress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import side.ipaddress.IpAddress.Ipv4Address;
import side.ipaddress.IpAddress.Ipv6Address;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static side.ipaddress.IpAddress.IPV4_PATTERN;
import static side.ipaddress.IpAddress.IPV6_PATTERN;

@Data
@Accessors(chain = true)
public abstract class IpNetwork {
    static final Pattern IPv4_NETWORK = Pattern.compile("(" + IPV4_PATTERN.pattern() + ")" + "/(\\d{1,2})");
    static final Pattern IPv6_NETWORK = Pattern.compile("(" + IPV6_PATTERN.pattern() + ")" + "/(\\d{1,2})");

    public static IpNetwork ipNetwork(String cidr) {
        Matcher matcher;
        if ((matcher = IPv4_NETWORK.matcher(cidr)).find()) {
            return new Ipv4Network(
                    IpAddress.ipv4Address(matcher.group(1)),
                    Integer.parseInt(matcher.group(matcher.groupCount()))
            );

        } else if ((matcher = IPV6_PATTERN.matcher(cidr)).find()) {
            return new Ipv6Network(
                    IpAddress.ipv6Address(matcher.group(1)),
                    Integer.parseInt(matcher.group(matcher.groupCount()))
            );
        }

        throw new UnsupportedOperationException("did not match ipv4 or ipv6 regex");
    }

    public abstract IpAddress getAddress();

    public abstract int getMaskBits();

    public IpAddress ip() {
        return getAddress();
    }

    public abstract IpNetwork network();

    // turn 24 into 255.255.255.0
    public abstract IpAddress netMask();

    // turn 24 into 0.0.0.255
    public abstract IpAddress hostMask();


    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Data
    public static class Ipv4Network extends IpNetwork {
        final Ipv4Address address;
        final int maskBits;

        @Override
        public IpNetwork network() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IpAddress netMask() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IpAddress hostMask() {
            throw new UnsupportedOperationException();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Data
    public static class Ipv6Network extends IpNetwork {
        final Ipv6Address address;
        final int maskBits;

        @Override
        public IpNetwork network() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IpAddress netMask() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IpAddress hostMask() {
            throw new UnsupportedOperationException();
        }
    }
}
