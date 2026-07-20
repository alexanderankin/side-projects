package side.pxe.dhcp4.logic;

import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DhcpPacketTest {
    @Test
    void wrapsOffsetBufferAndExposesMutableZeroCopyViews() {
        var backing = new byte[DhcpPacket.DEFAULT_CAPACITY + 13];
        var packet = new DhcpPacket(ByteBuffer.wrap(backing, 7, DhcpPacket.DEFAULT_CAPACITY))
                .op((byte) 1)
                .transactionId(0x12345678)
                .broadcast(true)
                .ethernetAddress(EthernetAddress.parse("02:00:00:00:00:01"))
                .magicCookie();
        var options = packet.writeOptions()
                .putMessageType(DhcpMessageType.DISCOVER)
                .putIpv4(SupportedOption.REQUESTED_IP, ip("10.0.0.9"))
                .putLeaseTime(Duration.ofHours(1))
                .putIpv4List(SupportedOption.DNS_SERVERS, List.of(ip("1.1.1.1"), ip("8.8.8.8")));
        packet.wireLength(DhcpPacket.OPTIONS_OFFSET + options.finish());

        assertEquals(1, backing[7]);
        assertEquals(0x12345678, packet.transactionId());
        assertTrue(packet.broadcast());
        assertEquals(EthernetAddress.parse("02:00:00:00:00:01"), packet.ethernetAddress());
        assertEquals(ip("10.0.0.9"), packet.options().ipv4(SupportedOption.REQUESTED_IP).orElseThrow());
        assertEquals(Duration.ofHours(1), packet.options().leaseTime().orElseThrow());
        assertEquals(List.of(ip("1.1.1.1"), ip("8.8.8.8")),
                packet.options().ipv4List(SupportedOption.DNS_SERVERS));

        backing[7 + DhcpPacket.OPTIONS_OFFSET + 2] = (byte) DhcpMessageType.REQUEST.wireValue();
        assertEquals(DhcpMessageType.REQUEST, packet.options().messageType().orElseThrow());
    }

    @Test
    void datagramSharesThePacketsBackingArrayAndExactWireLength() {
        var wire = new DhcpWire(UnpooledByteBufAllocator.DEFAULT);
        var allocated = wire.discover(42, EthernetAddress.parse("02:00:00:00:00:02"),
                java.util.Optional.empty());
        assertEquals(1, allocated.byteBuf().refCnt());
        try (allocated) {
            var packet = allocated.packet();
            var datagram = packet.toDatagramPacket(new java.net.InetSocketAddress("127.0.0.1", 6767));

            assertSame(packet.wireBuffer().array(), datagram.getData());
            assertEquals(packet.wireLength(), datagram.getLength());
            datagram.getData()[datagram.getOffset() + 4] = 1;
            assertNotEquals(42, packet.transactionId());
        }
        assertEquals(0, allocated.byteBuf().refCnt());
    }

    @Test
    void rejectsAnOptionThatRunsPastThePacketBoundary() {
        var data = ByteBuffer.allocate(DhcpPacket.OPTIONS_OFFSET + 3);
        var packet = new DhcpPacket(data);
        data.put(DhcpPacket.OPTIONS_OFFSET, (byte) SupportedOption.MESSAGE_TYPE.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 1, (byte) 10);

        assertThrows(IllegalArgumentException.class,
                packet::options);
    }

    @Test
    void optionParserSkipsPadsAndStopsAtEnd() {
        var data = ByteBuffer.allocate(DhcpPacket.OPTIONS_OFFSET + 8);
        var packet = new DhcpPacket(data);
        data.put(DhcpPacket.OPTIONS_OFFSET, (byte) 0);
        data.put(DhcpPacket.OPTIONS_OFFSET + 1, (byte) SupportedOption.MESSAGE_TYPE.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 2, (byte) 1);
        data.put(DhcpPacket.OPTIONS_OFFSET + 3, (byte) DhcpMessageType.OFFER.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 4, (byte) SupportedOption.END.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 5, (byte) SupportedOption.SERVER_IDENTIFIER.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 6, (byte) 1);
        data.put(DhcpPacket.OPTIONS_OFFSET + 7, (byte) 99);

        assertEquals(DhcpMessageType.OFFER, packet.options().messageType().orElseThrow());
        assertTrue(packet.options().value(SupportedOption.SERVER_IDENTIFIER).isEmpty());
    }

    @Test
    void eagerlyParsesSupportedAndUnknownOptionsIntoSlicedEntries() {
        var data = ByteBuffer.allocate(DhcpPacket.OPTIONS_OFFSET + 9);
        var packet = new DhcpPacket(data);
        data.put(DhcpPacket.OPTIONS_OFFSET, (byte) SupportedOption.MESSAGE_TYPE.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 1, (byte) 1);
        data.put(DhcpPacket.OPTIONS_OFFSET + 2, (byte) DhcpMessageType.DISCOVER.wireValue());
        data.put(DhcpPacket.OPTIONS_OFFSET + 3, (byte) 200);
        data.put(DhcpPacket.OPTIONS_OFFSET + 4, (byte) 2);
        data.put(DhcpPacket.OPTIONS_OFFSET + 5, (byte) 11);
        data.put(DhcpPacket.OPTIONS_OFFSET + 6, (byte) 12);
        data.put(DhcpPacket.OPTIONS_OFFSET + 7, (byte) SupportedOption.END.wireValue());

        var options = packet.options();

        assertEquals(3, options.entries().size());
        assertEquals(SupportedOption.MESSAGE_TYPE,
                options.entries().get(0).supportedOption().orElseThrow());
        assertEquals(200, options.entries().get(1).wireValue());
        assertTrue(options.entries().get(1).supportedOption().isEmpty());
        data.put(DhcpPacket.OPTIONS_OFFSET + 5, (byte) 99);
        assertEquals(99, Byte.toUnsignedInt(options.entries().get(1).value().get(0)));
    }

    @Test
    void pxeReplyContainsNextServerAndBootFileTuple() {
        var wire = new DhcpWire(UnpooledByteBufAllocator.DEFAULT);
        var pxe = new PxeBootConfiguration(ip("10.0.0.2"), "bootx64.efi");
        var config = new DhcpServerService.ServerConfig(
                ip("10.0.0.1"), ip("255.255.255.0"), List.of(ip("10.0.0.1")),
                List.of(ip("1.1.1.1")), new InetSocketAddress("127.0.0.1", 6868),
                java.util.Optional.of(pxe));

        try (var request = wire.discover(77, EthernetAddress.parse("02:00:00:00:00:03"),
                java.util.Optional.empty());
             var reply = wire.reply(request.packet(), DhcpMessageType.OFFER,
                     ip("10.0.0.100"), config, Duration.ofHours(1))) {
            var packet = reply.packet();
            var options = packet.options();

            assertEquals(pxe.nextServer(), DhcpWire.ipv4(packet.serverIp()));
            assertEquals(pxe.nextServer().getHostAddress(),
                    options.ascii(SupportedOption.TFTP_SERVER_NAME).orElseThrow());
            assertEquals(pxe.bootFileName(),
                    options.ascii(SupportedOption.BOOTFILE_NAME).orElseThrow());
        }
    }

    private static Inet4Address ip(String address) {
        return (Inet4Address) InetAddress.ofLiteral(address);
    }
}
