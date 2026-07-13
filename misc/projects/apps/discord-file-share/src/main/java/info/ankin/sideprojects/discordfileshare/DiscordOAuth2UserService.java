package info.ankin.sideprojects.discordfileshare;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final DiscordAccessService discordAccessService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(userRequest);
        String userId = user.getAttribute("id");
        if (userId == null || userId.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "discord_user_id_required",
                    "Discord user info did not include an id",
                    null));
        }
        return new DiscordFileSharePrincipal(
                user,
                discordAccessService.accessProfile(userId, userRequest.getAccessToken()));
    }
}
