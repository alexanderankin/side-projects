package side.pkg.msi.cab;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CabReader {
    @SneakyThrows
    public static void main(String[] args) {
        var cfHeader = new CfHeader();
        Path path = Paths.get(System.getProperty("user.home"), "Documents", "example.cab");

        try (InputStream inputStream = Files.newInputStream(path)) {
            cfHeader.setByteBuffer(ByteBuffer.wrap(inputStream.readNBytes(38)).order(ByteOrder.LITTLE_ENDIAN));
        }

        // cfHeader.setByteBuffer(ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN));

        System.out.println(Hex.encodeHexString(cfHeader.getSignature()));
        System.out.println(Arrays.equals(CfHeader.CAB_SIGNATURE, cfHeader.getSignature()));

        System.out.println(new ObjectMapper().writeValueAsString(cfHeader));
    }

    @Data
    @Accessors(chain = true)
    static class CfHeader {
        static final byte[] CAB_SIGNATURE;

        static {
            try {
                CAB_SIGNATURE = Hex.decodeHex("4D534346");
            } catch (DecoderException e) {
                throw new RuntimeException(e);
            }
        }

        ByteBuffer byteBuffer;

        public byte[] getSignature() {
            return new byte[]{
                    byteBuffer.get(0),
                    byteBuffer.get(1),
                    byteBuffer.get(2),
                    byteBuffer.get(3),
            };
        }

        public void setSignature(byte[] signature) {
            byteBuffer.put(0, signature[0]);
            byteBuffer.put(1, signature[1]);
            byteBuffer.put(2, signature[2]);
            byteBuffer.put(3, signature[3]);
        }

        public int getReserved1() {
            return byteBuffer.getInt(4);
        }

        public void setReserved1(int reserved1) {
            byteBuffer.putInt(4, reserved1);
        }

        // Total size of the CAB file
        public int getCbCabinet() {
            return byteBuffer.getInt(8);
        }

        public void setCbCabinet(int value) {
            byteBuffer.putInt(8, value);
        }

        public int getReserved2() {
            return byteBuffer.getInt(12);
        }

        public void setReserved2(int value) {
            byteBuffer.putInt(12, value);
        }

        // Offset of the CFFILE section
        public int getCoffFiles() {
            return byteBuffer.getInt(16);
        }

        public void setCoffFiles(int value) {
            byteBuffer.putInt(16, value);
        }

        public int getReserved3() {
            return byteBuffer.getInt(20);
        }

        public void setReserved3(int value) {
            byteBuffer.putInt(20, value);
        }

        public byte getVersionMinor() {
            return byteBuffer.get(24);
        }

        public void setVersionMinor(byte value) {
            byteBuffer.put(24, value);
        }

        public byte getVersionMajor() {
            return byteBuffer.get(25);
        }

        public void setVersionMajor(byte value) {
            byteBuffer.put(25, value);
        }

        public char getCFolders() {
            return byteBuffer.getChar(26);
        }

        public void setCFolders(char value) {
            byteBuffer.putChar(26, value);
        }

        public char getCFiles() {
            return byteBuffer.getChar(28);
        }

        public void setCFiles(char value) {
            byteBuffer.putChar(28, value);
        }

        public char getFlags() {
            return byteBuffer.getChar(30);
        }

        public void setFlags(char value) {
            byteBuffer.putChar(30, value);
        }

        public char getSetID() {
            return byteBuffer.getChar(32);
        }

        public void setSetID(char value) {
            byteBuffer.putChar(32, value);
        }

        public char getICabinet() {
            return byteBuffer.getChar(34);
        }

        public void setICabinet(char value) {
            byteBuffer.putChar(34, value);
        }

        public char getCbCFHeader() {
            return byteBuffer.getChar(36);
        }

        public void setCbCFHeader(char value) {
            byteBuffer.putChar(36, value);
        }
    }
}
