package info.ankin.projects.mc.discordlist.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/whoami")
public class WhoamiController {
    @GetMapping
    Map<String, Object> whoami(@AuthenticationPrincipal OAuth2LoginAuthenticationToken authentication) {
        OAuth2AccessToken accessToken = authentication.getAccessToken();
        Jwt accessTokenJwt = Jwt.withTokenValue(accessToken.getTokenValue()).build();
        Map<String, Object> claims = accessTokenJwt.getClaims();
        return claims;
    }
}
