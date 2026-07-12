package info.ankin.sideprojects.discordfileshare;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("file-share")
public record FileShareProperties(@NotNull Path rootDirectory) {}
