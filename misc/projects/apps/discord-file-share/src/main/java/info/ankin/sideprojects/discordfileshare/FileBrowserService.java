package info.ankin.sideprojects.discordfileshare;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FileBrowserService {

    private final Path rootDirectory;
    private final boolean restricted;

    public FileBrowserService(FileShareProperties properties) {
        this.rootDirectory = properties.rootDirectory().toAbsolutePath().normalize();
        this.restricted = !properties.guilds().isEmpty();
    }

    @PreAuthorize("isAuthenticated()")
    public DirectoryListing list(String requestedPath) throws IOException {
        Path directory = resolveInsideRoot(requestedPath);
        if (!Files.isDirectory(directory)) {
            throw new ResponseStatusException(NOT_FOUND, "Directory not found");
        }

        String currentPath = relativePath(directory);
        String parentPath = parentPath(directory);

        AccessProfile accessProfile = currentAccessProfile();
        if (!allowedDirectory(currentPath, accessProfile)) {
            throw new ResponseStatusException(FORBIDDEN, "Discord channel access is required");
        }
        Set<String> allowedChildren = allowedChildren(currentPath, accessProfile);

        try (var stream = Files.list(directory)) {
            List<FileEntry> entries = stream
                    .filter(path -> allowedChildren == null || allowedChildren.contains(path.getFileName().toString()))
                    .map(this::toFileEntry)
                    .sorted(Comparator
                            .comparing(FileEntry::directory).reversed()
                            .thenComparing(entry -> entry.name().toLowerCase())
                            .thenComparing(FileEntry::name))
                    .toList();

            return new DirectoryListing(currentPath, parentPath, directory, entries);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @PreAuthorize("isAuthenticated()")
    public Resource download(String requestedPath) throws IOException {
        Path file = resolveInsideRoot(requestedPath);
        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(NOT_FOUND, "File not found");
        }
        assertReadable(relativePath(file), currentAccessProfile());
        return new FileSystemResource(file);
    }

    @PreAuthorize("isAuthenticated()")
    public String filename(String requestedPath) throws IOException {
        return resolveInsideRoot(requestedPath).getFileName().toString();
    }

    private FileEntry toFileEntry(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            boolean directory = attributes.isDirectory();
            long size = directory ? -1 : attributes.size();
            return new FileEntry(
                    path.getFileName().toString(),
                    relativePath(path),
                    directory,
                    size,
                    displaySize(size),
                    attributes.lastModifiedTime().toInstant());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolveInsideRoot(String requestedPath) throws IOException {
        Path resolved = StringUtils.hasText(requestedPath)
                ? rootDirectory.resolve(requestedPath).normalize()
                : rootDirectory;

        if (!resolved.startsWith(rootDirectory)) {
            throw new ResponseStatusException(FORBIDDEN, "Path escapes configured root");
        }

        Path realRoot = rootDirectory.toRealPath();
        Path realResolved = resolved.toRealPath();
        if (!realResolved.startsWith(realRoot)) {
            throw new ResponseStatusException(FORBIDDEN, "Path escapes configured root");
        }

        return realResolved;
    }

    private String relativePath(Path path) {
        Path relative = rootDirectory.relativize(path);
        return relative.toString().replace(path.getFileSystem().getSeparator(), "/");
    }

    private String parentPath(Path directory) {
        if (directory.equals(rootDirectory)) {
            return null;
        }
        String parent = relativePath(directory.getParent());
        return parent.isEmpty() || allowedDirectory(parent, currentAccessProfile()) ? parent : "";
    }

    private AccessProfile currentAccessProfile() {
        if (!restricted) {
            return AccessProfile.unrestrictedAccess();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DiscordFileSharePrincipal principal) {
            return principal.accessProfile();
        }

        throw new ResponseStatusException(FORBIDDEN, "Discord server access is required");
    }

    private Set<String> allowedChildren(String currentPath, AccessProfile accessProfile) {
        if (accessProfile.unrestricted()) {
            return null;
        }
        if (currentPath.isEmpty()) {
            return accessProfile.guildFolders();
        }
        if (accessProfile.guildFolders().contains(currentPath)) {
            return accessProfile.channelFoldersByGuildFolder().getOrDefault(currentPath, Set.of());
        }
        return null;
    }

    private boolean allowedDirectory(String requestedPath, AccessProfile accessProfile) {
        if (accessProfile.unrestricted() || requestedPath == null || requestedPath.isEmpty()) {
            return true;
        }

        String[] parts = requestedPath.split("/");
        if (parts.length == 1) {
            return accessProfile.guildFolders().contains(parts[0]);
        }
        return accessProfile.channelFoldersByGuildFolder()
                .getOrDefault(parts[0], Set.of())
                .contains(parts[1]);
    }

    private void assertReadable(String requestedPath, AccessProfile accessProfile) {
        if (!allowedDirectory(requestedPath, accessProfile)) {
            throw new ResponseStatusException(FORBIDDEN, "Discord channel access is required");
        }
    }

    private static String displaySize(long bytes) {
        if (bytes < 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);

        return "%.1f %s".formatted(value, units[unit]);
    }
}
