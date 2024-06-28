package info.ankin.projects.ovp4j.protocol;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;

// https://openvpn.net/community-resources/openvpn-protocol/
public class OpenVpnProtocol {
    public static void main(String[] args) {
        ByteBuffer allocate = ByteBuffer.allocate(3);
        allocate.put("abc".getBytes());
        allocate.flip();

        System.out.println((char) allocate.get(2));
    }

    @Data
    @Accessors(chain = true)
    public static class Packet {
        ByteBuffer byteBuffer;

        int packetLength() {
            int position = byteBuffer.position();
            short packetLength = byteBuffer.getShort();
            byteBuffer.position(position);
            return Short.toUnsignedInt(packetLength);
        }

        int packetCode() {
            int position = byteBuffer.position();
            int packetCode = byteBuffer.get(2);
            byteBuffer.position(position);
            return packetCode;
        }
    }
}
