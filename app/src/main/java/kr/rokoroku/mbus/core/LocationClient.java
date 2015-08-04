package kr.rokoroku.mbus.core;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import java.util.Timer;
import java.util.TimerTask;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import kr.rokoroku.mbus.util.ViewUtils;

/**
 * Created by rok on 2015. 6. 28..
 */
public class LocationClient {

    private static final String TAG = "LocationClient";

    private static final int REQUEST_TIMEOUT_DELAY = 3000;
    private static Location sLastKnownLocation;

    private Context mContext;
    private Timer mTimer;
    private Listener mListener;
    private boolean isLocationRequested;

    private LocationClient(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        if (sLastKnownLocation == null) {
            sLastKnownLocation = SmartLocation.with(context).location().getLastLocation();
        }
    }

    public LocationClient(Context context) {
        this(context, null);
    }

    public void start(boolean force) {

        boolean shouldUpdate = sLastKnownLocation == null ||
                (System.currentTimeMillis() - sLastKnownLocation.getTime() > 2 * 60 * 1000);

        if (force || shouldUpdate) {
            if (isLocationProviderAvailable(mContext)) {

                SmartLocation.with(mContext).location()
                        .provider(new LocationGooglePlayServicesWithFallbackProvider(mContext))
                        .config(LocationParams.BEST_EFFORT)
                        .oneFix()
                        .start(location -> {
                            Log.d(TAG, "Location Updated Successfully");
                            handleLocationUpdate(location);
                            cancelTimeoutTimer();
                        });

                isLocationRequested = true;
                startTimeoutTimer(REQUEST_TIMEOUT_DELAY);

            } else {
                if (mListener != null) {
                    mListener.onError("LOCATION_UNAVAILABLE", null);
                }
                stop();
            }

        } else {
            isLocationRequested = true;
            handleLocationUpdate(sLastKnownLocation);
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
                        if (sLastKnownLocation != null) {
                            ViewUtils.runOnUiThread(() -> mListener.onLocationUpdate(sLastKnownLocation));
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
    }

    public void handleLocationUpdate(Location location) {
        cancelTimeoutTimer();

        if (location != null) {
            sLastKnownLocation = location;
        }
        if (isLocationRequested && mListener != null) {
            mListener.onLocationUpdate(location);
        }
        isLocationRequested = false;
    }

    public static void init(Context context) {
        new LocationClient(context).start(true);
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
        return sLastKnownLocation;
    }

    public static void setLastKnownLocation(Location location) {
        LocationClient.sLastKnownLocation = location;
    }

    public static boolean isLocationProviderAvailable(Context context) {
        return SmartLocation.with(context).location().state().isAnyProviderAvailable();
    }
}
