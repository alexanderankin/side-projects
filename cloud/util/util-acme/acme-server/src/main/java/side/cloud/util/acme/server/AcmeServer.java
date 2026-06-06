package side.cloud.util.acme.server;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import side.cloud.util.acme.lib.keys.SupportedClientKeyPair;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe;
import side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.RequestKeyPairSignature;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.lib.model.AcmeResources.Account.AccountStatus;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization;
import side.cloud.util.acme.lib.model.AcmeResources.Authorization.AuthorizationStatus;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge;
import side.cloud.util.acme.lib.model.AcmeResources.Challenge.ChallengeStatus;
import side.cloud.util.acme.lib.model.AcmeResources.Order.OrderStatus;
import side.cloud.util.acme.lib.model.challenge.SupportedChallengeType;
import side.cloud.util.acme.server.persistence.nonce.NonceService;
import side.cloud.util.acme.server.persistence.AcmeServerDao;

import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.nest;
import static side.cloud.util.acme.lib.keys.requests.AcmeRequestSerDe.REPLAY_NONCE;
import static side.cloud.util.acme.lib.model.AcmeRequests.AcmeResponse.TypedAcmeResponse.typedPayload;
import static side.cloud.util.acme.lib.model.ProblemDetailAcmeTypes.*;

@Slf4j
@RequiredArgsConstructor
public class AcmeServer {
    private final NonceService nonceService;
    private final AcmeServerDao dao;
    private final Config config;
    private final JsonMapper jsonMapper;

    public RouterFunction<ServerResponse> handler() {
        var route = RouterFunctions.route();

        route.GET(config.getDirectoryPath(),
                ignored -> ServerResponse.ok().body(config.getDirectory().toDirectory()));

        route.HEAD(config.getDirectory().getNewNonce(),
                ignored -> ServerResponse.ok().header(REPLAY_NONCE, nonceService.newNonce()).build());

        if (config.getDirectory().getNewAccount() != null)
            route.add(nest(path(config.getDirectory().getNewAccount()), accountRoute()));

        if (config.getDirectory().getNewOrder() != null)
            route.add(nest(path(config.getDirectory().getNewOrder()), orderRoute()));

        return route.build();
    }

    private RouterFunction<ServerResponse> accountRoute() {
        var accountRouteBuilder = RouterFunctions.route();
        accountRouteBuilder.POST("", r -> ser(newAccount(de(r))));
        accountRouteBuilder.POST("/{accountId}", r -> ser(getAccount(de(r), r.pathVariables())));
        accountRouteBuilder.POST("/{accountId}/orders", r -> ser(getOrders(de(r), r.pathVariables(), r.params())));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}", r -> ser(getOrder(de(r), r.pathVariables())));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/authorizations/{authorizationId}",
                r -> ser(getAuthorization(de(r))));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/authorizations/{authorizationId}/challenges/{challengeId}",
                r -> ser(getChallenge(de(r))));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/finalize", r -> ser(finalizeOrder(de(r))));
        accountRouteBuilder.POST("/{accountId}/orders/{orderId}/cert", r -> ser(getOrderCert(de(r))));
        return accountRouteBuilder.build();
    }

    private RouterFunction<ServerResponse> orderRoute() {
        var orderRouteBuilder = RouterFunctions.route();
        orderRouteBuilder.POST("", r -> ser(this.createOrder(de(r))));
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
            var accountUrl = UriComponentsBuilder.fromUri(config.getDirectory().toDirectory().getNewAccount()).pathSegment(accountId).build().toUri();
            var account = new AcmeResources.Account()
                    .setStatus(AccountStatus.valid)
                    .setContact(newAccount.getContact())
                    .setOrders(UriComponentsBuilder.fromUri(accountUrl).pathSegment("orders").build().toUri());

            // handle terms of service
            var meta = config.getDirectory().toDirectory().getMeta();
            if (meta != null && meta.getTermsOfService() != null) {
                if (!Boolean.TRUE.equals(newAccount.getTermsOfServiceAgreed()))
                    return typedPayload(userActionRequired.getProblemDetail().setDetail("TOS not agreed")).setCode(BAD_REQUEST);
                account.setTermsOfServiceAgreed(true);
            }

            if (config.getExternalAccountBinding().isEnabled()) {
                var eab = newAccount.getExternalAccountBinding();
                if (eab == null)
                    return typedPayload(externalAccountRequired.getProblemDetail()).setCode(UNAUTHORIZED);

                // requirements:
                // eab payload is jwk public key
                // eab using HS256/HS384/HS512
                // eab has keyId of credential id
                // eab has url of newAccount
                // eab signed with credential secret as MAC key

                var parser = ExternalAccountBindings.parse(eab);
                var ea = dao.getExternalAccountById(parser.kid());
                if (ea == null)
                    // eab has keyId of credential id
                    return typedPayload(externalAccountRequired.getProblemDetail()).setCode(UNAUTHORIZED);

                boolean verified = ExternalAccountBindings.verify(parser, ea.decodeMac())
                        .setUrl(config.getDirectory().toDirectory().getNewAccount())
                        .setEnabledAlgorithms(config.getExternalAccountBinding().getMacAlgorithms())
                        .setKeyPair(de.keyPair())
                        .verify();

                if (!verified)
                    return typedPayload(externalAccountRequired.getProblemDetail()).setCode(FORBIDDEN);

                account.setExternalAccountBinding(eab);
            }

            // save
            var newAccountInfo = new AcmeServerDao.ServerAccountEntity()
                    .setId(accountId)
                    .setKeyHash(keyHash)
                    .setAccount(account)
                    .setKeyPair(de.keyPair());
            dao.saveAccount(newAccountInfo);

            // return
            return typedPayload(account).setCode(CREATED).setLocation(accountUrl);
        } else {
            var account = accountInfo.getAccount();

            // update
            account.setContact(newAccount.getContact());
            dao.saveAccount(accountInfo);

            // return
            var accountId = accountInfo.getId();
            var accountUrl = UriComponentsBuilder.fromUri(config.getDirectory().toDirectory().getNewAccount()).path(accountId).build().toUri();
            return typedPayload(account).setCode(OK).setLocation(accountUrl);
        }
    }

    public AcmeResponse getAccount(RequestKeyPairSignature de, Map<String, String> params) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        return typedPayload(dao.getAccountById(params.get("accountId")).getAccount()).setCode(OK).setLocation(de.request().getUrl());
    }

    @SneakyThrows
    public AcmeResponse createOrder(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);
        // var accountByKeyHash = dao.getAccountByKeyHash(keyHash(de.keyPair()));
        // if (accountByKeyHash == null)
        //     return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);
        var accountById = dao.getAccountById(de.signature().getSignatures().getFirst().getHeader().getKeyID().substring(
                de.signature().getSignatures().getFirst().getHeader().getKeyID().lastIndexOf('/') + 1
        ));

        de.signature().getSignatures().getFirst().verify(accountById.getKeyPair().nimbusVerifier());

        var now = Instant.now();
        var newOrder = jsonMapper.convertValue(de.request().getPayload(), AcmeResources.NewOrder.class);

        var validationProblem = validateNewOrder(newOrder, now);
        if (validationProblem != null) return validationProblem;

        if (newOrder.getNotAfter() == null && config.getOrderMaxLifetime() != null)
            newOrder.setNotAfter(now.plus(config.getOrderMaxLifetime()));

        var orderId = nonceService.genNonce();
        var orderUrl = UriComponentsBuilder.fromUri(config.getDirectory().toDirectory().getNewAccount()).pathSegment("{accountId}", "orders", "{orderId}").build(accountById.getId(), orderId);
        // if (authorizations.isEmpty())
        //     return

        var order = new AcmeResources.Order()
                .setStatus(OrderStatus.pending)
                .setExpires(now.plus(config.getOrderDefaultExpiration()))
                .setIdentifiers(newOrder.getIdentifiers())
                .setNotBefore(newOrder.getNotBefore())
                .setNotAfter(newOrder.getNotAfter())
                .setError(null)
                .setFinalize(UriComponentsBuilder.fromUri(orderUrl).pathSegment("finalize").build().toUri());

        var orderEntity = new AcmeServerDao.ServerOrderEntity()
                .setId(orderId)
                .setAccountId(accountById.getId())
                .setOrder(order);
        order.setAuthorizations(genAuthorizations(orderEntity, now, orderUrl));
        dao.saveOrder(orderEntity);
        return typedPayload(order).setCode(CREATED).setLocation(orderUrl);
    }

    private AcmeResponse validateNewOrder(AcmeResources.NewOrder newOrder, Instant now) {
        if (newOrder.getNotBefore() != null && config.getOrderMaxNbfSkew() != null) {
            if (newOrder.getNotBefore().isBefore(now.minus(config.getOrderMaxNbfSkew())))
                return typedPayload(malformed.getProblemDetail().setDetail("new order not before is in the past")).setCode(BAD_REQUEST);
        }

        if (newOrder.getNotAfter() != null && config.getOrderMaxLifetime() != null) {
            if (newOrder.getNotAfter().isAfter(now.plus(config.getOrderMaxLifetime())))
                return typedPayload(malformed.getProblemDetail().setDetail("new order has too long duration")).setCode(BAD_REQUEST);
        }

        if (!newOrder.getIdentifiers().stream().allMatch(i -> config.getOrderSupportedIdentifierTypes().contains(i.getType())))
            return typedPayload(unsupportedIdentifier).setCode(BAD_REQUEST);

        // todo query challenge management object to see what identifiers it can support for verification

        return null;
    }

    private List<URI> genAuthorizations(AcmeServerDao.ServerOrderEntity order, Instant expires, URI orderUrl) {
        // inputs
        List<AcmeIdentifier> identifiers = order.getOrder().getIdentifiers();

        // outputs
        List<Tuple4<String, URI, Authorization, List<Tuple2<String, Challenge>>>> authorizations = new ArrayList<>();

        for (var identifier : identifiers) {
            List<SupportedChallengeType> types;
            if (identifier.getType() == AcmeIdentifier.AcmeIdentifierType.ip) {
                types = SupportedChallengeType.HTTP_TYPES;
            } else if (identifier.getType() == AcmeIdentifier.AcmeIdentifierType.dns) {
                if (identifier.getValue().startsWith(".*"))
                    types = SupportedChallengeType.DNS_TYPES;
                else
                    types = SupportedChallengeType.ALL_TYPES;
            } else {
                return List.of();
            }

            var supportedTypes = types.stream().filter(config.getChallengeSupportedTypes()::contains).toList();

            List<Tuple2<String, Challenge>> challenges = new ArrayList<>();
            for (var type : supportedTypes) {
                var challengeId = nonceService.genNonce();
                var challengeUrl = UriComponentsBuilder.fromUri(orderUrl).pathSegment("challenges", "{challengeId}").build(challengeId);
                var challenge = new Challenge()
                        .setType(type.getRfcName())
                        .setUrl(challengeUrl)
                        .setToken(nonceService.genNonce())
                        .setStatus(ChallengeStatus.pending);
                challenges.add(new Tuple2<>(challengeId, challenge));
            }

            var authorizationId = nonceService.genNonce();
            var authorizationUrl = UriComponentsBuilder.fromUri(orderUrl).pathSegment("authorizations", "{authorizationId}").build(authorizationId);
            var authorization = new Authorization()
                    .setIdentifier(identifier)
                    .setStatus(AuthorizationStatus.pending)
                    .setExpires(expires)
                    .setChallenges(challenges.stream().map(Tuple2::getValue).toList())
                    .setWildcard(identifier.getValue().startsWith("*."));

            authorizations.add(new Tuple4<>(authorizationId, authorizationUrl, authorization, challenges));
        }

        // arrange into order entity
        order.setAuthorizationIds(new ArrayList<>());
        order.setAuthorizations(new HashMap<>());
        order.setAuthorizationChallengeIds(new HashMap<>());
        for (var authorization : authorizations) {
            order.getAuthorizationIds().add(authorization.t1());
            order.getAuthorizations().put(authorization.t1(), authorization.t3());
            order.getAuthorizationChallengeIds().put(authorization.t1(), authorization.t4().stream().map(Tuple2::getKey).toList());
        }

        // return for response
        return authorizations.stream().map(Tuple4::t2).toList();
    }

    public AcmeResponse getOrders(RequestKeyPairSignature de, Map<String, String> params, MultiValueMap<String, String> query) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        var orderIds = dao.listOrdersForAccount(params.get("accountId"), 20, query.getFirst("lastId"));
        var orderUrls = orderIds.stream()
                .map(orderId -> UriComponentsBuilder.fromUri(config.getDirectory().toDirectory().getNewAccount())
                        .pathSegment("{accountId}", "orders", "{orderId}")
                        .build(params.get("accountId"), orderId))
                .toList();

        var ordersUrl = UriComponentsBuilder.fromUri(config.getDirectory().toDirectory().getNewAccount())
                .pathSegment("{accountId}", "orders")
                .build(params.get("accountId"));
        var next = orderUrls.isEmpty() ? null : UriComponentsBuilder.fromUri(ordersUrl)
                .replaceQueryParam("lastId", orderUrls.getLast())
                .build().toUri();
        return typedPayload(new AcmeResources.Orders().setOrders(orderUrls)).setCode(OK).setNext(next);
    }

    public AcmeResponse getOrder(RequestKeyPairSignature de, Map<String, String> params) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

        var problemResponse = validateAccount(de, params.get("accountId"));
        if (problemResponse != null)
            return problemResponse;

        var orderEntity = dao.getOrderById(params.get("orderId"));
        if (orderEntity == null)
            return typedPayload(malformed).setCode(BAD_REQUEST);
        return typedPayload(orderEntity.getOrder());
    }

    public AcmeResponse getAuthorization(RequestKeyPairSignature de) {
        if (validateNonce(de))
            return typedPayload(badNonce.getProblemDetail()).setCode(BAD_REQUEST);

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
        // var accountByKeyHash = dao.getAccountByKeyHash(keyHash(de.keyPair()));
        if (accountById == null/* && accountByKeyHash == null*/)
            return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);

        // if (accountById == null || accountByKeyHash == null) {
        //     if (accountByKeyHash == null)
        //         return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);
        //
        //     // key is right, account is gone
        //     return typedPayload(accountDoesNotExist.getProblemDetail()
        //             .setDetail(accountDoesNotExist.getProblemDetail().getDetail() + " (account has been deleted)")).setCode(NOT_FOUND);
        // }

        // valid
        if (!accountById.getAccount().getStatus().equals(AccountStatus.valid))
            return typedPayload(malformed.getProblemDetail().setDetail("this account is not valid")).setCode(FORBIDDEN);

        // key used is known
        // var actualKeyHash = keyHash(de.keyPair());
        // var expectedKeyHash = accountById.getKeyHash();
        // if (!constantTimeStringEquals(actualKeyHash, expectedKeyHash))
        //     return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);

        try {
            if (!de.withKeyPair(accountById.getKeyPair()).verify())
                return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);
        } catch (Exception e) {
            return typedPayload(unauthorized.getProblemDetail()).setCode(UNAUTHORIZED);
        }

        return null;
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

        @NestedConfigurationProperty
        @NotNull
        BaseUrlDirectory directory;

        @NotNull
        Duration orderDefaultExpiration = Duration.ofDays(1);

        Duration orderMaxNbfSkew;

        Duration orderMaxLifetime;

        @NotEmpty
        Set<AcmeIdentifier.AcmeIdentifierType> orderSupportedIdentifierTypes = Set.of(AcmeIdentifier.AcmeIdentifierType.dns);

        @NotEmpty
        Set<SupportedChallengeType> challengeSupportedTypes = Set.of(
                SupportedChallengeType.ChallengeHTTP01,
                SupportedChallengeType.ChallengeDNS01
        );

        @NotNull
        @Valid
        ExternalAccountBinding externalAccountBinding = new ExternalAccountBinding();

        @Data
        @Accessors(chain = true)
        public static class ExternalAccountBinding {
            boolean enabled;

            @NotEmpty
            Set<MacAlgorithm> macAlgorithms = Set.of(MacAlgorithm.HS256, MacAlgorithm.HS384, MacAlgorithm.HS512);
        }
    }

    record Tuple2<T1, T2>(T1 t1, T2 t2) {
        T1 getKey() {
            return t1;
        }

        T2 getValue() {
            return t2;
        }
    }

    record Tuple4<T1, T2, T3, T4>(T1 t1, T2 t2, T3 t3, T4 t4) {
    }
}
