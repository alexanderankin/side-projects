const { Environment } = require('./environment');
const { Client } = require('./client');

/**
 * @param {Client} client
 * @param {Logger} logger
 */
function setupProcessCallbacks(client, logger) {
  /**
   * @param {'unhandledRejection' | 'uncaughtException'} name
   */
  function setupCallback(name) {
    process.on(name, (reason, promise) => {
      logger.error(name, reason)
      client.reportError(reason).then()
    })
  }

  setupCallback('unhandledRejection')
  setupCallback('uncaughtException')
}

async function run() {
  let environment = new Environment();
  let logger = new Logger(environment);
  let client = new Client(environment);
  setupProcessCallbacks(client, logger);
}

module.exports = {
  Environment,
  Client,
  run,
  thing: function () {
    return 1;
  }
}
