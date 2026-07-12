package side.pxe.dhcp2.normie;

import lombok.extern.slf4j.Slf4j;
import side.pxe.dhcp2.DhcpIpAddress;
import side.pxe.dhcp2.normie.LeaseService.*;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DefaultLeaseService {
    Map<String, Lease> leasesByClient = new HashMap<>();
    Map<DhcpIpAddress, Lease> leasesByAddress = new HashMap<>();
    DhcpIpAddressRange range = new DhcpIpAddressRange(DhcpIpAddress.of("10.0.0.2"), DhcpIpAddress.of("10.0.0.255"));
    Duration defaultDuration = Duration.ofDays(1);
    Clock clock = Clock.systemUTC();

    public Optional<Lease> offer(OfferRequest request) {
        log.debug("begin offer with request: '{}'", request);
        var now = clock.instant();
        var keyString = request.client().getKeyString();
        Lease lease = leasesByClient.get(keyString);
        if (lease != null) {
            if ((lease.state() == LeaseState.ACTIVE || lease.state() == LeaseState.OFFERED) && now.isBefore(lease.expiresAt()))
                return Optional.of(lease);

            leasesByClient.remove(keyString, lease);
            leasesByAddress.remove(lease.id().address(), lease);
        }

        return range.addressStream()
                .filter(address -> {
                    if (!range.contains(address)) throw new IllegalStateException();
                    var forAddress = leasesByAddress.get(address);
                    return forAddress == null ||
                            !LeaseState.VALID.contains(forAddress.state()) ||
                            !forAddress.expiresAt().isAfter(now);
                })
                .findAny()
                .map(availableAddress -> new Lease(new LeaseId(request.client(), availableAddress),
                        now,
                        now.plus(request.preferredDuration().orElse(defaultDuration)),
                        LeaseState.OFFERED))
                .map(l -> {
                    leasesByClient.put(keyString, l);
                    leasesByAddress.put(l.id().address(), l);
                    return l;
                });
    }


    public RequestResult commit(CommitRequest request) {
        log.debug("begin commit with request: '{}'", request);
        var existingLease = leasesByClient.get(request.client().getKeyString());
        var now = clock.instant();
        if (existingLease != null) {
            if (existingLease.state() == LeaseState.ACTIVE && now.isBefore(existingLease.expiresAt())) {
                if (existingLease.id().address().equals(request.requestedAddress())) {
                    return new RequestResult.Ack(existingLease);
                } else {
                    return new RequestResult.Nak("Client already has a lease for another address");
                }
            }
        }
        LeaseOffer offer = offersByClient.get(request.client().getKeyString());
    //     if (offer == null) {
    //         return new RequestResult.Nak("No outstanding offer for client");
    //     }
    //     if (now.isAfter(offer.expiresAt())) {
    //         offersByClient.remove(request.client().getKeyString());
    //         return new RequestResult.Nak("Offer has expired");
    //     }
    //     if (!offer.address().equals(request.requestedAddress())) {
    //         return new RequestResult.Nak("Requested address does not match offered address");
    //     }
    //     if (offersByAddress.get(request.requestedAddress()) != offer) {
    //         offersByClient.remove(request.client().getKeyString());
    //         return new RequestResult.Nak("Address is not reserved for this client");
    //     }
    //     if (leasesByAddress.containsKey(request.requestedAddress())) {
    //         offersByClient.remove(request.client().getKeyString());
    //         offersByAddress.remove(request.requestedAddress());
    //         return new RequestResult.Nak("Address is already leased");
    //     }
    //
    //     Lease lease = new Lease(new LeaseId(request.client(), request.requestedAddress()),
    //             now,
    //             now.plus(request.preferredDuration().orElse(defaultDuration)),
    //             LeaseState.ACTIVE);
    //
    //     offersByClient.remove(request.client().getKeyString());
    //     leasesByClient.put(request.client().getKeyString(), lease);
    //     leasesByAddress.put(request.requestedAddress(), lease);
    //
        return new RequestResult.Ack(lease);
    }

    public RequestResult renew(RenewRequest request) {
        return null;
    }

    public boolean release(ClientKey client, DhcpIpAddress address) {
        return false;
    }

    public void decline(ClientKey client, DhcpIpAddress address) {

    }

    public Optional<Lease> findByClient(ClientKey client) {
        return Optional.empty();
    }

    public Optional<Lease> findByAddress(DhcpIpAddress address) {
        return Optional.empty();
    }
}
