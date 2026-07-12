package info.ankin.sideprojects.discordfileshare;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public record AccessProfile(
        Set<String> guildFolders,
        Map<String, Set<String>> channelFoldersByGuildFolder) implements Serializable {

    public boolean unrestricted() {
        return guildFolders == null && channelFoldersByGuildFolder == null;
    }

    public static AccessProfile unrestrictedAccess() {
        return new AccessProfile(null, null);
    }
}
