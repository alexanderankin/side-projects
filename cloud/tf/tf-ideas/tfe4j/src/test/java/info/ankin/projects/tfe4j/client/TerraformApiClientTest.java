package info.ankin.projects.tfe4j.client;

import info.ankin.projects.tfe4j.client.model.Models;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerraformApiClientTest {

    @Test
    void test_queryString() {
        var p = new Models.ListOrganizationsParameters()
                .setName("name")
                .setQuery("my-project");
        p.setPage(0).setSize(10);

        MultiValueMap<String, String> queryParams = new TerraformApiClient((WebClient) null) {
        }.queryString(p);

        assertEquals(new MultiValueMapAdapter<>(Map.ofEntries(
                Map.entry("q[name]", List.of("name")),
                Map.entry("q", List.of("my-project")),
                Map.entry("page[number]", List.of("0")),
                Map.entry("page[size]", List.of("10"))
        )), queryParams);

        assertEquals("page%5Bnumber%5D=0&q=my-project&q%5Bname%5D=name&page%5Bsize%5D=10",
                UriComponentsBuilder.fromPath("/").queryParams(queryParams).build(Map.of()).getRawQuery());
    }

}
