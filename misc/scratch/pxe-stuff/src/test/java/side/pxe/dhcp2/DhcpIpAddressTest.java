package side.pxe.dhcp2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class DhcpIpAddressTest {

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "''",
            "1.1.1",
            "1.1.1.a",
            "1.1.1.-1",
            "1.1.1.256",
    }, nullValues = "null")
    void testInvalidStringInputs(String input) {
        assertThrows(IllegalArgumentException.class, () -> DhcpIpAddress.of(input));
    }

    @Test
    void testInvalidByteArrayInput() {
        assertThrows(IllegalArgumentException.class, () -> DhcpIpAddress.of(new byte[3]));
        assertThrows(IllegalArgumentException.class, () -> DhcpIpAddress.of((byte[]) null));
    }

    @Test
    void testInvalidByteBufferInput() {
        assertThrows(IllegalArgumentException.class, () -> DhcpIpAddress.of(ByteBuffer.allocate(3)));
        assertThrows(IllegalArgumentException.class, () -> DhcpIpAddress.of((ByteBuffer) null));
    }

    @ParameterizedTest
    @CsvSource({
            "1.1.1.1",
            "1.1.2.1",
            "1.1.3.1",
            "8.8.8.8",
    })
    void testEqualsHashCode(String input) {
        var treeSet = new TreeSet<DhcpIpAddress>();
        assertTrue(treeSet.add(DhcpIpAddress.of(input)));
        assertFalse(treeSet.add(DhcpIpAddress.of(input).intBased()));
        assertFalse(treeSet.add(DhcpIpAddress.of(input).byteArrayBased()));
        assertFalse(treeSet.add(DhcpIpAddress.of(input).humanReadableBased()));
        assertTrue(treeSet.add(DhcpIpAddress.of(input + "1").intBased()));

        assertEquals(DhcpIpAddress.of(input), DhcpIpAddress.of(input).intBased());
        assertEquals(DhcpIpAddress.of(input), DhcpIpAddress.of(input).byteArrayBased());
        assertEquals(DhcpIpAddress.of(input), DhcpIpAddress.of(input).humanReadableBased());
        assertNotEquals(DhcpIpAddress.of(input), DhcpIpAddress.of(input + "1"));
    }

    @SuppressWarnings({"AssertBetweenInconvertibleTypes", "MisorderedAssertEqualsArguments"})
    @Test
    void testEqualsBad() {
        assertNotEquals(DhcpIpAddress.of("10.0.0.1"), 1);
    }

    @Test
    void test() {
        assertDoesNotThrow(() -> DhcpIpAddress.of(new byte[]{-64, -88, 0, 1}));
        assertDoesNotThrow(() -> DhcpIpAddress.of(new byte[]{-64, -88, 0, 1}).equals(DhcpIpAddress.of("192.168.0.1")));
        assertThrows(Exception.class, () -> {
            Set.of(DhcpIpAddress.of("192.168.0.1"), DhcpIpAddress.of(new byte[]{-64, -88, 0, 1}));
        });
        assertEquals("DhcpIpAddress.StringDhcpIpAddress(super=DhcpIpAddress.BaseIpAddress(humanReadableRepresentation=192.168.0.1, intRepresentation=-1062731775), ipAddress=192.168.0.1)",
                DhcpIpAddress.of("192.168.0.1").toString());
        assertEquals("DhcpIpAddress.IntDhcpIpAddress(super=DhcpIpAddress.BaseIpAddress(humanReadableRepresentation=192.168.0.1, intRepresentation=-1062731775), ipAddress=-1062731775)",
                DhcpIpAddress.of(-1062731775).toString());
        assertEquals(DhcpIpAddress.of(-1062731775), DhcpIpAddress.of("192.168.0.1"));
    }

    @Test
    void representations() {
        assertConvertsCorrectly(DhcpIpAddress.of("10.0.0.1").humanReadableBased());
        assertConvertsCorrectly(DhcpIpAddress.of("10.0.0.1").byteArrayBased());
        assertConvertsCorrectly(DhcpIpAddress.of("10.0.0.1").intBased());
        assertConvertsCorrectly(DhcpIpAddress.of("10.0.0.1").byteBufferBased());
    }

    private void assertConvertsCorrectly(DhcpIpAddress ip) {
        assertEquals(ip, ip.intBased());
        assertEquals(ip.intBased(), ip);
        assertEquals(ip.hashCode(), ip.intBased().hashCode());
        assertEquals(ip, ip.byteArrayBased());
        assertEquals(ip.byteArrayBased(), ip);
        assertEquals(ip.hashCode(), ip.byteArrayBased().hashCode());
        assertEquals(ip, ip.humanReadableBased());
        assertEquals(ip.humanReadableBased(), ip);
        assertEquals(ip.hashCode(), ip.humanReadableBased().hashCode());
        assertEquals(ip, ip.byteBufferBased());
        assertEquals(ip.byteBufferBased(), ip);
        assertEquals(ip.hashCode(), ip.byteBufferBased().hashCode());
    }

    @Test
    void testIntegerMinMax() {
        assertEquals(DhcpIpAddress.of("127.255.255.255"), DhcpIpAddress.of(Integer.MAX_VALUE));
        assertEquals(DhcpIpAddress.of("128.0.0.0"), DhcpIpAddress.of(Integer.MIN_VALUE));
    }

    @ParameterizedTest
    @CsvSource({
            // int vals
            // seems crazy, but...
            "int,min,max,1",
            "int,min,min,0",
            "int,max,min,-1",
            "int,max,max,0",

            // just replace with their ip values:
            // max -> 127.255.255.255
            // min -> 128.0.0.0
            "string,128.0.0.0,127.255.255.255,1",
            "string,128.0.0.0,128.0.0.0,0",
            "string,127.255.255.255,128.0.0.0,-1",
            "string,127.255.255.255,127.255.255.255,0",

            "int,0,0,0",
            "int,0,1,-1",
            "int,1,0,1",
            "int,1,1,0",
            "string,0.0.0.0,255.255.255.255,-1",
            "string,0.0.0.0,0.0.0.0,0",
            "string,0.0.0.0,0.0.0.1,-1",
            "string,0.0.0.1,0.0.0.1,0",
            "string,0.0.0.2,0.0.0.1,1",
            "string,10.0.0.2,0.0.0.1,1",
    })
    void testCompare(String type, String first, String second, int expected) {
        DhcpIpAddress firstIp, secondIp;
        switch (type) {
            case "int" -> {
                firstIp = DhcpIpAddress.of(first.matches("min|max") ? (first.charAt(2) == 'x' ? Integer.MAX_VALUE : Integer.MIN_VALUE) : Integer.parseInt(first));
                secondIp = DhcpIpAddress.of(second.matches("min|max") ? (second.charAt(2) == 'x' ? Integer.MAX_VALUE : Integer.MIN_VALUE) : Integer.parseInt(second));
            }
            case "string" -> {
                firstIp = DhcpIpAddress.of(first);
                secondIp = DhcpIpAddress.of(second);
            }
            case null, default -> throw new UnsupportedOperationException();
        }
        assertEquals(expected, firstIp.compareTo(secondIp));
    }
}
