package side.cloud.cloud.util.acme.lib.springdata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "acme-lib.springdata.jdbc-repository")
@Validated
public class SpringJdbcDefaultRepositoryProperties {
    /**
     * table to store the nonces in
     */
    @NotNull
    Map<AcmeTable, String> tableNames;

    /**
     * how many random bytes to use per nonce to form a hex string
     */
    @Min(10)
    @Max(1024)
    int bytesPerNonce = 32;

    /**
     * properties relating to table creation statements
     */
    @Valid
    @NotNull
    Ddl ddl = new Ddl();

    /**
     * if provided, will optimize certain queries
     */
    DatabaseDriver databaseDriver;

    @Data
    @Accessors(chain = true)
    public static class Ddl {
        /**
         * in case of multiple statements, such as index creation, which string to use to split the {@link #createTable} query
         */
        @NotBlank
        String separator = ";";

        /**
         * the default create table query (should be idempotent)
         */
        @NotBlank
        @Pattern(regexp = "__TABLE_NAME__")
        String createTable = """
                CREATE TABLE IF NOT EXISTS __TABLE_NAME__ (
                    nonce        VARCHAR(128) PRIMARY KEY,
                    not_before   TIMESTAMP NOT NULL,
                    expires_at   TIMESTAMP NOT NULL,
                    used_at      TIMESTAMP NULL
                );
                
                CREATE INDEX IF NOT EXISTS idx___TABLE_NAME___expires_at
                    ON __TABLE_NAME__ (expires_at);
                
                CREATE INDEX IF NOT EXISTS idx___TABLE_NAME___not_before
                    ON __TABLE_NAME__ (not_before);
                
                CREATE INDEX IF NOT EXISTS idx___TABLE_NAME___used_at
                    ON __TABLE_NAME__ (used_at);
                """;
    }

    public enum AcmeTable {
        NONCE,
        ACCOUNT,
        ORDER,
        CERTIFICATE,
    }
}
