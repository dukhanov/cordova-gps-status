package com.gpsstatus.cordova;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.location.GpsStatus;
import android.location.GnssStatus;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * GpsStatus Plugin
 */
public class GpsStatusCordova extends CordovaPlugin {

    public static final String TAG = "GpsStatus";
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_ADD_LISTENER = "addListener";
    private static final String ACTION_REMOVE_LISTENERS = "removeListeners";
    private static final String ERROR_GPS_PROVIDER_IS_NOT_AVAILABLE = "GPS provider is not available";
    private static final String ACCESS_FINE_LOCATION_IS_NOT_GRANTED = "ACCESS_FINE_LOCATION permission is not granted";

    private List<CallbackContext> onGpsStatusChangedCallbacks = new ArrayList<CallbackContext>();
    private static GpsStatus.Listener mGpsStatusListener = null;
    private static LocationManager mLocationManager = null;

    @Override
    protected void pluginInitialize() {
        mLocationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "GpsStatus action: " + action);

        boolean result = false;

        if (ACTION_START.equalsIgnoreCase(action)) {
            result = true;
            startGpsListener(callbackContext);
        } else if (ACTION_STOP.equals(action)) {
            result = true;
            stopGpsListener(callbackContext);
        } else if (ACTION_ADD_LISTENER.equals(action)) {
            result = true;
            addGpsListener(callbackContext);
        } else if (ACTION_REMOVE_LISTENERS.equals(action)) {
            result = true;
            removeGpsListeners(callbackContext);
        }

        return result;
    }

    private void startGpsListener(CallbackContext callbackContext) {
        final Boolean gpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(gpsProviderEnabled){
            try{
                mGpsStatusListener = getGpsStatusListener();
                mLocationManager.addGpsStatusListener(mGpsStatusListener);

                // todo implement for SDK above 23
                /*if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    mGpsStatusListener = getGpsStatusListener();
                    mLocationManager.addGpsStatusListener(mGpsStatusListener);
                } else {
                    mLocationManager.registerGnssStatusCallback(getGnssStatusCallback());
                }*/
            }
            catch(SecurityException e){
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
            }
        }
        else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ERROR_GPS_PROVIDER_IS_NOT_AVAILABLE));
        }
    }

    private void stopGpsListener(CallbackContext callbackContext) {
        if(mGpsStatusListener != null && mLocationManager != null){
            mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            mGpsStatusListener = null;
        } else {
            Log.d(TAG, "GPS location already stopped");
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    private void addGpsListener(CallbackContext callbackContext) {
        onGpsStatusChangedCallbacks.add(callbackContext);
    }

    private void removeGpsListeners(CallbackContext callbackContext) {
        JSONArray callbackIds = new JSONArray();
        for (CallbackContext cb:onGpsStatusChangedCallbacks) {
            callbackIds.put(cb.getCallbackId());
        }
        callbackContext.success(callbackIds);
        onGpsStatusChangedCallbacks.clear();
    }

    /**
     * Get new instance of the GPS status listener
     * @return new instance of GpsStatus.Listener
     */
    private GpsStatus.Listener getGpsStatusListener() {
        return new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                Log.d(TAG, "onGpsStatusChanged " + event);

                if (mLocationManager != null && onGpsStatusChangedCallbacks != null && !onGpsStatusChangedCallbacks.isEmpty()) {
                    PluginResult result;
                    if (ActivityCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        result = new PluginResult(PluginResult.Status.ERROR, ACCESS_FINE_LOCATION_IS_NOT_GRANTED);
                    } else {
                        JSONObject gpsStatusJson = gpsStatusToJSON(mLocationManager.getGpsStatus(null), event);
                        result = new PluginResult(PluginResult.Status.OK, gpsStatusJson);
                    }
                    result.setKeepCallback(true);
                    for (CallbackContext cb:onGpsStatusChangedCallbacks) {
                        cb.sendPluginResult(result);
                    }
                }
            }
        };
    }

    /**
     * Get new instance of the GPS status listener
     * @return new instance of GnssStatus.Callback
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private GnssStatus.Callback getGnssStatusCallback() {

        return new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                Log.d(TAG, "GPS started");
            }

            @Override
            public void onStopped() {
                Log.d(TAG, "GPS stopped");
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                Log.d(TAG,"onSatelliteStatusChanged" + status.toString());
            }
        };
    }

    /**
     * Converts GpsStatus into JSON.
     * @param gpsStatus Send a GpsStatus whenever the GPS fires
     * @return JSON representation of the satellite data
     */
    private static JSONObject gpsStatusToJSON(GpsStatus gpsStatus, int gpsEvent){
        final JSONObject json = new JSONObject();

        try {
            json.put("timestamp", Calendar.getInstance().getTimeInMillis());
            json.put("gpsEvent", gpsEvent);
            JSONArray satellites = new JSONArray();
            if(gpsStatus.getSatellites() != null) {
                final int timeToFirstFix = gpsStatus.getTimeToFirstFix();

                for(GpsSatellite sat: gpsStatus.getSatellites() ){
                    final JSONObject satellite = new JSONObject();

                    satellite.put("azimuth", sat.getAzimuth());
                    satellite.put("elevation", sat.getElevation());
                    satellite.put("PRN", sat.getPrn());
                    satellite.put("SNR", sat.getSnr());
                    satellite.put("timeToFirstFix", timeToFirstFix);
                    satellite.put("hasAlmanac", sat.hasAlmanac());
                    satellite.put("hasEphemeris", sat.hasEphemeris());
                    satellite.put("usedInFix", sat.usedInFix());

                    satellites.put(satellite);
                }
            }
            json.put("satellites", satellites);
        }
        catch (JSONException e){
            Log.e(TAG, "gpsStatusToJSON exception", e);
        }

        return json;
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (onGpsStatusChangedCallbacks != null && !onGpsStatusChangedCallbacks.isEmpty()) {
            onGpsStatusChangedCallbacks.clear();
        }
    }
}
