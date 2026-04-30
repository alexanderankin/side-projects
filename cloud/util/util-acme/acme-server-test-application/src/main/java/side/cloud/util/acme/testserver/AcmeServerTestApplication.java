package side.cloud.util.acme.testserver;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import side.cloud.util.acme.lib.model.AcmeResources;
import side.cloud.util.acme.server.AcmeServer;
import side.cloud.util.acme.server.BaseUrlDirectory;
import side.cloud.util.acme.server.nonce.InMemoryNonceRepository;
import side.cloud.util.acme.server.nonce.NonceService;
import side.cloud.util.acme.server.persistence.InMemoryAcmeServerDao;

import java.net.URI;
import java.security.SecureRandom;

@SpringBootApplication
public class AcmeServerTestApplication {
    static void main(String[] args) {
        SpringApplication.run(AcmeServerTestApplication.class, args);
    }


    @Bean
    @ConfigurationProperties(prefix = "acme-server.config")
    public AcmeServer.Config acmeServerConfig() {
        return new AcmeServer.Config();
    }


    @Validated
    @Bean
    @ConfigurationProperties(prefix = "acme-server.directory")
    public BaseUrlDirectory baseUrlDirectory(AcmeServer.Config acmeServerConfig,
                                             ServerProperties serverProperties) {
        var externalBaseUrl = acmeServerConfig.getExternalBaseUrl();
        throw new UnsupportedOperationException();
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return new AcmeServer(
                new NonceService(new InMemoryNonceRepository(), new SecureRandom(), new NonceService.Config()),
                new InMemoryAcmeServerDao(),
                acmeServerConfig()
                        .setDirectory(new AcmeResources.Directory()),
                JsonMapper.builder().findAndAddModules().build()
        ).handler();
    }

    @Data
    @Accessors(chain = true)
    @ConfigurationProperties(prefix = "app")
    @Component
    @Validated
    static class AppProperties {
        URI externalBaseUrl;
    }
}
