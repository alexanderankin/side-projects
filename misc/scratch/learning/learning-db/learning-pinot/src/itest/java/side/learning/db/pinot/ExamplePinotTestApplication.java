package side.learning.db.pinot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import side.learning.db.pinot.pinottc.ApachePinotClusterContainer;

import javax.sql.DataSource;

public class ExamplePinotTestApplication {
    static void main() {
        SpringApplication.from(ExamplePinotApplication::main)
                .with(ExamplePinotTestApplication.TestcontainersConfig.class)
                .run();
    }

    @SuppressWarnings("resource")
    @TestConfiguration
    static class TestcontainersConfig {
        @Bean
        Network network() {
            return Network.newNetwork();
        }

        @Bean
        GenericContainer<?> zookeeperContainer(Network network) {
            var zk = new GenericContainer<>(DockerImageName.parse("zookeeper:3.9"))
                    .withNetwork(network)
                    .withNetworkAliases(ApachePinotClusterContainer.ZOOKEEPER_ALIAS)
                    .withExposedPorts(ApachePinotClusterContainer.ZOOKEEPER_PORT);
            zk.start();
            return zk;
        }

        // jdbc:pinot://localhost:9000?brokers=localhost:8099
        @Bean
        ApachePinotClusterContainer pinotContainer(GenericContainer<?> zookeeperContainer, Network network) {
            var pinoContainer = new ApachePinotClusterContainer()
                    .withClusterNetwork(network)
                    .dependsOn(zookeeperContainer);

            pinoContainer.start();
            return pinoContainer;
        }

        @Bean
        DataSource pinotDataSource(ApachePinotClusterContainer pinot) {
            return DataSourceBuilder.create()
                    .url(pinot.getJdbcUrl())
                    // .username(pinot.getUsername())
                    // .password(pinot.getPassword())
                    .build();
        }
    }
}
