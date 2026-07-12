package info.ankin.sideprojects.discordfileshare;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class DiscordFileSharePrincipal implements OAuth2User, Serializable {

    private final OAuth2User delegate;
    private final AccessProfile accessProfile;

    public DiscordFileSharePrincipal(OAuth2User delegate, AccessProfile accessProfile) {
        this.delegate = delegate;
        this.accessProfile = accessProfile;
    }

    public AccessProfile accessProfile() {
        return accessProfile;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
