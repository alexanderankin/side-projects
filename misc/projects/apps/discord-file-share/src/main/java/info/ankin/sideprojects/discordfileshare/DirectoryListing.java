package info.ankin.sideprojects.discordfileshare;

import java.nio.file.Path;
import java.util.List;

public record DirectoryListing(String currentPath, String parentPath, Path absolutePath, List<FileEntry> entries) {}
