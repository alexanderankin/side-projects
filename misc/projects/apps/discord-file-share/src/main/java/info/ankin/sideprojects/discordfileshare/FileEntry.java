package info.ankin.sideprojects.discordfileshare;

import java.time.Instant;

public record FileEntry(
        String name,
        String relativePath,
        boolean directory,
        long size,
        String displaySize,
        Instant lastModified) {}
