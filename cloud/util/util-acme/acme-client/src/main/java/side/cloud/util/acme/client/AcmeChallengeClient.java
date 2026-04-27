package side.cloud.util.acme.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AcmeChallengeClient {
    private final AcmeClient acmeClient;
}
