package info.ankin.projects.tfe4j.client;

import info.ankin.projects.tfe4j.client.model.Models;
import info.ankin.projects.tfe4j.client.model.Wrappers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TerraformCloudClientTest extends BaseTest {

    TerraformCloudClient terraformCloudClient = new TerraformCloudClient(WebClient.builder().defaultHeaders(h -> h.setBearerAuth(USER_TOKEN)));

    @Disabled
    @SneakyThrows
    @Test
    void test_accountOps_readCurrent() {
        log.debug("{}", terraformCloudClient.accountOps().readCurrentEntity().block());
    }

    @SneakyThrows
    @Test
    void test_accountOps_readCurrent_parsing() {
        var obj = OBJECT_MAPPER.readValue(
                "{\"data\": {\"id\": \"user-\", \"type\": \"users\", \"attributes\":\n" +
                "{\"username\": \"api-org-some-org-token\", \"is-service-account\":\n" +
                "true, \"avatar-url\": \"https://www.gravatar.com/avatar/4a7951a7438df66eb3b30d414cdd359e\",\n" +
                "\"password\": null, \"enterprise-support\": false, \"is-site-admin\":\n" +
                "false, \"is-sso-login\": false, \"two-factor\": {\"enabled\": true, \"verified\":\n" +
                "true}, \"email\": \"api-org-some-org-token@hashicorp.com\", \"unconfirmed-email\":\n" +
                "null, \"has-git-hub-app-token\": false, \"is-confirmed\": true, \"is-sudo\":\n" +
                "false, \"has-linked-hcp\": false, \"permissions\":\n" +
                "{\"can-create-organizations\": false, \"can-view-settings\":\n" +
                "false, \"can-change-email\": true, \"can-change-username\":\n" +
                "true, \"can-manage-user-tokens\": false, \"can-view2fa-settings\":\n" +
                "false, \"can-manage-hcp-account\": false}}, \"relationships\":\n" +
                "{\"authentication-tokens\": {\"links\":\n" +
                "{\"related\": \"/api/v2/users/user-some-user/authentication-tokens\"}}, \"github-app-oauth-tokens\":\n" +
                "{\"links\":\n" +
                "{\"related\": \"/api/v2/users/user-some-user/github-app-oauth-tokens\"}}}, \"links\":\n" +
                "{\"self\": \"/api/v2/users/user-some-user\"}}}\n",
                Models.SingleUser.class);

        log.debug("{}", obj);
    }


    @Disabled
    @SneakyThrows
    @Test
    void test_accountOps_updateCurrent() {
        log.info("{}", terraformCloudClient.accountOps().updateCurrentEntity(new Models.UserUpdate().setUsername("daveankin-btest").setEmail("daveankin+btest@gmail.com").toItem().toSingle()).block());
    }

    @SneakyThrows
    @Test
    void test_accountOps_updateCurrent_parsing() {
        // https://developer.hashicorp.com/terraform/enterprise/api-docs/account#sample-payload
        String sampleRequest = "{\n" +
                               "  \"data\": {\n" +
                               "    \"type\": \"users\",\n" +
                               "    \"attributes\": {\n" +
                               "      \"email\": \"admin@example.com\",\n" +
                               "      \"username\": \"admin\"\n" +
                               "    }\n" +
                               "  }\n" +
                               "}";

        assertEquals(new Models.SingleUserUpdate()
                        .setData(new Models.UserUpdateItem()
                                .setAttributes(new Models.UserUpdate()
                                        .setEmail("admin@example.com")
                                        .setUsername("admin"))),
                OBJECT_MAPPER.readValue(sampleRequest, Models.SingleUserUpdate.class));

        String sampleResponse = "{\n" +
                                "  \"data\": {\n" +
                                "    \"id\": \"user-V3R563qtJNcExAkN\",\n" +
                                "    \"type\": \"users\",\n" +
                                "    \"attributes\": {\n" +
                                "      \"username\": \"admin\",\n" +
                                "      \"is-service-account\": false,\n" +
                                "      \"avatar-url\": \"https://www.gravatar.com/avatar/9babb00091b97b9ce9538c45807fd35f?s=100&d=mm\",\n" +
                                "      \"v2-only\": false,\n" +
                                "      \"is-site-admin\": true,\n" +
                                "      \"is-sso-login\": false,\n" +
                                "      \"email\": \"admin@hashicorp.com\",\n" +
                                "      \"unconfirmed-email\": null,\n" +
                                "      \"permissions\": {\n" +
                                "        \"can-create-organizations\": true,\n" +
                                "        \"can-change-email\": true,\n" +
                                "        \"can-change-username\": true\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"relationships\": {\n" +
                                "      \"authentication-tokens\": {\n" +
                                "        \"links\": {\n" +
                                "          \"related\": \"/api/v2/users/user-V3R563qtJNcExAkN/authentication-tokens\"\n" +
                                "        }\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"links\": {\n" +
                                "      \"self\": \"/api/v2/users/user-V3R563qtJNcExAkN\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "}";

        assertEquals(new Models.SingleUser().setData(new Models.UserItem()
                        .setId("user-V3R563qtJNcExAkN")
                        .setAttributes(new Models.User()
                                .setUsername("admin")
                                .setIsServiceAccount(false)
                                .setAvatarURL("https://www.gravatar.com/avatar/9babb00091b97b9ce9538c45807fd35f?s=100&d=mm")
                                .setV2Only(false)
                                .setIsSiteAdmin(true)
                                .setIsSsoLogin(false)
                                .setEmail("admin@hashicorp.com")
                                .setPermissions(new Models.User.UserPermissions()
                                        .setCreateOrganizations(true)
                                        .setChangeEmail(true)
                                        .setChangeUsername(true)))
                        .setRelationships(new LinkedHashMap<>(Map.of(
                                "authentication-tokens", new Wrappers.Relationship<>()
                                        .setLinks(new Wrappers.Links().setRelated("/api/v2/users/user-V3R563qtJNcExAkN/authentication-tokens"))
                        )))
                        .setLinks(new Wrappers.Links().setSelf("/api/v2/users/user-V3R563qtJNcExAkN"))
                ),
                OBJECT_MAPPER.readValue(sampleResponse, Models.SingleUser.class));
    }
}
