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
public class DhcpClientTransport extends BaseDhcpTransport {
    DatagramSocket client;

    public DhcpClientTransport(Ports ports) {
        super(ports);
    }

    @Override
    protected void doStart() {
        client = datagramSocket(ports.clientAnyInterface());
    }

    @Override
    protected void doStop() {
        client.close();
    }

    @SneakyThrows
    @Override
    public void send(DatagramPacket packet) {
        client.send(packet);
    }
}
