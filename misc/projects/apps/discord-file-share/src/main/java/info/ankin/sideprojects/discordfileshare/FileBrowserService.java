package info.ankin.sideprojects.discordfileshare;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FileBrowserService {

    private final Path rootDirectory;

    public FileBrowserService(FileShareProperties properties) {
        this.rootDirectory = properties.rootDirectory().toAbsolutePath().normalize();
    }

    @PreAuthorize("isAuthenticated()")
    public DirectoryListing list(String requestedPath) throws IOException {
        Path directory = resolveInsideRoot(requestedPath);
        if (!Files.isDirectory(directory)) {
            throw new ResponseStatusException(NOT_FOUND, "Directory not found");
        }

        String currentPath = relativePath(directory);
        String parentPath = parentPath(directory);

        try (var stream = Files.list(directory)) {
            List<FileEntry> entries = stream
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
        return relativePath(directory.getParent());
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
