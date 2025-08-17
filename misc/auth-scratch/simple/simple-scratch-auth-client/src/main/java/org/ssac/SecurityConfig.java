package org.ssac;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    @SneakyThrows
    @Bean
    SecurityFilterChain appSecurity(HttpSecurity http) {
        http.cors(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorizeRequests -> {
            authorizeRequests.requestMatchers("/", "/login", "/login/**").authenticated();
            authorizeRequests.anyRequest().authenticated();
        });

        http.exceptionHandling(exceptionHandling -> {
           exceptionHandling.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/spring-login"));
        });

        http.oauth2Client(Customizer.withDefaults());
        http.oauth2Login(Customizer.withDefaults());
        http.sessionManagement(sessionManagement -> {
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        });

        return http.build();
    }
}
