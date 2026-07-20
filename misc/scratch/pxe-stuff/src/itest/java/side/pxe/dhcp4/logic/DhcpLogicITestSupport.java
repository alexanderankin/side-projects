package side.pxe.dhcp4.logic;

import side.pxe.dhcp4.BaseDhcpTransport;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static side.pxe.dhcp4.logic.DhcpServerService.*;

final class DhcpLogicITestSupport {
    static final EthernetAddress CLIENT = EthernetAddress.parse("02:00:00:00:00:01");
    static final Inet4Address SERVER = ip("10.0.0.1");
    static final Inet4Address SUBNET = ip("255.255.255.0");
    static final List<Inet4Address> ROUTERS = List.of(ip("10.0.0.1"));
    static final List<Inet4Address> DNS = List.of(ip("1.1.1.1"), ip("8.8.8.8"));
    static final PxeBootConfiguration PXE = new PxeBootConfiguration(ip("10.0.0.2"), "pxelinux.0");

    private DhcpLogicITestSupport() {
    }

    static DhcpClientService.ClientConfig clientConfig(BaseDhcpTransport.Ports ports,
                                                        AtomicInteger transactionIds) {
        return new DhcpClientService.ClientConfig(
                CLIENT,
                new InetSocketAddress(InetAddress.getLoopbackAddress(), ports.server()),
                Duration.ofMillis(500),
                Duration.ofMillis(500),
                transactionIds::getAndIncrement);
    }

    static ServerConfig serverConfig(BaseDhcpTransport.Ports ports) {
        return new ServerConfig(
                SERVER,
                SUBNET,
                ROUTERS,
                DNS,
                new InetSocketAddress(InetAddress.getLoopbackAddress(), ports.client()),
                Optional.of(PXE));
    }

    static BaseDhcpTransport.Ports availablePorts() throws Exception {
        try (var server = new DatagramSocket(0); var client = new DatagramSocket(0)) {
            return new BaseDhcpTransport.Ports(server.getLocalPort(), client.getLocalPort());
        }
    }

    static Inet4Address ip(String value) {
        return (Inet4Address) InetAddress.ofLiteral(value);
    }

    static final class TrackingAllocator implements LeaseAllocator {
        final ConcurrentHashMap<Integer, LeaseOffer> offers = new ConcurrentHashMap<>();
        final AtomicInteger offerCalls = new AtomicInteger();
        final AtomicInteger requestCalls = new AtomicInteger();
        volatile boolean nak;
        volatile boolean failNextOffer;

        @Override
        public Optional<LeaseOffer> offer(OfferRequest request) {
            offerCalls.incrementAndGet();
            if (failNextOffer) {
                failNextOffer = false;
                throw new IllegalStateException("deliberate allocator failure");
            }
            int host = 20 + Math.floorMod(request.transactionId(), 200);
            var offer = new LeaseOffer(ip("10.0.0." + host), Duration.ofHours(1));
            offers.put(request.transactionId(), offer);
            return Optional.of(offer);
        }

        @Override
        public LeaseDecision request(LeaseRequest request) {
            requestCalls.incrementAndGet();
            if (nak) {
                return new LeaseDecision.Nak("test rejection");
            }
            var offer = offers.get(request.transactionId());
            if (offer == null || !offer.address().equals(request.requestedAddress())) {
                return new LeaseDecision.Nak("no matching offer");
            }
            return new LeaseDecision.Ack(offer);
        }
    }
}
