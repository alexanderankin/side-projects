package side.learning.db.pinot.pinottc;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ApachePinotClusterContainer extends GenericContainer<ApachePinotClusterContainer> {
    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("apachepinot/pinot:1.4.0-21-ms-openjdk");
    public static final DockerImageName DEFAULT_LOCALSTACK_IMAGE_NAME = DockerImageName.parse("localstack/localstack:stable");

    public static final int ZOOKEEPER_PORT = 2181;
    public static final String ZOOKEEPER_ALIAS = "zookeeper";
    public static final int CONTROLLER_PORT = 9000;
    public static final int BROKER_PORT = 8099;
    public static final int SERVER_PORT = 8098;
    public static final int LOCALSTACK_PORT = 4566;
    private static final String CONTROLLER_ALIAS = "pinot-controller";
    private static final String CONTROLLER_COMMAND = "StartController -zkAddress %s:%s".formatted(ZOOKEEPER_ALIAS, ZOOKEEPER_PORT);
    private static final String BROKER_ALIAS = "pinot-broker";
    private static final String BROKER_COMMAND = "StartBroker -zkAddress %s:%s".formatted(ZOOKEEPER_ALIAS, ZOOKEEPER_PORT);
    private static final String SERVER_ALIAS = "pinot-server";
    private static final String SERVER_COMMAND = "StartServer -zkAddress %s:%s".formatted(ZOOKEEPER_ALIAS, ZOOKEEPER_PORT);
    private static final String MINION_ALIAS = "pinot-minion";
    private static final String MINION_COMMAND = "StartMinion -zkAddress %s:%s".formatted(ZOOKEEPER_ALIAS, ZOOKEEPER_PORT);
    private static final String JAVA_OPTS = "JAVA_OPTS";

    private final DockerImageName pinotImageName;
    // from JdbcDatabaseContainer class
    protected Map<String, String> urlParameters = new HashMap<>();
    private boolean enableMinion;
    private boolean enableLocalstack;
    private Network clusterNetwork;
    private DockerImageName localstackImageName = DEFAULT_LOCALSTACK_IMAGE_NAME;
    @Getter
    private GenericContainer<?> pinotServer;
    @Getter
    private GenericContainer<?> pinotBroker;
    @Getter
    private GenericContainer<?> pinotMinion;
    @Getter
    private LocalStackContainer localStack;

    public ApachePinotClusterContainer() {
        this(DEFAULT_IMAGE_NAME);
    }

    public ApachePinotClusterContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        pinotImageName = dockerImageName;
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    }

    //<editor-fold desc="with-ers">
    public ApachePinotClusterContainer withEnableMinion(boolean enableMinion) {
        this.enableMinion = enableMinion;
        return this;
    }

    public ApachePinotClusterContainer withEnableLocalstack(boolean enableLocalstack) {
        this.enableLocalstack = enableLocalstack;
        return this;
    }

    public ApachePinotClusterContainer withClusterNetwork(Network clusterNetwork) {
        this.clusterNetwork = clusterNetwork;
        return this;
    }

    public ApachePinotClusterContainer withLocalstackImageName(DockerImageName localstackImageName) {
        this.localstackImageName = localstackImageName;
        return this;
    }
    //</editor-fold>

    @Override
    protected void configure() {
        if (clusterNetwork == null) {
            clusterNetwork = Network.newNetwork();
        }

        this
                .withNetwork(clusterNetwork)
                .withNetworkAliases(CONTROLLER_ALIAS)
                .withExposedPorts(CONTROLLER_PORT)
                .withEnv(JAVA_OPTS, getJavaOpts("1G", "4G"))
                .withEnv("LOG4J_CONSOLE_LEVEL", "warn")
                .withCommand(CONTROLLER_COMMAND)
                .waitingFor(getWaitStrategy("CONTROLLER"))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(CONTROLLER_ALIAS)));

        this.pinotBroker =
                new GenericContainer<>(pinotImageName)
                        .withNetwork(clusterNetwork)
                        .withNetworkAliases(BROKER_ALIAS)
                        .dependsOn(this)
                        .withExposedPorts(BROKER_PORT)
                        .withEnv(JAVA_OPTS, getJavaOpts("4G", "4G"))
                        .withEnv("LOG4J_CONSOLE_LEVEL", "warn")
                        .withCommand(BROKER_COMMAND)
                        .waitingFor(getWaitStrategy("BROKER"))
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(BROKER_ALIAS)));

        this.pinotServer =
                new GenericContainer<>(pinotImageName)
                        .withNetwork(clusterNetwork)
                        .withNetworkAliases(SERVER_ALIAS)
                        .dependsOn(pinotBroker)
                        .withExposedPorts(SERVER_PORT)
                        .withEnv(JAVA_OPTS, getJavaOpts("4G", "8G"))
                        .withEnv("LOG4J_CONSOLE_LEVEL", "warn")
                        .withCommand(SERVER_COMMAND)
                        .waitingFor(getWaitStrategy("SERVER"))
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(SERVER_ALIAS)));

        if (enableMinion) {
            this.pinotMinion =
                    new GenericContainer<>(pinotImageName)
                            .withNetwork(clusterNetwork)
                            .withNetworkAliases(MINION_ALIAS)
                            .dependsOn(pinotBroker)
                            .withEnv(JAVA_OPTS, getJavaOpts("4G", "8G"))
                            .withEnv("LOG4J_CONSOLE_LEVEL", "warn")
                            .withCommand(MINION_COMMAND)
                            .waitingFor(getWaitStrategy("MINION"))
                            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(MINION_ALIAS)));
        }

        if (enableLocalstack) {
            this.localStack = new LocalStackContainer(DEFAULT_LOCALSTACK_IMAGE_NAME)
                    .withNetwork(clusterNetwork)
                    .withNetworkAliases("localstack")
                    .withEnv("LOCALSTACK_HOST", "localstack")
                    .withExposedPorts(LOCALSTACK_PORT)
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("localstack")))
                    .withServices(LocalStackContainer.Service.S3);
        }
    }

    @SneakyThrows
    @Override
    protected void doStart() {
        super.doStart();
        Startables.deepStart(getAllContainers()).get();
    }

    public List<GenericContainer<?>> getAllContainers() {
        return Stream.of(this, pinotBroker, pinotServer, pinotMinion, localStack).filter(Objects::nonNull).toList();
    }

    private String getJavaOpts(String xms, String xmx) {
        return "-Dplugins.dir=/opt/pinot/plugins -Xms%s -Xmx%s -XX:+UseG1GC -XX:MaxGCPauseMillis=200".formatted(xms, xmx);
    }

    private LogMessageWaitStrategy getWaitStrategy(String service) {
        return Wait.forLogMessage("^(?:.*?)Started Pinot \\[%s\\] instance(?:.*?)$".formatted(service), 1);
    }

    public String getDriverClassName() {
        return "org.apache.pinot.client.PinotDriver";
    }

    public String getJdbcUrl() {
        // return "jdbc:pinot://localhost:%s?brokers=localhost:%s";

        urlParameters.computeIfAbsent("brokers",
                ignored -> pinotBroker.getHost() + ":" + pinotBroker.getMappedPort(BROKER_PORT));

        String additionalUrlParams = constructUrlParameters("?", "&");
        return (
                "jdbc:pinot://" +
                        getHost() +
                        ":" +
                        getMappedPort(CONTROLLER_PORT) +
                        // "/" +
                        // databaseName +
                        additionalUrlParams
        );
    }

    protected String constructUrlParameters(String startCharacter, String delimiter) {
        return constructUrlParameters(startCharacter, delimiter, "");
    }

    protected String constructUrlParameters(String startCharacter, String delimiter, String endCharacter) {
        String urlParameters = "";
        if (!this.urlParameters.isEmpty()) {
            String additionalParameters =
                    this.urlParameters.entrySet().stream().map(Object::toString).collect(Collectors.joining(delimiter));
            urlParameters = startCharacter + additionalParameters + endCharacter;
        }
        return urlParameters;
    }

    public String getUsername() {
        return "";
    }

    public String getPassword() {
        return "";
    }

    protected String getTestQueryString() {
        return "";
    }

    @Override
    public void stop() {
        getAllContainers().forEach(GenericContainer::stop);
    }
}
