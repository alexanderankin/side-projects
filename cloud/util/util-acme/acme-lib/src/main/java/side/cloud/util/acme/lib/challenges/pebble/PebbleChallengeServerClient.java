package side.cloud.util.acme.lib.challenges.pebble;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import side.cloud.util.acme.lib.challenges.Presenter;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class PebbleChallengeServerClient implements Presenter {
    private final RestClient restClient;

    public PebbleChallengeServerClient(URI baseUrl) {
        this(baseUrl, RestClient.builder());
    }

    public PebbleChallengeServerClient(URI baseUrl, RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl.toString()).build();
    }

    private void post(String path, Object body) {
        restClient.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
    }

    private String get(String path) {
        return restClient.post().uri(path).retrieve().body(String.class);
    }

    @Override
    public void addHttp01(String token, String content) {
        post("/add-http01", Map.of("token", token, "content", content));
    }

    @Override
    public void deleteHttp01(String token) {
        post("/del-http01", Map.of("token", token));
    }

    public void addHttpRedirect(String path, String targetURL) {
        post("/add-redirect", Map.of("path", path, "targetURL", targetURL));
    }

    public void deleteHttpRedirect(String path) {
        post("/del-redirect", Map.of("path", path));
    }

    public void setDefaultIPv4(String ip) {
        post("/set-default-ipv4", Map.of("ip", ip));
    }

    public void setDefaultIPv6(String ip) {
        post("/set-default-ipv6", Map.of("ip", ip));
    }

    @Override
    public void addDnsTxt(String host, String value) {
        post("/set-txt", Map.of("host", host, "value", value));
    }

    @Override
    public void deleteDnsTxt(String host) {
        post("/clear-txt", Map.of("host", host));
    }

    public void addDnsA(String host, List<String> addresses) {
        post("/add-a", Map.of("host", host, "addresses", addresses));
    }

    public void deleteDnsA(String host) {
        post("/clear-a", Map.of("host", host));
    }

    public void addDnsAAAA(String host, List<String> addresses) {
        post("/add-aaaa", Map.of("host", host, "addresses", addresses));
    }

    public void deleteDnsAAAA(String host) {
        post("/clear-aaaa", Map.of("host", host));
    }

    public void addDnsCAA(String host, List<Map<String, String>> policies) {
        post("/add-caa", Map.of("host", host, "policies", policies));
    }

    public void deleteDnsCAA(String host) {
        post("/clear-caa", Map.of("host", host));
    }

    public void setDnsCname(String host, String target) {
        post("/set-cname", Map.of("host", host, "target", target));
    }

    public void clearDnsCname(String host) {
        post("/clear-cname", Map.of("host", host));
    }

    public void setServfail(String host) {
        post("/set-servfail", Map.of("host", host));
    }

    public void clearServfail(String host) {
        post("/clear-servfail", Map.of("host", host));
    }

    @Override
    public void addTlsAlpn01(String host, String keyAuthorization) {
        post("/add-tlsalpn01", Map.of("host", host, "content", keyAuthorization));
    }

    @Override
    public void deleteTlsAlpn01(String host) {
        post("/del-tlsalpn01", Map.of("host", host));
    }

    public void clearRequestHistory() {
        post("/clear-request-history", Map.of());
    }

    public String getHttpHistory() {
        return get("/http-request-history");
    }

    public String getDnsHistory() {
        return get("/dns-request-history");
    }

    public String getTlsAlpnHistory() {
        return get("/tlsalpn01-request-history");
    }
}
