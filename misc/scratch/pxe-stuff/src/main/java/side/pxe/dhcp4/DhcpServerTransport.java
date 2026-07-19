package side.pxe.dhcp4;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DhcpServerTransport extends BaseDhcpTransport {
    DatagramSocket server;

    public DhcpServerTransport(Ports ports) {
        super(ports);
    }

    @Override
    protected void doStart() {
        server = datagramSocket(ports.serverAnyInterface());
    }

    @Override
    protected void doStop() {
        server.close();
    }

    @SneakyThrows
    @Override
    public void send(DatagramPacket packet) {
        server.send(packet);
    }
}
