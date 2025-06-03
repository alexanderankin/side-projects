package side.oci.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import picocli.AutoComplete;
import picocli.CommandLine;
import side.oci.helpers.model.BastionListItem;
import side.oci.helpers.model.MysqlClusterListItem;
import side.oci.helpers.model.OkeClusterListItem;
import side.oci.helpers.model.SessionItem;
import side.picocli.LogbackVerbosityMixin;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(
        name = "oci-helpers",
        description = "",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                OciHelpersCli.BastionUtils.class,
                OciHelpersCli.Utils.class,
                AutoComplete.GenerateCompletion.class,
        }
)
class OciHelpersCli implements Runnable {
    static final OciHelpers INSTANCE = new OciHelpers();
    static final RetryConfig RETRY_CONFIG = RetryConfig.custom()
            .maxAttempts(10)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnException(ignored -> true)
            .build();
    static final String HOME_SSH_ID_RSA_PUB = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa.pub").toString();

    @CommandLine.Mixin
    LogbackVerbosityMixin verbosityMixin;

    public static void main(String[] args) {
        // args = new String[]{"u", "co", "l"};
        System.exit(new CommandLine(new OciHelpersCli()).execute(args));
    }

    @Override
    public void run() {
        log.info("Verbosity mixin: {}", verbosityMixin);
    }

    @CommandLine.Command(name = "util", aliases = "u", description = "general utilities", subcommands = {
            Utils.Compartments.class,
            Utils.Config.class,
    })
    static class Utils {
        @CommandLine.Command(name = "compartments", aliases = {"co", "comp"})
        static class Compartments {
            @SneakyThrows
            @CommandLine.Command(name = "list", aliases = "l", description = "list compartments in tenancy")
            void print() {
                System.out.println(INSTANCE.mapper.writeValueAsString(INSTANCE.listCompartments()));
            }

            @SneakyThrows
            @CommandLine.Command(name = "get", aliases = "g", description = "get compartment")
            void get(@CommandLine.Option(names = {"-n", "--name"}, required = true) String name) {
                System.out.println(INSTANCE.mapper.writeValueAsString(INSTANCE.getCompartment(name)));
            }
        }

        @CommandLine.Command(name = "config", aliases = "c")
        static class Config {
            @SneakyThrows
            @CommandLine.Command(name = "print", aliases = "p", description = "print configuration")
            void print() {
                var config = INSTANCE.loadLocalConfig();
                log.info("Configuration: {}", config);
                System.out.println(INSTANCE.mapper.writeValueAsString(config));
            }
        }
    }

    @CommandLine.Command(name = "bastion-utils", aliases = "bu", description = "bastion connection utilities")
    static class BastionUtils {
        @CommandLine.Command(name = "forward-kubectl")
        @SneakyThrows
        void forwardKubectlPort(
                @CommandLine.Option(names = {"-c", "--compartment"}, required = true) String compartment,
                @CommandLine.Option(names = {"-b", "--bastion-name"}) String bastionName,
                @CommandLine.Option(names = {"-k", "--cluster-name"}) String clusterName
        ) {
            var c = INSTANCE.getCompartment(compartment);
            BastionListItem bastion = (
                    bastionName != null
                            ? INSTANCE.getCompartmentIdBastion(c.getId(), bastionName)
                            : INSTANCE.getCompartmentIdOnlyBastion(c.getId())
            );
            OkeClusterListItem cluster = (
                    clusterName != null
                            ? INSTANCE.getCompartmentIdOkeCluster(c.getId(), clusterName)
                            : INSTANCE.getCompartmentIdOnlyOkeCluster(c.getId())
            );

            String privateEndpoint = cluster.getEndpoints().getPrivateEndpoint();
            Assert.notNull(privateEndpoint, "Must have private endpoint on cluster to forward to private endpoint");
            var host = privateEndpoint.split(":")[0];
            var port = Integer.parseInt(privateEndpoint.split(":")[1]);

            var session = getAndWaitForSession(bastion, host, port);

            printSession(session, port, host);
            startSession(session, port, host);
        }

        @CommandLine.Command(name = "forward-mysql")
        @SneakyThrows
        void forwardMysqlPort(
                @CommandLine.Option(names = {"-c", "--compartment"}, required = true) String compartment,
                @CommandLine.Option(names = {"-b", "--bastion-name"}) String bastionName,
                @CommandLine.Option(names = {"-d", "-m", "--database-name", "--mysql-database-name"}) String dbName
        ) {
            var c = INSTANCE.getCompartment(compartment);
            BastionListItem bastion = (
                    bastionName != null
                            ? INSTANCE.getCompartmentIdBastion(c.getId(), bastionName)
                            : INSTANCE.getCompartmentIdOnlyBastion(c.getId())
            );
            MysqlClusterListItem cluster = (
                    dbName != null
                            ? INSTANCE.getCompartmentIdMysqlCluster(c.getId(), dbName)
                            : INSTANCE.getCompartmentIdOnlyMysqlCluster(c.getId())
            );

            var host = cluster.getEndpoints().getFirst().getIpAddress();
            var port = cluster.getEndpoints().getFirst().getPort();

            var session = getAndWaitForSession(bastion, host, port);
            printSession(session, port, host);
            startSession(session, port, host);
        }

        @NotNull
        private SessionItem getAndWaitForSession(BastionListItem bastion, String host, int port) throws Exception {
            var session = INSTANCE.createPortForwardingSession(bastion.getId(), HOME_SSH_ID_RSA_PUB, host, port);

            if (session.getSshMetadata() == null) {
                log.debug("ssh metadata is being retried for session {} (bastion {} on host {}/port {})", session.getId(), bastion.getName(), host, port);
                Callable<SessionItem.SshMetadata> sshMetadataSupplier = Retry.of("sshMetadata", RETRY_CONFIG)
                        .decorateCallable(() -> Optional.of(INSTANCE.getSession(session.getId())).map(SessionItem::getSshMetadata).orElseThrow());
                SessionItem.SshMetadata sessionSsh = sshMetadataSupplier.call();
                session.setSshMetadata(sessionSsh);
            }
            return session;
        }

        private void printSession(SessionItem session, int port, String host) throws JsonProcessingException {
            System.err.println(INSTANCE.mapper.writeValueAsString(session.getSshMetadata()));
            System.err.flush();
            System.out.println("ssh -N -L 127.0.0.1:" + port + ":" + host + ":" + port + " -p 22 " + session.getId() + "@host.bastion." + INSTANCE.getOrLoadDefailtProfile().getRegion() + ".oci.oraclecloud.com");
            System.out.flush();
        }

        @SneakyThrows
        private void startSession(SessionItem session, int port, String host) {
            long start;
            int exitCode;
            // todo work out retry strategy
            do {
                start = System.nanoTime();
                exitCode = new ProcessBuilder(("ssh -N -L 127.0.0.1:" + port + ":" + host + ":" + port + " -p 22 " + session.getId() + "@host.bastion." + INSTANCE.getOrLoadDefailtProfile().getRegion() + ".oci.oraclecloud.com").split(" "))
                        .inheritIO()
                        .start()
                        .waitFor();
            } while (exitCode != 0 && Duration.ofNanos(System.nanoTime() - start).compareTo(Duration.ofSeconds(10)) < 0);

            log.info("Session {} (to {}:{}) lasted for {}", session.getId(), host, port, Duration.ofNanos(System.nanoTime() - start));
        }
    }
}
