package info.ankin.simplebackups;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public final class Snapshot {
    private final UUID backupId;
    private final long epochMillis;
    private final long durationMillis;
    private final BackupMethod method;

    public Snapshot(UUID backupId, long epochMillis, long durationMillis, BackupMethod method) {
        if (backupId == null || epochMillis < 0 || durationMillis < 0 || method == null)
            throw new IllegalArgumentException("Invalid snapshot metadata");
        this.backupId = backupId;
        this.epochMillis = epochMillis;
        this.durationMillis = durationMillis;
        this.method = method;
    }

    public String prefix() { return "snapshots/" + epochMillis + "/"; }
}
