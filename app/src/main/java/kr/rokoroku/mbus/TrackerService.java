package kr.rokoroku.mbus;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.nlopez.smartlocation.SmartLocation;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;

public class TrackerService extends Service {

    private SmartLocation mLocationClient;
    private Listener mListener;
    private IBinder mBinder;
    private Timer mRecognitionTimer;
    private Timer mTrackingTimer;

    private DetectedActivity mLastActivity;

    private Route mRoute;
    private String mVehicleId;
    private RouteStation mCurrentStation;
    private RouteStation mDestinationStation;

    private final int RECOGNITION_TIME = 5000;

    public TrackerService() {
        this.mLocationClient = SmartLocation.with(this);
    }

    public DetectedActivity getLastActivity() {
        return mLastActivity;
    }

    public void startRecognition(Listener listener) {
        this.mListener = listener;
        this.mLocationClient.activity().start(detectedActivity -> {
            if(mLastActivity == null || mLastActivity.getType() != detectedActivity.getType()) {
                cancelTimer();
                switch (detectedActivity.getType()) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                        startTimer(() -> {
                            mListener.onTransit(detectedActivity);
                            startTracking();
                        });
                        break;
                    case DetectedActivity.RUNNING:
                    case DetectedActivity.WALKING:
                    case DetectedActivity.ON_FOOT:
                        startTimer(() -> mListener.onLeave(detectedActivity));
                        break;
                    case DetectedActivity.STILL:
                    case DetectedActivity.UNKNOWN:
                    default:
                        break;
                }
            }
        });
    }

    public void setCurrentStation(RouteStation station) {
        this.mCurrentStation = station;
    }

    private void startTracking() {
        if(mDestinationStation == null) return;
        if(mTrackingTimer != null) mTrackingTimer.cancel();
        mTrackingTimer = new Timer();
        mTrackingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<BusLocation> busLocationList = mRoute.getBusLocationList();

                //get current bus location object
                BusLocation currentBusLocation = null;
                if (mVehicleId != null) {
                    for (BusLocation busLocation : busLocationList) {
                        boolean found = mVehicleId.equals(busLocation.getId());
                        if (found) {
                            currentBusLocation = busLocation;
                            break;
                        }
                    }
                }

                //get current state
                if (currentBusLocation != null) {

                    Station currentStation = null;
                    Station headingStation = null;

                    String headingStationId = currentBusLocation.getTargetStationId();
                    String currentStationId = currentBusLocation.getCurrentStationId();
                    List<RouteStation> routeStationList = mRoute.getRouteStationList();
                    int behind = 0;
                    boolean found = false;

                    for (RouteStation routeStation : routeStationList) {
                        if (headingStation == null && routeStation.getId().equals(headingStationId)) {
                            headingStation = routeStation;
                        }
                        if (currentStation == null && routeStation.getId().equals(currentStationId)) {
                            currentStation = routeStation;
                        } else if (currentStation != null) {
                            behind++;
                            if (mDestinationStation.getId().equals(routeStation.getId())) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if(found) {

                    }

                }

            }
        }, BaseApplication.REFRESH_INTERVAL);
    }

    public void stopRecognition() {
        this.mLocationClient.activity().stop();
    }

    public void setRoute(Route route) {
        this.mRoute = route;
    }

    public void setDestinationStation(RouteStation destinationStation) {
        this.mDestinationStation = destinationStation;
    }

    private synchronized void startTimer(Runnable runnable) {
        mRecognitionTimer = new Timer(true);
        mRecognitionTimer.schedule(new TimerTask() {
            @Override
            public synchronized void run() {
                runnable.run();
                mRecognitionTimer = null;
            }
        }, RECOGNITION_TIME);
    }

    private synchronized void cancelTimer() {
        if (mRecognitionTimer != null) mRecognitionTimer.cancel();
        mRecognitionTimer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) mBinder = new LocalBinder();
        return mBinder;
    }


    static public boolean bindService(Activity activity, ServiceConnection connection) {
        try {
            Intent intent = new Intent(activity, TrackerService.class);
            activity.startService(intent);
            activity.bindService(intent, connection, BIND_AUTO_CREATE);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    static public boolean unbindService(Activity activity, ServiceConnection connection, boolean stopService) {
        try {
            Intent i = new Intent(activity, TrackerService.class);
            activity.unbindService(connection);

            if (stopService) {
                activity.stopService(i);
            }

        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public interface Listener {
        void onTransit(DetectedActivity detectedActivity);
        void onLeave(DetectedActivity detectedActivity);
        void onError(Throwable t);
    }

    // This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    public class LocalBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

}
