package side.pxe.dhcp4.logic;

import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;
import side.pxe.dhcp4.DhcpClientTransport;
import side.pxe.dhcp4.DhcpServerTransport;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static side.pxe.dhcp4.logic.DhcpLogicITestSupport.*;

class UdpDhcpServiceITest {
    @Test
    void performsConcurrentRfcFormattedDoraExchangesOverUdp() throws Exception {
        var ports = availablePorts();
        var serverTransport = new DhcpServerTransport(ports);
        var clientTransport = new DhcpClientTransport(ports);
        var allocator = new TrackingAllocator();
        var byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;

        try (var server = new DhcpServerService(
                serverTransport, serverConfig(ports), allocator, byteBufAllocator);
             var client = new DhcpClientService(clientTransport,
                     clientConfig(ports, new AtomicInteger(3000)), byteBufAllocator)) {
            server.start();
            client.start();

            var acquisitions = new ArrayList<CompletableFuture<DhcpClientService.ClientLease>>();
            for (int i = 0; i < 20; i++) {
                acquisitions.add(client.acquire());
            }
            CompletableFuture.allOf(acquisitions.toArray(CompletableFuture[]::new))
                    .get(5, TimeUnit.SECONDS);

            var leases = acquisitions.stream().map(CompletableFuture::join).toList();
            assertEquals(20, leases.stream()
                    .map(DhcpClientService.ClientLease::address).distinct().count());
            assertTrue(leases.stream().allMatch(lease -> lease.pxeBoot().equals(java.util.Optional.of(PXE))));
            assertEquals(20, allocator.offerCalls.get());
            assertEquals(20, allocator.requestCalls.get());
        }

        assertFalse(clientTransport.isStarted());
        assertFalse(serverTransport.isStarted());
    }
}
