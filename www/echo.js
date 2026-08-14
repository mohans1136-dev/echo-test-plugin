var exec = require('cordova/exec');

/**
 * Echo Plugin JavaScript Interface
 */
var Echo = {
    /**
     * Synchronously echoes a message string.
     * 
     * @param {string} message - The string message to echo back.
     * @param {function} successCallback - Success callback returning the echoed string.
     * @param {function} errorCallback - Error callback returning an error message.
     */
    echo: function (message, successCallback, errorCallback) {
        if (typeof message !== 'string') {
            if (errorCallback) errorCallback('Invalid argument: message must be a string');
            return;
        }
        exec(successCallback, errorCallback, 'Echo', 'echo', [message]);
    },

    /**
     * Asynchronously echoes a message on a background Kotlin thread with simulated latency.
     * 
     * @param {string} message - The message string to echo.
     * @param {number} delayMs - Delay in milliseconds before returning (default: 1000).
     * @param {function} successCallback - Success callback returning JSON response object.
     * @param {function} errorCallback - Error callback.
     */
    echoAsync: function (message, delayMs, successCallback, errorCallback) {
        var delay = typeof delayMs === 'number' ? delayMs : 1000;
        exec(successCallback, errorCallback, 'Echo', 'echoAsync', [message, delay]);
    },

    /**
     * Promise wrapper for echo, designed for modern OutSystems JavaScript actions.
     * 
     * @param {string} message - The string message to echo back.
     * @returns {Promise<string>} Resolves with the echoed message.
     */
    echoPromise: function (message) {
        return new Promise(function (resolve, reject) {
            if (typeof message !== 'string') {
                reject(new Error('Invalid argument: message must be a string'));
                return;
            }
            exec(resolve, reject, 'Echo', 'echo', [message]);
        });
    }
};

module.exports = Echo;
