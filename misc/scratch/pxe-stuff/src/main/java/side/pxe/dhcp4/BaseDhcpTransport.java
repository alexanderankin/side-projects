package side.pxe.dhcp4;

import io.netty.buffer.PooledByteBufAllocator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
@Accessors(chain = true)
@Data
public abstract class BaseDhcpTransport {
    protected final List<Consumer<DatagramPacket>> listeners = new CopyOnWriteArrayList<>();
    @Getter(AccessLevel.NONE)
    protected final Object lock = new Object();
    protected final Ports ports;
    protected volatile boolean started;
    @Setter(AccessLevel.NONE)
    private Thread thread;

    @SneakyThrows
    public void start() {
        synchronized (lock) {
            if (started)
                return;

            started = true;
            try {
                doStart();
            } catch (Exception e) {
                started = false;
                throw e;
            }
        }
    }

    @SneakyThrows
    public void stop() {
        synchronized (lock) {
            if (!started)
                return;

            started = false;
            doStop();

            if (thread != null && thread != Thread.currentThread()) {
                if (!thread.join(Duration.ofSeconds(3))) {
                    thread.interrupt();
                }
            }
        }
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public void addListener(Consumer<DatagramPacket> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<DatagramPacket> listener) {
        listeners.remove(listener);
    }

    public abstract void send(DatagramPacket packet);

    @SneakyThrows
    protected DatagramSocket datagramSocket(InetSocketAddress bind) {
        DatagramSocket listener = new DatagramSocket(null);
        listener.setBroadcast(true);
        listener.setReuseAddress(true);
        listener.bind(bind);
        startListening(listener);
        return listener;
    }

    protected void startListening(DatagramSocket socket) {
        thread = Thread.ofVirtual().name(getClass().getSimpleName()).start(() -> doListen(socket));
    }

    @SneakyThrows
    private void doListen(DatagramSocket socket) {
        while (started) {
            var buf = PooledByteBufAllocator.DEFAULT.heapBuffer(2048);
            try {
                var packet = new DatagramPacket(buf.array(), buf.arrayOffset(), buf.capacity());
                socket.receive(packet);
                dispatch(packet);
            } catch (SocketException e) {
                if (e.getCause() instanceof AsynchronousCloseException) {
                    log.debug("thread saw that socket is closed");
                    return;
                }
                log.warn("SocketException on thread", e);
            } catch (Exception e) {
                if (!started) {
                    log.debug("error while exiting thread", e);
                } else {
                    log.warn("error on thread during socket.receive", e);
                }
            } finally {
                buf.release();
            }
        }
    }

    protected void dispatch(DatagramPacket packet) {
        for (var listener : listeners) {
            try {
                listener.accept(packet);
            } catch (Exception e) {
                log.error("error with listener: {}: {}", listener, e.getMessage(), e);
            }
        }
    }

    public record Ports(int server, int client) {
        InetSocketAddress clientAnyInterface() {
            return new InetSocketAddress(client);
        }

        InetSocketAddress serverAnyInterface() {
            return new InetSocketAddress(server);
        }
    }
}
