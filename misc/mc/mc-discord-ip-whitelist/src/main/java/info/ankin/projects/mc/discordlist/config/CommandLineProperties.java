package info.ankin.projects.mc.discordlist.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "mc.discord-list.command-line")
public class CommandLineProperties {
    DefaultExecutor defaultExecutor = new DefaultExecutor();

    @Data
    @Accessors(chain = true)
    public static class DefaultExecutor {
        boolean enabled = true;
    }
}
