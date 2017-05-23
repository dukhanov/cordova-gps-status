# cordova-gps-status
A Cordova plugin for listening a GPS status

## Installation
```
$ cordova plugin add https://www.github.com/dukhanov/cordova-gps-status
```
### Android

 cordova build android

## Plugin API
It has been currently stripped to the minimum needed from a Javascript app.

The following functions are available:

* `GpsStatus.start(success, error)`. Start GPS status listener
  * success: called on function success
  * error: called on function error
* `GpsStatus.stop(success, error)`. Remove GPS status listeners
  * success: called on function success
  * error: called on function error
* `GpsStatus.addListener(listener, error)`. Add callback to GPS status listener
  * listener: function which will be called after GPS status will change
  * error: called on function error
* `GpsStatus.removeListeners(success, error)`. Remove all listeners from GPS status listener
  * success: called on function success
  * error: called on function error

## Sample usage code
```Javascript
function successStart() {
    console.log('GPS status start succeeded');
}

function errorStart(message) {
    console.log('GPS status start failed: ' + message);
}

function onGpsStatusChanged(result) {
    // (timestamp milliseconds) time of the event
    console.log(result.timestamp);
    // (Number) gps event type
    // GPS_EVENT_FIRST_FIX = 3 - Event sent when the GPS system has received its first fix since starting.
    // GPS_EVENT_SATELLITE_STATUS = 4 - Event sent periodically to report GPS satellite status.
    // GPS_EVENT_STARTED = 1 - Event sent when the GPS system has started.
    // GPS_EVENT_STOPPED = 2 - Event sent when the GPS system has stopped.
    console.log(result.gpsEvent);
    // (Number) the time required to receive the first fix since the most recent restart of the GPS engine
    console.log(result.timeToFirstFix);
    // (Array) list of satellites
    console.log(result.satellites);
    // (Number) the azimuth of the satellite in degrees.
    console.log(result.satellites[0].azimuth);
    // (Number) the elevation of the satellite in degrees.
    console.log(result.satellites[0].elevation);
    // (Number) the PRN (pseudo-random number) for the satellite.
    console.log(result.satellites[0].PRN);
    // (Number) the signal to noise ratio for the satellite.
    console.log(result.satellites[0].SNR);
    // (Boolean)  true if the GPS engine has almanac data for the satellite.
    console.log(result.satellites[0].hasAlmanac);
    // (Boolean) true if the GPS engine has ephemeris data for the satellite
    console.log(result.satellites[0].hasEphemeris);
    // (Boolean) true if the satellite was used by the GPS engine when calculating the most recent GPS fix.
    console.log(result.satellites[0].usedInFix);
}

function addListenerError(err){
    console.log('Add GPS status listener error' + err);
}

// Start
GpsStatus.start(successStart, errorStart);

// Subscription to GPS status listener
GpsStatus.addListener(onGpsStatusChanged, addListenerError)
```
