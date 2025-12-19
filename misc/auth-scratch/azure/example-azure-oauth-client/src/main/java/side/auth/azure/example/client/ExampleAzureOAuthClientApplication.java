package side.auth.azure.example.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
class ExampleAzureOAuthClientApplication {
    static void main(String[] args) {
        SpringApplication.run(ExampleAzureOAuthClientApplication.class, args);
    }

    @RestController
    @RequestMapping(path = "/api")
    static class ApiController {
        @GetMapping(path = "/whoami")
        String getWhoAmI() {
            return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }

        @PreAuthorize("hasAuthority('InternalUser')")
        @GetMapping(path = "/content/user")
        String getContentForUser() {
            return "user";
        }

        @PreAuthorize("hasAuthority('InternalAdmin')")
        @GetMapping(path = "/content/admin")
        String getContentForAdmin() {
            return "admin";
        }
    }

    @Slf4j
    @Configuration
    @EnableMethodSecurity
    static class AzureCustomOAuthConfiguration {
        @Bean
        public GrantedAuthoritiesMapper userAuthoritiesMapper() {
            return this::mapAuthorities;
        }

        List<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
            List<GrantedAuthority> list = authorities.stream()
                    .map(a -> switch (a) {
                        case OidcUserAuthority oidc -> extractAzureRoles(a, oidc);
                        case null -> List.<GrantedAuthority>of();
                        default -> List.<GrantedAuthority>of(a);
                    })
                    .flatMap(List::stream)
                    .toList();
            log.debug("mapped authorities from {} to {}", authorities, list);
            return list;
        }

        List<GrantedAuthority> extractAzureRoles(GrantedAuthority a,
                                                 OidcUserAuthority oidc) {
            if (!(oidc.getAttributes().get("roles") instanceof List<?> roles)) {
                return List.of(a);
            }
            return Stream.concat(
                    Stream.of(a),
                    roles.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(SimpleGrantedAuthority::new)
            ).toList();
        }
    }

    @Configuration
    static class InitializeDbConfiguration {
        @Bean
        public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("org/springframework/session/jdbc/schema-h2.sql"));
            populator.setContinueOnError(true); // for non-production code

            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(populator);
            initializer.setEnabled(true);
            return initializer;
        }
    }
}
