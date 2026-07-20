package side.pxe.dhcp4.logic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Optional;

final class DhcpWire {
    static final byte BOOT_REQUEST = 1;
    static final byte BOOT_REPLY = 2;

    private final ByteBufAllocator allocator;

    DhcpWire(ByteBufAllocator allocator) {
        this.allocator = java.util.Objects.requireNonNull(allocator, "allocator");
    }

    AllocatedDhcpPacket discover(int transactionId, EthernetAddress client,
                                 Optional<Inet4Address> preferredAddress) {
        var packet = request(transactionId, client);
        var options = packet.packet().writeOptions().putMessageType(DhcpMessageType.DISCOVER);
        preferredAddress.ifPresent(address -> options.putIpv4(SupportedOption.REQUESTED_IP, address));
        options.put(SupportedOption.PARAMETER_REQUEST_LIST,
                ByteBuffer.wrap(new byte[]{1, 3, 6, 51, 54, 66, 67}));
        return finish(packet, options);
    }

    AllocatedDhcpPacket request(int transactionId, EthernetAddress client,
                                Inet4Address requestedAddress, Inet4Address serverIdentifier) {
        var packet = request(transactionId, client);
        var options = packet.packet().writeOptions()
                .putMessageType(DhcpMessageType.REQUEST)
                .putIpv4(SupportedOption.REQUESTED_IP, requestedAddress)
                .putIpv4(SupportedOption.SERVER_IDENTIFIER, serverIdentifier)
                .put(SupportedOption.PARAMETER_REQUEST_LIST,
                        ByteBuffer.wrap(new byte[]{1, 3, 6, 51, 54, 66, 67}));
        return finish(packet, options);
    }

    AllocatedDhcpPacket reply(DhcpPacket request, DhcpMessageType type, Inet4Address address,
                              DhcpServerService.ServerConfig config, java.time.Duration leaseTime) {
        var allocated = allocate();
        var packet = allocated.packet()
                .op(BOOT_REPLY)
                .transactionId(request.transactionId())
                .broadcast(request.broadcast())
                .ethernetAddress(request.ethernetAddress())
                .yourIp(ByteBuffer.wrap(address.getAddress()))
                .serverIp(ByteBuffer.wrap(config.pxeBoot()
                        .map(PxeBootConfiguration::nextServer)
                        .orElse(config.serverIdentifier())
                        .getAddress()))
                .magicCookie();
        var options = packet.writeOptions()
                .putMessageType(type)
                .putIpv4(SupportedOption.SERVER_IDENTIFIER, config.serverIdentifier());
        if (type != DhcpMessageType.NAK) {
            options.putLeaseTime(leaseTime)
                    .putIpv4(SupportedOption.SUBNET_MASK, config.subnetMask());
            if (!config.routers().isEmpty()) {
                options.putIpv4List(SupportedOption.ROUTER, config.routers());
            }
            if (!config.dnsServers().isEmpty()) {
                options.putIpv4List(SupportedOption.DNS_SERVERS, config.dnsServers());
            }
            config.pxeBoot().ifPresent(pxe -> options
                    .putAscii(SupportedOption.TFTP_SERVER_NAME, pxe.nextServer().getHostAddress())
                    .putAscii(SupportedOption.BOOTFILE_NAME, pxe.bootFileName()));
        }
        return finish(allocated, options);
    }

    static Inet4Address ipv4(ByteBuffer value) {
        if (value.remaining() != 4) {
            throw new IllegalArgumentException("IPv4 address requires four bytes");
        }
        var bytes = new byte[4];
        value.get(value.position(), bytes);
        try {
            return (Inet4Address) InetAddress.getByAddress(bytes);
        } catch (UnknownHostException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    static Inet4Address zeroAddress() {
        return (Inet4Address) InetAddress.ofLiteral("0.0.0.0");
    }

    static boolean isZero(Inet4Address address) {
        return address.equals(zeroAddress());
    }

    private AllocatedDhcpPacket request(int transactionId, EthernetAddress client) {
        var allocated = allocate();
        allocated.packet()
                .op(BOOT_REQUEST)
                .transactionId(transactionId)
                .broadcast(true)
                .ethernetAddress(client)
                .magicCookie();
        return allocated;
    }

    private AllocatedDhcpPacket allocate() {
        var byteBuf = allocator.heapBuffer(DhcpPacket.DEFAULT_CAPACITY);
        try {
            return new AllocatedDhcpPacket(new DhcpPacket(byteBuf.nioBuffer(0, byteBuf.capacity())), byteBuf);
        } catch (RuntimeException e) {
            byteBuf.release();
            throw e;
        }
    }

    private static AllocatedDhcpPacket finish(AllocatedDhcpPacket allocated, DhcpOptions options) {
        allocated.packet().wireLength(DhcpPacket.OPTIONS_OFFSET + options.finish());
        return allocated;
    }

    record AllocatedDhcpPacket(DhcpPacket packet, ByteBuf byteBuf) implements AutoCloseable {
        @Override
        public void close() {
            byteBuf.release();
        }
    }
}
