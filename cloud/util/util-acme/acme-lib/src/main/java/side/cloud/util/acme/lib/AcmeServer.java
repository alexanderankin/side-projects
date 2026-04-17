package side.cloud.util.acme.lib;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nimbusds.jose.JWSObjectJSON;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import side.cloud.util.acme.lib.model.AcmeJwsObject;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.lib.model.AcmeResources.NewAccount;
import side.cloud.util.acme.lib.model.AcmeResources.NewOrder;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
public class AcmeServer {
    public static final String APPLICATION_JOSE_JSON = "application/jose+json";
    private final AcmeServerService acmeService;
    private final Config config;
    private final JsonMapper jsonMapper;
    private final RouterFunction<ServerResponse> routerFunction;

    public AcmeServer(AcmeServerService acmeService, Config config, JsonMapper jsonMapper) {
        this.acmeService = acmeService;
        this.config = jsonMapper.convertValue(config, Config.class); // clone because it is mutable
        this.jsonMapper = jsonMapper;
        routerFunction = RouterFunctions.route()
                .GET("/directory", ignored -> ServerResponse.ok().body(config.getDirectory()))
                .GET(config.getDirectory().getNewNonce().getPath(),
                        ignored -> ServerResponse.ok().header("Replay-Nonce", acmeService.newNonce()).build())
                .HEAD(config.getDirectory().getNewNonce().getPath(),
                        ignored -> ServerResponse.ok().header("Replay-Nonce", acmeService.newNonce()).build())
                .POST(config.getDirectory().getNewAccount().getPath(),
                        req -> {
                            var body = req.body(String.class);
                            var sckp = SupportedClientKeyPair.parseNewJwkFromJws(body);
                            var acmeJwsObject = sckp.verifyAndDeserialize(body);
                            if (!(acmeJwsObject instanceof AcmeJwsObject.JsonAcmeJwsObject jsonJws)) {
                                throw new RuntimeException("todo write exception message");
                            }
                            var account = acmeService.newAccount(sckp, jsonMapper.convertValue(jsonJws.getPayload(), NewAccount.class));
                            return ServerResponse.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE_JSON)
                                    .header(HttpHeaders.LOCATION, String.valueOf(account.id()))
                                    .body(account.resource());
                        })
                .POST(config.getDirectory().getNewOrder().getPath(),
                        req -> {
                            var body = req.body(String.class);
                            var json = JWSObjectJSON.parse(body);
                            var order = acmeService.createOrder(
                                    extractAccountId(json),
                                    json,
                                    jsonMapper.convertValue(json.getPayload().toJSONObject(), NewOrder.class));
                            return ServerResponse.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE_JSON)
                                    .header(HttpHeaders.LOCATION, order.id().toString())
                                    .body(order.resource());
                        })
                .POST(StringUtils.trimTrailingCharacter(config.getDirectory().getNewAuthz().getPath(), '/') + "/{authorizationId}",
                        req -> {
                            var body = req.body(String.class);
                            var json = JWSObjectJSON.parse(body);
                            var order = acmeService.getAuthorization(
                                    extractAccountId(json),
                                    json,
                                    req.param("authorizationId").orElseThrow());
                            return ServerResponse.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE_JSON)
                                    .header(HttpHeaders.LOCATION, order.id().toString())
                                    .body(order.resource());
                        })
                .POST(StringUtils.trimTrailingCharacter(config.getDirectory().getNewAuthz().getPath(), '/') +
                                "/{authorizationId}/challenges/{challengeId}",
                        req -> {
                            var body = req.body(String.class);
                            var json = JWSObjectJSON.parse(body);
                            var order = acmeService.getChallenge(
                                    extractAccountId(json),
                                    json,
                                    req.param("authorizationId").orElseThrow(),
                                    req.param("challengeId").orElseThrow());
                            return ServerResponse.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE_JSON)
                                    .header(HttpHeaders.LOCATION, order.id().toString())
                                    .body(order.resource());
                        })
                .build();
    }

    private static URI extractAccountId(JWSObjectJSON json) {
        var signatures = json.getSignatures();
        Assert.isTrue(signatures.size() == 1, "must have exactly one signature");
        return URI.create(signatures.getFirst().getUnprotectedHeader().getKeyID());
    }

    @SneakyThrows
    public Optional<ServerResponse> handle(ServerRequest request) {
        var handler = routerFunction.route(request).orElse(null);
        if (handler == null) {
            return Optional.empty();
        }

        return Optional.of(handler.handle(request));
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        @NotNull
        Directory directory;
        @NotEmpty
        Set<String> contactSupportedSchemes = Set.of("mailto");
    }
}
