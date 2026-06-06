package side.cloud.util.acme.server.persistence.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import side.cloud.util.acme.server.persistence.nonce.NonceRepository;

import java.sql.Timestamp;
import java.time.Instant;

@RequiredArgsConstructor
public class JdbcNonceRepository implements NonceRepository {
    private final String tablePrefix;
    private final JdbcClient jdbcClient;

    @Override
    public void createNonce(String nonce, Instant notBefore, Instant notAfter) {
        try {
            jdbcClient.sql("""
                            INSERT INTO __TABLE_PREFIX___nonce (nonce, not_before, not_after, used)
                            VALUES (:nonce, :notBefore, :notAfter, false)
                            """.replace("__TABLE_PREFIX__", tablePrefix))
                    .param("nonce", nonce)
                    .param("notBefore", Timestamp.from(notBefore))
                    .param("notAfter", Timestamp.from(notAfter))
                    .update();
        } catch (DuplicateKeyException ignored) {
            // Nonces should be unique. Treat duplicate creation as idempotent.
        }
    }

    @Override
    public boolean checkNonce(String nonce, Instant now) {
        return jdbcClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM __TABLE_PREFIX___nonce
                            WHERE nonce = :nonce
                              AND used = false
                              AND not_before <= :now
                              AND not_after >= :now
                        )
                        """.replace("__TABLE_PREFIX__", tablePrefix))
                .param("nonce", nonce)
                .param("now", Timestamp.from(now))
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean useNonce(String nonce, Instant now) {
        int updated = jdbcClient.sql("""
                        UPDATE __TABLE_PREFIX___nonce
                        SET used = true,
                            used_at = :now
                        WHERE nonce = :nonce
                          AND used = false
                          AND not_before <= :now
                          AND not_after >= :now
                        """.replace("__TABLE_PREFIX__", tablePrefix))
                .param("nonce", nonce)
                .param("now", Timestamp.from(now))
                .update();

        return updated == 1;
    }

    @Override
    public void cleanNonces(Instant now) {
        jdbcClient.sql("""
                        DELETE FROM __TABLE_PREFIX___nonce
                        WHERE not_after < :now
                           OR used = true
                        """.replace("__TABLE_PREFIX__", tablePrefix))
                .param("now", Timestamp.from(now))
                .update();
    }
}
