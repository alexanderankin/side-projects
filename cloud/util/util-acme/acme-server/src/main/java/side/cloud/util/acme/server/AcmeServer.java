package side.cloud.util.acme.server;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.function.*;
import org.springframework.web.util.UriComponentsBuilder;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.RequestKeyPairSignature;
import side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Account.AccountStatus;
import side.cloud.util.acme.lib.model.AcmeResources.Directory;
import side.cloud.util.acme.server.nonce.NonceService;
import side.cloud.util.acme.server.persistence.AcmeServerDao;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;
import static side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.REPLAY_NONCE;
import static side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse.TypedAcmeResponse.typedPayload;
import static side.cloud.util.acme.lib.model.ProblemDetailAcmeTypes.*;

@RequiredArgsConstructor
public class AcmeServer {
    private final NonceService nonceService;
    private final AcmeServerDao dao;
    private final Config config;
    private final JsonMapper jsonMapper;

    public RouterFunction<ServerResponse> handler() {
        var route = RouterFunctions.route();
        var d = config.getDirectory();
        route.GET(config.getDirectoryPath(), ignored -> ServerResponse.ok().body(d));
        route.HEAD(d.getNewNonce().getPath(), ignored -> ServerResponse.ok().header(REPLAY_NONCE, nonceService.newNonce()).build());
        route.add(RouterFunctions.nest(RequestPredicates.path(d.getNewAccount().getPath()), accountRoute()));
        return route.build();
    }

    private RouterFunction<ServerResponse> accountRoute() {
        var accountRouteBuilder = RouterFunctions.route();
        accountRouteBuilder.POST("", r -> ser(newAccount(de(r))));
        accountRouteBuilder.POST("/{accountId}", r -> ser(getAccount(de(r), r.pathVariables())));
        accountRouteBuilder.POST("/{accountId}/orders", r -> ser(getOrders(de(r), r.pathVariables(), r.params())));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}", r -> ser(getOrder(de(r), r.pathVariables())));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/challenges/{challengeId}", r -> ser(getChallenge(de(r))));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/finalize", r -> ser(finalizeOrder(de(r))));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/cert", r -> ser(getOrderCert(de(r))));
        return accountRouteBuilder.build();
    }

    private RouterFunction<ServerResponse> orderRoute() {
        var orderRouteBuilder = RouterFunctions.route();
        return orderRouteBuilder.build();
    }

    public AcmeResponse newAccount(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        Assert.notNull(de.keyPair(), "requestAndKeyPair.keyPair must not be null");

        if (!de.verify())
            return typedPayload(unauthorized.getProblemDetail()).setCode(BAD_REQUEST);

        var keyHash = keyHash(de.keyPair());
        var accountInfo = dao.getAccountByKeyHash(keyHash);

        var newAccount = jsonMapper.convertValue(de.request().getPayload(), AcmeResources.NewAccount.class);
        var readNotCreate = newAccount.getOnlyReturnExisting();
        if (readNotCreate && accountInfo == null) {
            return typedPayload(accountDoesNotExist.getProblemDetail()).setCode(BAD_REQUEST);
        }

        if (accountInfo == null) {
            // create
            var accountId = nonceService.genNonce();
            var accountUrl = UriComponentsBuilder.fromUri(config.directory.getNewAccount()).path(accountId).build().toUri();
            var account = new AcmeResources.Account()
                    .setStatus(AccountStatus.valid)
                    .setContact(newAccount.getContact())
                    .setOrders(UriComponentsBuilder.fromUri(accountUrl).path("orders").build().toUri());

            // handle terms of service
            if (config.directory.getMeta().getTermsOfService() != null) {
                if (!Boolean.TRUE.equals(newAccount.getTermsOfServiceAgreed()))
                    return typedPayload(userActionRequired.getProblemDetail().setDetail("TOS not agreed")).setCode(BAD_REQUEST);
                account.setTermsOfServiceAgreed(true);
            }

            // save
            var newAccountInfo = new AcmeServerDao.ServerAccountEntity(accountId, keyHash, account, de.keyPair());
            dao.saveAccount(newAccountInfo);

            // return
            return typedPayload(account).setCode(CREATED).setLocation(accountUrl);
        } else {
            var account = accountInfo.account();

            // update
            account.setContact(newAccount.getContact());
            dao.saveAccount(accountInfo);

            // return
            var accountId = accountInfo.id();
            var accountUrl = UriComponentsBuilder.fromUri(config.directory.getNewAccount()).path(accountId).build().toUri();
            return typedPayload(account).setCode(OK).setLocation(accountUrl);
        }
    }

    public AcmeResponse getAccount(RequestKeyPairSignature de, Map<String, String> params) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        return typedPayload(dao.getAccountById(params.get("accountId")).account()).setCode(OK).setLocation(de.request().getUrl());
    }

    public AcmeResponse getOrders(RequestKeyPairSignature de, Map<String, String> params, MultiValueMap<String, String> query) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        var orderIds = dao.listOrdersForAccount(params.get("accountId"), query.getFirst("lastId"));
        var orderUrls = orderIds.stream()
                .map(orderId -> UriComponentsBuilder.fromUri(config.directory.getNewOrder()).path(orderId).build().toUri())
                .toList();

        var next = orderUrls.isEmpty() ? null : UriComponentsBuilder.fromUri(de.request().getUrl())
                .replaceQueryParam("lastId", orderUrls.getLast())
                .build().toUri();
        return typedPayload(orderUrls).setCode(OK).setNext(next);
    }

    public AcmeResponse getOrder(RequestKeyPairSignature de, Map<String, String> params) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        throw new UnsupportedOperationException();
    }

    public AcmeResponse getChallenge(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        throw new UnsupportedOperationException();
    }

    public AcmeResponse finalizeOrder(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        throw new UnsupportedOperationException();
    }

    public AcmeResponse getOrderCert(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        throw new UnsupportedOperationException();
    }

    private boolean validateNonce(RequestKeyPairSignature de) {
        var nonce = de.request().getNonce();
        if (!StringUtils.hasText(nonce))
            return false;
        return nonceService.useNonce(nonce);
    }

    private AcmeResponse validateAccount(RequestKeyPairSignature de, String accountId) {
        // exists
        var accountById = dao.getAccountById(accountId);
        var accountByKeyHash = dao.getAccountByKeyHash(keyHash(de.keyPair()));
        if (accountById == null && accountByKeyHash == null)
            return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);

        if (accountById == null || accountByKeyHash == null) {
            if (accountByKeyHash == null)
                return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);

            // key is right, account is gone
            return typedPayload(accountDoesNotExist.getProblemDetail()
                    .setDetail(accountDoesNotExist.getProblemDetail().getDetail() + " (account has been deleted)")).setCode(NOT_FOUND);
        }

        // valid
        if (!accountById.account().getStatus().equals(AccountStatus.valid))
            return typedPayload(malformed.getProblemDetail().setDetail("this account is not valid")).setCode(FORBIDDEN);

        // key used is known
        var actualKeyHash = keyHash(de.keyPair());
        var expectedKeyHash = accountById.keyHash();
        if (constantTimeStringEquals(actualKeyHash, expectedKeyHash))
            return null;

        return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);
    }

    RequestKeyPairSignature de(ServerRequest request) {
        return AcmeRequestSerDe.deserialize(request);
    }

    ServerResponse ser(AcmeResponse acmeResponse) {
        var builder = ServerResponse.status(Objects.requireNonNullElse(acmeResponse.getCode(), 200));
        if (acmeResponse.getLocation() != null) {
            builder.location(acmeResponse.getLocation());
        }
        if (acmeResponse.getNext() != null) {
            // TODO: 4/18/26 wtf does this actually do
            builder.header(HttpHeaders.LINK, Link.of(acmeResponse.getNext().toASCIIString(), "next").toString());
        }
        if (acmeResponse instanceof AcmeResponse.TypedAcmeResponse<?> typedAcmeResponse) {
            return builder.body(typedAcmeResponse.getTypedPayload());
        } else if (acmeResponse.getPayload() != null) {
            return builder.body(acmeResponse.getPayload());
        }
        return builder.build();
    }

    String keyHash(SupportedClientKeyPair keyPair) {
        return DigestUtils.sha512Hex(keyPair.getKeyPairPublicEncoded());
    }

    boolean constantTimeStringEquals(String a, String b) {
        if (a == null || b == null)
            return false;
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
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
