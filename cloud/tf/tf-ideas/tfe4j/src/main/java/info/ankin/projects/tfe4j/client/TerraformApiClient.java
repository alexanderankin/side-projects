package info.ankin.projects.tfe4j.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import info.ankin.projects.tfe4j.client.model.JsonApiErrors;
import info.ankin.projects.tfe4j.client.model.Models;
import info.ankin.projects.tfe4j.client.model.TerraformClientResponseException;
import lombok.Getter;
import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TerraformApiClient {
    protected static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };
    @Getter
    protected final WebClient webClient;
    protected final ObjectMapper objectMapper = JsonMapper.builder().build();

    public TerraformApiClient(WebClient.Builder builder) {
        this(builder
                .baseUrl("https://app.terraform.io/api/v2")
                .filter(ExchangeFilterFunction.ofRequestProcessor(r -> Mono.just(ClientRequest.from(r)
                        // todo use https://docs.spring.io/spring-hateoas/docs/current/reference/html/#mediatypes.community.json:api
                        // but since it ties to a spring version, Models and Wrappers will do
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json")
                        .build())))
                .filter(ExchangeFilterFunction.ofResponseProcessor(r -> {
                    if (r.statusCode().is4xxClientError() ||
                        r.statusCode().is5xxServerError()) {
                        return r.bodyToMono(JsonApiErrors.class).map(TerraformClientResponseException::new).flatMap(Mono::error);
                    }
                    return Mono.just(r);
                }))
                .build());
    }

    public TerraformApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public AccountOps accountOps() {
        return new AccountOps(this);
    }

    public MultiValueMap<String, String> queryString(Object p) {
        return new MultiValueMapAdapter<>(objectMapper.convertValue(p, MAP_TYPE).entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> Map.entry(e.getKey(), List.of(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Value
    public static class AccountOps {
        TerraformApiClient terraformApiClient;

        //<editor-fold desc="read current">
        @SuppressWarnings("unused")
        public Mono<Models.SingleUser> readCurrent() {
            return terraformApiClient.webClient.get().uri("/account/details").retrieve().bodyToMono(Models.SingleUser.class);
        }

        /**
         * like {@link #readCurrent()} except returns entity with headers
         */
        public Mono<ResponseEntity<Models.SingleUser>> readCurrentEntity() {
            return terraformApiClient.webClient.get().uri("/account/details").retrieve().toEntity(Models.SingleUser.class);
        }
        //</editor-fold>

        //<editor-fold desc="update current">
        public Mono<Models.SingleUser> updateCurrent(Models.SingleUserUpdate update) {
            return terraformApiClient.webClient.patch().uri("/account/update").bodyValue(update).retrieve().bodyToMono(Models.SingleUser.class);
        }

        /**
         * like {@link #updateCurrent(Models.SingleUserUpdate)} except it shows how to wrap in items and singles
         */
        @SuppressWarnings("unused")
        public Mono<Models.SingleUser> updateCurrent(Models.UserUpdate update) {
            return updateCurrent(update.toItem().toSingle());
        }

        /**
         * like {@link #updateCurrent(Models.SingleUserUpdate)} except returns entity with headers
         */
        public Mono<ResponseEntity<Models.SingleUser>> updateCurrentEntity(Models.SingleUserUpdate update) {
            return terraformApiClient.webClient.patch().uri("/account/update").bodyValue(update).retrieve().toEntity(Models.SingleUser.class);
        }
        //</editor-fold>

        @SuppressWarnings("unused")
        public Mono<Models.SingleUser> updateCurrentPassword(Models.UserPasswordUpdate update) {
            return terraformApiClient.webClient.patch().uri("/account/password").bodyValue(update.toItem().toSingle()).retrieve().bodyToMono(Models.SingleUser.class);
        }

        /**
         * like {@link #updateCurrentPassword(Models.UserPasswordUpdate)} except returns entity with headers
         */
        @SuppressWarnings("unused")
        public Mono<ResponseEntity<Models.SingleUser>> updateCurrentPasswordEntity(Models.UserPasswordUpdate update) {
            return terraformApiClient.webClient.patch().uri("/account/password").bodyValue(update.toItem().toSingle()).retrieve().toEntity(Models.SingleUser.class);
        }

    }
}
