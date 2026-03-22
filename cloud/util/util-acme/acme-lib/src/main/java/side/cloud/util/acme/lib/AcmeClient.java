package side.cloud.util.acme.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeJwsObject.AcmeJwsHeader.JwkAcmeJwsHeader;
import side.cloud.util.acme.lib.model.AcmeJwsObject.AcmeJwsHeader.KidAcmeJwsHeader;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.*;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Data
@Accessors(chain = true)
public class AcmeClient {
    private final Configuration configuration;
    private final AcmeClientOperations acmeClientOperations;
    private final JsonMapper jsonMapper;
    private final AtomicReference<Directory> directory = new AtomicReference<>();

    public Directory directory() {
        if (directory.get() == null) {
            synchronized (this) {
                if (directory.get() == null) {
                    log.debug("directory() from {}", configuration.getDirectoryUrl());
                    directory.set(acmeClientOperations.directory(configuration.getDirectoryUrl()));
                }
            }
        }
        return directory.get();
    }

    public ResourceWithId<Account> newAccount(SupportedClientKeyPair keyPair, NewAccount resource) {
        var directory = directory();
        var object = new AcmeJwsObject.JsonAcmeJwsObject()
                .setPayload(jsonMapper.convertValue(resource, new TypeReference<>() {
                }))
                .setHeaders(new JwkAcmeJwsHeader()
                        .setJwk(keyPair.asJwk().toPublicJWK())
                        .setAlg(keyPair.getAlgorithm().name())
                        .setUrl(directory.getNewAccount()));
        var response = acmeClientOperations.post(directory.getNewAccount(), keyPair, object, Account.class, directory);
        return new ResourceWithId<>(response.getBody(), response.getHeaders().getLocation());
    }

    public ResourceWithId<Order> newOrder(SupportedClientKeyPair keyPair, URI accountId, NewOrder resource) {
        var directory = directory();
        var object = new AcmeJwsObject.JsonAcmeJwsObject()
                .setPayload(jsonMapper.convertValue(resource, new TypeReference<>() {
                }))
                .setHeaders(new KidAcmeJwsHeader()
                        .setKid(accountId)
                        .setAlg(keyPair.getAlgorithm().name())
                        .setUrl(directory.getNewOrder()));
        var response = acmeClientOperations.post(directory.getNewOrder(), keyPair, object, Order.class, directory);
        return new ResourceWithId<>(response.getBody(), response.getHeaders().getLocation());
    }

    public <T> T getResource(SupportedClientKeyPair keyPair, URI accountId, URI resourceId, Class<T> resourceClass) {
        var header = new KidAcmeJwsHeader()
                .setKid(accountId)
                .setAlg(keyPair.getAlgorithm().name())
                .setUrl(resourceId);
        return acmeClientOperations.postGet(resourceId, keyPair, header, resourceClass, directory()).getBody();
    }

    public <T> T postResource(SupportedClientKeyPair keyPair, URI accountId, URI resourceId, Map<String, Object> resource, Class<T> resourceClass) {
        var header = new KidAcmeJwsHeader()
                .setKid(accountId)
                .setAlg(keyPair.getAlgorithm().name())
                .setUrl(resourceId);
        var body = new AcmeJwsObject.JsonAcmeJwsObject().setPayload(resource).setHeaders(header);
        return acmeClientOperations.post(resourceId, keyPair, body, resourceClass, directory()).getBody();
    }

    @Data
    @Accessors(chain = true)
    public static class Configuration {
        URI directoryUrl;
    }
}
