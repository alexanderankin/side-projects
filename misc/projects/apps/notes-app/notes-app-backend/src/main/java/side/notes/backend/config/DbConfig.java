package side.notes.backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.CharJdbcType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.PagedModel;
import side.notes.backend.model.entity.Views;

import java.util.List;

@EnableJpaAuditing
@Configuration
public class DbConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer objectMapperDefVICustomizer() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.mixIn(PagedModel.class, PagedModelMixin.class);
            jacksonObjectMapperBuilder.mixIn(PagedModel.PageMetadata.class, PagedModelMixin.PageMetadataMixin.class);
        };
    }

    /**
     * <pre>
     * spring:
     *   jpa:
     *     properties:
     *       hibernate.dialect_resolvers: side.notes.backend.config.DbConfig$UuidDialectResolver
     * </pre>
     *
     * @see org.hibernate.cfg.AvailableSettings#DIALECT_RESOLVERS
     */
    public static class UuidDialectResolver implements DialectResolver {
        @Override
        public Dialect resolveDialect(DialectResolutionInfo info) {
            String dbName = info.getDatabaseName().toLowerCase();
            return dbName.contains("mysql") || dbName.contains("mariadb") ? new MySQLUuidAsStringDialect() : null; // fallback to default Hibernate resolution
        }
    }

    public static class MySQLUuidAsStringDialect extends MySQLDialect {
        @Override
        public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
            super.contributeTypes(typeContributions, serviceRegistry);
            // Override UUID binary(16) to char(36)
            typeContributions.getTypeConfiguration().getJdbcTypeRegistry().addDescriptor(SqlTypes.UUID, CharJdbcType.INSTANCE);
        }
    }

    public abstract static class PagedModelMixin<T> {
        @JsonView(Views.Default.class)
        public abstract List<T> getContent();

        @JsonView(Views.Default.class)
        public abstract PagedModel.PageMetadata getMetadata();

        public abstract static class PageMetadataMixin {
            @JsonInclude
            @JsonView(Views.Default.class)
            abstract long size();

            @JsonInclude
            @JsonView(Views.Default.class)
            abstract long number();

            @JsonInclude
            @JsonView(Views.Default.class)
            abstract long totalElements();

            @JsonInclude
            @JsonView(Views.Default.class)
            abstract long totalPages();
        }
    }
}
