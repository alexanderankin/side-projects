package side.cloud.util.acme.lib.containers.pebble;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class PebbleAcmeServerTestContainer extends GenericContainer<PebbleAcmeServerTestContainer> {
    public static final Map<String, String> DEFAULT_HMAC_KEYS = Map.of(
            "kid-1", "zWNDZM6eQGHWpSRTPal5eIUYFTu7EajVIoguysqZ9wG44nMEtx3MUAsUDkMTQ12W",
            "kid-2", "b10lLJs8l1GPIzsLP0s6pMt8O0XVGnfTaCeROxQM0BIt2XrJMDHJZBM5NuQmQJQH",
            "kid-3", "HjudV5qnbreN-n9WyFSH-t4HXuEx_XFen45zuxY-G1h6fr74V3cUM_dVlwQZBWmc"
    );
    private static final DockerImageName PEBBLE = DockerImageName.parse("ghcr.io/letsencrypt/pebble");
    private static final DockerImageName PEBBLE2 = DockerImageName.parse("ghcr.io/letsencrypt/pebble:2");
    private static final int DIR_PORT = 14000;
    private boolean apiStrictMode;
    private String dnsServer;
    private String configJson;
    private String configJsonMountLocation = "/test/config/pebble-config.json";

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

    public PebbleAcmeServerTestContainer withBuiltInConfig(BuiltInConfig builtInConfig) {
        Objects.requireNonNull(builtInConfig, "builtInConfig must not be null");
        return withConfigJsonMountLocation(builtInConfig.getMountLocation());
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
        if (configJson != null || BuiltInConfig.MOUNT_LOCATIONS.contains(configJsonMountLocation)) {
            newArgs.add("-config");
            newArgs.add(configJsonMountLocation);
            if (configJson != null)
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

    @RequiredArgsConstructor
    @Getter
    public enum BuiltInConfig {
        LOAD_GENERATOR_CONFIG("/test/config/load-generator-config.json"),
        PEBBLE_EXTERNAL_ACCOUNT_BINDINGS("/test/config/pebble-config-external-account-bindings.json"),
        PEBBLE("/test/config/pebble-config.json"),
        ;

        private static final Set<String> MOUNT_LOCATIONS = Arrays.stream(values()).map(BuiltInConfig::getMountLocation).collect(Collectors.toSet());

        private final String mountLocation;
    }
}
