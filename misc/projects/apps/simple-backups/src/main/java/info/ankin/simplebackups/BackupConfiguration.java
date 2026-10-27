package info.ankin.simplebackups;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public final class BackupConfiguration {
    private final UUID id;
    private final String name;
    private final URI from;
    private final URI to;

    public BackupConfiguration(UUID id, String name, URI from, URI to) {
        Objects.requireNonNull(id, "Backup ID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("A backup name is required");
        validateUri(from, Set.of("file", "ssh"), "Source");
        validateUri(to, Set.of("file", "s3"), "Destination");
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
    }

    private static void validateUri(URI uri, Set<String> schemes, String label) {
        if (uri == null || !schemes.contains(uri.getScheme()))
            throw new IllegalArgumentException(label + " must use " + schemes);
        if (uri.getQuery() != null || uri.getFragment() != null || uri.isOpaque())
            throw new IllegalArgumentException(label + " must be a folder URI without a query or fragment");
        if ("file".equals(uri.getScheme())) {
            if (!Path.of(uri).isAbsolute()) throw new IllegalArgumentException(label + " must be absolute");
        } else {
            if (uri.getHost() == null) throw new IllegalArgumentException(label + " needs a host / bucket name");
            if ("s3".equals(uri.getScheme()) && (uri.getUserInfo() != null || uri.getPort() != -1))
                throw new IllegalArgumentException("S3 URIs must be s3://bucket/prefix");
            if ("ssh".equals(uri.getScheme())) {
                if (uri.getPath() == null || !uri.getPath().startsWith("/"))
                    throw new IllegalArgumentException("SSH requires an absolute remote folder path");
                if (uri.getPort() == 0 || uri.getPort() > 65535)
                    throw new IllegalArgumentException("SSH port must be between 1 and 65535");
                if (uri.getUserInfo() != null && (uri.getUserInfo().isBlank() || uri.getUserInfo().contains(":")))
                    throw new IllegalArgumentException("Use ssh://username@host/path without a password");
            }
        }
    }
}
