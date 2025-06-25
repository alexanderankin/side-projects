package side;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;


public class CopyToken {
    @SneakyThrows
    public static void main(String[] args) {
        String inputFile = args.length > 0 ? args[0] : "/tmp/token";
        String rsaKeyFile = args.length > 1 ? args[1] : "/tmp/key";
        System.out.println(inputFile);
        String fileContents = Files.readString(Path.of(inputFile)).strip();
        String keyContents = Files.readString(Path.of(rsaKeyFile)).strip();
        var parts = Arrays.asList(fileContents.split("\\."));
        if (!(parts.size() >= 2)) {
            throw new RuntimeException("we need the first two parts of the token, but size was not >= 2");
        }
        String header = new String(Base64.getDecoder().decode(parts.getFirst()), StandardCharsets.UTF_8);
        String body = new String(Base64.getDecoder().decode(parts.get(1)), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        LinkedHashMap<String, Object> headerMap = objectMapper.readValue(header, new TypeReference<>() {
        });
        LinkedHashMap<String, Object> bodyMap = objectMapper.readValue(body, new TypeReference<>() {
        });

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).customParams(headerMap).build();

        long now = System.currentTimeMillis();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("iss")) {
                builder.claim("iss", now / 1000);
            } else if (key.equals("exp")) {
                builder.claim("iss", Instant.ofEpochMilli(now).plus(Duration.ofDays(365 * 10)).toEpochMilli() / 1000);
            } else {
                builder.claim(key, value);
            }
        }
        JWTClaimsSet jwtClaimsSet = builder.build();

        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);

        var privateKey = loadPrivateKeyFromPKCS1(keyContents);
        JWSSigner signer = new RSASSASigner(privateKey);
        jwt.sign(signer);

        System.out.println("JWT: ");
        System.out.println(jwt.serialize());
    }

    // Helper: decode PKCS#1 PEM (BEGIN RSA PRIVATE KEY) into RSAPrivateKey
    private static PrivateKey loadPrivateKeyFromPKCS1(String pem) throws Exception {
        try (PEMParser parser = new PEMParser(new StringReader(pem))) {
            Object object = parser.readObject();

            switch (object) {
                case PEMKeyPair keyPair -> {
                    PrivateKeyInfo privateKeyInfo = keyPair.getPrivateKeyInfo();
                    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
                }
                case RSAPrivateKey rsa -> {
                    RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
                            rsa.getModulus(),
                            rsa.getPublicExponent(),
                            rsa.getPrivateExponent(),
                            rsa.getPrime1(),
                            rsa.getPrime2(),
                            rsa.getExponent1(),
                            rsa.getExponent2(),
                            rsa.getCoefficient()
                    );

                    return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
                }
                default -> throw new RuntimeException("unknown private key type: " + object.getClass().getName());
            }
        }
    }
}
