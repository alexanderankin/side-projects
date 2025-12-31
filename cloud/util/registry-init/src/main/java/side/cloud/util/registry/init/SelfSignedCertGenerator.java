package side.cloud.util.registry.init;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RequiredArgsConstructor
public class SelfSignedCertGenerator {
    final BouncyCastleProvider bc;
    final SecureRandom secureRandom;

    @SneakyThrows
    public Certificate generate() {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", bc);
        kpg.initialize(2048, secureRandom);
        KeyPair keyPair = kpg.generateKeyPair();

        X500Name subject = new X500Name(
                RFC4519Style.INSTANCE,
                "C=XX,ST=StateName,L=CityName,O=CompanyName,OU=CompanySectionName,CN=localhost.local"
        );

        Instant now = Instant.now();
        Date notBefore = Date.from(now.minus(1, ChronoUnit.DAYS));
        Date notAfter = Date.from(now.plus(3650, ChronoUnit.DAYS));

        // === Certificate builder ===
        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        subject,
                        /* serial */ BigInteger.valueOf(System.currentTimeMillis()),
                        notBefore,
                        notAfter,
                        subject,
                        keyPair.getPublic()
                );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider(bc).build(keyPair.getPrivate());

        // === Build certificate ===
        X509CertificateHolder holder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(bc).getCertificate(holder);

        // cert.verify(keyPair.getPublic(), bc);

        // === Write PEM files ===
        StringWriter privateWriter = new StringWriter();
        try (JcaPEMWriter w = new JcaPEMWriter(privateWriter)) {
            w.writeObject(keyPair.getPrivate());
        }

        StringWriter publicWriter = new StringWriter();
        try (JcaPEMWriter w = new JcaPEMWriter(publicWriter)) {
            w.writeObject(cert);
        }

        return new Certificate(publicWriter.toString(), privateWriter.toString());
    }

    public record Certificate(String cert, String key) {
    }
}
