package side.pxe.dhcp3;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class UdpSocketDhcpTransportITest {
    @SneakyThrows
    @Test
    void test() {
        log.info("hello world");
        try (var server = new UdpSocketDhcpTransport();
             var client = new UdpSocketDhcpTransport()) {
            // server.setClientListenPort(6868);
            server.setServerListenPort(6767);
            server.startServer();
            client.setClientListenPort(6868);
            // client.setServerListenPort(6767);
            client.startClient();

            var serverEvents = new ArrayList<DhcpTransport.Message>();
            server.addEventListener(DhcpTransport.Event.SERVER_MESSAGE, serverEvents::add);

            var clientEvents = new ArrayList<DhcpTransport.Message>();
            client.addEventListener(DhcpTransport.Event.CLIENT_MESSAGE, clientEvents::add);

            CountDownLatch cdl = new CountDownLatch(2);
            server.addEventListener(DhcpTransport.Event.SERVER_MESSAGE, _ -> cdl.countDown());
            client.addEventListener(DhcpTransport.Event.CLIENT_MESSAGE, _ -> cdl.countDown());

            server.emitEvent(DhcpTransport.Event.CLIENT_MESSAGE, DhcpTransport.Message.of(ByteBuffer.wrap("goodbye".getBytes())));
            client.emitEvent(DhcpTransport.Event.SERVER_MESSAGE, DhcpTransport.Message.of(ByteBuffer.wrap("hello".getBytes())));

            assertTrue(cdl.await(5, TimeUnit.SECONDS));
            log.info("got back as serverEvents: {}", serverEvents.getFirst().toByteBuffer().asCharBuffer());
            log.info("got back as clientEvents: {}", clientEvents.getFirst().toByteBuffer().asCharBuffer());
        }
    }
}
