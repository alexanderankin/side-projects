package side.pxe.dhcp4.logic;

public class DhcpNakException extends RuntimeException {
    public DhcpNakException(String message) {
        super(message);
    }
}
