package info.ankin.sideprojects.discordfileshare;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DiscordAccessService {

    private static final long VIEW_CHANNEL = 1L << 10;
    private static final long ADMINISTRATOR = 1L << 3;

    private final FileShareProperties properties;
    private final RestClient discord;

    public DiscordAccessService(FileShareProperties properties) {
        this.properties = properties;
        this.discord = RestClient.builder()
                .baseUrl(properties.discord().apiBaseUrl())
                .build();
    }

    public AccessProfile accessProfile(String userId, OAuth2AccessToken accessToken) {
        if (properties.guilds().isEmpty()) {
            return AccessProfile.unrestrictedAccess();
        }

        Set<String> userGuildIds = currentUserGuilds(accessToken);
        Set<String> guildFolders = new HashSet<>();
        Map<String, Set<String>> channelsByGuildFolder = new HashMap<>();

        for (FileShareProperties.GuildFolder guild : properties.guilds()) {
            if (!userGuildIds.contains(guild.id())) {
                continue;
            }

            guildFolders.add(guild.folder());
            Set<String> channelFolders = accessibleChannelFolders(userId, accessToken, guild);
            channelsByGuildFolder.put(guild.folder(), channelFolders);
        }

        if (guildFolders.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "discord_guild_required",
                    "Discord user is not a member of any configured server",
                    null));
        }

        return new AccessProfile(Set.copyOf(guildFolders), copy(channelsByGuildFolder));
    }

    private Set<String> currentUserGuilds(OAuth2AccessToken accessToken) {
        DiscordGuild[] guilds = discord.get()
                .uri("/users/@me/guilds")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .retrieve()
                .body(DiscordGuild[].class);

        Set<String> ids = new HashSet<>();
        if (guilds != null) {
            for (DiscordGuild guild : guilds) {
                ids.add(guild.id());
            }
        }
        return ids;
    }

    private Set<String> accessibleChannelFolders(String userId, OAuth2AccessToken accessToken, FileShareProperties.GuildFolder guild) {
        if (guild.channels().isEmpty()) {
            return Set.of();
        }

        DiscordGuildMember member = currentUserGuildMember(accessToken, guild.id());
        Map<String, Long> rolePermissions = guildRolePermissions(guild.id());
        Map<String, DiscordChannel> channels = guildChannels(guild.id());
        Set<String> memberRoles = new HashSet<>(member.roles());
        Set<String> accessibleFolders = new HashSet<>();

        for (FileShareProperties.ChannelFolder configuredChannel : guild.channels()) {
            DiscordChannel channel = channels.get(configuredChannel.id());
            if (channel != null && canViewChannel(guild.id(), userId, memberRoles, rolePermissions, channel)) {
                accessibleFolders.add(configuredChannel.folder());
            }
        }

        return Set.copyOf(accessibleFolders);
    }

    private DiscordGuildMember currentUserGuildMember(OAuth2AccessToken accessToken, String guildId) {
        return discord.get()
                .uri("/users/@me/guilds/{guildId}/member", guildId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .retrieve()
                .body(DiscordGuildMember.class);
    }

    private Map<String, Long> guildRolePermissions(String guildId) {
        DiscordRole[] roles = botGet("/guilds/{guildId}/roles", DiscordRole[].class, guildId);
        Map<String, Long> permissions = new HashMap<>();
        if (roles != null) {
            for (DiscordRole role : roles) {
                permissions.put(role.id(), parsePermissions(role.permissions()));
            }
        }
        return permissions;
    }

    private Map<String, DiscordChannel> guildChannels(String guildId) {
        DiscordChannel[] channels = botGet("/guilds/{guildId}/channels", DiscordChannel[].class, guildId);
        Map<String, DiscordChannel> channelsById = new HashMap<>();
        if (channels != null) {
            for (DiscordChannel channel : channels) {
                channelsById.put(channel.id(), channel);
            }
        }
        return channelsById;
    }

    private <T> T botGet(String uri, Class<T> responseType, String guildId) {
        String botToken = properties.discord().botToken();
        if (botToken == null || botToken.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "discord_bot_token_required",
                    "DISCORD_BOT_TOKEN is required when channel folders are configured",
                    null));
        }

        return discord.get()
                .uri(uri, guildId)
                .header(HttpHeaders.AUTHORIZATION, "Bot " + botToken)
                .retrieve()
                .body(responseType);
    }

    private static boolean canViewChannel(
            String guildId,
            String userId,
            Set<String> memberRoles,
            Map<String, Long> rolePermissions,
            DiscordChannel channel) {
        long permissions = rolePermissions.getOrDefault(guildId, 0L);
        for (String role : memberRoles) {
            permissions |= rolePermissions.getOrDefault(role, 0L);
        }
        if ((permissions & ADMINISTRATOR) == ADMINISTRATOR) {
            return true;
        }

        permissions = applyOverwrite(permissions, channel.overwriteFor(guildId));

        long roleAllow = 0L;
        long roleDeny = 0L;
        for (String role : memberRoles) {
            DiscordPermissionOverwrite overwrite = channel.overwriteFor(role);
            if (overwrite != null && overwrite.type() == 0) {
                roleAllow |= parsePermissions(overwrite.allow());
                roleDeny |= parsePermissions(overwrite.deny());
            }
        }
        permissions &= ~roleDeny;
        permissions |= roleAllow;

        permissions = applyOverwrite(permissions, channel.overwriteFor(userId));
        return (permissions & VIEW_CHANNEL) == VIEW_CHANNEL;
    }

    private static long applyOverwrite(long permissions, DiscordPermissionOverwrite overwrite) {
        if (overwrite == null) {
            return permissions;
        }
        permissions &= ~parsePermissions(overwrite.deny());
        permissions |= parsePermissions(overwrite.allow());
        return permissions;
    }

    private static long parsePermissions(String permissions) {
        return permissions == null || permissions.isBlank() ? 0L : Long.parseUnsignedLong(permissions);
    }

    private static Map<String, Set<String>> copy(Map<String, Set<String>> source) {
        Map<String, Set<String>> copy = new HashMap<>();
        source.forEach((key, value) -> copy.put(key, Set.copyOf(value)));
        return Map.copyOf(copy);
    }

    record DiscordGuild(String id) {}

    record DiscordGuildMember(List<String> roles) {
        DiscordGuildMember {
            roles = roles == null ? List.of() : List.copyOf(roles);
        }
    }

    record DiscordRole(String id, String permissions) {}

    record DiscordChannel(String id, @JsonProperty("permission_overwrites") List<DiscordPermissionOverwrite> permissionOverwrites) {
        DiscordChannel {
            permissionOverwrites = permissionOverwrites == null ? List.of() : List.copyOf(permissionOverwrites);
        }

        DiscordPermissionOverwrite overwriteFor(String id) {
            return permissionOverwrites.stream()
                    .filter(overwrite -> id.equals(overwrite.id()))
                    .findFirst()
                    .orElse(null);
        }
    }

    record DiscordPermissionOverwrite(String id, int type, String allow, String deny) {}
}
