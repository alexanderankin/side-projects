import testcontainers from "testcontainers";

let { GenericContainer, StartedTestContainer } = testcontainers;

let containerConfig = new GenericContainer("ubuntu:24.04-ufw")
  .withAddedCapabilities("NET_ADMIN")
  .withEntrypoint([ "tail", "-f", "/dev/stdout" ]);

/**
 * @type {StartedTestContainer | null}
 */
export let container = null;

before(async () => {
  container = await containerConfig.start();
});
