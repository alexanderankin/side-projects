package side.cloud.util.packer;

import lombok.SneakyThrows;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

public class SshKeyPairGenerator {
    public static GeneratedKeyPair generateRsa4096() {
        return generateRsa4096("generated");
    }

    @SneakyThrows
    public static GeneratedKeyPair generateRsa4096(String comment) {
        // --- 1. Generate RSA keypair ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(4096);
        KeyPair kp = kpg.generateKeyPair();

        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();

        // --- 2. Write private key as PEM (OpenSSH/PKCS8 compatible) ---
        StringWriter privateKeyWriter = new StringWriter();
        try (JcaPEMWriter pem = new JcaPEMWriter(privateKeyWriter)) {
            pem.writeObject(privateKey);
        }

        // --- 3. Write OpenSSH-formatted public key ---
        RSAPublicKey rsaPub = (RSAPublicKey) publicKey;
        String publicKeyContents = encodeOpenSshPublicKey(rsaPub, comment);

        return new GeneratedKeyPair(publicKeyContents, privateKeyWriter.toString());
    }

    // Build "ssh-rsa AAAAB3..." format
    private static String encodeOpenSshPublicKey(RSAPublicKey pub, String comment) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Write string lengths + bytes (OpenSSH binary encoding)
        writeSshDashRsaString(out);
        writeMpint(out, pub.getPublicExponent());
        writeMpint(out, pub.getModulus());

        String b64 = java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        return "ssh-rsa " + b64 + " " + comment;
    }

    private static void writeSshDashRsaString(OutputStream out) throws IOException {
        byte[] bytes = "ssh-rsa".getBytes();
        out.write(intToBytes(bytes.length));
        out.write(bytes);
    }

    private static void writeMpint(OutputStream out, java.math.BigInteger bi) throws IOException {
        byte[] bytes = bi.toByteArray();
        out.write(intToBytes(bytes.length));
        out.write(bytes);
    }

    private static byte[] intToBytes(int v) {
        return new byte[]{
                (byte) (v >>> 24),
                (byte) (v >>> 16),
                (byte) (v >>> 8),
                (byte) v
        };
    }

    public record GeneratedKeyPair(String publicKey, String privateKey) {
    }
}
