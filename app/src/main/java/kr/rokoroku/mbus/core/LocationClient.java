package kr.rokoroku.mbus.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

/**
 * Created by rok on 2015. 6. 28..
 */
public class LocationClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Listener mListener;

    private LocationClient(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
        buildGoogleApiClient(mContext);
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setNumUpdates(1)
                .setMaxWaitTime(10000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void start() {
        if (mGoogleApiClient.isConnected()) {
            if (mLocationRequest == null) {
                createLocationRequest();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void stop() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void registerListener(Listener listener) {
        this.mListener = listener;
    }

    @Nullable
    public Location getLastLocation() {
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        start();
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

        if (mListener != null) {
            mListener.onError(failReason, connectionResult);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mListener != null) {
            mListener.onLocationUpdate(location);
        }
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

}
