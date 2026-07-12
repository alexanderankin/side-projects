package side.pxe.dhcp2.normie;

import side.pxe.dhcp2.DhcpIpAddress;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record DhcpIpAddressRange(DhcpIpAddress start, DhcpIpAddress end) {
    public DhcpIpAddressRange {
        if (start.compareTo(end) >= 0) {
            throw new IllegalArgumentException("end must be strictly after start");
        }
    }

    boolean contains(DhcpIpAddress address) {
        address = address.intBased();
        return start.intRepresentation() <= address.intRepresentation() && address.intRepresentation() <= end.intRepresentation();
    }

    Stream<DhcpIpAddress> addressStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(addressIterator(), 0), false);
    }

    Iterator<DhcpIpAddress> addressIterator() {
        return new DhcpIpAddressIterator();
    }

    private class DhcpIpAddressIterator implements Iterator<DhcpIpAddress> {
        DhcpIpAddress address = start;

        @Override
        public boolean hasNext() {
            return address.compareTo(end) < 0;
        }

        @Override
        public DhcpIpAddress next() {
            if (!hasNext())
                return null;
            var tmp = address;
            address = address.next();
            return tmp;
        }
    }
}
