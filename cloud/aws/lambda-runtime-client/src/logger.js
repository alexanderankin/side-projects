let levels = {
  OFF: 0,
  FATAL: 100,
  ERROR: 200,
  WARN: 300,
  INFO: 400,
  DEBUG: 500,
  TRACE: 600,
}

function noop() {
}

/**
 * @param {import('./index').Environment} environment
 * @returns {Logger}
 * @property {number} level The current logging level.
 * @property {Function} fatal Logs fatal messages if the current level allows.
 * @property {Function} error Logs error messages if the current level allows.
 * @property {Function} warn Logs warning messages if the current level allows.
 * @property {Function} info Logs informational messages if the current level allows.
 * @property {Function} debug Logs debug messages if the current level allows.
 * @property {Function} trace Logs trace messages if the current level allows.
 * @constructor
 */
function Logger(environment) {
  this.environment = environment;
  // default level for development
  this.level = levels.INFO;

  for (let key of Object.keys(levels)) {
    let levelValue = levels[key];
    if (levelValue === 0) continue;
    this[key.toLowerCase()] = levelValue >= this.level ? console.log : noop;
  }
}
