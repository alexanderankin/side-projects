package info.ankin.projects.tfe4j.client;

import org.springframework.web.reactive.function.client.WebClient;

public class TerraformCloudClient extends TerraformApiClient {
    public TerraformCloudClient(WebClient.Builder builder) {
        super(builder);
    }

    public TerraformCloudClient(WebClient webClient) {
        super(webClient);
    }
}
