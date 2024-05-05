/**
 * @returns {Environment}
 * @constructor
 */
function Environment() {
  if (!(this instanceof Environment)) return new Environment(...arguments);
  this.LAMBDA_TASK_ROOT = process.env.LAMBDA_TASK_ROOT;
  this.AWS_LAMBDA_RUNTIME_API = process.env.AWS_LAMBDA_RUNTIME_API;
  this._HANDLER = process.env._HANDLER;
}

module.exports = { Environment };
