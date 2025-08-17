package org.ssas;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class Rsa {
    @Bean
    JWKSource<SecurityContext> jwkSource(Rsa.RsaKeyProperties.RsaKey rsaKey) {
        Rsa.ParsedRsaKey parsedRsaKey = Rsa.ParsedRsaKey.generateRsaKey(rsaKey);

        JWK jwk = new RSAKey.Builder((RSAPublicKey) parsedRsaKey.parsedKeyPair().getPublic())
                .keyID("kid")
                .privateKey(parsedRsaKey.parsedKeyPair().getPrivate())
                .build();
        JWKSet jwkSet = new JWKSet(jwk);
        return ((jwkSelector, context) -> jwkSelector.select(jwkSet));
    }

    public static class RsaKeyProperties {
        @Component
        @ConfigurationProperties(prefix = "app.rsa")
        @Data
        @Accessors(chain = true)
        public static class RsaKey {
            String publicKey;
            String privateKey;
        }
    }

    record ParsedRsaKey(RsaKeyProperties.RsaKey key, KeyPair parsedKeyPair) {
        // An instance of java.security.KeyPair with keys generated on startup used to create the JWKSource above.
        public static ParsedRsaKey generateRsaKey(RsaKeyProperties.RsaKey rsaKey) {
            String publicKeyPem = rsaKey.getPublicKey();
            String privateKeyPem = rsaKey.getPrivateKey();

            // KeyPair keyPair;
            // try {
            //     KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            //     keyPairGenerator.initialize(2048);
            //     keyPair = keyPairGenerator.generateKeyPair();
            // } catch (Exception ex) {
            //     throw new IllegalStateException(ex);
            // }
            // return keyPair;

            try {
                // Load the private key
                privateKeyPem = privateKeyPem
                        .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                        .replaceAll("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] decodedPrivate = Base64.getDecoder().decode(privateKeyPem);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivate);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                // Load the public key
                publicKeyPem = publicKeyPem
                        .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                        .replaceAll("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] decodedPublic = Base64.getDecoder().decode(publicKeyPem);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodedPublic);
                PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

                return new ParsedRsaKey(rsaKey, new KeyPair(publicKey, privateKey));

            } catch (Exception e) {
                throw new IllegalStateException("Failed to load RSA key pair from PEM files", e);
            }
        }
    }
}
