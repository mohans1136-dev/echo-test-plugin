var exec = require('cordova/exec');

/**
 * Clerk Cordova Plugin JavaScript Interface
 */
var ClerkPlugin = {
    /**
     * Initializes the Clerk SDK with Shared Session Synchronization.
     * 
     * @param {string} publishableKey - Clerk Publishable Key (pk_test_... or pk_live_...)
     * @returns {Promise<Object>}
     */
    initialize: function (publishableKey) {
        return new Promise(function (resolve, reject) {
            if (!publishableKey || typeof publishableKey !== 'string') {
                reject(new Error('Clerk Publishable Key is required.'));
                return;
            }
            exec(resolve, reject, 'ClerkPlugin', 'initialize', [publishableKey]);
        });
    },

    /**
     * Triggers Hosted Auth / Native Sign-In.
     * 
     * @returns {Promise<Object>} User details and Session ID upon success.
     */
    signIn: function () {
        return new Promise(function (resolve, reject) {
            exec(resolve, reject, 'ClerkPlugin', 'signIn', []);
        });
    },

    /**
     * Revokes and signs out the current session across sibling apps.
     * 
     * @returns {Promise<Object>}
     */
    signOut: function () {
        return new Promise(function (resolve, reject) {
            exec(resolve, reject, 'ClerkPlugin', 'signOut', []);
        });
    },

    /**
     * Obtains the active Clerk JWT session token.
     * 
     * @returns {Promise<string>} Active JWT token string.
     */
    getToken: function () {
        return new Promise(function (resolve, reject) {
            exec(
                function (response) {
                    if (typeof response === 'object' && response.token) {
                        resolve(response.token);
                    } else {
                        resolve(response);
                    }
                },
                reject,
                'ClerkPlugin',
                'getToken',
                []
            );
        });
    },

    /**
     * Checks the current authentication status and shared session sync state.
     * 
     * @returns {Promise<Object>} Contains isAuthenticated, userId, sessionId, sharedSessionSynced.
     */
    getSessionState: function () {
        return new Promise(function (resolve, reject) {
            exec(resolve, reject, 'ClerkPlugin', 'getSessionState', []);
        });
    }
};

module.exports = ClerkPlugin;
