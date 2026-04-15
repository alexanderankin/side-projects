package side.cloud.util.acme.lib.challenges;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public enum KnownChallengeType {
    ChallengeHTTP01("http-01"),
    ChallengeTLSALPN01("tls-alpn-01"),
    ChallengeDNS01("dns-01"),
    ChallengeDNSAccount01("dns-account-01"),
    ChallengeDNSPersist01("dns-persist-01"),
    ;

    public static final List<KnownChallengeType> ALL_TYPES = Arrays.asList(values());
    public static final List<KnownChallengeType> HTTP_TYPES = List.of(ChallengeHTTP01, ChallengeTLSALPN01);
    public static final List<KnownChallengeType> DNS_TYPES = List.of(ChallengeDNS01, ChallengeDNSAccount01, ChallengeDNSPersist01);

    private final String rfcName;
}
