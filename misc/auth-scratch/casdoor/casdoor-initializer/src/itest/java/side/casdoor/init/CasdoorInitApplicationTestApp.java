package side.casdoor.init;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@SpringBootApplication
class CasdoorInitApplicationTestApp {
    static void main(String[] args) {
        SpringApplication.run(CasdoorInitApplicationTestApp.class, args);
    }

    @SuppressWarnings("resource")
    @TestConfiguration
    static class TestcontainersConfig {
        @Bean
        Network network() {
            return Network.newNetwork();
        }

        @Bean
        MySQLContainer<?> mysqlContainer(Network network) {
            return new MySQLContainer<>(DockerImageName.parse("mysql:8.4-oracle"))
                    .withNetworkAliases("mysql_db")
                    .withNetwork(network);
        }

        @Bean
        GenericContainer<?> casdoorContainer(MySQLContainer<?> mysqlContainer, Network network) {
            var casdoor = new GenericContainer<>(DockerImageName.parse("casbin/casdoor:2.196.1"))
                    .withNetwork(network)
                    .dependsOn(mysqlContainer)
                    .withExposedPorts(8000)
                    .withEnv(Map.ofEntries(
                            Map.entry("driverName", "mysql"),
                            Map.entry("dataSourceName", toGoDatasource(mysqlContainer)),
                            Map.entry("dbName", mysqlContainer.getDatabaseName())
                    ));

            casdoor.start();

            // noinspection HttpUrlsUsage
            casdoor.withEnv("casdoorUrl", "http://" + casdoor.getHost() + ":" + casdoor.getMappedPort(8000));

            return casdoor;
        }

        private String toGoDatasource(MySQLContainer<?> mysqlContainer) {
            return mysqlContainer.getUsername() + ":" + mysqlContainer.getPassword() + "@tcp(" + mysqlContainer.getNetworkAliases().getFirst() + ":3306)/";
        }

    }
}
