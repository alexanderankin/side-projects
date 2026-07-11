package side.pxe.dhcp;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Data
@Accessors(chain = true)
public class DhcpRequest {
    private static final byte OPTION_MESSAGE_TYPE = 53;

    ByteBuffer data;

    public static DhcpRequest parse(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < 240)
            return null;

        var request = new DhcpRequest().setData(byteBuffer);
        try {
            request.getChAddr();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
        return request;
    }

    public byte getHType() {
        return data.get(1);
    }

    public byte getHLength() {
        return data.get(2);
    }

    public ByteBuffer getXid() {
        return data.slice(4, 4);
    }

    public ByteBuffer getChAddr() {
        return data.slice(28, 16);
    }

    public MessageType getMessageType() {
        return MessageType.valueOfId(getOptionMessageType());
    }

    public byte getOptionMessageType() {
        int offset = 240;

        while (offset < data.capacity()) {
            int option = Byte.toUnsignedInt(data.get(offset++));

            if (option == 255) {
                break;
            }

            if (option == 0) {
                continue;
            }

            if (offset >= data.capacity()) {
                break;
            }

            int length = Byte.toUnsignedInt(data.get(offset++));

            if (offset + length > data.capacity()) {
                break;
            }

            if (option == OPTION_MESSAGE_TYPE && length >= 1) {
                return data.get(offset);
            }

            offset += length;
        }

        return 0;
    }

    @Getter
    @RequiredArgsConstructor
    public enum MessageType {
        DHCP_DISCOVER((byte) 1),
        DHCP_OFFER((byte) 2),
        DHCP_REQUEST((byte) 3),
        DHCP_ACK((byte) 5),
        ;

        private static final Map<Byte, MessageType> MAP = Arrays.stream(values())
                .collect(Collectors.toMap(MessageType::getId, Function.identity()));

        private final byte id;

        static MessageType valueOfId(byte optionMessageType) {
            return MAP.get(optionMessageType);
        }
    }
}
