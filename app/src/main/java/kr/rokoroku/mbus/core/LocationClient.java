package kr.rokoroku.mbus.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import java.security.Security;
import java.util.Timer;
import java.util.TimerTask;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.util.ViewUtils;

/**
 * Created by rok on 2015. 6. 28..
 */
public class LocationClient {

    private static final String TAG = "LocationClient";

    private static final int REQUEST_TIMEOUT_DELAY = 5000;
    private static Location sLastKnownLocation;

    private Context mContext;
    private Timer mTimer;
    private Listener mListener;
    private ActivityTracker mActivityTracker;
    private boolean isLocationRequested;

    private LocationClient(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        if (isPermissionGranted() && sLastKnownLocation == null) try {
            sLastKnownLocation = SmartLocation.with(context).location().getLastLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public LocationClient(Context context) {
        this(context, null);
    }

    public void start(boolean force) {

        if (isPermissionGranted()) {

            boolean shouldUpdate = sLastKnownLocation == null ||
                    (System.currentTimeMillis() - sLastKnownLocation.getTime() > 2 * 60 * 1000);

            if (force || shouldUpdate) {
                if (isLocationProviderAvailable(mContext)) {

                    SmartLocation.with(mContext)
                            .location(new LocationGooglePlayServicesWithFallbackProvider(mContext))
                            .config(LocationParams.BEST_EFFORT)
                            .oneFix()
                            .start(location -> {
                                Log.d(TAG, "Location Updated Successfully");
                                handleLocationUpdate(location);
                                cancelTimer();
                            });

                    isLocationRequested = true;

                    int timeoutDelay = REQUEST_TIMEOUT_DELAY;
                    if (sLastKnownLocation == null) {
                        timeoutDelay *= 2;
                    }
                    startTimer(timeoutDelay);

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
        } else {
            if (mListener != null) {
                mListener.onError("PERMISSION_DENIED", null);
            }
            stop();
        }
    }

    private synchronized void startTimer(int delay) {
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

    private synchronized void cancelTimer() {
        if (mTimer != null) mTimer.cancel();
        mTimer = null;
    }

    public void stop() {
        cancelTimer();
    }

    public void handleLocationUpdate(Location location) {
        cancelTimer();

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

    public void setActivityTracker(ActivityTracker tracker) {
        this.mActivityTracker = tracker;
    }

    public interface Listener {
        void onLocationUpdate(Location location);

        void onError(String failReason, ConnectionResult connectionResult);
    }

    public interface ActivityTracker {
        void onActivityChanged();

        void onLocationUpdate();
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context context;
        Listener listener;
        ActivityTracker activityTracker;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder listener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public Builder tracker(ActivityTracker activityTracker) {
            this.activityTracker = activityTracker;
            return this;
        }

        public LocationClient build() {
            LocationClient locationClient = new LocationClient(context, listener);
            locationClient.setActivityTracker(activityTracker);
            return locationClient;
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
        if (isPermissionGranted()) try {
            return SmartLocation.with(context).location().state().isAnyProviderAvailable();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(BaseApplication.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
