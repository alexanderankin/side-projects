package info.ankin.sideprojects.discordfileshare;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("file-share")
public record FileShareProperties(
        @NotNull Path rootDirectory,
        List<@Valid GuildFolder> guilds,
        Discord discord) {

    public FileShareProperties {
        guilds = guilds == null ? List.of() : List.copyOf(guilds);
        discord = discord == null ? new Discord("https://discord.com/api", null) : discord;
    }

    public record GuildFolder(
            @NotBlank String id,
            @NotBlank String folder,
            List<@Valid ChannelFolder> channels) {

        public GuildFolder {
            channels = channels == null ? List.of() : List.copyOf(channels);
        }
    }

    public record ChannelFolder(@NotBlank String id, @NotBlank String folder) {}

    public record Discord(@NotBlank String apiBaseUrl, String botToken) {}
}
