package side.pxe.dhcp4.logic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SupportedOption {
    PAD(0),
    SUBNET_MASK(1),
    ROUTER(3),
    DNS_SERVERS(6),
    REQUESTED_IP(50),
    LEASE_TIME(51),
    MESSAGE_TYPE(53),
    SERVER_IDENTIFIER(54),
    PARAMETER_REQUEST_LIST(55),
    TFTP_SERVER_NAME(66),
    BOOTFILE_NAME(67),
    END(255);

    private static final Map<Integer, SupportedOption> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(SupportedOption::wireValue, Function.identity()));

    private final int wireValue;

    public static Optional<SupportedOption> fromWireValue(int wireValue) {
        return Optional.ofNullable(MAP.get(wireValue));
    }
}
