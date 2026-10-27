package info.ankin.simplebackups;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AcceptNewHostKeysTest {
    @TempDir Path home;

    @Test void acceptsAndPersistsNewUnhashedKeysAndRecognizesThemOnNextConnection() throws Exception {
        Path file = home.resolve(".ssh/known_hosts");
        String host = "[server]:2222";
        var repository = new AcceptNewHostKeys(new JSch(), file);
        assertEquals(HostKeyRepository.OK, repository.check(host, key(1)));
        String saved = Files.readString(file);
        assertTrue(saved.contains(host + " ssh-ed25519 "));
        assertFalse(saved.contains("|1|"));

        var nextConnection = new AcceptNewHostKeys(new JSch(), file);
        assertEquals(HostKeyRepository.OK, nextConnection.check(host, key(1)));
        assertEquals(saved, Files.readString(file), "An already-known key must not be appended again");
    }

    @Test void changedKeysAreRejectedWithoutChangingKnownHosts() throws Exception {
        Path file = home.resolve("known_hosts");
        var repository = new AcceptNewHostKeys(new JSch(), file);
        repository.check("server", key(1));
        String saved = Files.readString(file);

        assertEquals(HostKeyRepository.CHANGED, repository.check("server", key(2)));
        assertEquals(saved, Files.readString(file));
        assertEquals(HostKeyRepository.OK, repository.check("server", key(1)));
    }

    @Test void appendsWithoutRewritingExistingEntriesOrComments() throws Exception {
        Path file = home.resolve("known_hosts");
        String existing = "# User's existing file\nother-server ssh-ed25519 " + new HostKey("other-server", key(1)).getKey();
        Files.writeString(file, existing);
        var repository = new AcceptNewHostKeys(new JSch(), file);
        assertEquals(HostKeyRepository.OK, repository.check("new-server", key(2)));
        assertTrue(Files.readString(file).startsWith(existing + "\nnew-server ssh-ed25519 "));
    }

    @Test void existingHashedAndRevokedEntriesRemainEffective() throws Exception {
        Path file = home.resolve("known_hosts");
        byte[] salt = new byte[20];
        Arrays.fill(salt, (byte) 7);
        var hmac = Mac.getInstance("HmacSHA1");
        hmac.init(new SecretKeySpec(salt, "HmacSHA1"));
        var base64 = Base64.getEncoder();
        String hashedHost = "|1|" + base64.encodeToString(salt) + "|"
                + base64.encodeToString(hmac.doFinal("server".getBytes(StandardCharsets.UTF_8)));
        String content = hashedHost + " ssh-ed25519 " + new HostKey("server", key(1)).getKey() + "\n"
                + "@revoked revoked-server ssh-ed25519 " + new HostKey("revoked-server", key(2)).getKey() + "\n";
        Files.writeString(file, content);
        var repository = new AcceptNewHostKeys(new JSch(), file);
        assertEquals(HostKeyRepository.OK, repository.check("server", key(1)));
        assertEquals(HostKeyRepository.CHANGED, repository.check("server", key(2)));
        // JSch's strict check inspects this marker after checking the key's bytes.
        assertEquals("@revoked", repository.getHostKey("revoked-server", "ssh-ed25519")[0].getMarker());
        assertEquals(content, Files.readString(file));
    }

    @Test void persistenceFailureDoesNotAcceptTheKey() throws Exception {
        Path parentFile = home.resolve("not-a-directory");
        Files.writeString(parentFile, "keep this");
        var repository = new AcceptNewHostKeys(new JSch(), parentFile.resolve("known_hosts"));
        assertThrows(IOException.class, () -> repository.check("server", key(1)));
        assertEquals(0, repository.getHostKey().length);
        assertEquals("keep this", Files.readString(parentFile));
    }

    private byte[] key(int value) {
        byte[] type = "ssh-ed25519".getBytes(StandardCharsets.US_ASCII);
        byte[] publicKey = new byte[32];
        Arrays.fill(publicKey, (byte) value);
        return ByteBuffer.allocate(4 + type.length + 4 + publicKey.length)
                .putInt(type.length).put(type).putInt(publicKey.length).put(publicKey).array();
    }
}
