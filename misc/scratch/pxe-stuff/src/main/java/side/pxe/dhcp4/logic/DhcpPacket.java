package side.pxe.dhcp4.logic;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DhcpPacket {
    public static final int FIXED_HEADER_LENGTH = 236;
    public static final int OPTIONS_OFFSET = 240;
    public static final int DEFAULT_CAPACITY = 576;

    private static final int OP = 0;
    private static final int HARDWARE_TYPE = 1;
    private static final int HARDWARE_LENGTH = 2;
    private static final int TRANSACTION_ID = 4;
    private static final int FLAGS = 10;
    private static final int CLIENT_IP = 12;
    private static final int YOUR_IP = 16;
    private static final int SERVER_IP = 20;
    private static final int GATEWAY_IP = 24;
    private static final int CLIENT_HARDWARE_ADDRESS = 28;
    private static final int COOKIE = 236;
    private static final int BROADCAST_FLAG = 0x8000;
    private static final int MAGIC_COOKIE = 0x63825363;

    private final ByteBuffer data;

    public DhcpPacket(ByteBuffer data) {
        if (data == null || data.remaining() < OPTIONS_OFFSET) {
            throw new IllegalArgumentException("DHCP packet requires at least " + OPTIONS_OFFSET + " bytes");
        }
        this.data = data.slice().order(ByteOrder.BIG_ENDIAN);
    }

    public static DhcpPacket from(DatagramPacket packet) {
        return new DhcpPacket(ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength()));
    }

    public byte op() {
        return data.get(OP);
    }

    public DhcpPacket op(byte value) {
        data.put(OP, value);
        return this;
    }

    public byte hardwareType() {
        return data.get(HARDWARE_TYPE);
    }

    public DhcpPacket hardwareType(byte value) {
        data.put(HARDWARE_TYPE, value);
        return this;
    }

    public int hardwareLength() {
        return Byte.toUnsignedInt(data.get(HARDWARE_LENGTH));
    }

    public DhcpPacket hardwareLength(int value) {
        if (value < 0 || value > 16) {
            throw new IllegalArgumentException("hardware address length must be between 0 and 16");
        }
        data.put(HARDWARE_LENGTH, (byte) value);
        return this;
    }

    public int transactionId() {
        return data.getInt(TRANSACTION_ID);
    }

    public DhcpPacket transactionId(int value) {
        data.putInt(TRANSACTION_ID, value);
        return this;
    }

    public boolean broadcast() {
        return (Short.toUnsignedInt(data.getShort(FLAGS)) & BROADCAST_FLAG) != 0;
    }

    public DhcpPacket broadcast(boolean value) {
        int flags = Short.toUnsignedInt(data.getShort(FLAGS));
        flags = value ? flags | BROADCAST_FLAG : flags & ~BROADCAST_FLAG;
        data.putShort(FLAGS, (short) flags);
        return this;
    }

    public ByteBuffer clientIp() {
        return slice(CLIENT_IP, 4);
    }

    public ByteBuffer yourIp() {
        return slice(YOUR_IP, 4);
    }

    public DhcpPacket yourIp(ByteBuffer value) {
        put(YOUR_IP, value, 4);
        return this;
    }

    public ByteBuffer serverIp() {
        return slice(SERVER_IP, 4);
    }

    public DhcpPacket serverIp(ByteBuffer value) {
        put(SERVER_IP, value, 4);
        return this;
    }

    public ByteBuffer gatewayIp() {
        return slice(GATEWAY_IP, 4);
    }

    public ByteBuffer clientHardwareAddress() {
        return slice(CLIENT_HARDWARE_ADDRESS, hardwareLength());
    }

    public EthernetAddress ethernetAddress() {
        if (hardwareType() != 1 || hardwareLength() != 6) {
            throw new IllegalArgumentException("only Ethernet DHCP packets are supported");
        }
        return EthernetAddress.read(clientHardwareAddress());
    }

    public DhcpPacket ethernetAddress(EthernetAddress value) {
        hardwareType((byte) 1).hardwareLength(6);
        value.write(slice(CLIENT_HARDWARE_ADDRESS, 6));
        for (int i = CLIENT_HARDWARE_ADDRESS + 6; i < CLIENT_HARDWARE_ADDRESS + 16; i++) {
            data.put(i, (byte) 0);
        }
        return this;
    }

    public DhcpPacket magicCookie() {
        data.putInt(COOKIE, MAGIC_COOKIE);
        return this;
    }

    public boolean hasMagicCookie() {
        return data.getInt(COOKIE) == MAGIC_COOKIE;
    }

    public DhcpOptions options() {
        return new DhcpOptions(slice(OPTIONS_OFFSET, data.limit() - OPTIONS_OFFSET));
    }

    public DhcpOptions writeOptions() {
        return DhcpOptions.writer(slice(OPTIONS_OFFSET, data.capacity() - OPTIONS_OFFSET));
    }

    public DhcpPacket wireLength(int length) {
        if (length < OPTIONS_OFFSET || length > data.capacity()) {
            throw new IllegalArgumentException("invalid DHCP packet length: " + length);
        }
        data.limit(length);
        return this;
    }

    public int wireLength() {
        return data.limit();
    }

    public ByteBuffer wireBuffer() {
        return data.duplicate().order(ByteOrder.BIG_ENDIAN).position(0);
    }

    public DatagramPacket toDatagramPacket(SocketAddress destination) {
        if (!data.hasArray()) {
            throw new IllegalStateException("zero-copy datagrams require an array-backed ByteBuffer");
        }
        var packet = new DatagramPacket(data.array(), data.arrayOffset(), data.limit());
        packet.setSocketAddress(destination);
        return packet;
    }

    private ByteBuffer slice(int offset, int length) {
        return data.slice(offset, length).order(ByteOrder.BIG_ENDIAN);
    }

    private void put(int offset, ByteBuffer value, int length) {
        if (value.remaining() != length) {
            throw new IllegalArgumentException("expected exactly " + length + " bytes");
        }
        data.put(offset, value, value.position(), length);
    }
}
