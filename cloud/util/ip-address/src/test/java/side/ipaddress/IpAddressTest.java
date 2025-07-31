package side.ipaddress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpAddressTest {
    @Test
    void test_ipv4AddressAsString() {
        System.out.println(IpAddress.ipAddress("127.0.0.1"));
        System.out.println(IpAddress.ipAddress("2001:db8:0:0:0:0:0:1"));
    }

}
