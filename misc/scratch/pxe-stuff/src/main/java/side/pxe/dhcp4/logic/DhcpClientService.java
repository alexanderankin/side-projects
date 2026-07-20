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
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

@Slf4j
public final class DhcpClientService implements AutoCloseable {
    private static final int XID_ATTEMPTS = 128;

    private final BaseDhcpTransport transport;
    private final ClientConfig config;
    private final DhcpWire wire;
    private final ConcurrentMap<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    private final Consumer<DatagramPacket> listener = this::receive;
    private final Object lifecycleLock = new Object();
    @Getter
    private volatile boolean started;
    private ScheduledExecutorService scheduler;

    public DhcpClientService(BaseDhcpTransport transport, ClientConfig config) {
        this(transport, config, PooledByteBufAllocator.DEFAULT);
    }

    public DhcpClientService(BaseDhcpTransport transport, ClientConfig config,
                             ByteBufAllocator byteBufAllocator) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.config = Objects.requireNonNull(config, "config");
        this.wire = new DhcpWire(byteBufAllocator);
    }

    public void start() {
        synchronized (lifecycleLock) {
            if (started) {
                log.debug("DHCP client service is already started for {}", config.client());
                return;
            }
            scheduler = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual().name("DhcpClientService-timeouts").factory());
            transport.addListener(listener);
            try {
                transport.start();
                started = true;
                log.debug("started DHCP client service for {} using server destination {}",
                        config.client(), config.serverDestination());
            } catch (RuntimeException e) {
                log.error("failed to start DHCP client service for {}: {}",
                        config.client(), e.getMessage(), e);
                transport.removeListener(listener);
                scheduler.shutdownNow();
                scheduler = null;
                throw e;
            }
        }
    }

    public CompletableFuture<ClientLease> acquire(AcquireRequest request) {
        Objects.requireNonNull(request, "request");
        if (!started) {
            throw new IllegalStateException("client service is not started");
        }

        Transaction transaction = null;
        for (int attempt = 0; attempt < XID_ATTEMPTS; attempt++) {
            int xid = config.transactionIds().getAsInt();
            var candidate = new Transaction(xid, request);
            if (transactions.putIfAbsent(xid, candidate) == null) {
                transaction = candidate;
                break;
            }
        }
        if (transaction == null) {
            throw new IllegalStateException("could not allocate a unique DHCP transaction ID");
        }

        var selected = transaction;
        selected.future.whenComplete((ignored, failure) -> {
            if (selected.future.isCancelled()) {
                fail(selected, new CancellationException("DHCP acquisition cancelled"));
            }
        });
        try {
            synchronized (selected) {
                log.debug("sending DHCPDISCOVER for transaction {} from {}",
                        xid(selected.xid), config.client());
                selected.timeout = scheduleTimeout(selected, Phase.WAITING_FOR_OFFER, config.offerTimeout());
                try (var packet = wire.discover(selected.xid, config.client(), request.preferredAddress())) {
                    transport.send(packet.packet().toDatagramPacket(config.serverDestination()));
                }
            }
        } catch (RuntimeException e) {
            fail(selected, e);
        }
        return selected.future;
    }

    public CompletableFuture<ClientLease> acquire() {
        return acquire(AcquireRequest.anyAddress());
    }

    public void stop() {
        synchronized (lifecycleLock) {
            if (!started) {
                log.debug("DHCP client service is already stopped for {}", config.client());
                return;
            }
            int activeTransactions = transactions.size();
            started = false;
            transport.removeListener(listener);
            transactions.values().forEach(transaction ->
                    fail(transaction, new CancellationException("DHCP client service stopped")));
            transactions.clear();
            scheduler.shutdownNow();
            scheduler = null;
            transport.stop();
            log.debug("stopped DHCP client service for {}; cancelled {} active transactions",
                    config.client(), activeTransactions);
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void receive(DatagramPacket datagram) {
        log.trace("received {}-byte DHCP datagram from {}",
                datagram.getLength(), datagram.getSocketAddress());
        try {
            var packet = DhcpPacket.from(datagram);
            if (packet.op() != DhcpWire.BOOT_REPLY || !packet.hasMagicCookie()) {
                log.debug("ignoring non-DHCP-reply datagram from {}", datagram.getSocketAddress());
                return;
            }
            var transaction = transactions.get(packet.transactionId());
            if (transaction == null) {
                log.debug("ignoring DHCP reply for unknown transaction {}", xid(packet.transactionId()));
                return;
            }
            if (!packet.ethernetAddress().equals(config.client())) {
                log.debug("ignoring DHCP reply for transaction {} addressed to another client",
                        xid(packet.transactionId()));
                return;
            }
            var options = packet.options();
            var type = options.messageType().orElse(null);
            if (type == null) {
                log.debug("ignoring DHCP reply without a message type for transaction {}",
                        xid(packet.transactionId()));
                return;
            }
            log.debug("received DHCP{} for transaction {} while in phase {}",
                    type, xid(packet.transactionId()), transaction.phase);
            synchronized (transaction) {
                if (type == DhcpMessageType.OFFER && transaction.phase == Phase.WAITING_FOR_OFFER) {
                    acceptOffer(transaction, packet, options);
                } else if ((type == DhcpMessageType.ACK || type == DhcpMessageType.NAK)
                        && transaction.phase == Phase.WAITING_FOR_ACK) {
                    acceptDecision(transaction, packet, options, type);
                } else {
                    log.debug("ignoring unexpected DHCP{} for transaction {} in phase {}",
                            type, xid(transaction.xid), transaction.phase);
                }
            }
        } catch (RuntimeException e) {
            log.debug("ignoring malformed DHCP response: {}", e.getMessage(), e);
        }
    }

    private void acceptOffer(Transaction transaction, DhcpPacket packet, DhcpOptions options) {
        log.trace("validating DHCPOFFER for transaction {}", xid(transaction.xid));
        var serverIdentifier = options.ipv4(SupportedOption.SERVER_IDENTIFIER).orElseThrow();
        var offeredAddress = DhcpWire.ipv4(packet.yourIp());
        if (DhcpWire.isZero(offeredAddress)) {
            throw new IllegalArgumentException("DHCPOFFER has no offered address");
        }
        transaction.serverIdentifier = serverIdentifier;
        transaction.offeredAddress = offeredAddress;
        transaction.phase = Phase.WAITING_FOR_ACK;
        transaction.timeout.cancel(false);
        transaction.timeout = scheduleTimeout(transaction, Phase.WAITING_FOR_ACK, config.ackTimeout());
        log.debug("accepted offer of {} from {}; sending DHCPREQUEST for transaction {}",
                offeredAddress.getHostAddress(), serverIdentifier.getHostAddress(), xid(transaction.xid));
        try (var request = wire.request(transaction.xid, config.client(), offeredAddress, serverIdentifier)) {
            transport.send(request.packet().toDatagramPacket(config.serverDestination()));
        }
    }

    private void acceptDecision(Transaction transaction, DhcpPacket packet, DhcpOptions options,
                                DhcpMessageType type) {
        log.trace("validating DHCP{} for transaction {}", type, xid(transaction.xid));
        var serverIdentifier = options.ipv4(SupportedOption.SERVER_IDENTIFIER).orElseThrow();
        if (!serverIdentifier.equals(transaction.serverIdentifier)) {
            log.debug("ignoring DHCP{} for transaction {} from unexpected server {}",
                    type, xid(transaction.xid), serverIdentifier.getHostAddress());
            return;
        }
        if (type == DhcpMessageType.NAK) {
            log.debug("received DHCPNAK from {} for transaction {}",
                    serverIdentifier.getHostAddress(), xid(transaction.xid));
            failLocked(transaction, new DhcpNakException("server rejected DHCP request"));
            return;
        }

        var address = DhcpWire.ipv4(packet.yourIp());
        if (!address.equals(transaction.offeredAddress)) {
            log.debug("ignoring DHCPACK for transaction {} because address {} does not match offer {}",
                    xid(transaction.xid), address.getHostAddress(),
                    transaction.offeredAddress.getHostAddress());
            return;
        }
        var lease = new ClientLease(
                address,
                serverIdentifier,
                options.ipv4(SupportedOption.SUBNET_MASK).orElseThrow(),
                options.ipv4List(SupportedOption.ROUTER),
                options.ipv4List(SupportedOption.DNS_SERVERS),
                options.leaseTime().orElseThrow(),
                pxeBoot(options));
        transaction.phase = Phase.COMPLETE;
        transaction.timeout.cancel(false);
        transactions.remove(transaction.xid, transaction);
        transaction.future.complete(lease);
        log.debug("completed DHCP transaction {} with lease {} for {}",
                xid(transaction.xid), address.getHostAddress(), lease.leaseTime());
    }

    private ScheduledFuture<?> scheduleTimeout(Transaction transaction, Phase phase, Duration timeout) {
        return scheduler.schedule(() -> {
            synchronized (transaction) {
                if (transaction.phase == phase) {
                    failLocked(transaction, new TimeoutException("timed out waiting for DHCP "
                            + (phase == Phase.WAITING_FOR_OFFER ? "OFFER" : "ACK")));
                }
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void fail(Transaction transaction, Throwable failure) {
        synchronized (transaction) {
            failLocked(transaction, failure);
        }
    }

    private void failLocked(Transaction transaction, Throwable failure) {
        log.trace("failing DHCP transaction {} in phase {}: {}",
                xid(transaction.xid), transaction.phase, failure.getMessage());
        if (transaction.phase == Phase.COMPLETE) {
            log.trace("DHCP transaction {} is already complete", xid(transaction.xid));
            return;
        }
        transaction.phase = Phase.COMPLETE;
        if (transaction.timeout != null) {
            log.trace("cancelling timeout for DHCP transaction {}", xid(transaction.xid));
            transaction.timeout.cancel(false);
        }
        transactions.remove(transaction.xid, transaction);
        transaction.future.completeExceptionally(failure);
    }

    private static Optional<PxeBootConfiguration> pxeBoot(DhcpOptions options) {
        var serverName = options.ascii(SupportedOption.TFTP_SERVER_NAME);
        var bootFile = options.ascii(SupportedOption.BOOTFILE_NAME);
        if (serverName.isEmpty() && bootFile.isEmpty()) {
            return Optional.empty();
        }
        if (serverName.isEmpty() || bootFile.isEmpty()) {
            throw new IllegalArgumentException("PXE boot response must contain both options 66 and 67");
        }
        var address = InetAddress.ofLiteral(serverName.orElseThrow());
        if (!(address instanceof Inet4Address nextServer)) {
            throw new IllegalArgumentException("PXE next server must be an IPv4 address");
        }
        return Optional.of(new PxeBootConfiguration(nextServer, bootFile.orElseThrow()));
    }

    private enum Phase {
        WAITING_FOR_OFFER,
        WAITING_FOR_ACK,
        COMPLETE
    }

    private static final class Transaction {
        private final int xid;
        private final AcquireRequest request;
        private final CompletableFuture<ClientLease> future = new CompletableFuture<>();
        private Phase phase = Phase.WAITING_FOR_OFFER;
        private Inet4Address serverIdentifier;
        private Inet4Address offeredAddress;
        private ScheduledFuture<?> timeout;

        private Transaction(int xid, AcquireRequest request) {
            this.xid = xid;
            this.request = request;
        }
    }

    public record ClientConfig(EthernetAddress client, InetSocketAddress serverDestination,
                               Duration offerTimeout, Duration ackTimeout, IntSupplier transactionIds) {
        public ClientConfig {
            Objects.requireNonNull(client, "client");
            Objects.requireNonNull(serverDestination, "serverDestination");
            requirePositive(offerTimeout, "offerTimeout");
            requirePositive(ackTimeout, "ackTimeout");
            Objects.requireNonNull(transactionIds, "transactionIds");
        }

        public static ClientConfig broadcast(EthernetAddress client, BaseDhcpTransport.Ports ports) {
            return new ClientConfig(client,
                    new InetSocketAddress(InetAddress.ofLiteral("255.255.255.255"), ports.server()),
                    Duration.ofSeconds(3), Duration.ofSeconds(3),
                    () -> ThreadLocalRandom.current().nextInt());
        }
    }

    public record AcquireRequest(Optional<Inet4Address> preferredAddress) {
        public AcquireRequest {
            preferredAddress = Objects.requireNonNull(preferredAddress, "preferredAddress");
        }

        public static AcquireRequest anyAddress() {
            return new AcquireRequest(Optional.empty());
        }
    }

    public record ClientLease(Inet4Address address, Inet4Address serverIdentifier,
                              Inet4Address subnetMask, List<Inet4Address> routers,
                              List<Inet4Address> dnsServers, Duration leaseTime,
                              Optional<PxeBootConfiguration> pxeBoot) {
        public ClientLease {
            Objects.requireNonNull(address, "address");
            Objects.requireNonNull(serverIdentifier, "serverIdentifier");
            Objects.requireNonNull(subnetMask, "subnetMask");
            routers = List.copyOf(routers);
            dnsServers = List.copyOf(dnsServers);
            requirePositive(leaseTime, "leaseTime");
            pxeBoot = Objects.requireNonNull(pxeBoot, "pxeBoot");
        }
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static String xid(int transactionId) {
        return Integer.toUnsignedString(transactionId);
    }
}
