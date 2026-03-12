package side.cloud.util.acme.lib;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import side.cloud.util.acme.lib.model.AcmeResources;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
public class AcmeServer {
    private final AcmeServerService acmeService;
    private final Config config;
    private final JsonMapper jsonMapper;
    private final RouterFunction<ServerResponse> routerFunction;

    public AcmeServer(AcmeServerService acmeService, Config config, JsonMapper jsonMapper) {
        this.acmeService = acmeService;
        this.config = jsonMapper.convertValue(config, Config.class); // clone because it is mutable
        this.jsonMapper = jsonMapper;
        routerFunction = RouterFunctions.route()
                .GET("/hello", req -> ServerResponse.ok().body("hello"))
                .build();
    }

    @SneakyThrows
    public Optional<ServerResponse> handle(ServerRequest request) {
        var handler = routerFunction.route(request).orElse(null);
        if (handler == null) {
            return Optional.empty();
        }

        return Optional.of(handler.handle(request));
    }

    @Data
    @Accessors(chain = true)
    public static class Config {
        @NotNull
        AcmeResources.Directory directory;
        @NotEmpty
        Set<String> contactSupportedSchemes = Set.of("mailto");
    }
}
