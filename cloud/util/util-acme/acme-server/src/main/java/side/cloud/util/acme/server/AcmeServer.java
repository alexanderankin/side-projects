package side.cloud.util.acme.server;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.RequestAndKeyPair;
import side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.server.nonce.NonceService;

import static side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.REPLAY_NONCE;

@RequiredArgsConstructor
public class AcmeServer {
    private final NonceService nonceService;
    private final Config config;

    public RouterFunction<ServerResponse> handler() {
        var route = RouterFunctions.route();
        var d = config.getDirectory();
        route.GET(config.getDirectoryPath(), ignored -> ServerResponse.ok().body(d));
        route.HEAD(d.getNewNonce().getPath(), ignored -> ServerResponse.ok().header(REPLAY_NONCE, nonceService.newNonce()).build());
        route.POST(d.getNewAccount().getPath(), r -> ser(newAccount(de(r))));

        return route.build();
    }

    RequestAndKeyPair de(ServerRequest request) {
        return AcmeRequestSerDe.deserialize(request);
    }

    ServerResponse ser(AcmeResponse acmeResponse) {
        var builder = ServerResponse.ok();
        if (acmeResponse.getLocation() != null) {
            builder.location(acmeResponse.getLocation());
        }
        if (acmeResponse.getNext() != null) {
            // TODO: 4/18/26 wtf does this actually do
            builder.header(HttpHeaders.LINK, Link.of(acmeResponse.getNext().toASCIIString(), "next").toString());
        }
        if (acmeResponse.getPayload() != null) {
            return builder.body(acmeResponse.getPayload());
        }
        return builder.build();
    }

    public AcmeResponse newAccount(RequestAndKeyPair requestAndKeyPair) {
        Assert.notNull(requestAndKeyPair.keyPair(), "requestAndKeyPair.keyPair must not be null");
        // requestAndKeyPair.keyPair().nimbusVerifier();
        throw new UnsupportedOperationException("need the things to verify here!!!");
    }

    @Data
    @Accessors(chain = true)
    @Validated
    public static class Config {
        String directoryPath;

        @NotNull
        Directory directory;
    }
}
