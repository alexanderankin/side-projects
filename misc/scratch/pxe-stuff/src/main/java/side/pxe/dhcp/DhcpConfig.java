package side.pxe.dhcp;

import java.util.Objects;

class DhcpConfig {
    PortMode portMode = PortMode.UNPRIVILEGED;
    Integer customListenPort;
    Integer customSendPort;

    enum PortMode {
        CANONICAL,
        CUSTOM,
        UNPRIVILEGED,
    }

    DhcpPorts clientPorts() {
        return switch (portMode) {
            case CANONICAL -> DhcpPorts.CLIENT;
            case UNPRIVILEGED -> DhcpPorts.CLIENT_UNPRIVILEGED;
            case CUSTOM -> new DhcpPorts(
                    Objects.requireNonNull(customListenPort),
                    Objects.requireNonNull(customSendPort)
            );
        };
    }

    DhcpPorts serverPorts() {
        return switch (portMode) {
            case CANONICAL -> DhcpPorts.CLIENT;
            case UNPRIVILEGED -> DhcpPorts.CLIENT_UNPRIVILEGED;
            case CUSTOM -> new DhcpPorts(
                    Objects.requireNonNull(customListenPort),
                    Objects.requireNonNull(customSendPort)
            );
        };
    }

    record DhcpPorts(int listen, int send) {
        static final DhcpPorts SERVER = new DhcpPorts(67, 68);
        static final DhcpPorts SERVER_UNPRIVILEGED = new DhcpPorts(6767, 6868);
        static final DhcpPorts CLIENT = SERVER.swap();
        static final DhcpPorts CLIENT_UNPRIVILEGED = SERVER.swap();

        DhcpPorts swap() {
            return new DhcpPorts(send, listen);
        }
    }
}
