package side.pxe;

import side.pxe.dhcp4.BaseDhcpTransport;
import side.pxe.dhcp4.DhcpServerTransport;
import side.pxe.dhcp4.logic.DhcpServerService;
import side.pxe.dhcp4.logic.PxeBootConfiguration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public final class PxeDhcpServer {
    private PxeDhcpServer() {
    }

    public static void main(String[] args) throws Exception {
        var serverAddress = address("PXE_SERVER_ADDRESS", "192.168.50.1");
        var offeredAddress = address("PXE_OFFER_ADDRESS", "192.168.50.100");
        var subnetMask = address("PXE_SUBNET_MASK", "255.255.255.0");
        var bootFile = setting("PXE_BOOT_FILE", "pxelinux.0");
        var lease = new DhcpServerService.LeaseOffer(
                offeredAddress,
                Duration.ofSeconds(Long.parseLong(setting("PXE_LEASE_SECONDS", "3600"))));

        var pxe = new PxeBootConfiguration(serverAddress, bootFile);
        var config = DhcpServerService.ServerConfig.broadcast(
                serverAddress,
                subnetMask,
                List.of(),
                List.of(),
                BaseDhcpTransport.Ports.DEFAULT,
                pxe);
        var allocator = new SingleAddressAllocator(lease);
        var server = new DhcpServerService(
                new DhcpServerTransport(BaseDhcpTransport.Ports.DEFAULT),
                config,
                allocator);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "pxe-dhcp-shutdown"));
        server.start();
        System.out.printf(
                "PXE DHCP server listening on %s:67; offering %s and boot file %s%n",
                serverAddress.getHostAddress(), offeredAddress.getHostAddress(), bootFile);
        new CountDownLatch(1).await();
    }

    private static Inet4Address address(String name, String fallback) {
        var address = InetAddress.ofLiteral(setting(name, fallback));
        if (address instanceof Inet4Address ipv4) {
            return ipv4;
        }
        throw new IllegalArgumentException(name + " must be an IPv4 address");
    }

    private static String setting(String name, String fallback) {
        var value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private record SingleAddressAllocator(DhcpServerService.LeaseOffer lease)
            implements DhcpServerService.LeaseAllocator {
        @Override
        public Optional<DhcpServerService.LeaseOffer> offer(DhcpServerService.OfferRequest request) {
            if (request.preferredAddress().isEmpty()
                    || request.preferredAddress().orElseThrow().equals(lease.address())) {
                return Optional.of(lease);
            }
            return Optional.empty();
        }

        @Override
        public DhcpServerService.LeaseDecision request(DhcpServerService.LeaseRequest request) {
            if (request.requestedAddress().equals(lease.address())) {
                return new DhcpServerService.LeaseDecision.Ack(lease);
            }
            return new DhcpServerService.LeaseDecision.Nak("address is outside the single-address PXE pool");
        }
    }
}
