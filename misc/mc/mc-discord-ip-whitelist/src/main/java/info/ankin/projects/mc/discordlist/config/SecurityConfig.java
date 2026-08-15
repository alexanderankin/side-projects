package info.ankin.projects.mc.discordlist.config;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @SneakyThrows
    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity httpSecurity, ServerProperties serverProperties) {
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.authorizeHttpRequests(c -> {
            c.requestMatchers("/oauth2/**").permitAll();
            c.requestMatchers("/actuator/**").permitAll();
            c.anyRequest().authenticated();
        });

        httpSecurity.oauth2Login(Customizer.withDefaults());
        httpSecurity.logout(c -> c.deleteCookies(serverProperties.getServlet().getSession().getCookie().getName()));
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return httpSecurity.build();
    }
}
