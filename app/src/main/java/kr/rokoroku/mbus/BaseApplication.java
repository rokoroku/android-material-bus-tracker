package kr.rokoroku.mbus;

import android.app.Application;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.LocationClient;

/**
 * Created by rok on 2015. 5. 31..
 */
public class BaseApplication extends Application {

    public static final int REFRESH_INTERVAL = 60 * 1000;

    public static boolean showAd = false;

    private static BaseApplication instance;
    private final static int[] themes = {R.style.AppTheme_Light, R.style.AppTheme};
    public static int themeIndex = 0;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        DatabaseFacade.init(this);
        LocationClient.init(this);
        ApiFacade.init(this);

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-XXXXX-Y"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

    }



    public int getThemeId() {
        return themes[themeIndex];
    }

    public void switchTheme() {
        themeIndex = (themeIndex + 1) % 2;
    }

    public static BaseApplication getInstance() {
        return instance;
    }

}
