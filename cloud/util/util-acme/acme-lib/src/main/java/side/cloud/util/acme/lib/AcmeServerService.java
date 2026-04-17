package side.cloud.util.acme.lib;

import com.nimbusds.jose.JWSObjectJSON;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;
import side.cloud.util.acme.lib.challenges.KnownChallengeType;
import side.cloud.util.acme.lib.model.AcmeError;
import side.cloud.util.acme.lib.model.AcmeError.UnknownAcmeError.UnknownAcmeErrorDto;
import side.cloud.util.acme.lib.model.AcmeIdentifier;
import side.cloud.util.acme.lib.model.AcmeResources.*;
import side.cloud.util.acme.lib.model.Repository;
import side.cloud.util.acme.lib.model.SupportedClientKeyPair;
import side.cloud.util.acme.lib.nonce.NonceService;

import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class AcmeServerService {
    private final Config config;
    private final Directory directory;
    private final NonceService nonceService;
    private final Repository<AccountKeyPair> accountRepository;
    private final Repository<AccountOrder> orderRepository;
    private final Repository<OrderAuthorization> authorizationRepository;

    public String newNonce() {
        return nonceService.newNonce();
    }

    private String genNonce() {
        return nonceService.generateNonce();
    }

    public ResourceWithId<Account> newAccount(SupportedClientKeyPair sckp, NewAccount newAccount) {
        var account = doNewAccount(newAccount);
        accountRepository.newItem(new AccountKeyPair(account.resource(), sckp.asPublic().serialize()), null, null);
        return account;
    }

    private ResourceWithId<Account> doNewAccount(NewAccount newAccount) {
        var serverTosUrl = directory.getMeta() == null ? null : directory.getMeta().getTermsOfService();
        var serverHasTos = serverTosUrl != null;
        if (serverHasTos && !Boolean.TRUE.equals(newAccount.getTermsOfServiceAgreed())) {
            throw new AcmeError.UnknownAcmeError("this server requires tos agreement",
                    new UnknownAcmeErrorDto()
                            .setDetail(serverTosUrl.toString())
                            .setType(URI.create("https://datatracker.ietf.org/doc/html/rfc8555/#section-7.3")));
        }

        var accountId = nonceService.generateNonce();
        var accountUri = UriComponentsBuilder.fromUri(directory.getNewAccount()).path(accountId).build().toUri();

        var account = new Account()
                .setStatus(Account.AccountStatus.valid)
                .setContact(newAccount.getContact())
                .setTermsOfServiceAgreed(serverHasTos)
                .setExternalAccountBinding(null) // todo fixme
                .setOrders(UriComponentsBuilder.fromUri(directory.getNewAccount()).path(accountId).path("orders").build().toUri());


        return new ResourceWithId<>(account, accountUri);
    }

    private String extractAccountId(URI accountUri) {
        var accountUriString = accountUri.toString();
        var accountsBaseUriString = directory.getNewAccount() + "/";
        if (!accountUriString.startsWith(accountsBaseUriString)) {
            return null;
        }

        return accountUriString.substring(accountsBaseUriString.length());
    }

    @SneakyThrows
    public ResourceWithId<Order> createOrder(URI accountUri, JWSObjectJSON json, NewOrder newOrder) {
        var accountId = extractAccountId(accountUri);
        var account = accountRepository.isItemValid(accountId);

        var verifier = SupportedClientKeyPair.deserialize(account.keyPair()).nimbusVerifier();
        json.getSignatures().getFirst().verify(verifier);

        Validations.validateNewOrder(newOrder, config);

        var orderId = UriComponentsBuilder.fromUri(directory.getNewOrder()).path(genNonce()).build().toUri();

        var authorizations = new ArrayList<ResourceWithId<Authorization>>(newOrder.getIdentifiers().size());
        for (var identifier : newOrder.getIdentifiers()) {
            var authorizationId = genNonce();
            var authorizationUri = UriComponentsBuilder.fromUri(orderId).path("authorizations").path(authorizationId).build().toUri();

            var challengeTypes = validChallengeTypes(identifier);
            var challenges = new ArrayList<Challenge>();
            for (var type : challengeTypes) {
                var challengeId = genNonce();
                var token = type == KnownChallengeType.ChallengeDNSPersist01 ? "" : genNonce();

                var challengeUri = UriComponentsBuilder.fromUri(authorizationUri).path("challenges").path(challengeId).build().toUri();
                var challenge = new Challenge()
                        .setType(type.getRfcName())
                        .setToken(token)
                        .setUrl(challengeUri)
                        .setStatus(Challenge.ChallengeStatus.pending);

                if (type == KnownChallengeType.ChallengeDNSPersist01) {
                    challenge.setIssuerDomainNames(config.getCaaIdentities());
                }

                challenges.add(challenge);
            }

            var authorization = new Authorization()
                    .setIdentifier(identifier)
                    .setStatus(Authorization.AuthorizationStatus.pending)
                    .setExpires(newOrder.getNotAfter())
                    .setChallenges(challenges)
                    .setWildcard(identifier.getValue().startsWith("*."));
            authorizations.add(new ResourceWithId<>(authorization, authorizationUri));
        }

        var order = new Order()
                .setStatus(Order.OrderStatus.pending)
                .setExpires(newOrder.getNotAfter())
                .setIdentifiers(newOrder.getIdentifiers())
                .setNotBefore(newOrder.getNotBefore())
                .setNotAfter(newOrder.getNotAfter())
                .setError(null)
                // .setAuthorizations()
                .setFinalize(UriComponentsBuilder.fromUri(accountUri).path("finalize").build().toUri())
                .setCertificate(null);
        order.setAuthorizations(authorizationUris);

        orderRepository.newItem(
                new AccountOrder(accountUri, order, authorizations),
                order.getNotBefore(),
                Duration.between(order.getNotBefore(), order.getNotAfter()));

        return new ResourceWithId<>(order, orderId);
    }

    private List<KnownChallengeType> validChallengeTypes(AcmeIdentifier identifier) {
        var identifierIsWildcard = identifier.getType() == AcmeIdentifier.AcmeIdentifierType.dns && identifier.getValue().startsWith("*.");
        if (identifierIsWildcard) {
            return KnownChallengeType.DNS_TYPES;
        }

        var identifierIsIp = identifier.getType() == AcmeIdentifier.AcmeIdentifierType.ip;
        if (identifierIsIp) {
            return KnownChallengeType.HTTP_TYPES;
        }

        return KnownChallengeType.ALL_TYPES;
    }

    @SneakyThrows
    public ResourceWithId<Authorization> getAuthorization(URI accountId, JWSObjectJSON json, String authorizationId) {
        var authorization = authorizationRepository.isItemValid(authorizationId);
        if (authorization == null)
            throw new IllegalArgumentException("authorizationId is not valid");
        var account = accountRepository.isItemValid(authorization.accountOrder().accountId().toString());
        if (account == null)
            throw new IllegalArgumentException("account is not valid");

        var actualKid = json.getSignatures().getFirst().getUnprotectedHeader().getKeyID();
        var expectedKid = accountId.toString();
        Assert.isTrue(MessageDigest.isEqual(actualKid.getBytes(), expectedKid.getBytes()), "request is not using the right account key");
        json.getSignatures().getFirst().verify(SupportedClientKeyPair.deserialize(account.keyPair()).nimbusVerifier());

        return authorization.authorization();
    }

    public ResourceWithId<Challenge> getChallenge(URI accountId, JWSObjectJSON json, String authorizationId, String challengeId) {
        var authorization = getAuthorization(accountId, json, authorizationId);
        return authorization.resource().getChallenges().stream()
                .filter(c -> UriComponentsBuilder.fromUri(c.getUrl()).build().getPathSegments().getLast().equals(challengeId))
                .findAny()
                .map(c -> new ResourceWithId<>(c, c.getUrl()))
                .orElseThrow();
    }

    // this is the account
    public record AccountKeyPair(Account account, String keyPair) {
    }

    // this is the order
    public record AccountOrder(URI accountId, Order order, List<ResourceWithId<Authorization>> authorizations) {
    }

    // this is purely for returning things
    public record OrderAuthorization(AccountOrder accountOrder, ResourceWithId<Authorization> authorization) {
        public Authorization getAuthorization() {
            return authorization.resource();
        }

        public URI getAuthorizationId() {
            return authorization.id();
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        Integer orderIdentifiersMaxPerOrder = 2;
        List<String> orderIdentifiersAllowedSuffixes;
        Duration orderMaxLifetime = Duration.ofDays(1);
        @NotNull
        @NotEmpty
        List<@NotBlank String> caaIdentities;
        // Duration orderMaxDrift = Duration.ofHours(1);
    }

    @Slf4j
    static class Validations {
        static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        static void validateNewOrder(NewOrder newOrder, Config config) {
            var set = validator.validate(new ValidatedNewOrder(newOrder, config));
            if (!set.isEmpty())
                throw new ConstraintViolationException("invalid new order per config", set);
        }

        @RequiredArgsConstructor
        static class ValidatedNewOrder {

            private final NewOrder order;
            private final Config config;

            @AssertTrue(message = "Too many identifiers")
            public boolean isWithinMaxIdentifiers() {
                Integer max = config.getOrderIdentifiersMaxPerOrder();
                return max == null || order.getIdentifiers().size() <= max;
            }

            @AssertTrue(message = "Identifiers have invalid suffixes")
            public boolean isHasValidSuffixes() {
                var allowed = config.getOrderIdentifiersAllowedSuffixes();
                if (allowed == null || allowed.isEmpty()) return true;

                return order.getIdentifiers().stream()
                        .allMatch(i -> allowed.stream().anyMatch(i.getValue()::endsWith));
            }

            @AssertTrue(message = "Invalid lifetime")
            public boolean isHasValidLifetime() {
                var maxLifetime = config.getOrderMaxLifetime();
                if (maxLifetime == null) return true;

                var now = Instant.now();
                var notBefore = Objects.requireNonNullElse(order.getNotBefore(), now);
                var notAfter = Objects.requireNonNullElse(order.getNotAfter(), now);

                if (notAfter.isBefore(notBefore)) return false;

                var lifetime = Duration.between(notBefore, notAfter);
                return lifetime.compareTo(maxLifetime) <= 0;
            }
        }
    }
}
