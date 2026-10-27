package info.ankin.simplebackups;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** JSch has no accept-new mode: enroll unknown keys before its strict host-key check. */
@Slf4j
final class AcceptNewHostKeys implements HostKeyRepository {
    @Delegate
    private final HostKeyRepository knownHosts;
    private final Path file;

    AcceptNewHostKeys(JSch ssh, Path file) throws IOException, JSchException {
        this.file = file;
        if (Files.exists(file)) {
            // Loading from a stream keeps JSch from rewriting the file or swallowing write errors.
            try (var input = Files.newInputStream(file)) {
                ssh.setKnownHosts(input);
            }
        }
        knownHosts = ssh.getHostKeyRepository();
    }

    @Override
    @SneakyThrows({IOException.class, JSchException.class})
    public synchronized int check(String host, byte[] key) {
        int result = knownHosts.check(host, key);
        if (result != NOT_INCLUDED) return result;

        var hostKey = new HostKey(host, key);
        Path directory = file.toAbsolutePath().getParent();
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            FilesSupport.privatePermissions(directory, true);
        }
        boolean newFile = !Files.exists(file);
        // The leading newline also handles an existing file without a final newline.
        Files.writeString(file, "\n" + host + " " + hostKey.getType() + " " + hostKey.getKey() + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        if (newFile) FilesSupport.privatePermissions(file, false);
        knownHosts.add(hostKey, null);
        log.info("Added SSH host key for {} to {}", host, file);
        return OK;
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return file.toString();
    }
}
