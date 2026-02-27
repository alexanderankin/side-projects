package info.ankin.projects.tfe4j.client;

import info.ankin.projects.tfe4j.client.model.Models;
import info.ankin.projects.tfe4j.client.model.TerraformClientResponseException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerraformEnterpriseClientTest extends BaseTest {

    TerraformEnterpriseClient client = new TerraformEnterpriseClient(WebClient.builder()
            .defaultHeaders(h -> h.setBearerAuth(USER_TOKEN))
            .baseUrl("http://localhost:8080")
            .build());

    @SneakyThrows
    @Test
    void test_adminOps_listOrg_parsing() {
        var orgs = OBJECT_MAPPER.readValue(read("/info/ankin/projects/tfe4j/client/model/test_adminOps_listOrg_parsing.response1.json"),
                Models.MultipleOrganizations.class);
        System.out.println(orgs);
    }

    @Disabled
    @SneakyThrows
    @Test
    void test_adminOps_listOrg() {
        try {
            Models.MultipleOrganizations block = client.adminOps().listOrganizations(new Models.ListOrganizationsParameters().setName("abc")).block();
            System.out.println(block);
        } catch (TerraformClientResponseException e) {
            System.out.println(e.getErrors());
        }
    }

    @SneakyThrows
    @Test
    void test_adminOps_getOrg_parsing() {
        System.out.println(OBJECT_MAPPER.readValue(read("/info/ankin/projects/tfe4j/client/model/test_adminOps_getOrg_parsing.response1.json"),
                Models.SingleOrganization.class));
    }

    @SneakyThrows
    @Test
    void test_adminOps_patchOrg_parsing() {
        assertEquals(new Models.Organization()
                        .setGlobalModuleSharing(true)
                        .toItem().toSingle(),
                OBJECT_MAPPER.readValue("{\n" +
                                        "  \"data\": {\n" +
                                        "    \"type\": \"organizations\",\n" +
                                        "    \"attributes\": {\n" +
                                        "      \"global-module-sharing\": true\n" +
                                        "    }\n" +
                                        "  }\n" +
                                        "}\n", Models.SingleOrganization.class));
    }

    @SneakyThrows
    @Test
    void test_adminOps_listOrgModuleConsumers_parsing() {
        System.out.println(OBJECT_MAPPER.readValue(read("/info/ankin/projects/tfe4j/client/model/test_adminOps_listOrgModuleConsumers_parsing.response1.json"),
                Models.MultipleOrganizations.class));
    }

    @SneakyThrows
    @Test
    void test_adminOps_listRuns_parsing() {
        System.out.println(OBJECT_MAPPER.readValue(read("/info/ankin/projects/tfe4j/client/model/test_adminOps_listRuns_parsing.response1.json"),
                Models.MultipleRuns.class));
    }

    @SneakyThrows
    @Test
    void test_adminOps_cancelRun_parsing() {
        System.out.println(OBJECT_MAPPER.readValue(read("/info/ankin/projects/tfe4j/client/model/test_adminOps_cancelRun_parsing.response1.json"),
                Models.SingleRun.class));
    }


}
