package side.pxe.dhcp4;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static side.pxe.dhcp4.DhcpTransportITestSupport.packet;
import static side.pxe.dhcp4.DhcpTransportITestSupport.payload;

class InMemoryDhcpTransportITest {
    @Test
    void serverProcessesMultiplePacketsAndSurvivesAListenerFailure() {
        var ports = BaseDhcpTransport.Ports.DEFAULT;
        var server = new InMemoryDhcpTransport(ports);
        var client = new InMemoryDhcpTransport(ports).connectTo(server);
        var failFirstPacket = new AtomicBoolean(true);
        List<String> requests = new ArrayList<>();
        List<String> replies = new ArrayList<>();

        server.addListener(ignored -> {
            if (failFirstPacket.getAndSet(false)) {
                throw new IllegalStateException("deliberate listener failure");
            }
        });
        server.addListener(request -> {
            var requestPayload = payload(request);
            requests.add(requestPayload);
            server.send(packet("offer:" + requestPayload));
        });
        client.addListener(reply -> replies.add(payload(reply)));

        try {
            server.start();
            client.start();
            for (int i = 0; i < 50; i++) {
                client.send(packet("discover:" + i));
            }
        } finally {
            client.stop();
            server.stop();
        }

        assertEquals(expected("discover:", 50), requests);
        assertEquals(expected("offer:discover:", 50), replies);
    }

    private static List<String> expected(String prefix, int count) {
        var values = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            values.add(prefix + i);
        }
        return values;
    }
}
