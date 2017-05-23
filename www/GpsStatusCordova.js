/**
 * A Cordova plugin for listening a GPS status
 */

var exec = cordova.require('cordova/exec'),
    channel = cordova.require('cordova/channel');

function GpsStatusCordova() {
    this.isStarted = false
}

/**
 * Start GpsStatus listener
 * - success: called on function success
 * - error: called on function error
 */
GpsStatusCordova.prototype.start = function(success, error) {
    if (this.isStarted) {
        console.warn('GpsStatus listener is already started')
    }
    this.isStarted = true
    success = success || function() {};
    error = error || function() {};
    exec(success, error, 'GpsStatusCordova', 'start', []);
};

/**
 * Stop GpsStatus listener
 * Input params:
 *  - success: called on function success
 *  - error: called on function error
 */
GpsStatusCordova.prototype.stop = function(success, error) {
    if (!this.isStarted) {
        console.warn('GpsStatus listener is already stopped')
    }
    this.isStarted = false
    success = success || function() {};
    error = error || function() {};
    exec(success, error, 'GpsStatusCordova', 'stop', []);
};

/**
 * Add listener's callback
 * Input params:
 *  - listener: function which will be called after GPS status will change
 *  - error: called on function error
 */
GpsStatusCordova.prototype.addListener = function(listener, error) {
    exec(listener, error, 'GpsStatusCordova', 'addListener', []);
};

/**
 * Remove all listeners from GPS status listener
 * Input params:
 *  - success: called on function success
 *  - error: called on function error
 */
GpsStatusCordova.prototype.removeListeners = function(success, error) {
    var cbIdRegex = /^GpsStatusCordova.*/;
    success = success || function() {};
    error = error || function() {};

    var overrideSuccess = function(response) {
        var callbacks = window.cordova.callbacks;
        for (var callbackId in callbacks) {
            if (callbacks.hasOwnProperty(callbackId) && callbackId.match(cbIdRegex)) {
                delete callbacks[callbackId];
            }
        }
        success(response);
    }
    exec(overrideSuccess, error, "GpsStatusCordova", "removeListeners", []);
}

module.exports = new GpsStatusCordova();
