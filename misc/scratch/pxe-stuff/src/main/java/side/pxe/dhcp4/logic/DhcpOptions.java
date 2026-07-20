package side.pxe.dhcp4.logic;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DhcpOptions implements Iterable<DhcpOptions.Option> {
    private final ByteBuffer data;
    private final ArrayList<Option> options = new ArrayList<>();
    private final boolean writable;
    private int writePosition;

    public DhcpOptions(ByteBuffer data) {
        this(data, false);
    }

    private DhcpOptions(ByteBuffer data, boolean writable) {
        this.data = data.slice().order(ByteOrder.BIG_ENDIAN);
        this.writable = writable;
        if (!writable) {
            parse();
        }
    }

    static DhcpOptions writer(ByteBuffer data) {
        return new DhcpOptions(data, true);
    }

    public List<Option> entries() {
        return Collections.unmodifiableList(options);
    }

    @Override
    public Iterator<Option> iterator() {
        return entries().iterator();
    }

    public Optional<ByteBuffer> value(SupportedOption requestedOption) {
        return option(requestedOption).map(Option::value);
    }

    public Optional<ByteBuffer> value(int requestedCode) {
        return options.stream()
                .filter(option -> option.wireValue() == requestedCode)
                .findFirst()
                .map(Option::value);
    }

    public Optional<Option> option(SupportedOption requestedOption) {
        return options.stream()
                .filter(option -> option.supportedOption().orElse(null) == requestedOption)
                .findFirst();
    }

    public Optional<DhcpMessageType> messageType() {
        return value(SupportedOption.MESSAGE_TYPE).map(value -> {
            requireLength(SupportedOption.MESSAGE_TYPE.wireValue(), value, 1);
            return DhcpMessageType.fromWireValue(Byte.toUnsignedInt(value.get(0)));
        });
    }

    public Optional<Inet4Address> ipv4(SupportedOption option) {
        return value(option).map(value -> {
            requireLength(option.wireValue(), value, 4);
            return ipv4(value, 0);
        });
    }

    public List<Inet4Address> ipv4List(SupportedOption option) {
        return value(option).map(value -> {
            if (value.remaining() == 0 || value.remaining() % 4 != 0) {
                throw new IllegalArgumentException("option " + option.wireValue()
                        + " must contain IPv4 addresses");
            }
            var result = new ArrayList<Inet4Address>(value.remaining() / 4);
            for (int offset = 0; offset < value.remaining(); offset += 4) {
                result.add(ipv4(value, offset));
            }
            return List.copyOf(result);
        }).orElseGet(List::of);
    }

    public Optional<Duration> leaseTime() {
        return value(SupportedOption.LEASE_TIME).map(value -> {
            requireLength(SupportedOption.LEASE_TIME.wireValue(), value, 4);
            return Duration.ofSeconds(Integer.toUnsignedLong(value.getInt(0)));
        });
    }

    public Optional<String> ascii(SupportedOption option) {
        return value(option).map(value -> StandardCharsets.US_ASCII.decode(value.duplicate()).toString());
    }

    public DhcpOptions put(SupportedOption option, ByteBuffer value) {
        if (option == SupportedOption.PAD || option == SupportedOption.END) {
            throw new IllegalArgumentException("PAD and END do not contain values");
        }
        return put(option.wireValue(), value);
    }

    public DhcpOptions put(int code, ByteBuffer value) {
        requireWriter();
        int length = value.remaining();
        if (code <= 0 || code >= SupportedOption.END.wireValue() || length > 255) {
            throw new IllegalArgumentException("invalid DHCP option code or length");
        }
        requireWritable(length + 2);
        data.put(writePosition++, (byte) code);
        data.put(writePosition++, (byte) length);
        data.put(writePosition, value, value.position(), length);
        var valueSlice = data.slice(writePosition, length).order(ByteOrder.BIG_ENDIAN);
        options.add(new Option(code, SupportedOption.fromWireValue(code), valueSlice));
        writePosition += length;
        return this;
    }

    public DhcpOptions putMessageType(DhcpMessageType type) {
        return put(SupportedOption.MESSAGE_TYPE,
                ByteBuffer.wrap(new byte[]{(byte) type.wireValue()}));
    }

    public DhcpOptions putAscii(SupportedOption option, String value) {
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(value)) {
            throw new IllegalArgumentException("option " + option.wireValue()
                    + " must contain only US-ASCII characters");
        }
        return put(option, ByteBuffer.wrap(value.getBytes(StandardCharsets.US_ASCII)));
    }

    public DhcpOptions putIpv4(SupportedOption option, Inet4Address address) {
        return put(option, ByteBuffer.wrap(address.getAddress()));
    }

    public DhcpOptions putIpv4List(SupportedOption option, List<Inet4Address> addresses) {
        var value = ByteBuffer.allocate(addresses.size() * 4);
        addresses.forEach(address -> value.put(address.getAddress()));
        return put(option, value.flip());
    }

    public DhcpOptions putLeaseTime(Duration duration) {
        long seconds = duration.toSeconds();
        if (seconds < 0 || seconds > 0xffff_ffffL) {
            throw new IllegalArgumentException("lease time does not fit an unsigned 32-bit value");
        }
        return put(SupportedOption.LEASE_TIME,
                ByteBuffer.allocate(4).putInt((int) seconds).flip());
    }

    public int finish() {
        requireWriter();
        requireWritable(1);
        data.put(writePosition++, (byte) SupportedOption.END.wireValue());
        options.add(new Option(SupportedOption.END.wireValue(), Optional.of(SupportedOption.END),
                data.slice(writePosition, 0)));
        return writePosition;
    }

    private void parse() {
        int offset = 0;
        while (offset < data.limit()) {
            int code = Byte.toUnsignedInt(data.get(offset++));
            var supported = SupportedOption.fromWireValue(code);
            if (code == SupportedOption.END.wireValue()) {
                options.add(new Option(code, supported, data.slice(offset, 0)));
                return;
            }
            if (code == SupportedOption.PAD.wireValue()) {
                options.add(new Option(code, supported, data.slice(offset, 0)));
                continue;
            }
            if (offset >= data.limit()) {
                throw new IllegalArgumentException("option " + code + " is missing its length");
            }
            int length = Byte.toUnsignedInt(data.get(offset++));
            if (offset + length > data.limit()) {
                throw new IllegalArgumentException("option " + code + " exceeds the packet boundary");
            }
            options.add(new Option(code, supported,
                    data.slice(offset, length).order(ByteOrder.BIG_ENDIAN)));
            offset += length;
        }
    }

    private void requireWriter() {
        if (!writable) {
            throw new IllegalStateException("parsed DHCP options are read-only as a collection; value slices remain mutable");
        }
    }

    private void requireWritable(int length) {
        if (writePosition + length > data.capacity()) {
            throw new IllegalArgumentException("DHCP options exceed packet capacity");
        }
    }

    private static void requireLength(int code, ByteBuffer value, int length) {
        if (value.remaining() != length) {
            throw new IllegalArgumentException("option " + code + " must contain " + length + " bytes");
        }
    }

    private static Inet4Address ipv4(ByteBuffer value, int offset) {
        var bytes = new byte[4];
        value.get(offset, bytes);
        try {
            return (Inet4Address) InetAddress.getByAddress(bytes);
        } catch (UnknownHostException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public record Option(int wireValue, Optional<SupportedOption> supportedOption, ByteBuffer value) {
        public Option {
            supportedOption = Objects.requireNonNull(supportedOption, "supportedOption");
            value = Objects.requireNonNull(value, "value").slice().order(ByteOrder.BIG_ENDIAN);
        }
    }
}
