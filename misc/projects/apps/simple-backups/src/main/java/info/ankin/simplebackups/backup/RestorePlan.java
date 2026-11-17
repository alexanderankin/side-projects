package info.ankin.simplebackups.backup;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
@Accessors(fluent = true)
public final class RestorePlan {
    private final Path contents;
    private final URI destination;
    private final Map<String, String> destinationInventory;
    private final List<String> deletions;
}
