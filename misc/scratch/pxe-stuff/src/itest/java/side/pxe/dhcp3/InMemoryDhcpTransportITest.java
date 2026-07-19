package side.pxe.dhcp3;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryDhcpTransportITest {

    @SneakyThrows
    @Test
    void test() {
        try (var s = new InMemoryDhcpTransport();
             var c = new InMemoryDhcpTransport().connectToServer(s)) {

            CompletableFuture<DhcpTransport.Message> m = new CompletableFuture<>();
            s.addEventListener(DhcpTransport.Event.SERVER_MESSAGE, m::complete);
            c.emitEvent(DhcpTransport.Event.SERVER_MESSAGE,
                    DhcpTransport.Message.of(StandardCharsets.UTF_8.encode(CharBuffer.wrap("hello"))));

            assertNotNull(m.get(3, TimeUnit.SECONDS));
            var mValue = m.join();
            System.out.println(mValue.toByteBuffer().asCharBuffer());
        }
    }
}
