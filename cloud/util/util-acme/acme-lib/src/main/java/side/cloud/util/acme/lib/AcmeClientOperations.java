package side.cloud.util.acme.lib;

import org.springframework.http.ResponseEntity;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;

public interface AcmeClientOperations {
    Directory directory(URI directoryUrl);

    String newNonce(Directory directory);

    /**
     *
     * @param uri       target endpoint
     * @param keyPair   used to sign JWS
     * @param jwsObject header and body
     * @param tClass    response body class
     * @param directory for getting nonce
     * @param <T>       type of the response body
     * @return responseEntity object containing code, headers, and body
     */
    <T> ResponseEntity<T> post(URI uri, SupportedClientKeyPair keyPair, AcmeJwsObject jwsObject, Class<T> tClass, Directory directory);

    default <T> ResponseEntity<T> postGet(URI uri, SupportedClientKeyPair keyPair, AcmeJwsObject.AcmeJwsHeader jwsHeader, Class<T> tClass, Directory directory) {
        return post(uri, keyPair, new AcmeJwsObject.BlankAcmeJwsObject().setHeaders(jwsHeader), tClass, directory);
    }
}
