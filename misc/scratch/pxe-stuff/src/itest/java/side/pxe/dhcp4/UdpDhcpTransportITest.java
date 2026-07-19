package side.pxe.dhcp4;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static side.pxe.dhcp4.DhcpTransportITestSupport.*;

class UdpDhcpTransportITest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static Set<String> expected(String prefix, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> prefix + i)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void serverProcessesBurstOfRequestsAndRepliesToEveryClientPacket() throws Exception {
        var ports = availablePorts();
        var server = new DhcpServerTransport(ports);
        var client = new DhcpClientTransport(ports);
        int packetCount = 32;
        Set<String> requests = ConcurrentHashMap.newKeySet();
        Set<String> replies = ConcurrentHashMap.newKeySet();
        var repliesReceived = new CountDownLatch(packetCount);

        server.addListener(request -> {
            var requestPayload = payload(request);
            requests.add(requestPayload);
            server.send(packet("offer:" + requestPayload, request.getPort()));
        });
        client.addListener(reply -> {
            replies.add(payload(reply));
            repliesReceived.countDown();
        });

        try {
            server.start();
            client.start();
            for (int i = 0; i < packetCount; i++) {
                client.send(packet("discover:" + i, ports.server()));
            }

            assertTrue(repliesReceived.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS),
                    () -> "received " + replies.size() + " of " + packetCount + " replies");
            assertEquals(expected("discover:", packetCount), requests);
            assertEquals(expected("offer:discover:", packetCount), replies);
        } finally {
            client.stop();
            server.stop();
        }
    }

    @Test
    void listenerFailureDoesNotPreventOtherListenersOrLaterPackets() throws Exception {
        var ports = availablePorts();
        var server = new DhcpServerTransport(ports);
        var client = new DhcpClientTransport(ports);
        var failFirstPacket = new AtomicBoolean(true);
        var replies = new LinkedBlockingQueue<String>();

        server.addListener(ignored -> {
            if (failFirstPacket.getAndSet(false)) {
                throw new IllegalStateException("deliberate listener failure");
            }
        });
        server.addListener(request -> server.send(packet("ack:" + payload(request), request.getPort())));
        client.addListener(reply -> replies.add(payload(reply)));

        try {
            server.start();
            client.start();
            client.send(packet("one", ports.server()));
            client.send(packet("two", ports.server()));

            assertEquals("ack:one", replies.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
            assertEquals("ack:two", replies.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
        } finally {
            client.stop();
            server.stop();
        }
    }

    @Test
    void transportsCanStopAndRestartOnTheSamePorts() throws Exception {
        var ports = availablePorts();
        var server = new DhcpServerTransport(ports);
        var client = new DhcpClientTransport(ports);
        var received = new LinkedBlockingQueue<String>();
        server.addListener(packet -> received.add(payload(packet)));

        try {
            for (int round = 0; round < 2; round++) {
                server.start();
                client.start();
                client.send(packet("round:" + round, ports.server()));
                assertEquals("round:" + round, received.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
                client.stop();
                server.stop();
                assertFalse(client.isStarted());
                assertFalse(server.isStarted());
            }
        } finally {
            client.stop();
            server.stop();
        }
    }
}
