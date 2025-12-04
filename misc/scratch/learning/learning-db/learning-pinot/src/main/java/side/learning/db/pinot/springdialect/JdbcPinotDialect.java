package side.learning.db.pinot.springdialect;

import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.render.SelectRenderContext;
import org.springframework.lang.NonNull;

public class JdbcPinotDialect implements JdbcDialect {
    public static final JdbcPinotDialect INSTANCE = new JdbcPinotDialect();
    private static final LimitClause LIMIT_CLAUSE = new PinotLimitClause();
    private static final PinotSelectRenderContext SELECT_RENDER_CONTEXT = new PinotSelectRenderContext();

    @NonNull
    @Override
    public LimitClause limit() {
        return LIMIT_CLAUSE;
    }

    @NonNull
    @Override
    public LockClause lock() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SelectRenderContext getSelectContext() {
        return SELECT_RENDER_CONTEXT;
    }

    /**
     * copied from postgres
     */
    private static class PinotLimitClause implements LimitClause {
        @NonNull
        @Override
        public String getLimit(long limit) {
            return "LIMIT " + limit;
        }

        @NonNull
        @Override
        public String getOffset(long offset) {
            return "OFFSET " + offset;
        }

        @NonNull
        @Override
        public String getLimitOffset(long limit, long offset) {
            return String.format("LIMIT %d OFFSET %d", limit, offset);
        }

        @NonNull
        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    }

    private static class PinotSelectRenderContext implements SelectRenderContext {
    }
}
