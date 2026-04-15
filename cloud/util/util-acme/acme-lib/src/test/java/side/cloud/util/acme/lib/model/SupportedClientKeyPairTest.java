package side.cloud.util.acme.lib.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportedClientKeyPairTest {

    @ParameterizedTest
    @EnumSource(value = SupportedClientKeyPairAlgorithm.class, names = {"RS512", "RS384"}, mode = EnumSource.Mode.EXCLUDE)
    void test_serde(SupportedClientKeyPairAlgorithm algorithm) {
        var keyPair = algorithm.generate();

        var deserialized = SupportedClientKeyPair.deserialize(keyPair.serialize());
        assertEquals(algorithm, deserialized.getAlgorithm());
        assertArrayEquals(keyPair.getKeyPairPublicEncoded(), deserialized.getKeyPair().getPublic().getEncoded());
    }

    @ParameterizedTest
    @EnumSource(value = SupportedClientKeyPairAlgorithm.class, names = {"RS512", "RS384"}, mode = EnumSource.Mode.EXCLUDE)
    void test_serdePublic(SupportedClientKeyPairAlgorithm algorithm) {
        var keyPair = algorithm.generate().asPublic();

        var deserialized = SupportedClientKeyPair.deserialize(keyPair.serialize());
        assertEquals(algorithm, deserialized.getAlgorithm());
        assertArrayEquals(keyPair.getKeyPairPublicEncoded(), deserialized.getKeyPair().getPublic().getEncoded());
    }

}
