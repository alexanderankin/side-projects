package side.cloud.util.acme.lib.model;

import java.net.URI;

public record AcmeAccountRequest<T>(URI url, URI accountId, T payload) {
}
