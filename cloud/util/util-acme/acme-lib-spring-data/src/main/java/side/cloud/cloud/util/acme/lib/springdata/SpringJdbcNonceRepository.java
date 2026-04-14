package side.cloud.cloud.util.acme.lib.springdata;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.JdbcClient;
import side.cloud.util.acme.lib.nonce.NonceRepository;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class SpringJdbcNonceRepository implements NonceRepository, InitializingBean {
    private final SpringJdbcNonceRepositoryProperties properties;
    private final JdbcClient jdbcClient;
    private final SecureRandom secureRandom;
    private Queries queries;

    @Override
    public String newItem(String ignored, Instant notBefore, Duration expiresIn) {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        var newNonce = HexFormat.of().formatHex(bytes);
        var dbNotBefore = notBefore.minusSeconds(30);
        var expiresAt = notBefore.plus(expiresIn);

        log.debug("inserting nonce {} valid from {} util {}", newNonce, dbNotBefore, expiresAt);
        jdbcClient.sql(queries.insert)
                .params(newNonce, Timestamp.from(dbNotBefore), Timestamp.from(expiresAt))
                .update();

        return newNonce;
    }

    @Override
    public String isItemValid(String nonce) {
        var now = Timestamp.from(Instant.now());

        var isValid = switch (properties.getDatabaseDriver()) {
            case POSTGRESQL, MYSQL, MARIADB ->
                    jdbcClient.sql(queries.exists).params(nonce, now, now).query(Boolean.class).single();
            case null, default ->
                    jdbcClient.sql(queries.exists).params(nonce, now, now).query(Integer.class).optional().isPresent();
        };
        log.debug("checking nonce {} and it is valid: {}", nonce, isValid);
        return isValid ? nonce : null;
    }

    @Override
    public String useItem(String nonce) {
        var now = Timestamp.from(Instant.now());
        var updated = jdbcClient.sql(queries.useNonce).params(now, nonce, now, now).update();
        var successful = updated == 1;
        log.debug("using nonce {} was successful: {} (updated: {})", nonce, successful, updated);
        return successful ? nonce : null;
    }

    @Override
    public List<String> cleanExpiredItems(Instant instantNow) {
        log.debug("cleaning expired and used nonces");
        var now = Timestamp.from(instantNow);
        return switch (properties.getDatabaseDriver()) {
            case POSTGRESQL, SQLITE -> jdbcClient.sql(queries.cleanupReturning)
                    .param(now)
                    .query(String.class)
                    .list();

            // MySQL/MariaDB don't support RETURNING in same way → fallback
            case null, default -> {
                var nonces = jdbcClient.sql(queries.cleanupSelect)
                        .param(now)
                        .query(String.class)
                        .list();

                if (!nonces.isEmpty()) {
                    jdbcClient.sql(queries.cleanup).param(now).update();
                }

                yield nonces;
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        var ddl = properties.getDdl();
        var ddlRawDefault = ddl.getCreateTable();
        var ddlQueries = Arrays.stream(ddlRawDefault.split(ddl.getSeparator()))
                .map(q -> q.replaceAll("__TABLE_NAME__", properties.getTableName()))
                .filter(Predicate.not(String::isBlank))
                .map(String::strip)
                .toList();

        var isMysql = properties.getDatabaseDriver() == DatabaseDriver.MYSQL;
        if (isMysql) {
            var noIfNotExistsDdlQueries = ddlQueries.stream()
                    .map(s -> s.replace("INDEX IF NOT EXISTS", "INDEX"))
                    .toList();

            for (var query : noIfNotExistsDdlQueries) {
                try {
                    jdbcClient.sql(query).update();
                } catch (BadSqlGrammarException badSql) {
                    var sqlException = badSql.getSQLException();
                    var sqlState = sqlException == null ? null : sqlException.getSQLState();
                    if (Objects.equals(sqlState, "42000") &&
                            sqlException.getErrorCode() == 1061) {
                        log.debug("created index even though it existed on mysql");
                    } else {
                        throw badSql;
                    }
                }
            }
        } else {
            for (var query : ddlQueries) {
                jdbcClient.sql(query).update();

            }
        }

        if (queries == null) {
            queries = Queries.builder()
                    .insert("INSERT INTO %s (nonce, not_before, expires_at) VALUES (?, ?, ?)".formatted(properties.getTableName()))
                    .exists(switch (properties.getDatabaseDriver()) {
                        case POSTGRESQL, MYSQL, MARIADB -> Queries.EXISTS_OPT.formatted(properties.getTableName());
                        case null, default -> Queries.EXISTS_GENERIC.formatted(properties.getTableName());
                    })
                    .useNonce(Queries.USE_NONCE.formatted(properties.getTableName()))
                    .cleanup("DELETE FROM %s WHERE expires_at <= ? OR used_at IS NOT NULL"
                            .formatted(properties.getTableName()))
                    .cleanupSelect("SELECT nonce FROM %s WHERE expires_at <= ? OR used_at IS NOT NULL"
                            .formatted(properties.getTableName()))
                    .cleanupReturning("DELETE FROM %s WHERE expires_at <= ? OR used_at IS NOT NULL RETURNING nonce"
                            .formatted(properties.getTableName()))
                    .build();
        }
    }

    @Value
    @Builder(toBuilder = true)
    private static class Queries {
        private static final String EXISTS_OPT = """
                SELECT EXISTS (
                    SELECT 1
                    FROM %s
                    WHERE nonce = ?
                      AND used_at IS NULL
                      AND not_before <= ?
                      AND expires_at > ?
                )
                """.replaceAll("\n", " ").replaceAll(" {2,10}", " ");

        private static final String EXISTS_GENERIC = """
                SELECT 1
                FROM %s
                WHERE nonce = ?
                  AND used_at IS NULL
                  AND not_before <= ?
                  AND expires_at > ?
                LIMIT 1
                """.replaceAll("\n", " ").replaceAll(" {2,10}", " ");


        private static final String USE_NONCE = """
                UPDATE %s
                SET used_at = ?
                WHERE nonce = ?
                  AND used_at IS NULL
                  AND not_before <= ?
                  AND expires_at > ?
                """.replaceAll("\n", " ").replaceAll(" {2,10}", " ");

        String insert;
        String exists;
        String useNonce;
        String cleanup;
        String cleanupSelect;
        String cleanupReturning;
    }
}
