package side.pxe;

import io.netty.buffer.PooledByteBufAllocator;
import side.pxe.dhcp.DhcpRequest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class BasicDhcpServer {
    private static final int DHCP_SERVER_PORT = 67;
    private static final int DHCP_CLIENT_PORT = 68;

    private static final byte DHCP_DISCOVER = 1;
    private static final byte DHCP_OFFER = 2;
    private static final byte DHCP_REQUEST = 3;
    private static final byte DHCP_ACK = 5;

    private static final byte OPTION_MESSAGE_TYPE = 53;
    private static final byte OPTION_SUBNET_MASK = 1;
    private static final byte OPTION_ROUTER = 3;
    private static final byte OPTION_DNS = 6;
    private static final byte OPTION_LEASE_TIME = 51;
    private static final byte OPTION_SERVER_ID = 54;
    private static final byte OPTION_END = (byte) 255;

    private static final byte[] MAGIC_COOKIE = {
            99, (byte) 130, 83, 99
    };

    private final InetAddress serverAddress;
    private final InetAddress offeredAddress;
    private final InetAddress subnetMask;
    private final InetAddress router;
    private final InetAddress dnsServer;

    public BasicDhcpServer(
            String serverAddress,
            String offeredAddress,
            String subnetMask,
            String router,
            String dnsServer
    ) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.offeredAddress = InetAddress.getByName(offeredAddress);
        this.subnetMask = InetAddress.getByName(subnetMask);
        this.router = InetAddress.getByName(router);
        this.dnsServer = InetAddress.getByName(dnsServer);
    }

    private static byte findMessageType(byte[] packet) {
        int offset = 240;

        while (offset < packet.length) {
            int option = Byte.toUnsignedInt(packet[offset++]);

            if (option == 255) {
                break;
            }

            if (option == 0) {
                continue;
            }

            if (offset >= packet.length) {
                break;
            }

            int length = Byte.toUnsignedInt(packet[offset++]);

            if (offset + length > packet.length) {
                break;
            }

            if (option == OPTION_MESSAGE_TYPE && length >= 1) {
                return packet[offset];
            }

            offset += length;
        }

        return 0;
    }

    private static void addOption(
            ByteBuffer buffer,
            byte option,
            byte[] value
    ) {
        buffer.put(option);
        buffer.put((byte) value.length);
        buffer.put(value);
    }

    public static void main(String[] args) throws Exception {
        BasicDhcpServer server = new BasicDhcpServer(
                "192.168.50.1",   // DHCP server
                "192.168.50.100", // address offered to every client
                "255.255.255.0",
                "192.168.50.1",   // router
                "1.1.1.1"         // DNS
        );

        server.run();
    }

    public void run() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(DHCP_SERVER_PORT)) {
            socket.setBroadcast(true);

            System.out.println("DHCP server listening on UDP port 67");

            while (true) {
                var buf = PooledByteBufAllocator.DEFAULT.buffer(576);
                try {
                    var receiveBuffer = buf.array();
                    DatagramPacket packet =
                            new DatagramPacket(receiveBuffer, receiveBuffer.length);

                    socket.receive(packet);

                    /*
                    byte[] request =
                            Arrays.copyOf(packet.getData(), packet.getLength());

                    if (request.length < 240) {
                        continue;
                    }
                    */

                    var request = DhcpRequest.parse(buf.nioBuffer());
                    if (request == null)
                        continue;

                    byte messageType = request.getOptionMessageType();

                    if (messageType == DHCP_DISCOVER) {
                        System.out.println("Received DHCPDISCOVER");
                        sendResponse(socket, request, DHCP_OFFER);
                    } else if (messageType == DHCP_REQUEST) {
                        System.out.println("Received DHCPREQUEST");
                        sendResponse(socket, request, DHCP_ACK);
                    }
                } finally {
                    buf.release();
                }
            }
        }
    }

    private void sendResponse(
            DatagramSocket socket,
            DhcpRequest request,
            byte responseType
    ) throws Exception {
        var buf = PooledByteBufAllocator.DEFAULT.buffer(576);
        ByteBuffer response = buf.nioBuffer();

        try {
            // BOOTP fixed header.
            response.put((byte) 2);              // op: BOOTREPLY
            response.put(request.getHType());    // htype
            response.put(request.getHLength());  // hlen
            response.put((byte) 0);              // hops

            response.put(request.getXid());      // xid
            response.putShort((short) 0);        // secs
            response.putShort((short) 0x8000);   // flags: broadcast

            response.put(new byte[4]);            // ciaddr
            response.put(offeredAddress.getAddress()); // yiaddr
            response.put(serverAddress.getAddress());  // siaddr
            response.put(new byte[4]);            // giaddr

            response.put(request.getChAddr());    // chaddr
            response.put(new byte[64]);           // sname
            response.put(new byte[128]);          // file

            response.put(MAGIC_COOKIE);

            addOption(response, OPTION_MESSAGE_TYPE, new byte[]{responseType});
            addOption(response, OPTION_SERVER_ID, serverAddress.getAddress());
            addOption(response, OPTION_SUBNET_MASK, subnetMask.getAddress());
            addOption(response, OPTION_ROUTER, router.getAddress());
            addOption(response, OPTION_DNS, dnsServer.getAddress());

            ByteBuffer leaseTime = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(3600);
            addOption(response, OPTION_LEASE_TIME, leaseTime.array());

            response.put(OPTION_END);

            byte[] payload = Arrays.copyOf(response.array(), response.position());

            DatagramPacket reply = new DatagramPacket(
                    payload,
                    payload.length,
                    InetAddress.getByName("255.255.255.255"),
                    DHCP_CLIENT_PORT
            );

            socket.send(reply);

            System.out.printf(
                    "Sent %s offering %s%n",
                    responseType == DHCP_OFFER
                            ? "DHCPOFFER"
                            : "DHCPACK",
                    offeredAddress.getHostAddress()
            );
        } finally {
            buf.release();
        }
    }
}
