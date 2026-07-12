package side.pxe.dhcp2.leases;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class Leaser {
    Config config;
    Clock clock = Clock.systemUTC();

    public Lease offer(LeaseRequest leaseRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * FindOffer must only return a still-valid offer matching the client and
     * transaction. It prevents a client from requesting an address that was
     * never offered to it.
     */
    public Lease findOffer(ByteBuffer clientId, ByteBuffer mac, ByteBuffer ip) {
        return Optional.ofNullable(findLease(clientId, mac, ip))
                .filter(e -> e.getState() == Lease.State.offered)
                .orElse(null);
    }

    /**
     * FindLease finds either an offered or active lease for the client/IP
     */
    public Lease findLease(ByteBuffer clientId, ByteBuffer mac, ByteBuffer ip) {
        throw new UnsupportedOperationException();
    }

    public Lease activate(Lease lease, Duration duration) {
        throw new UnsupportedOperationException();
    }

    public void release(Lease lease) {
        throw new UnsupportedOperationException();
    }

    public void decline(Lease lease) {
        throw new UnsupportedOperationException();
    }

    @Data
    @Accessors(chain = true)
    public static class LeaseRequest {
        ByteBuffer clientId;
        ByteBuffer mac;
        int transactionId;
        ByteBuffer requestedIp;
    }

    @Data
    @Accessors(chain = true)
    public static class Lease {
        ByteBuffer ip;
        ByteBuffer clientId;
        ByteBuffer mac;
        int transactionId;
        State state;
        Instant offeredAt;
        Instant expiresAt;
        Instant declinedAt;

        public enum State { offered, active, declined }
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        int subnetMask;
        int router;
        int[] dns;

        Duration leaseDuration;
        Duration renewalTime;
        Duration rebindingTime;
    }

    public abstract sealed static class LeaserException extends RuntimeException {
        LeaserException(String message) {
            super(message);
        }

        public static final class NoAddressAvailable extends LeaserException {
            NoAddressAvailable() {
                super("no address available");
            }
        }

        public static final class LeaseNotFound extends LeaserException {
            LeaseNotFound() {
                super("lease not found");
            }
        }

        public static final class InvalidRequest extends LeaserException {
            InvalidRequest() {
                super("invalid DHCP request");
            }
        }
    }
}
