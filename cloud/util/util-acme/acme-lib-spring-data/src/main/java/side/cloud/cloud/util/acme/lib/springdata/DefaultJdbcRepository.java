/*
package side.cloud.cloud.util.acme.lib.springdata;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.JdbcClient;
import side.cloud.util.acme.lib.model.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class DefaultJdbcRepository<T> implements Repository<T>, InitializingBean {
    private final SpringJdbcDefaultRepositoryProperties properties;
    private final JdbcClient jdbcClient;
    private final Function<T, String> keyFunction;
    private Queries queries;

    @Override
    public String newItem(T item, Instant notBefore, Duration expiresIn) {
        var key = keyFunction.apply(item);

        var dbNotBefore = notBefore.minusSeconds(30);
        var expiresAt = notBefore.plus(expiresIn);

        log.debug("inserting item {} valid from {} until {}", key, dbNotBefore, expiresAt);

        jdbcClient.sql(queries.insert)
                .params(
                        key,
                        java.sql.Timestamp.from(dbNotBefore),
                        java.sql.Timestamp.from(expiresAt)
                )
                .update();

        return key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T isItemValid(String key) {
        var now = java.sql.Timestamp.from(Instant.now());

        var isValid = switch (properties.getDatabaseDriver()) {
            case POSTGRESQL, MYSQL, MARIADB ->
                    jdbcClient.sql(queries.exists)
                            .params(key, now, now)
                            .query(Boolean.class)
                            .single();

            case null, default ->
                    jdbcClient.sql(queries.exists)
                            .params(key, now, now)
                            .query(Integer.class)
                            .optional()
                            .isPresent();
        };

        log.debug("checking key {} and it is valid: {}", key, isValid);

        return isValid ? (T) key : null;
    }

    @Override
    // @SuppressWarnings("unchecked")
    public T useItem(String key) {
        var now = java.sql.Timestamp.from(Instant.now());

        var updated = jdbcClient.sql(queries.useNonce)
                .params(now, key, now, now)
                .update();

        var successful = updated == 1;

        log.debug("using key {} was successful: {} (updated: {})", key, successful, updated);

        return successful ? (T) key : null;
    }

    @Override
    public List<T> cleanExpiredItems(Instant now) {
        return List.of();
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
*/
