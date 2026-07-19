package side.pxe.dhcp4;

import lombok.SneakyThrows;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

final class DhcpTransportITestSupport {
    private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

    static DatagramPacket packet(String payload) {
        var bytes = payload.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(bytes, bytes.length);
    }

    static DatagramPacket packet(String payload, int destinationPort) {
        var packet = packet(payload);
        packet.setSocketAddress(new InetSocketAddress(LOOPBACK, destinationPort));
        return packet;
    }

    static String payload(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    static BaseDhcpTransport.Ports availablePorts() {
        int server;
        int client;
        try (var socket = new DatagramSocket(0)) {
            server = socket.getLocalPort();
            try (var socket1 = new DatagramSocket(0)) {
                client = socket1.getLocalPort();
            }
        }
        return new BaseDhcpTransport.Ports(server, client);
    }

}
