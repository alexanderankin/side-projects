package info.ankin.sideprojects.discordfileshare;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DirectoryListingModel {
    private String currentPath;
    private String parentPath;
    private Path absolutePath;
    private List<FileEntry> entries;

    @Data
    @Accessors(chain = true)
    public static class FileEntry {
        private String name;
        private String relativePath;
        private boolean directory;
        private long size;
        private String displaySize;
        private Instant lastModified;
    }
}
