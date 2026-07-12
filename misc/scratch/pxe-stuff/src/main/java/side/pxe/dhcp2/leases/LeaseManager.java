package side.pxe.dhcp2.leases;

import side.pxe.dhcp2.DhcpIpAddress;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface LeaseManager {
    DhcpIpAddressRange getRange();

    Lease reserve();

    void release(Lease lease);

    int remaining();

    record Lease(UUID id, String clientId, DhcpIpAddress address) {
    }

    record DhcpIpAddressRange(DhcpIpAddress start, DhcpIpAddress end) {
        public DhcpIpAddressRange {
            if (start.compareTo(end) >= 0) {
                throw new IllegalArgumentException("end must be strictly after start");
            }
        }
    }
}
