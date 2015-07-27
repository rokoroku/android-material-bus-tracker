package kr.rokoroku.mbus.core;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.util.ViewUtils;

/**
 * Created by rok on 2015. 6. 28..
 */
public class LocationClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int CONNECTION_TIMEOUT_DELAY = 5000;
    private static final int REQUEST_TIMEOUT_DELAY = 3000;

    private static Location mLastKnownLocation;
    private static GoogleApiClient sGoogleApiClient;
    private static LocationManager sDeviceLocationManager;

    private Context mContext;
    private LocationCallback mGoogleApiLocationCallback;
    private LocationListener mDeviceLocationListener;

    private Timer mTimer;
    private Listener mListener;
    private boolean isLocationRequested;

    private LocationClient(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        if (isGooglePlayServiceAvailable()) {
            buildGoogleApiClient(context);
        } else {
            buildDeviceLocationManager(context);
        }
    }

    public LocationClient(Context context) {
        this(context, null);
    }

    protected synchronized void buildDeviceLocationManager(Context context) {
        if (sDeviceLocationManager == null) {
            // Getting LocationManager object from System Service LOCATION_SERVICE
            sDeviceLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Getting the best location provider
            String provider = getBestLocationProvider(sDeviceLocationManager);

            // Getting Current Location
            Location lastKnownLocation = sDeviceLocationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                mLastKnownLocation = lastKnownLocation;
            }

        }

        // Build listener
        if (mDeviceLocationListener == null) {
            mDeviceLocationListener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    handleLocationUpdate(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    if (status != LocationProvider.AVAILABLE) {
                        if (mListener != null) {
                            mListener.onError("TEMPORARILY_UNAVAILABLE", null);
                        }
                        stop();
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d("ProviderEnabled", "provider: " + provider);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    if (mListener != null) {
                        mListener.onError("PROVIDER_DISABLED", null);
                    }
                    stop();
                }
            };
        }
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        if (sGoogleApiClient == null) {
            sGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // build listener
        if (mGoogleApiLocationCallback == null) {
            mGoogleApiLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    Location location = result.getLastLocation();
                    if (location != null) {
                        handleLocationUpdate(location);
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (!locationAvailability.isLocationAvailable()) {
                        if (mListener != null) {
                            mListener.onError("TEMPORARILY_UNAVAILABLE", null);
                        }
                        stop();
                    }
                }
            };
        }
    }

    protected String getBestLocationProvider(LocationManager locationManager) {
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        return locationManager.getBestProvider(criteria, true);
    }

    public boolean isGooglePlayServiceAvailable() {
        // Getting Google Play availability status
        int availablilty = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        return availablilty == ConnectionResult.SUCCESS;
    }

    public void start(boolean force) {

        boolean shouldUpdate = mLastKnownLocation == null ||
                (System.currentTimeMillis() - mLastKnownLocation.getTime() > 5 * 60 * 1000);

        if (force || shouldUpdate) {
            if (isLocationEnabled(mContext)) {
                if (sGoogleApiClient != null) {
                    if (sGoogleApiClient.isConnected()) {
                        // Request location update
                        LocationRequest locationRequest = new LocationRequest()
                                .setNumUpdates(1)
                                .setMaxWaitTime(CONNECTION_TIMEOUT_DELAY)
                                .setExpirationTime(CONNECTION_TIMEOUT_DELAY)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        LocationServices.FusedLocationApi.requestLocationUpdates(sGoogleApiClient,
                                locationRequest, mGoogleApiLocationCallback, Looper.getMainLooper());

                        isLocationRequested = true;
                        startTimeoutTimer(REQUEST_TIMEOUT_DELAY);

                    } else {
                        sGoogleApiClient.connect();
                        startTimeoutTimer(CONNECTION_TIMEOUT_DELAY);
                    }

                } else if (sDeviceLocationManager != null) {
                    String provider = getBestLocationProvider(sDeviceLocationManager);
                    sDeviceLocationManager.requestSingleUpdate(provider,
                            mDeviceLocationListener, Looper.getMainLooper());

                    isLocationRequested = true;
                    startTimeoutTimer(REQUEST_TIMEOUT_DELAY);

                } else if (mListener != null) {
                    mListener.onError("LOCATION_UNAVAILABLE", null);
                }
            } else {
                if (mListener != null) {
                    mListener.onError("LOCATION_DISABLED", null);
                }
                stop();
            }

        } else {
            isLocationRequested = true;
            handleLocationUpdate(mLastKnownLocation);
        }
    }

    private synchronized void startTimeoutTimer(int delay) {
        if (mTimer != null) mTimer.cancel();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isLocationRequested) {
                    if (mListener != null) {
                        if (mLastKnownLocation != null) {
                            ViewUtils.runOnUiThread(() -> mListener.onLocationUpdate(mLastKnownLocation));
                        } else {
                            ViewUtils.runOnUiThread(() -> mListener.onError("REQUEST_TIMEOUT", null));
                        }
                    }
                    isLocationRequested = false;
                    mTimer = null;
                }
            }
        }, delay);
    }

    private synchronized void cancelTimeoutTimer() {
        if (mTimer != null) mTimer.cancel();
        mTimer = null;
    }

    public void stop() {
        cancelTimeoutTimer();
        if (sGoogleApiClient != null) {
            if (sGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(sGoogleApiClient, mGoogleApiLocationCallback);
                sGoogleApiClient = null;
            }
        }
        if (sDeviceLocationManager != null) {
            sDeviceLocationManager = null;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        cancelTimeoutTimer();

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(sGoogleApiClient);
        if (lastLocation != null) {
            mLastKnownLocation = lastLocation;
        }
        start(false);
    }

    @Override
    public void onConnectionSuspended(int errorCode) {
        cancelTimeoutTimer();

        String failReason;
        switch (errorCode) {
            case CAUSE_SERVICE_DISCONNECTED:
                failReason = "SERVICE_DISCONNECTED";
                break;

            case CAUSE_NETWORK_LOST:
                failReason = "NETWORK_LOST";
                break;

            default:
                failReason = "UNKNOWN_ERROR_CODE(" + errorCode + ")";
                break;
        }
        if (mListener != null) {
            mListener.onError(failReason, null);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        cancelTimeoutTimer();

        String failReason;
        int errorCode = connectionResult.getErrorCode();
        switch (errorCode) {
            case 0:
                failReason = "SUCCESS";
                break;
            case 1:
                failReason = "SERVICE_MISSING";
                break;
            case 2:
                failReason = "SERVICE_VERSION_UPDATE_REQUIRED";
                break;
            case 3:
                failReason = "SERVICE_DISABLED";
                break;
            case 4:
                failReason = "SIGN_IN_REQUIRED";
                break;
            case 5:
                failReason = "INVALID_ACCOUNT";
                break;
            case 6:
                failReason = "RESOLUTION_REQUIRED";
                break;
            case 7:
                failReason = "NETWORK_ERROR";
                break;
            case 8:
                failReason = "INTERNAL_ERROR";
                break;
            case 9:
                failReason = "SERVICE_INVALID";
                break;
            case 10:
                failReason = "DEVELOPER_ERROR";
                break;
            case 11:
                failReason = "LICENSE_CHECK_FAILED";
                break;
            case 12:
            default:
                failReason = "UNKNOWN_ERROR_CODE(" + errorCode + ")";
                break;
            case 13:
                failReason = "CANCELED";
                break;
            case 14:
                failReason = "TIMEOUT";
                break;
            case 15:
                failReason = "INTERRUPTED";
                break;
            case 16:
                failReason = "API_UNAVAILABLE";
                break;
            case 17:
                failReason = "SIGN_IN_FAILED";
                break;
            case 18:
                failReason = "SERVICE_UPDATING";
                break;
        }

        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            sGoogleApiClient = null;
            buildDeviceLocationManager(mContext);
            start(false);

        } else if (mListener != null) {
            mListener.onError(failReason, connectionResult);
        }
    }

    public void handleLocationUpdate(Location location) {
        cancelTimeoutTimer();

        if (location != null) {
            mLastKnownLocation = location;
        }
        if (isLocationRequested && mListener != null) {
            mListener.onLocationUpdate(location);
        }
        isLocationRequested = false;
    }

    public static void init(Context context) {
        final LocationClient locationClient = new LocationClient(context);
        locationClient.buildDeviceLocationManager(context);
        locationClient.start(true);
    }

    public interface Listener {
        void onLocationUpdate(Location location);

        void onError(String failReason, ConnectionResult connectionResult);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context context;
        Listener listener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder listener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public LocationClient build() {
            return new LocationClient(context, listener);
        }
    }

    @Nullable
    public static Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    public static void setLastKnownLocation(Location location) {
        LocationClient.mLastKnownLocation = location;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {

        }

        return gps_enabled || network_enabled;
    }
}
