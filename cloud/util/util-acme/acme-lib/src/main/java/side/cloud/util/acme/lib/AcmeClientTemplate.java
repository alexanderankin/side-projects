package side.cloud.util.acme.lib;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Map;

@RequiredArgsConstructor
public class AcmeClientTemplate implements AcmeClientOperations {
    private final RestClient restClient;
    private final URI directoryUri;
    private final SupportedClientKeyPair keyPair;

    @Override
    public String newNonce(AcmeResources.Directory directory) {
        return restClient.get().uri(directory.getNewNonce()).retrieve().toBodilessEntity().getHeaders().getFirst("Replay-Nonce");
    }

    @Override
    public AcmeResources.Directory directory() {
        return restClient.get().uri(directoryUri).retrieve().body(AcmeResources.Directory.class);
    }

    @Override
    public ResponseEntity<String> post(URI uri, JwsHeader header, Map<String, Object> payload) {
        var body = keyPair.signAndSerialize(new AcmeJwsObject()
                .setHeaders(header)
                .setPayload(payload));
        return restClient.post().uri(uri).body(body).retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> postAsGet(URI uri, JwsHeader header) {
        return null;
    }
}
