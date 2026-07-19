package side.pxe.dhcp3;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class UdpSocketDhcpTransport extends DhcpTransport {
    private final AtomicReference<Sockets> sockets = new AtomicReference<>();
    int serverListenPort = 67;
    int clientListenPort = 68;
    ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
    Supplier<ExecutorService> executorServiceSupplier = Executors::newVirtualThreadPerTaskExecutor;

    @SneakyThrows
    @Override
    public void emitEvent(Event event, Message message) {
        var sockets = this.sockets.get();
        if (sockets == null)
            return;
        var datagramPacket = message.toDatagramPacket();
        switch (event) {
            case CLIENT_MESSAGE -> {
                datagramPacket.setSocketAddress(new InetSocketAddress(InetAddress.ofLiteral("255.255.255.255"), serverListenPort));
                sockets.server.send(datagramPacket);
            }
            case SERVER_MESSAGE -> {
                datagramPacket.setSocketAddress(new InetSocketAddress(InetAddress.ofLiteral("255.255.255.255"), clientListenPort));
                sockets.client.send(datagramPacket);
            }
        }
    }

    @Override
    public void startClient() {
        startSockets(Event.CLIENT_MESSAGE);
    }

    @Override
    public void startServer() {
        startSockets(Event.SERVER_MESSAGE);
    }

    private void startSockets(Event event) {
        var sockets = Sockets.of(this);
        if (this.sockets.compareAndSet(null, sockets)) {
            sockets.start(event);
        }
        switch (event) {
            case CLIENT_MESSAGE -> super.startClient();
            case SERVER_MESSAGE -> super.startServer();
        }
    }

    @SneakyThrows
    public void stop() {
        var sockets = this.sockets.get();
        if (sockets != null) {
            sockets.close();
            this.sockets.compareAndSet(sockets, null);
        }
        super.stop();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class Sockets implements AutoCloseable {
        DatagramSocket server;
        DatagramSocket client;
        ExecutorService executorService;
        UdpSocketDhcpTransport transport;

        @SuppressWarnings("resource")
        static Sockets of(UdpSocketDhcpTransport transport) {
            return new Sockets().setTransport(transport);
        }

        @SneakyThrows
        static DatagramSocket listener(Event event, UdpSocketDhcpTransport transport, ExecutorService executorService) {
            DatagramSocket listener = new DatagramSocket(null);
            listener.setBroadcast(true);
            listener.setReuseAddress(true);
            switch (event) {
                case CLIENT_MESSAGE -> listener.bind(new InetSocketAddress(transport.clientListenPort));
                case SERVER_MESSAGE -> listener.bind(new InetSocketAddress(transport.serverListenPort));
            }
            executorService.submit(() -> {
                while (!executorService.isShutdown()) {
                    var buf = transport.allocator.buffer(2048);
                    try {
                        listener.receive(new DatagramPacket(buf.array(), buf.capacity()));
                        transport.dispatchEvent(event, Message.of(buf.nioBuffer()));
                    } catch (IOException e) {
                        log.error("error during {} receive: {}", event, e.getMessage(), e);
                    } finally {
                        buf.release();
                    }
                }
            });
            return listener;
        }

        void start(Event event) {
            executorService = transport.executorServiceSupplier.get();
            switch (event) {
                case CLIENT_MESSAGE -> client = listener(Event.CLIENT_MESSAGE, transport, executorService);
                case SERVER_MESSAGE -> server = listener(Event.SERVER_MESSAGE, transport, executorService);
            }
        }

        @Override
        public void close() {
            switch (transport.started) {
                case CLIENT_MESSAGE -> client.close();
                case SERVER_MESSAGE -> server.close();
            }
            executorService.shutdown();
        }
    }
}
