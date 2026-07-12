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
                          "priority": 1,
                          "request": {
                            "method": "GET",
                            "urlPath": "/authorize-outsider"
                          },
                          "response": {
                            "status": 302,
                            "headers": {
                              "Location": "{{{request.query.redirect_uri}}}?code=outsider-code&state={{{request.query.state}}}"
                            }
                          }
                        },
                        {
                          "priority": 5,
                          "request": {
                            "method": "GET",
                            "urlPath": "/authorize"
                          },
                          "response": {
                            "status": 302,
                            "headers": {
                              "Location": "{{{request.query.redirect_uri}}}?code=itest-code&state={{{request.query.state}}}"
                            }
                          }
                        },
                        {
                          "priority": 1,
                          "request": {
                            "method": "POST",
                            "urlPath": "/token-outsider"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "{\\"access_token\\":\\"outsider-access-token\\",\\"token_type\\":\\"Bearer\\",\\"expires_in\\":300,\\"scope\\":\\"identify email guilds guilds.members.read\\"}"
                          }
                        },
                        {
                          "priority": 5,
                          "request": {
                            "method": "POST",
                            "urlPath": "/token"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "{\\"access_token\\":\\"itest-access-token\\",\\"token_type\\":\\"Bearer\\",\\"expires_in\\":300,\\"scope\\":\\"identify email guilds guilds.members.read\\"}"
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
                        },
                        {
                          "priority": 1,
                          "request": {
                            "method": "GET",
                            "urlPath": "/users/@me/guilds",
                            "headers": {
                              "Authorization": {
                                "contains": "outsider-access-token"
                              }
                            }
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "[]"
                          }
                        },
                        {
                          "priority": 5,
                          "request": {
                            "method": "GET",
                            "urlPath": "/users/@me/guilds"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "[{\\"id\\":\\"guild-1\\",\\"name\\":\\"Guild One\\"}]"
                          }
                        },
                        {
                          "request": {
                            "method": "GET",
                            "urlPath": "/users/@me/guilds/guild-1/member"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "{\\"roles\\":[\\"role-allowed\\"]}"
                          }
                        },
                        {
                          "request": {
                            "method": "GET",
                            "urlPath": "/guilds/guild-1/roles"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "[{\\"id\\":\\"guild-1\\",\\"permissions\\":\\"0\\"},{\\"id\\":\\"role-allowed\\",\\"permissions\\":\\"0\\"}]"
                          }
                        },
                        {
                          "request": {
                            "method": "GET",
                            "urlPath": "/guilds/guild-1/channels"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "body": "[{\\"id\\":\\"channel-allowed\\",\\"name\\":\\"allowed\\",\\"permission_overwrites\\":[{\\"id\\":\\"role-allowed\\",\\"type\\":0,\\"allow\\":\\"1024\\",\\"deny\\":\\"0\\"}]},{\\"id\\":\\"channel-hidden\\",\\"name\\":\\"hidden\\",\\"permission_overwrites\\":[]}]"
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
        registry.add("spring.security.oauth2.client.registration.discord.scope[0]", () -> "identify");
        registry.add("spring.security.oauth2.client.registration.discord.scope[1]", () -> "email");
        registry.add("spring.security.oauth2.client.registration.discord.scope[2]", () -> "guilds");
        registry.add("spring.security.oauth2.client.registration.discord.scope[3]", () -> "guilds.members.read");
        registry.add("spring.security.oauth2.client.provider.discord-outsider.authorization-uri", () -> providerBaseUrl + "/authorize-outsider");
        registry.add("spring.security.oauth2.client.provider.discord-outsider.token-uri", () -> providerBaseUrl + "/token-outsider");
        registry.add("spring.security.oauth2.client.provider.discord-outsider.user-info-uri", () -> providerBaseUrl + "/users/@me");
        registry.add("spring.security.oauth2.client.provider.discord-outsider.user-name-attribute", () -> "id");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.provider", () -> "discord-outsider");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.client-id", () -> "outsider-client");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.client-secret", () -> "outsider-secret");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.authorization-grant-type", () -> "authorization_code");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.redirect-uri", () -> "{baseUrl}/login/oauth2/code/{registrationId}");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.scope[0]", () -> "identify");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.scope[1]", () -> "email");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.scope[2]", () -> "guilds");
        registry.add("spring.security.oauth2.client.registration.discord-outsider.scope[3]", () -> "guilds.members.read");
        registry.add("file-share.discord.api-base-url", () -> providerBaseUrl);
        registry.add("file-share.discord.bot-token", () -> "itest-bot-token");
        registry.add("file-share.guilds[0].id", () -> "guild-1");
        registry.add("file-share.guilds[0].folder", () -> "guild-one");
        registry.add("file-share.guilds[0].channels[0].id", () -> "channel-allowed");
        registry.add("file-share.guilds[0].channels[0].folder", () -> "channel-allowed");
        registry.add("file-share.guilds[0].channels[1].id", () -> "channel-hidden");
        registry.add("file-share.guilds[0].channels[1].folder", () -> "channel-hidden");
    }

    @Test
    void redirectsAnonymousUsersToDiscordLogin() throws Exception {
        HttpResponse<String> response = send(newClient(), appUri("/"));

        assertThat(response.statusCode(), equalTo(302));
        assertThat(response.headers().firstValue("location").orElseThrow(), containsString("/login"));
    }

    @Test
    void redirectsAnonymousUsersAwayFromFileDownloads() throws Exception {
        HttpResponse<String> response = send(newClient(), appUri("/download?path=hello.txt"));

        assertThat(response.statusCode(), equalTo(302));
        assertThat(response.headers().firstValue("location").orElseThrow(), containsString("/login"));
    }

    @Test
    void logsInThroughDockerizedOauthProviderAndDownloadsFile() throws Exception {
        HttpClient client = newClient();

        login(client, "/oauth2/authorization/discord");

        HttpResponse<String> index = send(client, appUri("/"));
        assertThat(index.statusCode(), equalTo(200));
        assertThat(index.body(), containsString("Index of /"));
        assertThat(index.body(), containsString("guild-one/"));

        HttpResponse<String> guildIndex = send(client, appUri("/?path=guild-one"));
        assertThat(guildIndex.statusCode(), equalTo(200));
        assertThat(guildIndex.body(), containsString("channel-allowed/"));
        assertThat(guildIndex.body().contains("channel-hidden/"), equalTo(false));

        HttpResponse<String> channelIndex = send(client, appUri("/?path=guild-one/channel-allowed"));
        assertThat(channelIndex.statusCode(), equalTo(200));
        assertThat(channelIndex.body(), containsString("hello.txt"));
        assertThat(channelIndex.body(), containsString("download"));

        HttpResponse<String> hiddenChannel = send(client, appUri("/?path=guild-one/channel-hidden"));
        assertThat(hiddenChannel.statusCode(), equalTo(403));

        HttpResponse<String> download = send(client, appUri("/download?path=guild-one/channel-allowed/hello.txt"));
        assertThat(download.statusCode(), equalTo(200));
        assertThat(download.body(), equalTo("hello from integration test\n"));
    }

    @Test
    void failsLoginWhenUserIsNotInAConfiguredDiscordServer() throws Exception {
        HttpClient client = newClient();

        URI loginRedirect = send(client, appUri("/oauth2/authorization/discord-outsider"))
                .headers()
                .firstValue("location")
                .map(URI::create)
                .orElseThrow();
        URI providerRedirect = location(loginRedirect, send(client, loginRedirect));
        URI failureRedirect = location(providerRedirect, send(client, providerRedirect));

        assertThat(failureRedirect.toString(), containsString("/login?error"));
        HttpResponse<String> failurePage = send(client, failureRedirect);
        assertThat(failurePage.statusCode(), equalTo(200));
        assertThat(failurePage.body(), containsString("Invalid credentials"));

        HttpResponse<String> index = send(client, appUri("/"));
        assertThat(index.statusCode(), equalTo(302));
        assertThat(index.headers().firstValue("location").orElseThrow(), containsString("/login"));
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

    private void login(HttpClient client, String authorizationPath) throws Exception {
        URI loginRedirect = location(appUri(authorizationPath), send(client, appUri(authorizationPath)));
        URI providerRedirect = location(loginRedirect, send(client, loginRedirect));
        URI callback = location(providerRedirect, send(client, providerRedirect));
        HttpResponse<String> callbackResponse = send(client, callback);
        if (callbackResponse.statusCode() != 200 && callbackResponse.statusCode() != 302) {
            throw new AssertionError("callback " + callback + " returned " + callbackResponse.statusCode() + " " + callbackResponse.body());
        }
    }

    private static HttpResponse<String> send(HttpClient client, URI uri) throws Exception {
        return client.send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static URI location(URI baseUri, HttpResponse<?> response) {
        assertThat("redirect from " + baseUri, response.statusCode(), equalTo(302));
        return baseUri.resolve(response.headers().firstValue("location").orElseThrow());
    }
}
