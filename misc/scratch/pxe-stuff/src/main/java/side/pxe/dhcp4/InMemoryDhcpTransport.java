package side.pxe.dhcp4;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Objects;

public class InMemoryDhcpTransport extends BaseDhcpTransport {
    private volatile InMemoryDhcpTransport peer;

    public InMemoryDhcpTransport(Ports ports) {
        super(ports);
    }

    public InMemoryDhcpTransport connectTo(InMemoryDhcpTransport peer) {
        this.peer = Objects.requireNonNull(peer, "peer");
        peer.peer = this;
        return this;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
    }

    @Override
    public void send(DatagramPacket packet) {
        if (!started) {
            throw new IllegalStateException("transport is not started");
        }

        var destination = peer;
        if (destination == null) {
            throw new IllegalStateException("transport is not connected");
        }
        if (!destination.started) {
            throw new IllegalStateException("connected transport is not started");
        }

        destination.dispatch(copy(packet));
    }

    private static DatagramPacket copy(DatagramPacket packet) {
        var payload = Arrays.copyOfRange(
                packet.getData(),
                packet.getOffset(),
                packet.getOffset() + packet.getLength());
        var copy = new DatagramPacket(payload, payload.length);
        copy.setSocketAddress(packet.getSocketAddress());
        return copy;
    }
}
