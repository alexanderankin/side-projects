package side.as;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Predicate;

@SpringBootApplication
public class DiscordOAuthClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscordOAuthClientApplication.class, args);
    }

    @Slf4j
    @Data
    @Accessors(chain = true)
    @Component
    @ConfigurationProperties(prefix = "app")
    @Validated
    static class AppProps {
        @NotNull
        URI baseUri;
    }

    @Slf4j
    @Data
    @Accessors(chain = true)
    @Component
    @ConfigurationProperties(prefix = "app.oauth")
    @Validated
    static class OAuthProps {

        @NotNull
        Map<String, @Valid OauthClient> clients;

        @Data
        @Accessors(chain = true)
        static class OauthClient {
            /**
             * credential for talking to auth server
             */
            @ToString.Exclude
            @NotBlank
            String clientId;
            /**
             * credential for talking to auth server
             */
            @ToString.Exclude
            @NotBlank
            String clientSecret;

            /**
             * UI page on auth server
             */
            @NotNull
            URI redirectUri;

            /**
             * API endpoint on auth server to turn code to token
             */
            @NotNull
            URI tokenEndpoint;


            @NotBlank
            String callback;
            List<String> scopes;

            @NotNull
            String nameClaim = "sub";

            OpaqueServerInfo opaque = new OpaqueServerInfo();
            OidcServerInfo oidc = new OidcServerInfo();

            @ToString.Include
            private String clientSecret() {
                return clientSecret == null ? null : "***";
            }

            @ToString.Include
            private String clientId() {
                return clientId == null ? null : StringUtils.truncate(clientId, 5);
            }

            @NotNull
            ServerInfoType serverInfoType() {
                int enabledServerTypes = 0;
                if (opaque != null && opaque.enabled) enabledServerTypes += 1;
                if (oidc != null && oidc.enabled) enabledServerTypes += 1;

                Assert.isTrue(enabledServerTypes == 1, "must enable 1 and only one server type");

                if (opaque != null && opaque.enabled) return ServerInfoType.OPAQUE;
                if (oidc != null && oidc.enabled) return ServerInfoType.OIDC;
                throw new UnsupportedOperationException("unreachable");
            }

            public List<String> scopesOrDefaultScopes() {
                return Optional.ofNullable(scopes).filter(Predicate.not(List::isEmpty)).orElse(List.of("openid", "profile"));
            }

            enum ServerInfoType {
                OPAQUE, OIDC,
            }

            @Data
            @Accessors(chain = true)
            static class OpaqueServerInfo {
                boolean enabled;

                /**
                 * API endpoint to turn token to principal
                 */
                URI introspectionEndpoint;
            }

            @Data
            @Accessors(chain = true)
            static class OidcServerInfo {
                boolean enabled;

                URI issuer;
            }
        }
    }

    @Configuration
    static class SecurityConfig {
        @Bean
        SecurityContextRepository securityContextRepository() {
            return new HttpSessionSecurityContextRepository();
        }

        @SneakyThrows
        @Bean
        public SecurityFilterChain appSecurity(HttpSecurity http,
                                               SecurityContextRepository securityContextRepository) {
            http.setSharedObject(SecurityContextRepository.class, securityContextRepository);

            http.cors(AbstractHttpConfigurer::disable);
            http.csrf(AbstractHttpConfigurer::disable);

            http.authorizeHttpRequests(authorizeRequests -> {
                authorizeRequests.requestMatchers("/api").authenticated();
                authorizeRequests.anyRequest().permitAll();
            });

            http.anonymous(AbstractHttpConfigurer::disable);

            http.securityContext(s -> s.requireExplicitSave(false));

            return http.build();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Controller
    @RequestMapping(path = "")
    static class HomeController {
        final OAuthProps oauthProps;
        final RestClient.Builder restClientBuilder;
        // final SecurityContextRepository securityContextRepository;
        final AuthFetcherFromToken authFetcherFromToken;

        public static boolean isAuthed(Authentication auth) {
            return auth != null;
        }

        public static boolean isAuthed(HttpSession httpSession) {
            log.debug("isAuthed: {}, {}", httpSession.getId(), IteratorUtils.toList(httpSession.getAttributeNames().asIterator()));
            return false;
        }

        @GetMapping(path = "")
        public ResponseEntity<String> index(UriComponentsBuilder uri) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (!isAuthed(auth)) {
                log.debug("index - redirecting user to login - not authed");
                return ResponseEntity.status(302).location(uri.replacePath("/login").build().toUri()).build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body("""
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <title>Welcome</title>
                                <script>
                                    document.addEventListener("DOMContentLoaded", () => {
                                        fetch("/api/whoami").then(r => r.text()).then(t => window.welcome.innerText = `Welcome, ${t}!`);

                                        window.logout.addEventListener("click", () => {
                                            fetch("/logout").then(() => window.location.reload());
                                        });
                                    });
                                </script>
                            </head>
                            <body style="margin: 30px; background: #eee; color: #333;">
                                <h1>Hello World!</h1>
                                <h2 id="welcome">Welcome!</h2>
                                <br />
                                <hr />
                                <br />
                                <h3 id="logout">Logout</h3>
                            </body>
                            </html>
                            """);
        }

        @GetMapping("/logout")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void logout() {
            SecurityContextHolder.clearContext();
        }

        @GetMapping("/login")
        public ResponseEntity<String> login(UriComponentsBuilder uri,
                                            HttpHeaders headers,
                                            HttpSession httpSession) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (isAuthed(auth)) {
                log.debug("login - redirecting user to home - authed: '{}'", auth);
                return ResponseEntity.status(302).location(uri.replacePath("/").build().toUri()).build();
            }

            String referrer = Optional.ofNullable(headers.getFirst(HttpHeaders.REFERER))
                    .orElse(uri.replacePath("").build().toUriString());
            httpSession.setAttribute(getClass().getName() + ".redirect", referrer);
            log.debug("login - saved referrer: {}", referrer);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body("""
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <title>Login</title>
                                <script>
                                    // document.addEventListener("DOMContentLoaded", () => {
                                    //     fetch("/api/whoami").then(r => r.text()).then(t => window.welcome.innerText = `Welcome, ${t}!`);
                                    // });
                                </script>
                            </head>
                            <body style="margin: 30px; background: #eee; color: #333;">
                                <h1>Login</h1>
                                <h2>Select a login option!</h2>
                                <a href="/login/discord">discord</a>
                            </body>
                            </html>
                            """);
        }

        @GetMapping("/login/{oauth}")
        public ResponseEntity<String> login(@PathVariable(name = "oauth") String oauth,
                                            HttpSession httpSession,
                                            UriComponentsBuilder uri) {
            // if (!isAuthed(httpSession)) {
            //     httpSession.setAttribute(getClass().getName() + ".redirect", uri.toUriString());
            //     return ResponseEntity.status(302).location(uri.replacePath("/login").build().toUri()).build();
            // }

            OAuthProps.OauthClient client = oauthProps.getClients().get(oauth);

            var redirectUriBuilder = UriComponentsBuilder.fromUri(client.getRedirectUri());

            httpSession.setAttribute(getClass().getName() + ".oauth.oauth", oauth);
            var state = UUID.randomUUID().toString();
            httpSession.setAttribute(getClass().getName() + ".oauth.state", state);

            redirectUriBuilder.queryParam("client_id", client.getClientId());
            redirectUriBuilder.queryParam("response_type", "code");
            redirectUriBuilder.queryParam("state", state);
            redirectUriBuilder.queryParam("redirect_uri", client.getCallback());
            redirectUriBuilder.queryParam("scope", String.join(" ", client.scopesOrDefaultScopes()));

            log.debug("login/{oauth} - redirecting the user to client {} with params {}", oauth, redirectUriBuilder.build().getQueryParams());
            var redirectUri = redirectUriBuilder.build(Map.of("oauth", oauth));
            return ResponseEntity.status(302).location(redirectUri).build();
        }

        @GetMapping("/oauth/callback")
        public ResponseEntity<String> callback(HttpServletRequest httpServletRequestRequest,
                                               HttpServletResponse httpServletResponseResponse,
                                               HttpSession httpSession,
                                               @RequestParam MultiValueMap<String, String> params,
                                               UriComponentsBuilder uri) {
            var codeFromRedirect = params.getFirst("code");
            if (codeFromRedirect == null) return ResponseEntity.badRequest().body("Missing code in query parameter");
            log.debug("oauth/callback - calling back, read code from redirect: {}", codeFromRedirect);

            var stateFromRedirect = params.getFirst("state");
            if (stateFromRedirect == null) return ResponseEntity.badRequest().body("Missing state in query parameter");
            log.debug("oauth/callback - calling back, read state from redirect: {}", stateFromRedirect);

            var stateValue = httpSession.getAttribute(getClass().getName() + ".oauth.state");
            if (!(stateValue instanceof String state)) return ResponseEntity.badRequest().body("no state");
            log.debug("oauth/callback - calling back, read state from session: {}", state);

            if (!MessageDigest.isEqual(stateFromRedirect.getBytes(StandardCharsets.UTF_8), state.getBytes(StandardCharsets.UTF_8))) {
                return ResponseEntity.badRequest().body("bad state");
            }
            log.debug("oauth/callback - calling back, state matched successfully");

            var oauthValue = httpSession.getAttribute(getClass().getName() + ".oauth.oauth");
            if (!(oauthValue instanceof String oauth)) return ResponseEntity.badRequest().body("not part of a grant");
            log.debug("oauth/callback - calling back, the client name from the session was: {}", oauthValue);

            OAuthProps.OauthClient client = oauthProps.getClients().get(oauth);
            log.debug("oauth/callback - calling back, the client was {} from name from the session", client.getRedirectUri());

            ResponseEntity<AccessToken> response;
            AccessToken accessToken;
            try {
                response = restClientBuilder.build().post()
                        .uri(client.getTokenEndpoint())
                        .headers(h -> h.setBasicAuth(client.getClientId(), client.getClientSecret()))
                        .body(new MultiValueMapAdapter<>(Map.ofEntries(
                                Map.entry("grant_type", List.of("authorization_code")),
                                Map.entry("redirect_uri", List.of(client.getCallback())),
                                Map.entry("code", List.of(codeFromRedirect))
                        )))
                        .retrieve()
                        .toEntity(AccessToken.class);
            } catch (RestClientResponseException re) {
                log.debug("oauth/callback - the response from the issuer was bad");
                log.trace("oauth/callback - the response from the issuer was a {}, body: {}, headers: {}", re.getStatusCode(), re.getResponseBodyAsString(), re.getResponseHeaders());
                return ResponseEntity.badRequest().body(re.getMessage());
            } catch (Exception exception) {
                log.error("oauth/callback - callback for {}: {}", oauth, exception.getMessage(), exception);
                return ResponseEntity.internalServerError().body(exception.getMessage());
            }

            accessToken = response.getBody();
            if (accessToken == null) return ResponseEntity.badRequest().body("bad");
            log.debug("oauth/callback - got a successful response back from the issuer");

            var auth = authFetcherFromToken.fetchFromToken(oauth, client, accessToken);

            if (auth == null)
                return ResponseEntity.badRequest().body("did not trust the token from " + oauth);
            SecurityContextHolder.clearContext();
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(auth);
            // securityContextRepository.saveContext(context, httpServletRequestRequest, httpServletResponseResponse);

            URI redirectTarget;
            var redirectValue = httpSession.getAttribute(getClass().getName() + ".redirect");
            if (redirectValue instanceof String redirect) {
                redirectTarget = URI.create(redirect);
            } else {
                redirectTarget = uri.replacePath("/").build().toUri();
            }

            log.debug("oauth/callback - got back an auth we trust: {}, redirecting to {}", auth, redirectTarget);

            // clean up oauth2 auth params
            httpSession.removeAttribute(getClass().getName() + ".oauth.state");
            httpSession.removeAttribute(getClass().getName() + ".oauth.oauth");
            httpSession.removeAttribute(getClass().getName() + ".redirect");

            return ResponseEntity.status(302).location(redirectTarget).build();
        }

        @Slf4j
        @RequiredArgsConstructor
        @Service
        static class AuthFetcherFromToken {
            final RestClient.Builder restClientBuilder;
            final Map<String, JwtDecoder> cache = new HashMap<>();

            public Authentication fetchFromToken(String clientKey, OAuthProps.OauthClient client, AccessToken accessToken) {
                return switch (client.serverInfoType()) {
                    case OIDC -> {
                        JwtDecoder decoder = jwtDecoder(client.getOidc());
                        Jwt jwt;
                        try {
                            jwt = decoder.decode(accessToken.getAccessToken());
                        } catch (JwtException e) {
                            yield null;
                        }

                        yield new JwtBearerTokenAuthenticationConverter().convert(jwt);
                    }
                    case OPAQUE -> {
                        try {
                            var response = restClientBuilder.build().get().uri(client.getOpaque().getIntrospectionEndpoint())
                                    .headers(h -> h.setBearerAuth(accessToken.getAccessToken()))
                                    .retrieve()
                                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
                                    });
                            var body = response.getBody();
                            Assert.notNull(body, "no body in response from client introspectionEndpoint");

                            List<GrantedAuthority> authorities = List.of();
                            OAuth2User user = new DefaultOAuth2User(authorities, body, client.getNameClaim());
                            yield new OAuth2AuthenticationToken(user, authorities, clientKey);
                        } catch (RestClientResponseException re) {
                            log.debug("oauth/callback - the response from the introspectionEndpoint was a {}, body: {}, headers: {}", re.getStatusCode(), re.getResponseBodyAsString(), re.getResponseHeaders());
                            throw new AccessDeniedException("could not introspect token");
                        } catch (Exception e) {
                            log.debug("oauth/callback - introspection request failed: {}", e.getMessage(), e);
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not request to introspect token", e);
                        }
                    }
                };
            }

            private JwtDecoder jwtDecoder(OAuthProps.OauthClient.OidcServerInfo client) {
                return cache.computeIfAbsent(String.valueOf(client.getIssuer()), i -> NimbusJwtDecoder.withIssuerLocation(i).build());
            }
        }

        @Data
        @Accessors(chain = true)
        static class AccessToken {
            @JsonProperty("access_token")
            private String accessToken;
            @JsonProperty("token_type")
            private String tokenType;
            @JsonProperty("refresh_token")
            private String refreshToken;
            @JsonProperty("expires_in")
            private Integer expiresIn;
        }
    }

    @Slf4j
    @RestController
    @RequestMapping(path = "/api")
    static class ApiController {
        @GetMapping("/whoami")
        public WhoAmI whoami(HttpSession httpSession) {
            var map = new HashMap<String, Object>();
            for (String s : IteratorUtils.toList(httpSession.getAttributeNames().asIterator())) {
                map.put(s, Objects.toString(httpSession.getAttribute(s)));
            }
            log.info("whoami {}", map);

            return new WhoAmI()
                    .setName(SecurityContextHolder.getContext().getAuthentication().getName())
                    .setRedirect((String) httpSession.getAttribute(getClass().getName() + ".redirect"));
        }

        @Data
        @Accessors(chain = true)
        static class WhoAmI {
            String name;
            String redirect;
        }
    }
}
