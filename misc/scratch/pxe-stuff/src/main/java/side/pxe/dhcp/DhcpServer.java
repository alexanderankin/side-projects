package side.pxe.dhcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HexFormat;

@Slf4j
public class DhcpServer {
    ByteBufAllocator byteBufAllocator = PooledByteBufAllocator.DEFAULT;
    DhcpConfig dhcpConfig;

    @SneakyThrows
    void run() {
        try (var dhcpSockets = new DhcpSocket(dhcpConfig)) {
            log.info("DhcpServer listening on {}", dhcpSockets.socket.getLocalPort());

            while (!Thread.currentThread().isInterrupted()) {
                try (var packet = new DhcpSocket.Packet(byteBufAllocator)) {
                    dhcpSockets.socket.receive(packet.dg);
                    var dhcpRequest = DhcpRequest.parse(packet.buf.nioBuffer());
                    if (dhcpRequest == null) {
                        log.debug("packet was not a DhcpRequest");
                        log.trace("packet was not a DhcpRequest: '{}'", HexFormat.of().formatHex(packet.buf.array()));
                        continue;
                    }
                    try (var reply = new DhcpSocket.Packet(byteBufAllocator)) {
                        switch (dhcpRequest.getMessageType()) {
                            case DHCP_DISCOVER -> {
                            }
                            case DHCP_REQUEST -> {
                            }
                            case null, default -> {
                            }
                        }
                    }
                }
            }
        }
    }

    @Data
    @Accessors(chain = true)
    static class DhcpSocket implements AutoCloseable {
        @ToString.Exclude
        DatagramSocket socket;

        @SneakyThrows
        DhcpSocket(DhcpConfig dhcpConfig) {
            socket = new DatagramSocket(dhcpConfig.serverPorts().listen());
            socket.setBroadcast(true);
        }

        @Override
        public void close() {
            socket.close();
        }

        @Getter
        static class Packet implements AutoCloseable {
            final ByteBuf buf;
            final DatagramPacket dg;

            Packet(ByteBufAllocator byteBufAllocator) {
                buf = byteBufAllocator.buffer(576);
                dg = new DatagramPacket(buf.array(), buf.array().length);
            }

            @Override
            public void close() {
                buf.release();
            }
        }
    }

}
