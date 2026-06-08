package side.cloud.util.acme.testserver;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import side.cloud.util.acme.server.AcmeServer;
import side.cloud.util.acme.server.nonce.InMemoryNonceRepository;
import side.cloud.util.acme.server.nonce.NonceService;
import side.cloud.util.acme.server.persistence.InMemoryAcmeServerDao;

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


    @Bean
    public RouterFunction<ServerResponse> routerFunction(AcmeServer.Config acmeServerConfig) {
        return new AcmeServer(
                new NonceService(new InMemoryNonceRepository(), new SecureRandom(), new NonceService.Config()),
                new InMemoryAcmeServerDao(),
                acmeServerConfig,
                JsonMapper.builder().findAndAddModules().build()
        ).handler();
    }
}
