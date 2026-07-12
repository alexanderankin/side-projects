package side.pxe.dhcp2.normie;

import side.pxe.dhcp2.DhcpIpAddress;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import static side.pxe.dhcp2.Util.concatArray;

public interface LeaseService {
    // Optional<LeaseOffer> offer(OfferRequest request);

    RequestResult commit(CommitRequest request);

    RequestResult renew(RenewRequest request);

    boolean release(ClientKey client, DhcpIpAddress address);

    void decline(ClientKey client, DhcpIpAddress address);

    Optional<Lease> findByClient(ClientKey client);

    Optional<Lease> findByAddress(DhcpIpAddress address);

    enum LeaseState {
        OFFERED, ACTIVE, EXPIRED, DECLINED,
        ;

        public static final Set<LeaseState> VALID = Set.of(OFFERED, ACTIVE);
        public static final Set<LeaseState> INVALID = Set.of(EXPIRED, DECLINED);
    }

    sealed interface RequestResult {
        record Ack(Lease lease) implements RequestResult {
        }

        record Nak(String reason) implements RequestResult {
        }

        record Ignore() implements RequestResult {
        }
    }

    sealed interface ClientKey {
        byte[] getKey();

        default String getKeyString() {
            return Base64.getUrlEncoder().encodeToString(getKey());
        }

        record Option61(byte[] value) implements ClientKey {
            @Override
            public byte[] getKey() {
                return concatArray("option61".getBytes(), value);
            }
        }

        record Hardware(int hardwareType, byte[] address) implements ClientKey {
            @Override
            public byte[] getKey() {
                return concatArray("hardware".getBytes(), ByteBuffer.allocate(4).putInt(hardwareType).array(), address);
            }
        }
    }

    record LeaseId(ClientKey clientKey, DhcpIpAddress address) {
    }

    record Lease(LeaseId id, Instant allocatedAt, Instant expiresAt, LeaseState state) {
    }

    record ExpiredLease(Lease lease, Instant expiredAt) {
    }

    record OfferRequest(ClientKey client, Optional<DhcpIpAddress> preferredAddress,
                        Optional<Duration> preferredDuration /* NetworkContext network */) {
    }

    record CommitRequest(ClientKey client, DhcpIpAddress requestedAddress,
                         Optional<Duration> preferredDuration /* NetworkContext network */) {
    }

    record RenewRequest(ClientKey client, DhcpIpAddress currentAddress,
                        Optional<Duration> preferredDuration /* NetworkContext network */) {
    }
}
