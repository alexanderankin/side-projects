package side.pxe.dhcp2.normie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import side.pxe.dhcp2.DhcpIpAddress;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DhcpIpAddressRangeTest {
    @ParameterizedTest
    @CsvSource({
            "'0.0.0.0', '0.0.0.0'",
            "'0.0.0.1', '0.0.0.0'",
            "'1.0.0.1', '1.0.0.0'",
            "'1.0.0.1', '1.0.0.1'",
    })
    void testBadConstructorArguments(String from, String to) {
        assertThrows(IllegalArgumentException.class, () -> new DhcpIpAddressRange(DhcpIpAddress.of(from), DhcpIpAddress.of(to)));
    }

    @Test
    void test() {
        assertEquals(List.of("10.0.0.0"),
                new DhcpIpAddressRange(DhcpIpAddress.of("10.0.0.0"), DhcpIpAddress.of("10.0.0.1"))
                        .addressStream()
                        .map(DhcpIpAddress::humanReadableRepresentation)
                        .toList());
    }

}
