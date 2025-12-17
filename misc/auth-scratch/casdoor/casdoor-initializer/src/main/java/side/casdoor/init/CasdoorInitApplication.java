package side.casdoor.init;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.util.*;


@Slf4j
@Command(
        name = "casdoor-init",
        description = "setup casdoor",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                CasdoorInitApplication.Init.class,
                AutoComplete.GenerateCompletion.class,
        }
)
public class CasdoorInitApplication {
    static void main(String[] args) {
        // args = new String[]{"init"};
        ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger(Logger.ROOT_LOGGER_NAME)
                .iteratorForAppenders()
                .forEachRemaining(a -> {
                    if (a instanceof ConsoleAppender<ILoggingEvent> consoleAppender) {
                        consoleAppender.setOutputStream(System.err);
                    }
                });

        System.exit(new CommandLine(CasdoorInitApplication.class).execute(args));
    }

    @Data
    @Accessors(chain = true)
    @Command(name = "init")
    static class Init implements Runnable {
        final static String CASDOOR_DEFAULT_ORGANIZATION = "built-in";
        final static String CASDOOR_ADMIN_USER = "admin";
        final static String CASDOOR_ADMIN_PASSWORD = "123";

        // @Option(names = "--base-url", defaultValue = "http://localhost:8000")
        // URI baseUrl;
        // @Option(names = {"-u", "--username"}, description = "default admin username", defaultValue = "admin")
        // String username;
        // @Option(names = {"-p", "--password"}, description = "default admin password", defaultValue = "123")
        // String password;
        // @Option(names = "--default-organization", defaultValue = "built-in")
        // String defaultOrganization;
        // @Option(names = "--default-certificate-name", defaultValue = "cert-built-in")
        // String defaultCertificateName;
        // @Option(names = {"-nu", "--new-username", "--new-admin-username"}, description = "new admin username", required = true)
        // String newUsername;
        // @Option(names = {"-np", "--new-password", "--new-admin-password"}, description = "new admin password", required = true)
        // String newPassword;
        // @Option(names = {"-no", "--new-org", "--new-org-name"}, description = "new organization name", defaultValue = "New Org")
        // String newOrganization;

        @Option(names = "--casdoor-url")
        String casdoorUrl = "http://localhost:8000";

        @Option(names = "--new-client-name", defaultValue = "sample-client")
        String newClientName = "sample-client";

        @Option(names = "--new-client-owner", defaultValue = CASDOOR_ADMIN_USER)
        String newClientOwner = CASDOOR_ADMIN_USER;

        @Option(names = "--new-client-display-name", defaultValue = "Sample Client")
        String newClientDisplayName = "Sample Client";

        @Option(names = "--new-client-description", defaultValue = "Sample Client")
        String newClientDescription = "Sample Client";

        @Option(names = "--new-client-homepage-url")
        String newClientHomepageUrl;

        @Option(names = "--new-client-client-id", defaultValue = "sample-client-id")
        String newClientClientId = "sample-client-id";

        @Option(names = "--new-client-client-secret", defaultValue = "sample-client-secret")
        String newClientClientSecret = "sample-client-secret";

        @Option(names = "--new-client-redirect-url", defaultValue = "http://localhost:8080,http://localhost:8081", split = ",")
        List<String> newClientRedirectUrls = Arrays.asList("http://localhost:8080,http://localhost:8081".split(","));

        @Option(names = "--new-client-cert-name", defaultValue = "cert-built-in")
        String newClientCertName = "cert-built-in";

        @Option(names = "--new-client-grant-type", defaultValue = "AUTHORIZATION_CODE")
        List<Application.GrantType> newClientGrantTypes = List.of(Application.GrantType.AUTHORIZATION_CODE);

        @Option(names = "--new-user-email", defaultValue = "admin@localhost.local")
        String newUserEmail ="admin@localhost.local";

        @Option(names = "--new-user-password", defaultValue = "new-admin-passwrd")
        String newUserPassword = "new-admin-passwrd";

        @Option(names = "--new-org-owner", defaultValue = CASDOOR_ADMIN_USER)
        String newOrgOwner = CASDOOR_ADMIN_USER;

        @Option(names = "--new-org-name", defaultValue = "new-organization")
        String newOrgName = "new-organization";

        @Option(names = "--new-org-website", defaultValue = "https://example.com")
        String newOrgWebsite = "https://example.com";

        @Option(names = "--new-org-logo")
        String newOrgLogo;

        @Option(names = "--new-org-favicon", defaultValue = "https://example.com/favicon.ico")
        String newOrgFavicon = "https://example.com/favicon.ico";

        @Option(names = "--new-org-password-type", defaultValue = "BCRYPT")
        Organization.PasswordType newOrgPasswordType = Organization.PasswordType.BCRYPT;

        @Option(names = "--new-org-password-salt")
        String newOrgPasswordSalt = UUID.randomUUID().toString();

        @Option(names = "--new-org-password-option", defaultValue = "HAS_AT_LEAST_8,HAS_UPPER_LOWER_AND_DIGIT,HAS_SPECIAL_CHARACTERS", split = ",")
        List<Organization.PasswordValidatorOption> newOrgPasswordOptions = List.of(
                Organization.PasswordValidatorOption.HAS_AT_LEAST_8,
                Organization.PasswordValidatorOption.HAS_UPPER_LOWER_AND_DIGIT,
                Organization.PasswordValidatorOption.HAS_SPECIAL_CHARACTERS
        );

        @Option(names = "--new-org-country-code", defaultValue = "US")
        List<String> newOrgCountryCodes = List.of("US");

        @Option(names = "--new-org-language", defaultValue = "en")
        List<String> newOrgLanguages = List.of("en");

        @Option(names = {"-n", "--dry-run"}, defaultValue = "false")
        boolean dryRun;

        @SneakyThrows
        @Override
        public void run() {
            log.info("here are our settings: {}", this);
            if (dryRun) {
                log.info("Dry run enabled");
                System.out.println(new ObjectMapper().writeValueAsString(this));
                return;
            }

            var newClient = new NewClient()
                    .setName(newClientName)
                    .setDisplayName(newClientDisplayName)
                    .setDescription(newClientDescription)
                    .setHomepageUrl(newClientHomepageUrl)
                    .setClientId(newClientClientId)
                    .setClientSecret(newClientClientSecret)
                    .setRedirectUrls(newClientRedirectUrls)
                    .setCert(newClientCertName)
                    .setGrantTypes(newClientGrantTypes);

            var newUser = new NewUser()
                    .setEmail(newUserEmail)
                    .setPassword(newUserPassword);

            var org = new Organization()
                    .setOwner(newOrgOwner)
                    .setName(newOrgName.replaceAll("\\s+", "_"))
                    .setCreatedTime(Instant.now())
                    .setDisplayName(newOrgName)
                    .setWebsiteUrl(newOrgWebsite)
                    .setLogo(newOrgLogo)
                    .setFavicon(newOrgFavicon)
                    .setPasswordType(newOrgPasswordType)
                    .setPasswordSalt(newOrgPasswordSalt)
                    .setPasswordOptions(List.of(
                            Organization.PasswordValidatorOption.HAS_AT_LEAST_8,
                            Organization.PasswordValidatorOption.HAS_UPPER_LOWER_AND_DIGIT,
                            Organization.PasswordValidatorOption.HAS_SPECIAL_CHARACTERS
                    ))
                    .setCountryCodes(newOrgCountryCodes)
                    .setDefaultAvatar(null)
                    .setTags(List.of("casdoor"))
                    .setLanguages(newOrgLanguages)
                    .setInitScore(100)
                    .setProfilePublic(false)
                    .setUseEmailAsUsername(true)
                    .setThemeData(new Organization.ThemeData()
                            .setThemeType("default")
                            .setColorPrimary("#CC2929")
                            .setBorderRadius(0)
                            .setCompact(true)
                            .setEnabled(true))
                    .setAccountItems(List.of(
                            Organization.AccountItem.of("Organization", true, "Public", "Admin"),
                            Organization.AccountItem.of("ID", true, "Public", "Immutable"),
                            Organization.AccountItem.of("Name", true, "Public", "Admin"),
                            Organization.AccountItem.of("DisplayName", true, "Public", "Self"),
                            Organization.AccountItem.of("Avatar", true, "Public", "Self"),
                            Organization.AccountItem.of("User type", true, "Public", "Admin"),
                            Organization.AccountItem.of("Password", false, "Self", "Self"),
                            Organization.AccountItem.of("Email", true, "Public", "Admin"),
                            Organization.AccountItem.of("Phone", true, "Public", "Self"),
                            Organization.AccountItem.of("Country code", true, "Public", "Self"),
                            Organization.AccountItem.of("Country/Region", true, "Public", "Self"),
                            Organization.AccountItem.of("Location", true, "Public", "Self"),
                            Organization.AccountItem.of("Address", true, "Public", "Self"),
                            Organization.AccountItem.of("Affiliation", true, "Public", "Self"),
                            Organization.AccountItem.of("Title", true, "Public", "Self"),
                            Organization.AccountItem.of("Gender", true, "Public", "Self"),
                            Organization.AccountItem.of("Birthday", true, "Public", "Self"),
                            Organization.AccountItem.of("Score", true, "Public", "Admin"),
                            Organization.AccountItem.of("Signup application", true, "Public", "Admin"),
                            Organization.AccountItem.of("Groups", true, "Public", "Admin"),
                            Organization.AccountItem.of("Roles", true, "Public", "Immutable"),
                            Organization.AccountItem.of("Permissions", true, "Public", "Immutable"),
                            Organization.AccountItem.of("Properties", false, "Admin", "Admin"),
                            Organization.AccountItem.of("Is online", true, "Admin", "Admin"),
                            Organization.AccountItem.of("Is admin", true, "Admin", "Admin"),
                            Organization.AccountItem.of("Is forbidden", true, "Admin", "Admin"),
                            Organization.AccountItem.of("Is deleted", true, "Admin", "Admin"),
                            Organization.AccountItem.of("Multi-factor authentication", true, "Self", "Self"),
                            Organization.AccountItem.of("Managed accounts", true, "Self", "Self"),
                            Organization.AccountItem.of("WebAuthn credentials", true, "Self", "Self"),
                            Organization.AccountItem.of("Tag", false, "Public", "Admin"),
                            Organization.AccountItem.of("Need update password", true, "Public", "Admin")
                    ));

            RestClient restClient = RestClient.builder()
                    .baseUrl(casdoorUrl)
                    .build();

            ResponseEntity<Response> addOrgResponse;
            try {
                addOrgResponse = restClient.post()
                        .uri(u -> u.path("/api/add-organization")
                                .queryParam("username", CASDOOR_DEFAULT_ORGANIZATION + "/" + CASDOOR_ADMIN_USER)
                                .queryParam("password", CASDOOR_ADMIN_PASSWORD)
                                .build())
                        .body(org)
                        .retrieve()
                        // .onStatus(ignored -> true, (ignoredRequest, ignoredResponse) -> {
                        // })
                        .toEntity(Response.class);
            } catch (RestClientResponseException e) {
                throw new RuntimeException("Error (" + e.getStatusCode() + ") in adding organization: " + e.getResponseBodyAsString() + "/" + e.getResponseHeaders());
            }

            if (!validateCasdoorResponse(addOrgResponse)) {
                throw new RuntimeException("Failed to create organization: " + addOrgResponse);
            }

            log.info("added org '{}' => {}", org, addOrgResponse.getBody());

            Application application = new Application()
                    .setOwner(newClientOwner)
                    .setOrganization(CASDOOR_DEFAULT_ORGANIZATION) // this was hard-coded to "built-in"
                    .setName(newClient.getName())
                    .setDisplayName(newClient.getDisplayName())
                    .setDescription(newClient.getDescription())
                    .setHomepageUrl(newClient.getHomepageUrl())
                    .setLogo(null)
                    .setEnablePassword(true)
                    .setEnableSignUp(false)
                    .setEnableSigninSession(false)
                    .setClientId(newClient.getClientId())
                    .setClientSecret(newClient.getClientSecret())
                    .setRedirectUris(newClient.getRedirectUrls())
                    .setCert(newClient.getCert())
                    .setTokenFormat("JWT")
                    .setExpireInHours(12.0)
                    .setRefreshExpireInHours(168.0)
                    .setOrgChoiceMode("None")
                    .setIsShared(true)
                    .setSigninMethods(List.of(new Application.SigninMethod().setName("Password").setDisplayName("Password").setRule("All")))
                    .setSignupItems(List.of(new Application.SignupItem().setName("Username").setVisible(true).setRequired(true).setRule("None")))
                    .setProviders(List.of(new Application.ProviderItem().setName("provider_captcha_default"),
                            new Application.ProviderItem().setName("EmailProvider").setRule("All")))
                    .setGrantTypes(newClient.getGrantTypes())
                    .setCreatedTime(Instant.now())
                    .setFormOffset(2)
                    .setFooterHtml("<style>#footer {display: none;}<style>");

            ResponseEntity<Response> addApplicationResponse;
            try {
                addApplicationResponse = restClient.post()
                        .uri(u -> u.path("/api/add-application")
                                .queryParam("username", CASDOOR_DEFAULT_ORGANIZATION + "/" + CASDOOR_ADMIN_USER)
                                .queryParam("password", CASDOOR_ADMIN_PASSWORD)
                                .build())
                        .body(application)
                        .retrieve()
                        .toEntity(Response.class);
            } catch (RestClientResponseException e) {
                throw new RuntimeException("Error (" + e.getStatusCode() + ") in adding application: " + e.getResponseBodyAsString() + "/" + e.getResponseHeaders());
            }

            if (!validateCasdoorResponse(addApplicationResponse)) {
                throw new RuntimeException("Failed to create application: " + addApplicationResponse);
            }

            log.info("added application '{}' => {}", application, addApplicationResponse.getBody());

            // JsonNode appExtra = addApplicationResponse.getBody().getAdditionalProperties().getOrDefault("data", NullNode.getInstance());

            var certResponse = restClient.get()
                    .uri(u -> u.path("/api/get-cert")
                            .queryParam("username", CASDOOR_DEFAULT_ORGANIZATION + "/" + CASDOOR_ADMIN_USER)
                            .queryParam("password", CASDOOR_ADMIN_PASSWORD)
                            .queryParam("id", CASDOOR_ADMIN_USER + "/" + application.getCert())
                            .build())
                    .retrieve()
                    .toEntity(Response.class);

            var certPublicKey = Objects.requireNonNull(certResponse.getBody(), "Failed to get cert: no body").getAdditionalProperties().getOrDefault("data", NullNode.getInstance()).path("certificate").asText();

            User user = new User()
                    .setOwner(newOrgName)
                    .setName(newUser.getEmail().split("@")[0])
                    .setCreatedTime(Instant.now())
                    .setType("normal-user")
                    .setPassword(newUser.getPassword())
                    .setPasswordSalt(null)
                    .setDisplayName("casdoor-init-admin")
                    .setEmail(newUser.getEmail())
                    .setAdmin(true)
                    .setDeleted(false)
                    .setForbidden(false)
                    .setSignupApplication(application.getName());

            ResponseEntity<Response> addUserResponse;
            try {
                addUserResponse = restClient.post()
                        .uri(u -> u.path("/api/add-user")
                                .queryParam("username", CASDOOR_DEFAULT_ORGANIZATION + "/" + CASDOOR_ADMIN_USER)
                                .queryParam("password", CASDOOR_ADMIN_PASSWORD)
                                .build())
                        .body(user)
                        .retrieve()
                        .toEntity(Response.class);
            } catch (RestClientResponseException e) {
                throw new RuntimeException("error adding user: " + e.getStatusCode() + ": " + e.getResponseBodyAsString() + "/" + e.getResponseHeaders());
            }

            if (!validateCasdoorResponse(addUserResponse)) {
                throw new RuntimeException("Failed to create user: " + addUserResponse);
            }

            log.info("added user '{}' => {}", user, addUserResponse.getBody());

            var output = new OutputData()
                    .setClientId(application.getClientId())
                    .setClientSecret(application.getClientSecret())
                    .setApplication(application.getName())
                    .setOrganization(application.getOrganization())
                    .setCert(certPublicKey);

            log.info("output data: {}", output);
            System.out.println(new ObjectMapper().writeValueAsString(output));
        }

        private boolean validateCasdoorResponse(ResponseEntity<Response> responseEntity) {
            return responseEntity.getStatusCode().is2xxSuccessful() && Optional.ofNullable(responseEntity.getBody()).filter(Response::statusOk).isPresent();
        }
    }

    @Data
    @Accessors(chain = true)
    public static class NewClient {
        String name;
        String displayName;
        String description;
        String homepageUrl;
        String clientId;
        String clientSecret;
        String cert;
        List<String> redirectUrls;
        List<Application.GrantType> grantTypes;
    }

    @Data
    @Accessors(chain = true)
    public static class NewUser {
        String email;
        String password;
        String description;
        String homepageUrl;
        String clientId;
        String clientSecret;
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Organization {
        String owner;
        String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createdTime;
        String displayName;
        String websiteUrl;
        String logo;
        String favicon;
        PasswordType passwordType;
        String passwordSalt;
        List<PasswordValidatorOption> passwordOptions;
        List<String> countryCodes;
        String defaultAvatar;
        String defaultApplication;
        List<String> tags;
        List<String> languages;
        ThemeData themeData;
        String masterPassword;
        int initScore;
        boolean enableSoftDeletion;
        boolean isProfilePublic;
        boolean useEmailAsUsername;
        List<MfaItem> mfaItems;
        List<AccountItem> accountItems;

        public enum PasswordValidatorOption {
            @JsonProperty("AtLeast6") HAS_AT_LEAST_6,
            @JsonProperty("AtLeast8") HAS_AT_LEAST_8,
            @JsonProperty("Aa123") HAS_UPPER_LOWER_AND_DIGIT,
            @JsonProperty("SpecialChar") HAS_SPECIAL_CHARACTERS,
            @JsonProperty("NoRepeat") HAS_NO_REPEAT_CHARACTERS,
        }

        public enum PasswordType {
            @JsonProperty("salt") SHA256_SALT,
            @JsonProperty("sha512-salt") SHA512_SALT,
            @JsonProperty("md5-salt") MD5_SALT,
            @JsonProperty("bcrypt") BCRYPT,
            @JsonProperty("pbkdf2-salt") PBKDF2_SALT,
            @JsonProperty("argon2id") ARGON2ID,
            @JsonProperty("pbkdf2-django") PBKDF2_DJANGO,
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ThemeData {
            String themeType;
            String colorPrimary;
            int borderRadius;
            boolean isCompact;
            boolean isEnabled;
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MfaItem {
            String name;
            String rule;
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AccountItem {
            String name;
            boolean visible;
            String viewRule;
            String modifyRule;

            static AccountItem of(String name, boolean visible, String view, String modify) {
                return new AccountItem().setName(name).setVisible(visible).setViewRule(view).setModifyRule(modify);
            }
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Application {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createdTime;
        private String owner;
        private String name;
        private String displayName;
        private String logo;
        private String title;
        private String favicon;
        private Integer order;
        private String homepageUrl;
        private String description;
        private String organization;
        private String cert;
        private String defaultGroup;
        private String headerHtml;
        private Boolean enablePassword;
        private Boolean enableSignUp;
        private Boolean disableSignin;
        private Boolean enableSigninSession;
        private Boolean enableAutoSignin;
        private Boolean enableCodeSignin;
        private Boolean enableExclusiveSignin;
        private Boolean enableSamlCompress;
        private Boolean enableSamlC14n10;
        private Boolean enableSamlPostBinding;
        private Boolean disableSamlAttributes;
        private Boolean useEmailAsSamlNameId;
        private Boolean enableWebAuthn;
        private Boolean enableLinkWithEmail;
        private String orgChoiceMode;
        private String samlReplyUrl;
        private List<ProviderItem> providers;
        private List<SigninMethod> signinMethods;
        private List<SignupItem> signupItems;
        private List<SigninItem> signinItems;
        private List<GrantType> grantTypes;
        private Organization organizationObj;
        private String certPublicKey;
        private List<String> tags;
        private List<SamlItem> samlAttributes;
        private String samlHashAlgorithm;
        @JsonProperty("isShared")
        private Boolean isShared;
        private String ipRestriction;

        private String clientId;
        private String clientSecret;
        private List<String> redirectUris;
        private String forcedRedirectOrigin;
        private String tokenFormat;
        private String tokenSigningMethod;
        private List<String> tokenFields;
        private List<JwtItem> tokenAttributes;
        private Double expireInHours;
        private Double refreshExpireInHours;
        private String signupUrl;
        private String signinUrl;
        private String forgetUrl;
        private String affiliationUrl;
        private String ipWhitelist;
        private String termsOfUse;
        private String signupHtml;
        private String signinHtml;
        private ThemeData themeData;
        private String footerHtml;
        private String formCss;
        private String formCssMobile;
        private Integer formOffset;
        private String formSideHtml;
        private String formBackgroundUrl;
        private String formBackgroundUrlMobile;

        private Integer failedSigninLimit;
        private Integer failedSigninFrozenTime;
        private Integer codeResendTimeout;

        public enum GrantType {
            @JsonProperty("authorization_code") AUTHORIZATION_CODE,
            @JsonProperty("password") PASSWORD,
            @JsonProperty("client_credentials") CLIENT_CREDENTIALS,
            @JsonProperty("token") TOKEN,
            @JsonProperty("id_token") ID_TOKEN,
            @JsonProperty("urn:ietf:params:oauth:grant-type:device_code") DEVICE_CODE,
            @JsonProperty("refresh_token") REFRESH_TOKEN,
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ProviderItem {
            String name;
            String rule;
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SigninMethod {
            String name;
            String displayName;
            String rule;
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SigninItem {
            String name;
            boolean visible;
            String label;
            String placeholder;
            String rule;
            boolean isCustom;
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SamlItem {
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class JwtItem {
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ThemeData {
        }

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SignupItem {
            String label;
            String name;
            String placeholder;
            boolean prompted;
            String regex;
            boolean required;
            String rule;
            boolean visible;
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class User {
        String owner;
        String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createdTime;
        String type;
        String password;
        String passwordSalt;
        String displayName;
        String email;

        boolean isAdmin;
        boolean isDeleted;
        boolean isForbidden;

        String signupApplication;
    }

    @Data
    @Accessors(chain = true)
    public static class Response {
        @JsonAnySetter
        @JsonAnyGetter
        @JsonIgnore
        Map<String, JsonNode> additionalProperties;

        String status;
        String msg;
        String sub;
        String name;

        @JsonIgnore
        public boolean statusOk() {
            return "ok".equals(status);
        }
    }


    @Data
    @Accessors(chain = true)
    public static class OutputData {
        @JsonProperty("client_id")
        String clientId;
        @JsonProperty("client_secret")
        String clientSecret;
        String application;
        String organization;
        String cert;
    }
}
