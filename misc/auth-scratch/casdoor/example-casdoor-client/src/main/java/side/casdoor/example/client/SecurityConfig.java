package side.casdoor.example.client;

import lombok.SneakyThrows;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @SneakyThrows
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    SecurityFilterChain appSfc(HttpSecurity httpSecurity) {
        httpSecurity.authorizeHttpRequests(c -> c.anyRequest().authenticated());
        // httpSecurity.csrf(AbstractHttpConfigurer::disable);
        // httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.oauth2Login(Customizer.withDefaults());
        httpSecurity.oauth2Client(Customizer.withDefaults());
        return httpSecurity.build();
    }

    @SneakyThrows
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 20)
    SecurityFilterChain actuatorSfc(HttpSecurity httpSecurity, ManagementServerProperties mp) {
        httpSecurity.securityMatchers(c -> c.requestMatchers(r -> r.getLocalPort() == mp.getPort()));
        httpSecurity.authorizeHttpRequests(c -> c.anyRequest().permitAll());
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }
}
