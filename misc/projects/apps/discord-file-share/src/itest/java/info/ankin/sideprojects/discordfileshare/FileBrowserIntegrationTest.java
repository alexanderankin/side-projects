package info.ankin.sideprojects.discordfileshare;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileBrowserIntegrationTest {

    @Container
    private static final GenericContainer<?> OAUTH_PROVIDER = new GenericContainer<>(
            DockerImageName.parse("wiremock/wiremock:3.9.1"))
            .withExposedPorts(8080)
            .withCommand("--global-response-templating")
            .withCopyToContainer(Transferable.of("""
                    {
                      "mappings": [
                        {
                          "request": {
                            "method": "GET",
                            "urlPath": "/authorize"
                          },
                          "response": {
                            "status": 302,
                            "headers": {
                              "Location": "{{request.query.redirect_uri}}?code=itest-code&state={{request.query.state}}"
                            }
                          }
                        },
                        {
                          "request": {
                            "method": "POST",
                            "urlPath": "/token"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "{\\"access_token\\":\\"itest-access-token\\",\\"token_type\\":\\"Bearer\\",\\"expires_in\\":300,\\"scope\\":\\"identify email\\"}"
                          }
                        },
                        {
                          "request": {
                            "method": "GET",
                            "urlPath": "/users/@me"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "{\\"id\\":\\"123\\",\\"username\\":\\"integration-user\\",\\"global_name\\":\\"Integration User\\",\\"email\\":\\"itest@example.test\\"}"
                          }
                        }
                      ]
                    }
                    """), "/home/wiremock/mappings/oauth-provider.json")
            .waitingFor(Wait.forHttp("/__admin").forStatusCode(200));

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void applicationProperties(DynamicPropertyRegistry registry) {
        OAUTH_PROVIDER.start();

        String providerBaseUrl = "http://%s:%d".formatted(
                OAUTH_PROVIDER.getHost(),
                OAUTH_PROVIDER.getMappedPort(8080));
        registry.add("file-share.root-directory", () -> "src/itest/resources/shared-files");
        registry.add("spring.security.oauth2.client.provider.discord.authorization-uri", () -> providerBaseUrl + "/authorize");
        registry.add("spring.security.oauth2.client.provider.discord.token-uri", () -> providerBaseUrl + "/token");
        registry.add("spring.security.oauth2.client.provider.discord.user-info-uri", () -> providerBaseUrl + "/users/@me");
        registry.add("spring.security.oauth2.client.registration.discord.client-id", () -> "itest-client");
        registry.add("spring.security.oauth2.client.registration.discord.client-secret", () -> "itest-secret");
    }

    @Test
    void redirectsAnonymousUsersToDiscordLogin() throws Exception {
        HttpResponse<String> response = send(newClient(), appUri("/"));

        assertThat(response.statusCode(), equalTo(302));
        assertThat(response.headers().firstValue("location").orElseThrow(), containsString("/oauth2/authorization/discord"));
    }

    @Test
    void redirectsAnonymousUsersAwayFromFileDownloads() throws Exception {
        HttpResponse<String> response = send(newClient(), appUri("/download?path=hello.txt"));

        assertThat(response.statusCode(), equalTo(302));
        assertThat(response.headers().firstValue("location").orElseThrow(), containsString("/oauth2/authorization/discord"));
    }

    @Test
    void logsInThroughDockerizedOauthProviderAndDownloadsFile() throws Exception {
        HttpClient client = newClient();

        URI loginRedirect = location(appUri("/"), send(client, appUri("/")));
        URI providerRedirect = location(loginRedirect, send(client, loginRedirect));
        URI callback = location(providerRedirect, send(client, providerRedirect));
        HttpResponse<String> callbackResponse = send(client, callback);
        assertThat(callbackResponse.statusCode(), equalTo(302));

        HttpResponse<String> index = send(client, appUri("/"));
        assertThat(index.statusCode(), equalTo(200));
        assertThat(index.body(), containsString("Index of /"));
        assertThat(index.body(), containsString("documents/"));
        assertThat(index.body(), containsString("hello.txt"));
        assertThat(index.body(), containsString("download"));

        HttpResponse<String> download = send(client, appUri("/download?path=hello.txt"));
        assertThat(download.statusCode(), equalTo(200));
        assertThat(download.body(), equalTo("hello from integration test\n"));
    }

    private HttpClient newClient() {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private URI appUri(String path) {
        return URI.create("http://localhost:%d%s".formatted(port, path));
    }

    private static HttpResponse<String> send(HttpClient client, URI uri) throws Exception {
        return client.send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static URI location(URI baseUri, HttpResponse<?> response) {
        assertThat(response.statusCode(), equalTo(302));
        return baseUri.resolve(response.headers().firstValue("location").orElseThrow());
    }
}
