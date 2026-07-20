package side.pxe.dhcp4.logic;

import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;
import side.pxe.dhcp4.BaseDhcpTransport;
import side.pxe.dhcp4.InMemoryDhcpTransport;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static side.pxe.dhcp4.logic.DhcpLogicITestSupport.*;

class InMemoryDhcpServiceITest {
    @Test
    void completesDoraAndCorrelatesMultipleTransactions() {
        var ports = BaseDhcpTransport.Ports.DEFAULT;
        var serverTransport = new InMemoryDhcpTransport(ports);
        var clientTransport = new InMemoryDhcpTransport(ports).connectTo(serverTransport);
        var allocator = new TrackingAllocator();
        var byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
        var transactionIds = new AtomicInteger(1000);
        List<DhcpMessageType> serverMessages = new ArrayList<>();
        List<DhcpMessageType> clientMessages = new ArrayList<>();
        serverTransport.addListener(packet -> serverMessages.add(messageType(packet)));
        clientTransport.addListener(packet -> clientMessages.add(messageType(packet)));

        try (var server = new DhcpServerService(
                serverTransport, serverConfig(ports), allocator, byteBufAllocator);
             var client = new DhcpClientService(
                     clientTransport, clientConfig(ports, transactionIds), byteBufAllocator)) {
            server.start();
            client.start();

            var futures = new ArrayList<java.util.concurrent.CompletableFuture<DhcpClientService.ClientLease>>();
            for (int i = 0; i < 25; i++) {
                futures.add(client.acquire());
            }
            var leases = futures.stream().map(java.util.concurrent.CompletableFuture::join).toList();

            assertEquals(25, leases.size());
            assertEquals(25, leases.stream().map(DhcpClientService.ClientLease::address).distinct().count());
            leases.forEach(lease -> {
                assertEquals(SERVER, lease.serverIdentifier());
                assertEquals(SUBNET, lease.subnetMask());
                assertEquals(ROUTERS, lease.routers());
                assertEquals(DNS, lease.dnsServers());
                assertEquals(java.util.Optional.of(PXE), lease.pxeBoot());
            });
            assertEquals(25, allocator.offerCalls.get());
            assertEquals(25, allocator.requestCalls.get());
            assertEquals(50, serverMessages.size());
            assertEquals(50, clientMessages.size());
            for (int i = 0; i < 25; i++) {
                assertEquals(DhcpMessageType.DISCOVER, serverMessages.get(i * 2));
                assertEquals(DhcpMessageType.REQUEST, serverMessages.get(i * 2 + 1));
                assertEquals(DhcpMessageType.OFFER, clientMessages.get(i * 2));
                assertEquals(DhcpMessageType.ACK, clientMessages.get(i * 2 + 1));
            }
        }
    }

    @Test
    void nakCompletesTheClientFutureExceptionallyWithoutStoppingServices() {
        var ports = BaseDhcpTransport.Ports.DEFAULT;
        var serverTransport = new InMemoryDhcpTransport(ports);
        var clientTransport = new InMemoryDhcpTransport(ports).connectTo(serverTransport);
        var allocator = new TrackingAllocator();
        allocator.nak = true;

        try (var server = new DhcpServerService(serverTransport, serverConfig(ports), allocator);
             var client = new DhcpClientService(clientTransport,
                     clientConfig(ports, new AtomicInteger(2000)))) {
            server.start();
            client.start();

            var failure = assertThrows(CompletionException.class, () -> client.acquire().join());
            assertInstanceOf(DhcpNakException.class, failure.getCause());
            assertTrue(server.isStarted());
            assertTrue(client.isStarted());
        }
    }

    @Test
    void allocatorFailureAndMalformedPacketOnlyLoseTheAffectedTransaction() {
        var ports = BaseDhcpTransport.Ports.DEFAULT;
        var serverTransport = new InMemoryDhcpTransport(ports);
        var clientTransport = new InMemoryDhcpTransport(ports).connectTo(serverTransport);
        var allocator = new TrackingAllocator();
        allocator.failNextOffer = true;
        var normalConfig = clientConfig(ports, new AtomicInteger(4000));
        var quickTimeoutConfig = new DhcpClientService.ClientConfig(
                normalConfig.client(), normalConfig.serverDestination(),
                Duration.ofMillis(75), Duration.ofMillis(75), normalConfig.transactionIds());

        try (var server = new DhcpServerService(serverTransport, serverConfig(ports), allocator);
             var client = new DhcpClientService(clientTransport, quickTimeoutConfig)) {
            server.start();
            client.start();

            var failedAcquisition = client.acquire();
            var wire = new DhcpWire(io.netty.buffer.UnpooledByteBufAllocator.DEFAULT);
            var unrelatedOffer = new DhcpServerService.LeaseOffer(ip("10.0.0.99"), Duration.ofHours(1));
            try (var unrelatedRequest = wire.discover(9999, CLIENT, java.util.Optional.empty());
                 var unrelatedReply = wire.reply(unrelatedRequest.packet(), DhcpMessageType.OFFER,
                         unrelatedOffer.address(), serverConfig(ports), unrelatedOffer.leaseTime())) {
                serverTransport.send(unrelatedReply.packet().toDatagramPacket(
                        new InetSocketAddress(InetAddress.getLoopbackAddress(), ports.client())));
            }

            var firstFailure = assertThrows(CompletionException.class, failedAcquisition::join);
            assertInstanceOf(TimeoutException.class, firstFailure.getCause());

            var malformed = new DatagramPacket(new byte[DhcpPacket.OPTIONS_OFFSET], DhcpPacket.OPTIONS_OFFSET);
            malformed.setSocketAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), ports.client()));
            serverTransport.send(malformed);

            var lease = client.acquire().join();
            assertEquals(ip("10.0.0.21"), lease.address());
            assertTrue(server.isStarted());
            assertTrue(client.isStarted());
            assertEquals(2, allocator.offerCalls.get());
            assertEquals(1, allocator.requestCalls.get());
        }
    }

    private static DhcpMessageType messageType(java.net.DatagramPacket datagram) {
        return DhcpPacket.from(datagram).options().messageType().orElseThrow();
    }
}
