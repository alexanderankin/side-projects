package side.cloud.util.acme.lib;

import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

public class PebbleAcmeTestContainer extends GenericContainer<PebbleAcmeTestContainer> {
    private static final DockerImageName PEBBLE = DockerImageName.parse("ghcr.io/letsencrypt/pebble");
    private static final int DIR_PORT = 14000;

    public PebbleAcmeTestContainer() {
        this(PEBBLE);
    }

    public PebbleAcmeTestContainer(DockerImageName image) {
        super(image);
        image.assertCompatibleWith(PEBBLE);
        addExposedPort(DIR_PORT);
        this.waitingFor(Wait.forHttps("/dir").forPort(DIR_PORT).allowInsecure());
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
