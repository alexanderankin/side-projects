package side.learning.db.pinot.springdialect;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.dialect.DialectResolver;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.lang.NonNull;

import java.sql.Connection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class PinotAwareDialectProvider extends DialectResolver.DefaultDialectProvider {
    @NonNull
    @Override
    public Optional<Dialect> getDialect(@NonNull JdbcOperations operations) {
        return super.getDialect(operations)
                .or(() -> Objects.requireNonNullElse(operations.execute(this::doInConnection), Optional.empty()));
    }

    @SneakyThrows
    private Optional<Dialect> doInConnection(Connection con) {
        String name = con.getMetaData()
                .getDatabaseProductName()
                .toLowerCase(Locale.ENGLISH);
        if (!name.equals("apache_pinot")) {
            log.warn("did not find apache_pinot, not returning pinot dialect");
            return Optional.empty();
        }
        log.debug("successfully detected apache pinot");
        return Optional.of(JdbcPinotDialect.INSTANCE);
    }
}
