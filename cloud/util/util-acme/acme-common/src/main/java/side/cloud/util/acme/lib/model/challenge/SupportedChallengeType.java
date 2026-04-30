package side.cloud.util.acme.lib.model.challenge;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@ToString
public enum SupportedChallengeType {
    ChallengeHTTP01("http-01"),
    ChallengeTLSALPN01("tls-alpn-01"),
    ChallengeDNS01("dns-01"),
    ChallengeDNSAccount01("dns-account-01"),
    ChallengeDNSPersist01("dns-persist-01"),
    ;

    public static final List<SupportedChallengeType> ALL_TYPES = Arrays.asList(values());
    public static final List<SupportedChallengeType> HTTP_TYPES = List.of(ChallengeHTTP01, ChallengeTLSALPN01);
    public static final List<SupportedChallengeType> DNS_TYPES = List.of(ChallengeDNS01, ChallengeDNSAccount01, ChallengeDNSPersist01);
    public static final Map<String, SupportedChallengeType> RFC_NAMES = Arrays.stream(values())
            .collect(Collectors.toMap(SupportedChallengeType::getRfcName, Function.identity()));

    @JsonValue
    private final String rfcName;

    public static SupportedChallengeType valueOfRfcName(String rfcName) {
        var supportedChallengeType = RFC_NAMES.get(rfcName);
        if (supportedChallengeType == null) {
            throw new UnsupportedOperationException("Unknown RFC name " + rfcName);
        }
        return supportedChallengeType;
    }
}
