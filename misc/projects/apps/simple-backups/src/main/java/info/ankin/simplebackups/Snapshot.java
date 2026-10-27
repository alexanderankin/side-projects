package info.ankin.simplebackups;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class Snapshot {
    private final UUID backupId;
    private final long epochMillis;
    private final long durationMillis;
    private final BackupMethod method;

    @JsonCreator
    public Snapshot(@JsonProperty("backupId") UUID backupId, @JsonProperty("epochMillis") long epochMillis,
                    @JsonProperty("durationMillis") long durationMillis, @JsonProperty("method") BackupMethod method) {
        if (backupId == null || epochMillis < 0 || durationMillis < 0 || method == null)
            throw new IllegalArgumentException("Invalid snapshot metadata");
        this.backupId = backupId;
        this.epochMillis = epochMillis;
        this.durationMillis = durationMillis;
        this.method = method;
    }

    public String prefix() {
        return "snapshots/" + epochMillis + "/";
    }
}
