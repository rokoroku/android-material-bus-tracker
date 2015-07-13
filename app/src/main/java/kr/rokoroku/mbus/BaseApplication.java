package kr.rokoroku.mbus;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.Database;
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

        Database.init(this);
        LocationClient.init(this);
        ApiFacade.init(this);

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-XXXXX-Y"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

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
