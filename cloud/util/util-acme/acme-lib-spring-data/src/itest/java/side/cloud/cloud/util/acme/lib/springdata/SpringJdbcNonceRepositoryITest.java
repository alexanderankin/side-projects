package side.cloud.cloud.util.acme.lib.springdata;

import org.junit.jupiter.api.Nested;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import side.cloud.util.acme.lib.nonce.NonceRepository;
import side.cloud.util.acme.lib.nonce.NonceRepositoryTestSuite;

import java.security.SecureRandom;

class SpringJdbcNonceRepositoryITest {
    static DataSourceProperties containerToDsp(JdbcDatabaseContainer<?> container) {
        container.start();
        var properties = new DataSourceProperties();
        properties.setUrl(container.getJdbcUrl());
        properties.setUsername(container.getUsername());
        properties.setPassword(container.getPassword());
        return properties;
    }

    static SpringJdbcNonceRepository driverToRepo(DatabaseDriver driver, DataSourceProperties dataSourceProperties) {
        var repo = new SpringJdbcNonceRepository(
                new SpringJdbcNonceRepositoryProperties()
                        .setTableName("nonces")
                        .setDatabaseDriver(driver),
                JdbcClient.create(dataSourceProperties.initializeDataSourceBuilder().build()),
                new SecureRandom()
        );

        repo.afterPropertiesSet();
        return repo;
    }

    @Nested
    class PostgreSQLTest extends NonceRepositoryTestSuite {
        static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

        @Override
        protected NonceRepository createRepository() {
            postgreSQLContainer.start();
            return driverToRepo(DatabaseDriver.POSTGRESQL, containerToDsp(postgreSQLContainer));
        }
    }

    @Nested
    class MySQLTest extends NonceRepositoryTestSuite {
        static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4-oracle");

        @Override
        protected NonceRepository createRepository() {
            mySQLContainer.start();
            return driverToRepo(DatabaseDriver.MYSQL, containerToDsp(mySQLContainer));
        }
    }

    @Nested
    class MariaDBTest extends NonceRepositoryTestSuite {
        static MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>("mariadb:12-noble");

        @Override
        protected NonceRepository createRepository() {
            mariaDBContainer.start();
            return driverToRepo(DatabaseDriver.MARIADB, containerToDsp(mariaDBContainer));
        }
    }

    @Nested
    class SqliteTest extends NonceRepositoryTestSuite {
        static DataSourceProperties sqliteDataSourceProperties;

        static {
            sqliteDataSourceProperties = new DataSourceProperties();
            sqliteDataSourceProperties.setUrl("jdbc:sqlite::memory:");
        }

        @Override
        protected NonceRepository createRepository() {
            return driverToRepo(DatabaseDriver.SQLITE, sqliteDataSourceProperties);
        }
    }
}
