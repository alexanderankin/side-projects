package side.cloud.util.acme.client;

import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.Startables;
import side.cloud.util.acme.lib.containers.pebble.PebbleAcmeServerTestContainer;
import side.cloud.util.acme.lib.containers.pebble.PebbleChallengeServerTestContainer;

import java.net.URI;
import java.util.Set;

public class PebbleContainers implements AutoCloseable, Startable {
    final PebbleAcmeServerTestContainer pebbleContainer;
    final PebbleChallengeServerTestContainer challengeServerContainer;
    final Network network;

    public PebbleContainers() {
        network = Network.newNetwork();
        challengeServerContainer = new PebbleChallengeServerTestContainer();
        challengeServerContainer.withNetwork(network).withNetworkAliases("pebble-challenge-server");
        pebbleContainer = new PebbleAcmeServerTestContainer();
        pebbleContainer.withNetwork(network).withNetworkAliases("pebble");
        pebbleContainer.withDnsServer("pebble-challenge-server:" + PebbleChallengeServerTestContainer.DNS_SERVER_PORT);
    }

    public URI getPebbleDirectoryUrl() {
        return pebbleContainer.directory();
    }

    @Override
    public void start() {
        Startables.deepStart(getDependencies()).join();
    }

    @Override
    public void stop() {
        network.close();
        pebbleContainer.stop();
        challengeServerContainer.stop();

    }

    @Override
    public Set<Startable> getDependencies() {
        return Set.of(pebbleContainer, challengeServerContainer);
    }

    @Override
    public void close() {
        stop();
    }
}
