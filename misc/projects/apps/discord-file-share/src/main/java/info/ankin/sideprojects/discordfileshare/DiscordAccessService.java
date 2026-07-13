package info.ankin.sideprojects.discordfileshare;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Service
public class DiscordAccessService {
    private final FileShareProperties properties;
    private final DiscordClient discordClient;

    public AccessProfile accessProfile(String userId, OAuth2AccessToken accessToken) {
        Set<String> userGuildIds = discordClient.guilds(accessToken).stream()
                .map(DiscordClient.DiscordGuild::getId)
                .collect(Collectors.toSet());

        properties.getGuilds().entrySet().stream()
                .filter(e -> userGuildIds.contains(e.getKey()))
                .map(e -> {
                    Map<String, Set<String>> channelsByGuildFolder = new HashMap<>();

                    e.getValue().getChannels().stream().map(FileShareProperties.ChannelFolder::getId).filter(channelId -> {
                        return discordClient.isMember(userId, channelId);
                    });
                })

        Set<String> guildFolders = new HashSet<>();

        for (Map.Entry<String, FileShareProperties.GuildFolder> guild : properties.getGuilds().entrySet()) {
            if (!userGuildIds.contains(guild.getKey())) {
                continue;
            }

            guildFolders.add(guild.getKey());
            Set<String> channelFolders = accessibleChannelFolders(userId, accessToken, guild);
            channelsByGuildFolder.put(guild.getKey(), channelFolders);
        }

        if (guildFolders.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "discord_guild_required",
                    "Discord user is not a member of any configured server",
                    null));
        }

        return new AccessProfile()
                .setGuildFolders(Set.copyOf(guildFolders))
                .setChannelFoldersByGuildFolder(new HashMap<>(channelsByGuildFolder));
    }

    private Set<String> accessibleChannelFolders(String userId, OAuth2AccessToken accessToken, Map.Entry<String, FileShareProperties.GuildFolder> guild) {
        if (guild.getValue().getChannels().isEmpty()) {
            return Set.of();
        }

        DiscordGuildMember member = currentUserGuildMember(accessToken, guild.getKey());
        Map<String, Long> rolePermissions = guildRolePermissions(guild.getKey());
        Map<String, DiscordChannel> channels = guildChannels(guild.getKey());
        Set<String> memberRoles = new HashSet<>(member.getRoles());
        Set<String> accessibleFolders = new HashSet<>();

        for (FileShareProperties.ChannelFolder configuredChannel : guild.getValue().getChannels()) {
            DiscordChannel channel = channels.get(configuredChannel.getId());
            if (channel != null && canViewChannel(guild.getKey(), userId, memberRoles, rolePermissions, channel)) {
                accessibleFolders.add(configuredChannel.getId());
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
                permissions.put(role.getId(), parsePermissions(role.getPermissions()));
            }
        }
        return permissions;
    }

    private Map<String, DiscordChannel> guildChannels(String guildId) {
        DiscordChannel[] channels = botGet("/guilds/{guildId}/channels", DiscordChannel[].class, guildId);
        Map<String, DiscordChannel> channelsById = new HashMap<>();
        if (channels != null) {
            for (DiscordChannel channel : channels) {
                channelsById.put(channel.getId(), channel);
            }
        }
        return channelsById;
    }

    private <T> T botGet(String uri, Class<T> responseType, String guildId) {
        String botToken = properties.getDiscord().getBotToken();
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
            if (overwrite != null && overwrite.getType() == 0) {
                roleAllow |= parsePermissions(overwrite.getAllow());
                roleDeny |= parsePermissions(overwrite.getDeny());
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
        permissions &= ~parsePermissions(overwrite.getDeny());
        permissions |= parsePermissions(overwrite.getAllow());
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



    @Data
    @Accessors(chain = true)
    static class DiscordGuildMember {
        private List<String> roles = List.of();
    }

    @Data
    @Accessors(chain = true)
    static class DiscordRole {
        private String id;
        private String permissions;
    }

    @Data
    @Accessors(chain = true)
    static class DiscordChannel {
        private String id;

        @JsonProperty("permission_overwrites")
        private List<DiscordPermissionOverwrite> permissionOverwrites = List.of();

        DiscordPermissionOverwrite overwriteFor(String id) {
            return permissionOverwrites.stream()
                    .filter(overwrite -> id.equals(overwrite.getId()))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Data
    @Accessors(chain = true)
    static class DiscordPermissionOverwrite {
        private String id;
        private int type;
        private String allow;
        private String deny;
    }
}
