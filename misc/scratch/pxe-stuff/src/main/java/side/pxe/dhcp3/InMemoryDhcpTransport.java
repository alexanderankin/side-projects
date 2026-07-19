package side.pxe.dhcp3;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class InMemoryDhcpTransport extends DhcpTransport {
    @ToString.Exclude
    InMemoryDhcpTransport client;
    @ToString.Exclude
    InMemoryDhcpTransport server;

    public InMemoryDhcpTransport connectToServer(InMemoryDhcpTransport other) {
        other.setClient(this);
        this.setServer(other);
        other.startServer();
        this.startClient();
        return this;
    }

    public InMemoryDhcpTransport connectToClient(InMemoryDhcpTransport other) {
        this.setClient(other);
        other.setServer(this);
        other.startClient();
        startServer();
        return this;
    }

    @Override
    public void emitEvent(Event event, Message message) {
        switch (event) {
            case CLIENT_MESSAGE -> {
                if (client != null)
                    client.dispatchEvent(Event.CLIENT_MESSAGE, message);
            }
            case SERVER_MESSAGE -> {
                if (server != null)
                    server.dispatchEvent(Event.SERVER_MESSAGE, message);
            }
        }
    }
}
