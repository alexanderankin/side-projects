package side.cloud.util.acme.lib.keys.requests;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObjectJSON;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.ServerRequest;
import side.cloud.util.acme.lib.keys.AcmeJwsObject;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPairAlgorithm;
import side.cloud.util.acme.lib.model.AcmeRequests;

import java.net.URI;
import java.security.KeyPair;
import java.util.List;

@RequiredArgsConstructor
public class AcmeRequestSerDe {
    public static final String APPLICATION_JOSE_JSON = "application/jose+json";
    public static final String REPLAY_NONCE = "Replay-Nonce";

    public static RequestEntity<String> serialize(RequestAndKeyPair requestAndKeyPair) {
        AcmeRequests.AcmeRequest acmeRequest = requestAndKeyPair.request();
        SupportedClientKeyPair keyPair = requestAndKeyPair.keyPair();

        // headers
        AcmeJwsObject.AcmeJwsHeader headers;
        var kid = acmeRequest.getAccountId();
        if (kid != null)
            headers = new AcmeJwsObject.AcmeJwsHeader.KidAcmeJwsHeader().setKid(kid);
        else
            headers = new AcmeJwsObject.AcmeJwsHeader.JwkAcmeJwsHeader().setJwk(keyPair.asJwk().toPublicJWK());

        headers.setAlg(keyPair.getAlgorithm().name())
                .setUrl(acmeRequest.getUrl())
                .setNonce(acmeRequest.getNonce());

        // object
        AcmeJwsObject acmeJwsObject;
        var requestPayload = acmeRequest.getPayload();
        if (requestPayload != null) {
            acmeJwsObject = new AcmeJwsObject.JsonAcmeJwsObject().setPayload(requestPayload);
        } else {
            acmeJwsObject = new AcmeJwsObject.BlankAcmeJwsObject();
        }
        acmeJwsObject.setHeaders(headers);


        var body = keyPair.signAndSerialize(acmeJwsObject);

        return RequestEntity.post(acmeRequest.getUrl())
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE_JSON)
                .body(body);
    }

    @SneakyThrows
    public static RequestEntity<String> from(ServerRequest serverRequest) {
        return RequestEntity.method(serverRequest.method(), serverRequest.uri()).body(serverRequest.body(String.class));
    }

    @SneakyThrows
    public static ServerRequest from(RequestEntity<String> requestEntity) {
        return ServerRequest.create(new MockServerRequest(requestEntity), List.of(new StringHttpMessageConverter()));
    }

    @SneakyThrows
    public static RequestAndKeyPair deserialize(ServerRequest serverRequest) {
        var nonce = serverRequest.headers().firstHeader(REPLAY_NONCE);
        Assert.isTrue(nonce != null, REPLAY_NONCE + " must not be present");

        var body = serverRequest.body(String.class);
        var json = JWSObjectJSON.parse(body);
        Assert.isTrue(json.getSignatures().size() == 1, "must have exactly one signature");
        var signature = json.getSignatures().getFirst();

        var keyId = signature.getUnprotectedHeader().getKeyID();
        var keyIdNotNull = keyId != null;
        var jwk = signature.getHeader().getJWK();
        var jwkNotNull = jwk != null;
        Assert.isTrue((keyIdNotNull || jwkNotNull) && !(keyIdNotNull && jwkNotNull), "must have either kid or jwk but not both");

        var payload = json.getPayload();
        var payloadObject = (payload == null || payload.toString().isEmpty()) ? null : payload.toJSONObject();

        var request = new AcmeRequests.AcmeRequest()
                .setUrl(serverRequest.uri())
                .setNonce(nonce)
                .setAccountId(keyIdNotNull ? URI.create(keyId) : null)
                .setPayload(payloadObject);

        var keyPair = keyIdNotNull ? null : new SupportedClientKeyPair()
                .setAlgorithm(SupportedClientKeyPairAlgorithm.valueOf(signature.getHeader().getAlgorithm().getName()))
                .setKeyPair(
                        new KeyPair(switch (jwk) {
                            case RSAKey rsaKey -> rsaKey.toPublicKey();
                            case ECKey ecKey -> ecKey.toPublicKey();
                            case OctetKeyPair okp -> okp.toPublicKey();
                            case null, default -> throw new JOSEException("Unsupported JWK type");
                        }, null)
                );

        return new RequestAndKeyPair(request, keyPair);
    }

    public record RequestAndKeyPair(AcmeRequests.AcmeRequest request, SupportedClientKeyPair keyPair) {
    }

}
