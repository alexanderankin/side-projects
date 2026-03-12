package side.cloud.util.acme.lib.model;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static side.cloud.util.acme.lib.model.SupportedClientKeyPairAlgorithm.*;

class SupportedClientKeyPairAlgorithmTest {

    @Test
    void test_example() {
        System.out.println(ES256.serialize(ES256.generate()));
    }

    @Test
    void test_SupportedClientKeyPairAlgorithm() {
        var rs = RS256.generate();
        var es = ES256.generate();
        var ed = EdDSA.generate();

        var rsp = SupportedClientKeyPair.deserialize(rs.serialize());
        var esp = SupportedClientKeyPair.deserialize(es.serialize());
        var edp = SupportedClientKeyPair.deserialize(ed.serialize());

        assertThat(rs, is(rsp));
        assertThat(es, is(esp));
        assertThat(ed, is(edp));
    }

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(SupportedClientKeyPairAlgorithm.class)
    void test_signVerifyAllAlgorithms(SupportedClientKeyPairAlgorithm alg) {
        var kp = alg.generate();
        var object = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.parse(alg.name())).build(),
                new Payload(Map.of("hello", "world"))
        );
        System.out.println("Algorithm: " + alg);
        System.out.println(new String(object.getSigningInput()));
        object.sign(kp.nimbusSigner());
        System.out.println(object.serialize());
        assertThat(object.verify(kp.nimbusVerifier()), is(true));
    }

    @SneakyThrows
    @Test
    void test_nimbusSignerVerifier() {
        var kp = SupportedClientKeyPairAlgorithm.EdDSA.generate();
        var header = new JWSHeader.Builder(JWSAlgorithm.parse(kp.getAlgorithm().name())).build();
        var object = new JWSObject(header, new Payload(Map.of("hello", "world")));

        Ed25519Signer signer = (Ed25519Signer) kp.nimbusSigner();
        System.out.println(new String(object.getSigningInput()));
        object.sign(signer);
        System.out.println(object.serialize());
        assertThat(object.verify(kp.nimbusVerifier()), is(true));
    }

    @SneakyThrows
    @Test
    void test() {
        JWSObjectJSON jwsObjectJSON = new JWSObjectJSON(
                new Payload(Map.of("hello", "world"))
        );

        System.out.println(jwsObjectJSON.getPayload().toJSONObject());

        var kp = ES256.generate();
        jwsObjectJSON.sign(
                new JWSHeader.Builder(JWSAlgorithm.parse(kp.getAlgorithm().name()))
                        .jwk(kp.asJwk().toPublicJWK())
                        .build(),
                kp.nimbusSigner()
        );

        System.out.println(jwsObjectJSON.serializeFlattened());
    }
}
