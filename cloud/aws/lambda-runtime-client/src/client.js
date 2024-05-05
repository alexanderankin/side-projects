// noinspection HttpUrlsUsage
// it comes from the docs:
// https://docs.aws.amazon.com/lambda/latest/dg/runtimes-walkthrough.html#runtimes-walkthrough-function

let { Environment } = require('./environment')

/**
 * @param {Environment} environment
 * @returns {Client}
 * @constructor
 */
function Client(environment) {
  if (!(this instanceof Client)) return new Client(new Environment(), ...arguments);
  this.environment = environment;
  this.nextUrl = `http://${self.environment.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/next`
}

/**
 * @param {string} requestId
 */
Client.prototype.responseUrl = function responseUrl(requestId) {
  return `http://${self.environment.AWS_LAMBDA_RUNTIME_API}/2018-06-01/runtime/invocation/${requestId}/response`
}

/**
 * https://docs.aws.amazon.com/lambda/latest/dg/runtimes-walkthrough.html#runtimes-walkthrough-function
 * @returns {Promise<void>}
 */
Client.prototype.serveNextRequest = async function serveNextRequest() {
  // noinspection HttpUrlsUsage
  let eventData = await fetch(this.nextUrl);
  let requestId = eventData.headers.get('Lambda-Runtime-Aws-Request-Id')
  let responseUrl = this.responseUrl(requestId);
  await fetch(responseUrl, { body: JSON.stringify({ hello: 'world' }) });
}

/**
 * Report an error for an invocation if requestId provided,
 * or for initialization, otherwise.
 *
 * @param {Error} error
 * @param {?string} requestId
 * @returns {Promise<void>}
 */
Client.prototype.reportError = async function reportError(error, requestId = null) {
  let apiBaseUrl = this.environment.AWS_LAMBDA_RUNTIME_API;

  let url = requestId !== null
      ? `${apiBaseUrl}/2018-06-01/runtime/invocation/${requestId}/error`
      : `${apiBaseUrl}/2018-06-01/runtime/init/error`;

  await fetch(url, {
    method: 'POST',
    body: {
      errorMessage: '',
      errorType: 'string',
      stackTrace: error.stack.split('\n')
    },
    headers: {
      'Lambda-Runtime-Function-Error-Type': error.name || typeof error
    }
  });
}

module.exports = { Client };
