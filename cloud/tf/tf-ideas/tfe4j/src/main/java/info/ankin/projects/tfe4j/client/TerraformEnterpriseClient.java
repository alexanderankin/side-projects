package info.ankin.projects.tfe4j.client;

import info.ankin.projects.tfe4j.client.model.Models;
import info.ankin.projects.tfe4j.client.model.Wrappers;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

public class TerraformEnterpriseClient extends TerraformApiClient {
    public TerraformEnterpriseClient(WebClient.Builder builder) {
        super(builder);
    }

    public TerraformEnterpriseClient(WebClient webClient) {
        super(webClient);
    }

    public AdminOps adminOps() {
        return new AdminOps(this);
    }

    // untested - not public on tf cloud
    @Value
    public static class AdminOps {
        TerraformEnterpriseClient client;

        //<editor-fold desc="organizations">
        public Mono<Models.MultipleOrganizations> listOrganizations() {
            return listOrganizations(null);
        }

        public Mono<Models.MultipleOrganizations> listOrganizations(Models.ListOrganizationsParameters parameters) {
            return client.webClient.get().uri(u -> {
                UriBuilder builder = u.path("/admin/organizations");
                if (parameters != null) builder.queryParams(client.queryString(parameters));
                return builder.build();
            }).retrieve().bodyToMono(Models.MultipleOrganizations.class);
        }

        public Mono<Models.SingleOrganization> getOrganization(String name) {
            return client.webClient.get().uri("/admin/organizations/{name}", name).retrieve().bodyToMono(Models.SingleOrganization.class);
        }

        public Mono<Models.SingleOrganization> patchOrganization(String name, Models.SingleOrganization singleOrganization) {
            return client.webClient.patch().uri("/admin/organizations/{name}", name)
                    .bodyValue(singleOrganization)
                    .retrieve().bodyToMono(Models.SingleOrganization.class);
        }

        public Mono<ResponseEntity<Void>> deleteOrganization(String name) {
            return client.webClient.delete().uri("/admin/organizations/{name}", name).retrieve().toBodilessEntity();
        }

        public Mono<Models.MultipleOrganizations> getOrganizationModuleConsumers(String name) {
            return client.webClient.get().uri("/api/v2/admin/organizations/{name}/relationships/module-consumers", name)
                    .retrieve().bodyToMono(Models.MultipleOrganizations.class);
        }

        public Mono<ResponseEntity<Void>> getOrganizationModuleConsumers(String name, Wrappers.Multiple<?> consumers) {
            return Mono.error(new UnsupportedOperationException("this api is deprecated"));
        }

        public Mono<Models.MultipleOrganizations> getOrganizationProviderConsumers(String name) {
            return client.webClient.get().uri("/api/v2/admin/organizations/{name}/relationships/provider-consumers", name)
                    .retrieve().bodyToMono(Models.MultipleOrganizations.class);
        }
        //</editor-fold>

        //<editor-fold desc="runs">
        public Mono<Models.MultipleRuns> listRuns() {
            return listRuns(new Models.ListRunsParameters());
        }

        public Mono<Models.MultipleRuns> listRuns(Models.ListRunsParameters listRunsParameters) {
            return client.webClient.get().uri(u -> u.path("/admin/runs").queryParams(client.queryString(listRunsParameters)).build()).retrieve().bodyToMono(Models.MultipleRuns.class);
        }

        public Mono<Models.SingleRun> cancelRun(String id) {
            return client.webClient.post().uri("/admin/runs/{id}/actions/force-cancel", id).retrieve().bodyToMono(Models.SingleRun.class);
        }
        //</editor-fold>
    }
}
