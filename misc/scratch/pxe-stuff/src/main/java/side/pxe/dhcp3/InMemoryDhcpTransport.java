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
    @Override
    public void emitEvent(Event event, Message message) {
        dispatchEvent(switch (event) {
            case CLIENT_MESSAGE -> Event.SERVER_MESSAGE;
            case SERVER_MESSAGE -> Event.CLIENT_MESSAGE;
        }, message);
    }
}
