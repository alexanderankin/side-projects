package side.oauthcli;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.server.HttpServer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class OAuthClient {
    private final OAuthClientClient client;
    private final RestClient restClient;

    public TokenResponse clientCredentials() {
        return restClient.post().uri(client.getTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new MultiValueMapAdapter<>(Map.ofEntries(
                        Map.entry("grant_type", List.of("client_credentials")),
                        Map.entry("scope", List.of(client.scope())),
                        Map.entry("client_id", List.of(client.getClientId())),
                        Map.entry("client_secret", List.of(client.getClientSecret()))
                )))
                .retrieve()
                .body(TokenResponse.class);
    }

    @SneakyThrows
    public TokenResponse authorizationCode(Integer port, boolean pkce, Consumer<String> authUrlPresenter) {
        var urlCallback = new CompletableFuture<String>();
        var serverBuilder = HttpServer.create()
                .accessLog(true)
                .route(rb -> {
                    rb.route(r -> Objects.equals(r.path(), "callback"),
                            (req, res) -> {
                                urlCallback.complete(req.uri());
                                return res.status(202).send();
                            });
                });
        if (port != null)
            serverBuilder = serverBuilder.port(port);

        var server = serverBuilder.bindNow();

        var url = "http://127.0.0.1:" + server.port() + "/callback";
        var state = PkceUtil.createCodeVerifier();
        var pkceCode = pkce ? PkceUtil.generateCode() : null;
        URI authorizationUrl = buildAuthorizationUrl(url, state, pkceCode == null ? null : pkceCode.challenge());
        authUrlPresenter.accept(authorizationUrl.toString());
        log.debug("presented auth url, waiting 1 min");
        var urlValue = urlCallback.get(1, TimeUnit.MINUTES);
        var urlParams = UriComponentsBuilder.fromUriString(urlValue).build().getQueryParams();
        var urlParamCode = urlParams.getFirst("code");
        var urlParamState = urlParams.getFirst("state");
        if (!MessageDigest.isEqual(urlParamState.getBytes(), state.getBytes())) {
            throw new IllegalStateException("state is not equal: ours=" + state + "; theirs=" + urlParamState);
        }

        var tokenBody = new HashMap<String, List<String>>();
        tokenBody.put("grant_type", List.of("authorization_code"));
        tokenBody.put("code", List.of(urlParamCode));
        tokenBody.put("redirect_url", List.of(url));
        if (pkceCode != null)
            tokenBody.put("code_verifier", List.of(pkceCode.verifier()));

        return restClient.post().uri(client.getTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new MultiValueMapAdapter<>(tokenBody))
                .retrieve()
                .body(TokenResponse.class);
    }

    private URI buildAuthorizationUrl(String redirectUri, String state, String challenge) {
        var builder = UriComponentsBuilder.fromUriString(client.getAuthorizationEndpoint());
        builder
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", client.scope())
                .queryParam("state", state);

        if (challenge != null) {
            builder.queryParam("code_challenge", challenge)
                    .queryParam("code_challenge_method", "S256");
        }
        return builder.build().toUri();
    }

    TokenResponse deviceGrant(Consumer<String> verificationPresenter, Runnable waiter) {
        var deviceAuth = restClient.post().uri(client.getAuthorizationEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new MultiValueMapAdapter<>(Map.ofEntries(
                        Map.entry("grant_type", List.of("urn:ietf:params:oauth:grant-type:device_code")),
                        Map.entry("client_id", List.of(client.getClientId())),
                        Map.entry("scope", List.of(client.scope()))
                )))
                .retrieve()
                .body(DeviceCodeAuthorization.class);

        verificationPresenter.accept("Visit this url to provide code '" + deviceAuth.getUserCode() + "': " + deviceAuth.getVerificationUri());
        waiter.run();

        return restClient.post().uri(client.getTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new MultiValueMapAdapter<>(Map.ofEntries(
                        Map.entry("grant_type", List.of("urn:ietf:params:oauth:grant-type:device_code")),
                        Map.entry("client_id", List.of(client.getClientId())),
                        Map.entry("device_code", List.of(deviceAuth.getDeviceCode()))
                )))
                .retrieve()
                .body(TokenResponse.class);
    }

    TokenResponse refreshGrant(boolean includeClientId, String refreshToken) {
        var tokenBody = new HashMap<String, List<String>>();
        tokenBody.put("grant_type", List.of("refresh_token"));
        tokenBody.put("refresh_token", List.of(refreshToken));
        if (includeClientId) {
            tokenBody.put("client_id", List.of(client.getClientId()));
            tokenBody.put("client_secret", List.of(client.getClientSecret()));
        }
        return restClient.post().uri(client.getTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new MultiValueMapAdapter<>(tokenBody))
                .retrieve()
                .body(TokenResponse.class);
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OAuthClientClient {
        String name;
        String clientId;
        @ToString.Exclude
        String clientSecret;
        List<String> scopes;
        String issuer;
        String tokenEndpoint;
        String jwksUri;
        String userinfoEndpoint;
        String authorizationEndpoint;
        List<String> tokenResponses;

        static OAuthClientClient of(String name, OAuthClientCli.Clients.ClientOptions.LiteralClientOptions clientOptions) {
            return new OAuthClientClient()
                    .setName(name)
                    .setClientId(clientOptions.getClientId())
                    .setClientSecret(clientOptions.getClientSecret())
                    .setScopes(clientOptions.getScope())
                    .setTokenEndpoint(clientOptions.getTokenEndpoint());
        }

        String scope() {
            if (scopes == null || scopes.size() != 1)
                throw new UnsupportedOperationException();

            return scopes.getFirst();
        }
    }

    @Data
    @Accessors(chain = true)
    public static class TokenResponse {
        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        Map<String, Object> additionalProperties;

        @JsonProperty("token_type")
        String tokenType;
        @JsonProperty("expires_in")
        Integer expiresIn;
        @JsonProperty("access_token")
        String accessToken;
    }

    @Data
    @Accessors(chain = true)
    private static class DeviceCodeAuthorization {
        @JsonIgnore
        @JsonAnyGetter
        @JsonAnySetter
        Map<String, Object> additionalProperties;

        @JsonProperty("device_code")
        String deviceCode;
        @JsonProperty("user_code")
        String userCode;
        @JsonProperty("verification_uri")
        String verificationUri;
        @JsonProperty("verification_uri_complete")
        String verificationUriComplete;
        @JsonProperty("interval")
        Integer interval;
        @JsonProperty("expires_in")
        Integer expiresIn;
    }

    /**
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc7636>rfc7636</a>
     */
    private static class PkceUtil {
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        static PkceCode generateCode() {
            var codeVerifier = createCodeVerifier();
            var codeChallenge = createCodeChallenge(codeVerifier);
            return new PkceCode(codeVerifier, codeChallenge);
        }

        /**
         * Creates an RFC 7636 PKCE code_verifier.
         * <p>
         * Generates 32 cryptographically secure random bytes and base64url-encodes
         * them without padding, producing a 43-character verifier.
         *
         * @return codeVerifier
         * @see <a href=https://datatracker.ietf.org/doc/html/rfc7636#section-4.1>rfc7636 4.1</a>
         */
        static String createCodeVerifier() {
            byte[] randomBytes = new byte[32];
            SECURE_RANDOM.nextBytes(randomBytes);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);
        }

        /**
         * 4.2. Client Creates the Code Challenge
         * <p>
         * The client then creates a code challenge derived from the code verifier...
         * <p>
         * S256
         * code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
         *
         * @param codeVerifier output of {@link #createCodeVerifier()}
         * @return code challenge
         * @see <a href=https://datatracker.ietf.org/doc/html/rfc7636#section-4.2>rfc7636 4.2</a>
         */
        @SneakyThrows
        static String createCodeChallenge(String codeVerifier) {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(digest);
        }

        record PkceCode(String verifier, String challenge) {
        }
    }
}
