package side.ufw.web;


import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonSubTypes({
        @JsonSubTypes.Type(UfwRule.UfwRangeRule.class),
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ufwRuleType")
public abstract sealed class UfwRule {

    public enum Proto { tcp, udp }

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    @JsonTypeName("range")
    public static final class UfwRangeRule extends UfwRule {
        @NotNull
        @Valid
        UfwIpAddressRange allowInFrom;
        @NotEmpty
        List<@NotNull @Valid PortRange> toAnyPort;
        @NotNull
        Proto proto;
        @Nullable
        String comment;
    }

    @Data
    @Accessors(chain = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ipVersion")
    public static abstract sealed class UfwIpAddressRange {
        @NotNull
        @PositiveOrZero
        Integer maskSize;
        @NotBlank
        String ipAddress;

        @AssertTrue
        @JsonIgnore
        public boolean isMaskSizeLessOrEqualToMax() {
            if (maskSize == null) return true;
            return maskSize <= maxMaskSize();
        }

        public abstract int maxMaskSize();

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        @JsonTypeName("ipv4")
        public static final class UfwIpV4AddressRange extends UfwIpAddressRange {
            @AssertTrue
            @JsonIgnore
            public boolean isValidIpv4Address() {
                try {
                    // noinspection ResultOfMethodCallIgnored
                    Inet4Address.ofLiteral(ipAddress);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }

            @Override
            public int maxMaskSize() {
                return 32;
            }
        }

        @ToString(callSuper = true)
        @EqualsAndHashCode(callSuper = true)
        @Data
        @Accessors(chain = true)
        @JsonTypeName("ipv6")
        public static final class UfwIpV6AddressRange extends UfwIpAddressRange {
            @AssertTrue
            @JsonIgnore
            public boolean isValidIpv6Address() {
                try {
                    // noinspection ResultOfMethodCallIgnored
                    Inet6Address.ofLiteral(ipAddress);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }

            @Override
            public int maxMaskSize() {
                return 128;
            }
        }
    }

    @Data
    @Accessors(chain = true)
    public static class PortRange {
        private static java.util.regex.Pattern NUMERIC = java.util.regex.Pattern.compile("^\\d{1,8}$");
        private static java.util.regex.Pattern NUM_RANGE = java.util.regex.Pattern.compile("^(\\d{1,8}):(\\d{1,8})$");

        int from;
        int to;

        @JsonCreator
        public static PortRange parse(String expression) {
            {
                var matcher = NUMERIC.matcher(expression);
                if (matcher.matches()) {
                    var value = Integer.parseInt(expression);
                    return new PortRange().setFrom(value).setTo(value);
                }
            }

            {
                var matcher = NUM_RANGE.matcher(expression);
                if (matcher.matches()) {
                    return new PortRange()
                            .setFrom(Integer.parseInt(matcher.group(1)))
                            .setTo(Integer.parseInt(matcher.group(2)));
                }
            }

            throw new UnsupportedOperationException("not recognized expression: " + expression);
        }

        @JsonValue
        public String stringify() {
            return from == to ? String.valueOf(from) : from + ":" + to;
        }
    }
}
