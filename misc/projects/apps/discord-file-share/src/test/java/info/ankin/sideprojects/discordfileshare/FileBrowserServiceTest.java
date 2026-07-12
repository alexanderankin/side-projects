package info.ankin.sideprojects.discordfileshare;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBrowserServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void listsDirectoriesBeforeFiles() throws Exception {
        Files.createDirectory(tempDir.resolve("folder"));
        Files.writeString(tempDir.resolve("file.txt"), "hello");

        FileBrowserService service = new FileBrowserService(new FileShareProperties(tempDir));

        DirectoryListing listing = service.list(null);

        assertThat(listing.entries().stream().map(FileEntry::name).toList(), contains("folder", "file.txt"));
        assertThat(listing.entries().get(1).displaySize(), equalTo("5 B"));
    }

    @Test
    void rejectsTraversalOutsideRoot() {
        FileBrowserService service = new FileBrowserService(new FileShareProperties(tempDir));

        assertThrows(ResponseStatusException.class, () -> service.list("../"));
    }
}
