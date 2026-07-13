package info.ankin.sideprojects.discordfileshare;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordClient {
    private final RestClient.Builder builder;
    private RestClient restClient;

    @PostConstruct
    void init() {
        restClient = builder.build();
    }

    List<DiscordGuild> guilds(OAuth2AccessToken accessToken) {
        return restClient.get()
            .uri("/users/@me/guilds")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
    }

    boolean isMember(String userId, @NotBlank String channelId) {
        return false;
    }

    @Data
    @Accessors(chain = true)
    static class DiscordGuild {
        private String id;
    }
}
