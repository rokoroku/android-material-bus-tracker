package kr.rokoroku.mbus.core;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.util.TimeUtils;

/**
 * Created by rok on 2015. 6. 28..
 */
public class LocationClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static Location mLastKnownLocation;

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mDeviceLocationManager;
    private android.location.LocationListener mDeviceLocationListener;

    private Listener mListener;

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

    public boolean isGooglePlayServiceAvailable() {
        // Getting Google Play availability status
        int availablilty = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        return availablilty == ConnectionResult.SUCCESS;
    }

    protected synchronized void buildDeviceLocationManager(Context context) {
        if (mDeviceLocationManager == null) {
            // Getting LocationManager object from System Service LOCATION_SERVICE
            mDeviceLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Getting the best location provider
            String provider = getBestLocationProvider(mDeviceLocationManager);

            // Getting Current Location
            Location lastKnownLocation = mDeviceLocationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                mLastKnownLocation = lastKnownLocation;
            }

            // Build listener
            if (mDeviceLocationListener == null) {
                mDeviceLocationListener = new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        LocationClient.this.onLocationChanged(location);
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
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected String getBestLocationProvider(LocationManager locationManager) {
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        return locationManager.getBestProvider(criteria, true);
    }


    public void start(boolean force) {

        boolean shouldUpdate = mLastKnownLocation == null ||
                (System.currentTimeMillis() - mLastKnownLocation.getTime() > 5 * 60 * 1000);

        if (force || shouldUpdate) {
            if (isLocationEnabled(mContext)) {
                if (mGoogleApiClient != null) {
                    if (mGoogleApiClient.isConnected()) {
                        // Request location update
                        LocationRequest locationRequest = new LocationRequest()
                                .setNumUpdates(1)
                                .setMaxWaitTime(5000)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                    } else {
                        mGoogleApiClient.connect();
                    }

                } else if (mDeviceLocationManager != null) {
                    String provider = getBestLocationProvider(mDeviceLocationManager);
                    mDeviceLocationManager.requestSingleUpdate(provider, mDeviceLocationListener, Looper.getMainLooper());
                }
            } else {
                if(mListener != null) {
                    mListener.onError("LOCATION_DISABLED", null);
                }
                stop();
            }

        } else {
            onLocationChanged(mLastKnownLocation);
        }
    }

    public void stop() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient = null;
            }
        } else if (mDeviceLocationManager != null) {
            mDeviceLocationManager = null;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            mLastKnownLocation = lastLocation;
        }
        start(false);
    }

    @Override
    public void onConnectionSuspended(int errorCode) {
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
            mGoogleApiClient = null;
            buildDeviceLocationManager(mContext);
            start(false);
        } else if (mListener != null) {
            mListener.onError(failReason, connectionResult);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLastKnownLocation = location;
        }
        if (mListener != null) {
            mListener.onLocationUpdate(location);
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public static void init(Context context) {
        final LocationClient locationClient = new LocationClient(context);
        locationClient.buildDeviceLocationManager(context);
        locationClient.setListener(new Listener() {
            @Override
            public void onLocationUpdate(Location location) {
                locationClient.stop();
            }

            @Override
            public void onError(String failReason, ConnectionResult connectionResult) {
                locationClient.stop();
            }
        });
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
            LocationClient locationClient = new LocationClient(context, listener);
            return locationClient;
        }
    }

    @Nullable
    public static Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        return gps_enabled || network_enabled;
    }
}
