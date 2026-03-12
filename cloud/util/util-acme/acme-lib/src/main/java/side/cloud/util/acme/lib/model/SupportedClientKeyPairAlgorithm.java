package side.cloud.util.acme.lib.model;

import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public enum SupportedClientKeyPairAlgorithm {
    RS256, RS384, RS512,
    ES256, ES384, ES512,
    /**
     * Ed25519
     */
    EdDSA,
    ;

    private static final Provider BC = new BouncyCastleProvider();

    @SneakyThrows
    private static KeyPair generateRsa(int bits) {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", BC);
        kpg.initialize(bits);
        return kpg.generateKeyPair();
    }

    @SneakyThrows
    private static KeyPair generateEc(String curve) {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", BC);
        kpg.initialize(new ECGenParameterSpec(curve));
        return kpg.generateKeyPair();
    }

    @SneakyThrows
    private static KeyPair generateEd25519() {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519", BC);
        return kpg.generateKeyPair();
    }

    @SneakyThrows
    public SupportedClientKeyPair generate() {
        var kp = switch (this) {
            case RS256 -> generateRsa(2048);
            case RS384 -> generateRsa(3072);
            case RS512 -> generateRsa(4096);
            case ES256 -> generateEc("secp256r1");
            case ES384 -> generateEc("secp384r1");
            case ES512 -> generateEc("secp521r1");
            case EdDSA -> generateEd25519();
        };

        return new SupportedClientKeyPair()
                .setAlgorithm(this)
                .setKeyPair(kp);
    }

    public String serialize(SupportedClientKeyPair keyPair) {
        return keyPair.serialize();
    }

    @SneakyThrows
    public KeyPair parse(String serialized) {
        return SupportedClientKeyPair.deserialize(serialized).getKeyPair();
    }

    public KeyFactory keyFactory() throws GeneralSecurityException {
        return switch (this) {
            case RS256, RS384, RS512 -> KeyFactory.getInstance("RSA", BC);
            case ES256, ES384, ES512 -> KeyFactory.getInstance("EC", BC);
            case EdDSA -> KeyFactory.getInstance("Ed25519", BC);
        };
    }

}
