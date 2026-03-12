package side.cloud.util.acme.lib.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.Base64URL;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class SupportedClientKeyPair {
    private static final Base64.Encoder B64E = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    @NotNull
    @EqualsAndHashCode.Exclude
    KeyPair keyPair;
    @NotNull
    SupportedClientKeyPairAlgorithm algorithm;

    @JsonCreator
    @SneakyThrows
    public static SupportedClientKeyPair deserialize(String encoded) {
        String[] parts = encoded.split(":", 4);
        if (parts.length != 4) {
            throw new RuntimeException("Invalid serialized key format");
        }

        String version = parts[0];
        String alg = parts[1];

        if (!"v1".equals(version)) {
            throw new RuntimeException("Unsupported version: " + version);
        }

        var supportedAlg = SupportedClientKeyPairAlgorithm.valueOf(alg);
        KeyFactory kf = supportedAlg.keyFactory();
        var pub = kf.generatePublic(new X509EncodedKeySpec(B64D.decode(parts[2])));
        var priv = kf.generatePrivate(new PKCS8EncodedKeySpec(B64D.decode(parts[3])));

        return new SupportedClientKeyPair()
                .setAlgorithm(supportedAlg)
                .setKeyPair(new KeyPair(pub, priv));
    }

    /**
     * @see org.bouncycastle.asn1.pkcs.PrivateKeyInfo#toASN1Primitive()
     */
    @SneakyThrows
    public static byte[] extractPrivateKeyOctets(byte[] encoded) {
        ASN1Primitive primitive = ASN1Primitive.fromByteArray(encoded);
        PrivateKeyInfo keyInfo = PrivateKeyInfo.getInstance(primitive);
        ASN1OctetString privateKeyOctet = keyInfo.getPrivateKey(); // Outer PKCS#8 OCTET STRING
        byte[] inner = privateKeyOctet.getOctets();
        ASN1OctetString oct = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(inner)); // Decode inner ASN.1 structure
        return oct.getOctets();
    }

    @SneakyThrows
    public static byte[] extractPublicKeyOctets(byte[] encoded) {
        ASN1Primitive primitive = ASN1Primitive.fromByteArray(encoded);
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(primitive);
        byte[] key = keyInfo.getPublicKeyData().getBytes();
        if (key.length != 32)
            throw new IllegalStateException("Expected 32 byte Ed25519 public key, got " + key.length);
        return key;
    }

    @AssertTrue
    public boolean isHasBothPublicAndPrivate() {
        if (keyPair == null)
            return true;
        return keyPair.getPublic() != null && keyPair.getPrivate() != null;
    }

    @AssertTrue
    public boolean isBothPublicAndPrivateSameAlgorithm() {
        return !isHasBothPublicAndPrivate() ||
                (Objects.equals(keyPair.getPublic().getAlgorithm(),
                        keyPair.getPrivate().getAlgorithm()));
    }

    @EqualsAndHashCode.Include
    public byte[] getKeyPairPrivateEncoded() {
        if (keyPair == null) {
            return null;
        }
        var keyPairPrivate = keyPair.getPrivate();
        if (keyPairPrivate == null) {
            return null;
        }
        return keyPairPrivate.getEncoded();
    }

    @EqualsAndHashCode.Include
    public byte[] getKeyPairPublicEncoded() {
        if (keyPair == null) {
            return null;
        }
        var keyPairPublic = keyPair.getPublic();
        if (keyPairPublic == null) {
            return null;
        }
        return keyPairPublic.getEncoded();
    }

    @JsonValue
    public String serialize() {
        String pub = B64E.encodeToString(keyPair.getPublic().getEncoded());
        String priv = B64E.encodeToString(keyPair.getPrivate().getEncoded());

        return "v1:" + algorithm.name() + ":" + pub + ":" + priv;
    }

    @SneakyThrows
    public byte[] sign(byte[] input) {
        var signer = bcSigner(Mode.SIGN);
        signer.reset();
        signer.update(input, 0, input.length);
        return signer.generateSignature();
    }

    @SneakyThrows
    public boolean verify(byte[] input, byte[] signature) {
        var signer = bcSigner(Mode.VERIFY);
        signer.reset();
        signer.update(input, 0, input.length);
        return signer.verifySignature(signature);
    }

    public Signer bcSigner(Mode verify) {
        return BcSignerFactory.INSTANCE.signer(keyPair, algorithm, verify);
    }

    public JWK asJwk() {
        var jwsAlgorithm = JWSAlgorithm.parse(algorithm.name());
        return switch (algorithm) {
            case RS256, RS384, RS512 -> new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .algorithm(jwsAlgorithm)
                    .build();
            case ES256, ES384, ES512 -> {
                var curve = switch (algorithm) {
                    case ES256 -> Curve.P_256;
                    case ES384 -> Curve.P_384;
                    case ES512 -> Curve.P_521;
                    case null, default -> throw new UnsupportedOperationException();
                };
                yield new ECKey.Builder(curve, (ECPublicKey) keyPair.getPublic())
                        .privateKey((ECPrivateKey) keyPair.getPrivate())
                        .algorithm(jwsAlgorithm)
                        .build();
            }
            case EdDSA -> {
                OctetKeyPair okp = new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(extractPublicKeyOctets(getKeyPair().getPublic().getEncoded())))
                        .d(Base64URL.encode(extractPrivateKeyOctets(getKeyPair().getPrivate().getEncoded())))
                        .build();
                yield new OctetKeyPair.Builder(okp)
                        .algorithm(jwsAlgorithm)
                        .build();
            }
        };
    }

    @SneakyThrows
    public JWSSigner nimbusSigner() {
        return switch (algorithm) {
            case RS256, RS384, RS512 -> new RSASSASigner(keyPair.getPrivate());
            case ES256, ES384, ES512 -> new com.nimbusds.jose.crypto.ECDSASigner((ECPrivateKey) keyPair.getPrivate());
            case EdDSA -> {
                var okp = (OctetKeyPair) asJwk();
                yield new com.nimbusds.jose.crypto.Ed25519Signer(okp);
            }
        };
    }

    @SneakyThrows
    public JWSVerifier nimbusVerifier() {
        return switch (algorithm) {
            case RS256, RS384, RS512 -> new RSASSAVerifier((RSAPublicKey) keyPair.getPublic());
            case ES256, ES384, ES512 -> new com.nimbusds.jose.crypto.ECDSAVerifier((ECPublicKey) keyPair.getPublic());
            case EdDSA -> {
                OctetKeyPair okp = new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(extractPublicKeyOctets(getKeyPair().getPublic().getEncoded())))
                        .build();
                yield new com.nimbusds.jose.crypto.Ed25519Verifier(okp);
            }
        };
    }

    @SneakyThrows
    public String signAndSerialize(AcmeJwsObject jwsObject) {
        JWSObjectJSON jwsObjectJSON = new JWSObjectJSON(new Payload(jwsObject.getPayload()));
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(algorithm.name()))
                .jwk(asJwk().toPublicJWK())
                .customParams(jwsObject.getHeaders())
                .build();

        jwsObjectJSON.sign(jwsHeader, nimbusSigner());
        return jwsObjectJSON.serializeFlattened();
    }

    public enum Mode {
        SIGN, VERIFY
    }

    static class BcSignerFactory {
        static final BcSignerFactory INSTANCE = new BcSignerFactory();

        Signer signer(KeyPair keyPair, SupportedClientKeyPairAlgorithm algorithm, Mode mode) {
            boolean forSigning = mode == Mode.SIGN;

            return switch (algorithm) {
                case EdDSA -> {
                    var signer = new Ed25519Signer();
                    if (forSigning) {
                        signer.init(true, new Ed25519PrivateKeyParameters(keyPair.getPrivate().getEncoded()));
                    } else {
                        signer.init(false,
                                new org.bouncycastle.crypto.params.Ed25519PublicKeyParameters(
                                        keyPair.getPublic().getEncoded(), 0));
                    }
                    yield signer;
                }

                case RS256, RS384, RS512 -> {
                    var digest = switch (algorithm) {
                        case RS256 -> new SHA256Digest();
                        case RS384 -> new SHA384Digest();
                        case RS512 -> new SHA512Digest();
                        default -> throw new UnsupportedOperationException();
                    };

                    var signer = new RSADigestSigner(digest);

                    if (forSigning) {
                        var rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
                        signer.init(true,
                                new RSAKeyParameters(true,
                                        rsaPrivateKey.getModulus(),
                                        rsaPrivateKey.getPrivateExponent()));
                    } else {
                        var rsaPublicKey = (java.security.interfaces.RSAPublicKey) keyPair.getPublic();
                        signer.init(false,
                                new RSAKeyParameters(false,
                                        rsaPublicKey.getModulus(),
                                        rsaPublicKey.getPublicExponent()));
                    }

                    yield signer;
                }

                case ES256, ES384, ES512 -> {
                    var digest = switch (algorithm) {
                        case ES256 -> new SHA256Digest();
                        case ES384 -> new SHA384Digest();
                        case ES512 -> new SHA512Digest();
                        default -> throw new UnsupportedOperationException();
                    };

                    try {
                        var signer = new DSADigestSigner(new ECDSASigner(), digest);

                        if (forSigning) {
                            var privateKeyParams = (ECPrivateKeyParameters)
                                    PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
                            signer.init(true, privateKeyParams);
                        } else {
                            var publicKeyParams =
                                    org.bouncycastle.crypto.util.PublicKeyFactory.createKey(
                                            keyPair.getPublic().getEncoded());
                            signer.init(false, publicKeyParams);
                        }

                        yield signer;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                case null -> throw new UnsupportedOperationException();
            };
        }
    }
}
