package side.cloud.util.acme.lib.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class PebbleChallengeServerTestContainer extends GenericContainer<PebbleChallengeServerTestContainer> {
    public static final int TLS_ALPN_01_PORT = 5001;
    public static final int HTTP_01_PORT = 5002;
    public static final int HTTPS_01_PORT = 5003;
    public static final int DNS_OVER_HTTP_PORT = 8443;
    public static final int DNS_SERVER_PORT = 8053;
    public static final int MGMT_PORT = 8055;
    private static final DockerImageName PEBBLE_CH = DockerImageName.parse("ghcr.io/letsencrypt/pebble-challtestsrv");
    private static final DockerImageName PEBBLE_CH2 = DockerImageName.parse("ghcr.io/letsencrypt/pebble-challtestsrv:2");
    private String dnsOverHttpCert;
    private String dnsOverHttpCertMountPath = "/pebble-challtestsrv-config/doh.pem";
    private String dnsOverHttpCertKey;
    private String dnsOverHttpCertKeyMountPath = "/pebble-challtestsrv-config/doh.key";

    public PebbleChallengeServerTestContainer() {
        this(PEBBLE_CH2);
    }

    public PebbleChallengeServerTestContainer(DockerImageName image) {
        super(image);
        image.assertCompatibleWith(PEBBLE_CH);
        // noinspection resource
        withExposedPorts(TLS_ALPN_01_PORT, HTTP_01_PORT, HTTPS_01_PORT, DNS_OVER_HTTP_PORT, DNS_SERVER_PORT, MGMT_PORT);
        this.waitingFor(Wait.forHttp("/").forPort(MGMT_PORT).forStatusCode(404));
    }

    public PebbleChallengeServerTestContainer withDnsOverHttpCert(String dnsOverHttpCert) {
        this.dnsOverHttpCert = dnsOverHttpCert;
        return this;
    }

    public PebbleChallengeServerTestContainer withDnsOverHttpCertMountPath(String dnsOverHttpCertMountPath) {
        Objects.requireNonNull(dnsOverHttpCertMountPath, "dnsOverHttpCertMountPath must not be null");
        this.dnsOverHttpCertMountPath = dnsOverHttpCertMountPath;
        return this;
    }

    public PebbleChallengeServerTestContainer withDnsOverHttpCertKey(String dnsOverHttpCertKey) {
        this.dnsOverHttpCertKey = dnsOverHttpCertKey;
        return this;
    }

    public PebbleChallengeServerTestContainer withDnsOverHttpCertKeyMountPath(String dnsOverHttpCertKeyMountPath) {
        Objects.requireNonNull(dnsOverHttpCertKeyMountPath, "dnsOverHttpCertKeyMountPath must not be null");
        this.dnsOverHttpCertKeyMountPath = dnsOverHttpCertKeyMountPath;
        return this;
    }

    @SuppressWarnings("resource")
    @Override
    protected void configure() {
        if (dnsOverHttpCert != null && dnsOverHttpCertKey != null) {
            setCommand("-doh-cert", dnsOverHttpCertMountPath, "-doh-cert-key", dnsOverHttpCertKeyMountPath);
            withCopyToContainer(Transferable.of(dnsOverHttpCert), dnsOverHttpCertMountPath);
            withCopyToContainer(Transferable.of(dnsOverHttpCertKey), dnsOverHttpCertKeyMountPath);
        }
        super.configure();
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo, boolean reused) {
        super.containerIsStarted(containerInfo, reused);
        RestClient.builder().baseUrl(baseUrl()).build().post().uri("/set-default-ipv4").body(Map.of("ip", Objects.requireNonNull(containerInfo.getNetworkSettings().getNetworks().values().iterator().next().getIpAddress()))).retrieve().toBodilessEntity();
    }

    public URI baseUrl() {
        return UriComponentsBuilder.newInstance().scheme("http").host(getHost()).port(getMappedPort(MGMT_PORT)).build().toUri();
    }
}
