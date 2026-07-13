package info.ankin.sideprojects.discordfileshare;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccessProfile implements Serializable {
    private Set<String> guildFolders;
    private Map<String, Set<String>> channelFoldersByGuildFolder;

    public boolean unrestricted() {
        return guildFolders == null && channelFoldersByGuildFolder == null;
    }

    public static AccessProfile unrestrictedAccess() {
        return new AccessProfile();
    }
}
