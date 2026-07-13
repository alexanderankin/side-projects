package info.ankin.sideprojects.discordfileshare;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Validated
@Accessors(chain = true)
@ConfigurationProperties("file-share")
public class FileShareProperties {

    /**
     * file sharing host folder root
     */
    @NotNull
    private Path rootDirectory;

    /**
     * list of discord servers server id to server
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID">discord docs</a>
     */
    @NotNull
    @NotEmpty
    private Map<String, @Valid GuildFolder> guilds = new LinkedHashMap<>();

    @Valid
    private Discord discord = new Discord();

    @Data
    @Accessors(chain = true)
    public static class GuildFolder {
        private List<@Valid ChannelFolder> channels = new ArrayList<>();
    }

    @Data
    @Accessors(chain = true)
    public static class ChannelFolder {
        @NotBlank
        private String id;
    }

    @Data
    @Accessors(chain = true)
    public static class Discord {
        @NotBlank
        private String apiBaseUrl = "https://discord.com/api";

        @NotBlank
        private String botToken;
    }
}
