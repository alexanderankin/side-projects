package org.ssas;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    OAuth2AuthorizationServerConfigurer authServerConfig = OAuth2AuthorizationServerConfigurer.authorizationServer();

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder().username("admin").password("admin").roles("ADMIN").build()
        );
    }

    // @SneakyThrows
    // @Bean
    // public SecurityFilterChain appSecurity(HttpSecurity http) {
    //     http.cors(Customizer.withDefaults());
    //     http.csrf(Customizer.withDefaults());
    //     http.securityMatcher("/login", "/login/**", "/error", "/error/**");
    //     http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
    //     http.formLogin(Customizer.withDefaults());
    //     return http.build();
    // }

    @SneakyThrows
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http.cors(Customizer.withDefaults());
        http.csrf(Customizer.withDefaults());
        http
                // .securityMatcher(authServerConfig.getEndpointsMatcher())
                .oauth2ResourceServer(resourceServer -> {
                    resourceServer.jwt(Customizer.withDefaults());
                })
                .with(authServerConfig, authServer -> {
                    authServer.oidc(Customizer.withDefaults());
                })
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/assets/**", "/login").permitAll()
                                .requestMatchers("/login", "/login/**").permitAll()
                                .requestMatchers("/error", "/error/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin -> {
                    // formLogin
                    //         .loginPage("/login")
                })
        // .oauth2Login(oauth2Login -> {
        //     // oauth2Login
        //     //         .loginPage("/login")
        // })
        //
        ;

        return http.build();
    }

    // @SneakyThrows
    // @Bean
    // @Order(-10)
    // SecurityFilterChain authServerSecurity(HttpSecurity httpSecurity) {
    //     httpSecurity.cors(AbstractHttpConfigurer::disable);
    //     httpSecurity.csrf(AbstractHttpConfigurer::disable);
    //
    //
    //     httpSecurity.securityMatcher(authServerConfig.getEndpointsMatcher());
    //
    //     httpSecurity.with(authServerConfig, a -> {
    //         a.oidc(Customizer.withDefaults());
    //         // a.oidc(o -> {
    //         //     o.providerConfigurationEndpoint(providerConfiguration -> {
    //         //         providerConfiguration.providerConfigurationCustomizer(builder ->
    //         //                 builder.idTokenSigningAlgorithms(l -> {
    //         //                     l.clear();
    //         //                     l.add("RS512");
    //         //                 }));
    //         //     });
    //         // });
    //     });
    //
    //     httpSecurity.oauth2ResourceServer(server -> {
    //         server.jwt(Customizer.withDefaults());
    //     });
    //
    //     httpSecurity.authorizeHttpRequests(requests -> {
    //         requests.requestMatchers("/.well-known/**").permitAll();
    //         requests.requestMatchers("/login", "/login/**").permitAll();
    //         requests.requestMatchers("/oauth2", "/oauth2/**").permitAll();
    //         requests.anyRequest().authenticated();
    //     });
    //
    //     // httpSecurity.exceptionHandling(exception -> {
    //     //     exception.authenticationEntryPoint((req, resp, e) -> {
    //     //         resp.sendRedirect("/exceptionHandling-authServerSecurity");
    //     //     });
    //     // });
    //
    //     return httpSecurity.build();
    // }
    //
    // @SneakyThrows
    // @Bean
    // SecurityFilterChain applicationSecurity(HttpSecurity httpSecurity,
    //                                         JwtDecoder jwtDecoder) {
    //     httpSecurity.securityMatcher(request -> !SecurityConfig.this.authServerConfig.getEndpointsMatcher().matches(request));
    //     httpSecurity.formLogin(Customizer.withDefaults());
    //
    //     httpSecurity.cors(AbstractHttpConfigurer::disable);
    //     httpSecurity.csrf(AbstractHttpConfigurer::disable);
    //     // httpSecurity.oauth2Login(Customizer.withDefaults());
    //     // httpSecurity.oauth2ResourceServer(Customizer.withDefaults());
    //     // httpSecurity.oauth2ResourceServer(server -> {
    //     //     server.jwt(jwtConfigurer -> {
    //     //         jwtConfigurer.decoder(jwtDecoder);
    //     //     });
    //     // });
    //
    //
    //     httpSecurity.authorizeHttpRequests(requests -> {
    //         requests.requestMatchers("/actuator/**").permitAll();
    //         requests.requestMatchers("/login", "/login/**").permitAll();
    //         requests.anyRequest().authenticated();
    //     });
    //
    //     httpSecurity.exceptionHandling(exception -> {
    //         exception.accessDeniedPage("/exceptionHandling-applicationSecurity");
    //     });
    //     // httpSecurity.formLogin(Customizer.withDefaults());
    //
    //     return httpSecurity.build();
    // }

}
