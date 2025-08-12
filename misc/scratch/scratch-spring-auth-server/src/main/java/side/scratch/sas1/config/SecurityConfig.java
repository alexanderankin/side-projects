package side.scratch.sas1.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // @Bean // Spring Security filter chain for the Protocol Endpoints.
    // @Order(1) // lower value is higher precedence
    // public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
    //         throws Exception {
    //     OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
    //             OAuth2AuthorizationServerConfigurer.authorizationServer();
    //
    //     http
    //             .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
    //             .with(authorizationServerConfigurer,
    //                     (OAuth2AuthorizationServerConfigurer authorizationServer) -> {
    //                         // Enable OpenID Connect 1.0
    //                         authorizationServer.oidc(Customizer.withDefaults());
    //                     })
    //             .authorizeHttpRequests((authorize) -> {
    //                 authorize.anyRequest().authenticated();
    //             })
    //             // Redirect to the login page when not authenticated from the
    //             // authorization endpoint
    //             .exceptionHandling((exceptions) -> exceptions
    //                     .defaultAuthenticationEntryPointFor(
    //                             new LoginUrlAuthenticationEntryPoint("/login"),
    //                             new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
    //     ;
    //
    //     return http.build();
    // }

    @Bean // A Spring Security filter chain for authentication.
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> {
                    authorize.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll();
                    authorize.requestMatchers("/static/**").permitAll();
                    authorize.requestMatchers("/static/**").permitAll();
                    authorize.requestMatchers("/error").permitAll();
                    authorize.requestMatchers("/error/**").permitAll();
                    authorize.anyRequest().authenticated();
                })
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin((t) -> {
                    t.loginPage("/login").permitAll();
                })
                .formLogin(Customizer.withDefaults())
        ;

        return http.build();
    }

    @Bean // An instance of UserDetailsService for retrieving users to authenticate.
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean // An instance of RegisteredClientRepository for managing clients.
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("oidc-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient);
    }

    @Bean // An instance of com.nimbusds.jose.jwk.source.JWKSource for signing access tokens.
    public JWKSource<SecurityContext> jwkSource(RsaKeyProperties rsaKeyProperties) {
        var list = rsaKeyProperties.getKeys().entrySet().stream()
                .map(e -> Map.entry(e.getKey(), ParsedRsaKey.generateRsaKey(e.getValue())))
                .toList();

        List<JWK> jwklist = new ArrayList<>();
        for (var e : list) {
            var parsed = e.getValue();
            KeyPair keyPair = parsed.parsedKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(parsed.key().getKeyId().toString())
                    .build();
            // JWKSet jwkSet = new JWKSet(rsaKey);
            jwklist.add(rsaKey);
        }
        return new ImmutableJWKSet<>(new JWKSet(jwklist));
    }

    @Bean // An instance of JwtDecoder for decoding signed access tokens.
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean // An instance of AuthorizationServerSettings to configure Spring Authorization Server.
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    record ParsedRsaKey(RsaKeyProperties.RsaKey key, KeyPair parsedKeyPair) {
        // An instance of java.security.KeyPair with keys generated on startup used to create the JWKSource above.
        private static ParsedRsaKey generateRsaKey(RsaKeyProperties.RsaKey rsaKey) {
            String publicKeyPem = rsaKey.getPublicKey();
            String privateKeyPem = rsaKey.getPrivateKey();

            // KeyPair keyPair;
            // try {
            //     KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            //     keyPairGenerator.initialize(2048);
            //     keyPair = keyPairGenerator.generateKeyPair();
            // } catch (Exception ex) {
            //     throw new IllegalStateException(ex);
            // }
            // return keyPair;

            try {
                // Load the private key
                privateKeyPem = privateKeyPem
                        .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                        .replaceAll("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] decodedPrivate = Base64.getDecoder().decode(privateKeyPem);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivate);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                // Load the public key
                publicKeyPem = publicKeyPem
                        .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                        .replaceAll("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] decodedPublic = Base64.getDecoder().decode(publicKeyPem);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodedPublic);
                PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

                return new ParsedRsaKey(rsaKey, new KeyPair(publicKey, privateKey));

            } catch (Exception e) {
                throw new IllegalStateException("Failed to load RSA key pair from PEM files", e);
            }
        }
    }
}
