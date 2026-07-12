package side.pxe.dhcp2;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Accessors(chain = true)
public class DhcpPacket {
    //<editor-fold desc="constants">
    private static final int OP_OFFSET = 0;
    private static final int HARDWARE_TYPE_OFFSET = 1;
    private static final int HARDWARE_LEN_OFFSET = 2;
    private static final int HOPS_OFFSET = 3;
    private static final int TRANSACTION_ID_OFFSET = 4;
    private static final int SECONDS_OFFSET = 8;
    private static final int FLAGS_OFFSET = 10;
    private static final int CLIENT_IP_OFFSET = 12;
    private static final int YOUR_IP_OFFSET = 16;
    private static final int SERVER_IP_OFFSET = 20;
    private static final int GATEWAY_IP_OFFSET = 24;
    private static final int CLIENT_MAC_OFFSET = 28;
    private static final int SERVER_NAME_OFFSET = 44;
    private static final int SERVER_NAME_LENGTH = 64;
    private static final int BOOT_FILE_OFFSET = 108;
    private static final int BOOT_FILE_LENGTH = 128;

    private static final int BROADCAST_FLAG = 0x8000;
    private static final int MIN_PACKET_LENGTH = 236;
    private static final int COOKIE_OFFSET = MIN_PACKET_LENGTH;
    private static final int OPTIONS_OFFSET = 240;
    //</editor-fold>

    private final ByteBuffer data;

    public DhcpPacket(ByteBuffer data) {
        if (data == null || data.capacity() < MIN_PACKET_LENGTH) {
            throw new IllegalArgumentException(
                    "DHCP packet must contain at least " + MIN_PACKET_LENGTH + " bytes"
            );
        }

        this.data = data.duplicate().order(ByteOrder.BIG_ENDIAN);
    }

    public byte getOp() {
        return data.get(OP_OFFSET);
    }

    public DhcpPacket setOp(byte value) {
        data.put(OP_OFFSET, value);
        return this;
    }

    public byte getHardwareType() {
        return data.get(HARDWARE_TYPE_OFFSET);
    }

    public DhcpPacket setHardwareType(byte value) {
        data.put(HARDWARE_TYPE_OFFSET, value);
        return this;
    }

    public byte getHardwareLen() {
        return data.get(HARDWARE_LEN_OFFSET);
    }

    public DhcpPacket setHardwareLen(byte value) {
        data.put(HARDWARE_LEN_OFFSET, value);
        return this;
    }

    public byte getHops() {
        return data.get(HOPS_OFFSET);
    }

    public DhcpPacket setHops(byte value) {
        data.put(HOPS_OFFSET, value);
        return this;
    }

    public int getTransactionID() {
        return data.getInt(TRANSACTION_ID_OFFSET);
    }

    public DhcpPacket setTransactionID(int value) {
        data.putInt(TRANSACTION_ID_OFFSET, value);
        return this;
    }

    public short getSeconds() {
        return data.getShort(SECONDS_OFFSET);
    }

    public DhcpPacket setSeconds(short value) {
        data.putShort(SECONDS_OFFSET, value);
        return this;
    }

    public boolean isBroadcast() {
        return (data.getShort(FLAGS_OFFSET) & 0x8000) != 0;
    }

    public DhcpPacket setBroadcast(boolean value) {
        short flags = data.getShort(FLAGS_OFFSET);

        if (value)
            flags |= (short) 0x8000;
        else
            flags &= (short) ~0x8000;

        data.putShort(FLAGS_OFFSET, flags);
        return this;
    }

    public ByteBuffer getClientIP() {
        return data.slice(CLIENT_IP_OFFSET, 4);
    }

    public DhcpPacket setClientIP(ByteBuffer value) {
        data.put(CLIENT_IP_OFFSET, value, 0, 4);
        return this;
    }

    public ByteBuffer getYourIP() {
        return data.slice(YOUR_IP_OFFSET, 4);
    }

    public DhcpPacket setYourIP(ByteBuffer value) {
        data.put(YOUR_IP_OFFSET, value, 0, 4);
        return this;
    }

    public ByteBuffer getServerIP() {
        return data.slice(SERVER_IP_OFFSET, 4);
    }

    public DhcpPacket setServerIP(ByteBuffer value) {
        data.put(SERVER_IP_OFFSET, value, 0, 4);
        return this;
    }

    public ByteBuffer getGatewayIP() {
        return data.slice(GATEWAY_IP_OFFSET, 4);
    }

    public DhcpPacket setGatewayIP(ByteBuffer value) {
        data.put(GATEWAY_IP_OFFSET, value, 0, 4);
        return this;
    }

    public ByteBuffer getClientMAC() {
        return data.slice(CLIENT_MAC_OFFSET, Byte.toUnsignedInt(getHardwareLen()));
    }

    public DhcpPacket setClientMAC(ByteBuffer value) {
        data.put(CLIENT_MAC_OFFSET, value, 0, value.remaining());
        return this;
    }

    public ByteBuffer getServerName() {
        return data.slice(SERVER_NAME_OFFSET, 64);
    }

    public DhcpPacket setServerName(ByteBuffer value) {
        data.put(SERVER_NAME_OFFSET, value, 0, value.remaining());
        return this;
    }

    public ByteBuffer getBootFile() {
        return data.slice(BOOT_FILE_OFFSET, 128);
    }

    public DhcpPacket setBootFile(ByteBuffer value) {
        data.put(BOOT_FILE_OFFSET, value, 0, value.remaining());
        return this;
    }

    public ByteBuffer getCookie() {
        return data.slice(COOKIE_OFFSET, 4);
    }

    public DhcpPacket setCookie(ByteBuffer value) {
        data.put(COOKIE_OFFSET, value, 0, 4);
        return this;
    }

    public ByteBuffer getOptions() {
        return data.slice(OPTIONS_OFFSET, data.capacity() - (OPTIONS_OFFSET));
    }

    public DhcpPacket setOptions(ByteBuffer value) {
        data.put(OPTIONS_OFFSET, value, 0, value.remaining());
        return this;
    }
}
