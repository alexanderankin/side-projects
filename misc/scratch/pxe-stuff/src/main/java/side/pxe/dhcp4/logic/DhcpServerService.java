package side.pxe.dhcp4.logic;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import side.pxe.dhcp4.BaseDhcpTransport;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public final class DhcpServerService implements AutoCloseable {
    private final BaseDhcpTransport transport;
    private final ServerConfig config;
    private final LeaseAllocator allocator;
    private final DhcpWire wire;
    private final Consumer<DatagramPacket> listener = this::receive;
    private final Object lifecycleLock = new Object();
    private final Object allocatorLock = new Object();
    @Getter
    private volatile boolean started;

    public DhcpServerService(BaseDhcpTransport transport, ServerConfig config, LeaseAllocator allocator) {
        this(transport, config, allocator, PooledByteBufAllocator.DEFAULT);
    }

    public DhcpServerService(BaseDhcpTransport transport, ServerConfig config, LeaseAllocator allocator,
                             ByteBufAllocator byteBufAllocator) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.config = Objects.requireNonNull(config, "config");
        this.allocator = Objects.requireNonNull(allocator, "allocator");
        this.wire = new DhcpWire(byteBufAllocator);
    }

    public void start() {
        synchronized (lifecycleLock) {
            if (started) {
                log.debug("DHCP server service is already started as {}",
                        config.serverIdentifier().getHostAddress());
                return;
            }
            transport.addListener(listener);
            try {
                transport.start();
                started = true;
                log.debug("started DHCP server service as {}; replies target {}",
                        config.serverIdentifier().getHostAddress(), config.clientDestination());
            } catch (RuntimeException e) {
                log.error("failed to start DHCP server service as {}: {}",
                        config.serverIdentifier().getHostAddress(), e.getMessage(), e);
                transport.removeListener(listener);
                throw e;
            }
        }
    }

    public void stop() {
        synchronized (lifecycleLock) {
            if (!started) {
                log.debug("DHCP server service is already stopped as {}",
                        config.serverIdentifier().getHostAddress());
                return;
            }
            started = false;
            transport.removeListener(listener);
            transport.stop();
            log.debug("stopped DHCP server service as {}", config.serverIdentifier().getHostAddress());
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void receive(DatagramPacket datagram) {
        log.trace("received {}-byte DHCP server datagram from {}",
                datagram.getLength(), datagram.getSocketAddress());
        try {
            var packet = DhcpPacket.from(datagram);
            if (packet.op() != DhcpWire.BOOT_REQUEST || !packet.hasMagicCookie()) {
                log.debug("ignoring non-DHCP-request datagram from {}", datagram.getSocketAddress());
                return;
            }
            var client = packet.ethernetAddress();
            var options = packet.options();
            var type = options.messageType().orElse(null);
            if (type == DhcpMessageType.DISCOVER) {
                handleDiscover(packet, client, options);
            } else if (type == DhcpMessageType.REQUEST) {
                handleRequest(packet, client, options);
            } else {
                log.debug("ignoring DHCP{} received by server for transaction {}",
                        type, xid(packet.transactionId()));
            }
        } catch (RuntimeException e) {
            log.debug("ignoring malformed DHCP request: {}", e.getMessage(), e);
        }
    }

    private void handleDiscover(DhcpPacket packet, EthernetAddress client, DhcpOptions options) {
        log.trace("processing DHCPDISCOVER from {} for transaction {}",
                client, xid(packet.transactionId()));
        var request = new OfferRequest(client, packet.transactionId(),
                options.ipv4(SupportedOption.REQUESTED_IP));
        Optional<LeaseOffer> offer;
        synchronized (allocatorLock) {
            offer = allocator.offer(request);
        }
        if (offer.isPresent()) {
            var value = offer.orElseThrow();
            log.debug("offering {} to {} for transaction {}",
                    value.address().getHostAddress(), client, xid(packet.transactionId()));
            sendReply(packet, DhcpMessageType.OFFER, value);
        } else {
            log.debug("allocator declined to offer an address to {} for transaction {}",
                    client, xid(packet.transactionId()));
        }
    }

    private void handleRequest(DhcpPacket packet, EthernetAddress client, DhcpOptions options) {
        log.trace("processing DHCPREQUEST from {} for transaction {}",
                client, xid(packet.transactionId()));
        var serverIdentifier = options.ipv4(SupportedOption.SERVER_IDENTIFIER).orElse(null);
        if (serverIdentifier == null || !serverIdentifier.equals(config.serverIdentifier())) {
            log.debug("ignoring DHCPREQUEST for transaction {} addressed to server {}",
                    xid(packet.transactionId()),
                    serverIdentifier == null ? "<unspecified>" : serverIdentifier.getHostAddress());
            return;
        }
        var requestedAddress = options.ipv4(SupportedOption.REQUESTED_IP).orElseThrow();
        LeaseDecision decision;
        synchronized (allocatorLock) {
            decision = allocator.request(new LeaseRequest(
                    client, packet.transactionId(), requestedAddress, serverIdentifier));
        }
        switch (decision) {
            case LeaseDecision.Ack(var lease) -> {
                log.debug("acknowledging lease {} for {} in transaction {}",
                        lease.address().getHostAddress(), client, xid(packet.transactionId()));
                sendReply(packet, DhcpMessageType.ACK, lease);
            }
            case LeaseDecision.Nak(var reason) -> {
                log.debug("rejecting lease request from {} in transaction {}: {}",
                        client, xid(packet.transactionId()), reason);
                sendNak(packet, reason);
            }
            case LeaseDecision.Ignore ignored -> {
                log.debug("allocator ignored lease request from {} in transaction {}",
                        client, xid(packet.transactionId()));
            }
        }
    }

    private void sendReply(DhcpPacket request, DhcpMessageType type, LeaseOffer lease) {
        try (var reply = wire.reply(request, type, lease.address(), config, lease.leaseTime())) {
            transport.send(reply.packet().toDatagramPacket(config.clientDestination()));
        }
    }

    private void sendNak(DhcpPacket request, String reason) {
        log.debug("rejecting DHCP request {}: {}", Integer.toUnsignedString(request.transactionId()), reason);
        try (var reply = wire.reply(request, DhcpMessageType.NAK,
                DhcpWire.zeroAddress(), config, Duration.ofSeconds(1))) {
            transport.send(reply.packet().toDatagramPacket(config.clientDestination()));
        }
    }

    public record ServerConfig(Inet4Address serverIdentifier, Inet4Address subnetMask,
                               List<Inet4Address> routers, List<Inet4Address> dnsServers,
                               InetSocketAddress clientDestination,
                               Optional<PxeBootConfiguration> pxeBoot) {
        public ServerConfig {
            Objects.requireNonNull(serverIdentifier, "serverIdentifier");
            Objects.requireNonNull(subnetMask, "subnetMask");
            routers = List.copyOf(routers);
            dnsServers = List.copyOf(dnsServers);
            Objects.requireNonNull(clientDestination, "clientDestination");
            pxeBoot = Objects.requireNonNull(pxeBoot, "pxeBoot");
        }

        public ServerConfig(Inet4Address serverIdentifier, Inet4Address subnetMask,
                            List<Inet4Address> routers, List<Inet4Address> dnsServers,
                            InetSocketAddress clientDestination) {
            this(serverIdentifier, subnetMask, routers, dnsServers, clientDestination, Optional.empty());
        }

        public static ServerConfig broadcast(Inet4Address serverIdentifier, Inet4Address subnetMask,
                                             List<Inet4Address> routers, List<Inet4Address> dnsServers,
                                             BaseDhcpTransport.Ports ports) {
            return new ServerConfig(serverIdentifier, subnetMask, routers, dnsServers,
                    new InetSocketAddress(InetAddress.ofLiteral("255.255.255.255"), ports.client()),
                    Optional.empty());
        }

        public static ServerConfig broadcast(Inet4Address serverIdentifier, Inet4Address subnetMask,
                                             List<Inet4Address> routers, List<Inet4Address> dnsServers,
                                             BaseDhcpTransport.Ports ports, PxeBootConfiguration pxeBoot) {
            return new ServerConfig(serverIdentifier, subnetMask, routers, dnsServers,
                    new InetSocketAddress(InetAddress.ofLiteral("255.255.255.255"), ports.client()),
                    Optional.of(pxeBoot));
        }
    }

    public interface LeaseAllocator {
        Optional<LeaseOffer> offer(OfferRequest request);

        LeaseDecision request(LeaseRequest request);
    }

    public record OfferRequest(EthernetAddress client, int transactionId,
                               Optional<Inet4Address> preferredAddress) {
        public OfferRequest {
            Objects.requireNonNull(client, "client");
            preferredAddress = Objects.requireNonNull(preferredAddress, "preferredAddress");
        }
    }

    public record LeaseRequest(EthernetAddress client, int transactionId,
                               Inet4Address requestedAddress, Inet4Address serverIdentifier) {
        public LeaseRequest {
            Objects.requireNonNull(client, "client");
            Objects.requireNonNull(requestedAddress, "requestedAddress");
            Objects.requireNonNull(serverIdentifier, "serverIdentifier");
        }
    }

    public record LeaseOffer(Inet4Address address, Duration leaseTime) {
        public LeaseOffer {
            Objects.requireNonNull(address, "address");
            Objects.requireNonNull(leaseTime, "leaseTime");
            if (leaseTime.isZero() || leaseTime.isNegative()) {
                throw new IllegalArgumentException("leaseTime must be positive");
            }
        }
    }

    public sealed interface LeaseDecision {
        record Ack(LeaseOffer lease) implements LeaseDecision {
            public Ack {
                Objects.requireNonNull(lease, "lease");
            }
        }

        record Nak(String reason) implements LeaseDecision {
            public Nak {
                Objects.requireNonNull(reason, "reason");
            }
        }

        record Ignore() implements LeaseDecision {
        }
    }

    private static String xid(int transactionId) {
        return Integer.toUnsignedString(transactionId);
    }
}
