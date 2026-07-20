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
public enum DhcpMessageType {
    DISCOVER(1),
    OFFER(2),
    REQUEST(3),
    ACK(5),
    NAK(6);

    private static final Map<Integer, DhcpMessageType> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(DhcpMessageType::wireValue, Function.identity()));
    private final int wireValue;

    public static DhcpMessageType fromWireValue(int wireValue) {
        return Optional.ofNullable(MAP.get(wireValue))
                .orElseThrow(() -> new IllegalArgumentException("unsupported DHCP message type: " + wireValue));
    }
}
