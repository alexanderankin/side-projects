package side.cloud.util.acme.lib.containers;

import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

public class PebbleAcmeServerTestContainer extends GenericContainer<PebbleAcmeServerTestContainer> {
    private static final DockerImageName PEBBLE = DockerImageName.parse("ghcr.io/letsencrypt/pebble");
    private static final DockerImageName PEBBLE2 = DockerImageName.parse("ghcr.io/letsencrypt/pebble:2");
    private static final int DIR_PORT = 14000;
    private boolean apiStrictMode;
    private String dnsServer;
    private String configJson;
    private String configJsonMountLocation = "/pebble-config/pebble-config.json";

    public PebbleAcmeServerTestContainer() {
        this(PEBBLE2);
    }

    public PebbleAcmeServerTestContainer(DockerImageName image) {
        super(image);
        image.assertCompatibleWith(PEBBLE);
        addExposedPort(DIR_PORT);
        this.waitingFor(Wait.forHttps("/dir").forPort(DIR_PORT).allowInsecure());
    }

    public PebbleAcmeServerTestContainer withApiStrictMode(boolean apiStrictMode) {
        this.apiStrictMode = apiStrictMode;
        return this;
    }

    public PebbleAcmeServerTestContainer withDnsServer(String dnsServer) {
        this.dnsServer = dnsServer;
        return this;
    }

    public PebbleAcmeServerTestContainer withConfigJson(String configJson) {
        this.configJson = configJson;
        return this;
    }

    public PebbleAcmeServerTestContainer withConfigJsonMountLocation(String mountLocation) {
        Objects.requireNonNull(mountLocation, "mountLocation must not be null");
        this.configJsonMountLocation = mountLocation;
        return this;
    }

    @Override
    protected void configure() {
        var newArgs = new ArrayList<String>();
        if (apiStrictMode) {
            newArgs.add("-strict");
        }
        if (dnsServer != null) {
            newArgs.add("-dnsserver");
            newArgs.add(dnsServer);
        }
        if (configJson != null) {
            newArgs.add("-config");
            newArgs.add(configJsonMountLocation);
            // noinspection resource
            withCopyToContainer(Transferable.of(configJson), configJsonMountLocation);
        }
        if (!newArgs.isEmpty()) {
            setCommand(newArgs.toArray(new String[0]));
        }
        super.configure();
    }

    public URI directory() {
        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(getHost())
                .port(getMappedPort(DIR_PORT))
                .path("/dir")
                .build()
                .toUri();
    }
}
