package side.pxe.dhcp3;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Data
@Accessors(chain = true)
public abstract class DhcpTransport implements AutoCloseable {
    protected List<Consumer<Message>> serverConsumers;
    protected List<Consumer<Message>> clientConsumers;
    protected Event started;

    public void addEventListener(Event event, Consumer<Message> messageConsumer) {
        (switch (event) {
            case CLIENT_MESSAGE -> clientConsumers = Objects.requireNonNullElseGet(clientConsumers, ArrayList::new);
            case SERVER_MESSAGE -> serverConsumers = Objects.requireNonNullElseGet(serverConsumers, ArrayList::new);
        }).add(messageConsumer);
    }

    public abstract void emitEvent(Event event, Message message);

    protected void dispatchEvent(Event event, Message message) {
        if (started == null) {
            log.debug("dispatching message with type {}: {} on stopped transport: {}", event, message, this);
            return;
        }

        if (event != started) {
            log.debug("currently only support one direction of server - dispatching {} event on a {} side listener", event, started);
            return;
        }

        var consumers = switch (event) {
            case CLIENT_MESSAGE -> clientConsumers;
            case SERVER_MESSAGE -> serverConsumers;
        };

        if (consumers != null) {
            for (Consumer<Message> consumer : consumers) {
                try {
                    consumer.accept(message);
                } catch (Exception e) {
                    log.error("error in consumer '{}': {}", consumer, e.getMessage(), e);
                }
            }
        }
    }

    public void startClient() {
        started = Event.CLIENT_MESSAGE;
    }

    public void startServer() {
        started = Event.SERVER_MESSAGE;
    }

    public void stop() {
        started = null;
    }

    @Override
    public void close() {
        stop();
    }

    public enum Event {
        CLIENT_MESSAGE,
        SERVER_MESSAGE,
    }

    public interface Message {
        static Message of(ByteBuffer byteBuffer) {
            return () -> byteBuffer;
        }

        default DatagramPacket toDatagramPacket() {
            var bb = toByteBuffer();
            if (bb.hasArray()) {
                var offset = bb.arrayOffset();
                var start = offset + bb.position();
                var end = offset + bb.capacity();
                return new DatagramPacket(bb.array(), start, end);
            }

            var buf = new byte[bb.remaining()];
            bb.get(bb.position(), buf);
            return new DatagramPacket(buf, buf.length);
        }

        ByteBuffer toByteBuffer();
    }
}
