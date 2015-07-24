package kr.rokoroku.mbus;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.LocationClient;
import retrofit.RestAdapter;

/**
 * Created by rok on 2015. 5. 31..
 */
public class BaseApplication extends Application {

    public static final int REFRESH_INTERVAL = 30 * 1000;
    public static final String SHARED_PREFERENCE_KEY = "pref";
    public static final String PREFERENCE_DB_VERSION = "pref_db_version";
    public static final String PREFERENCE_HOME_SCREEN = "pref_home_screen";
    public static final String PREFERENCE_THEME = "pref_theme";

    public static boolean showAd = false;
    public static RestAdapter.LogLevel logLevel;
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private static BaseApplication instance;
    private final static int[] themes = {R.style.AppTheme_Light, R.style.AppTheme};
    private int themeIndex = 0;


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

        if (BuildConfig.DEBUG) {
            Fabric.with(this, new Answers());
            logLevel = RestAdapter.LogLevel.HEADERS_AND_ARGS;
        } else {
            Fabric.with(this, new Crashlytics());
            logLevel = RestAdapter.LogLevel.NONE;
        }

        // set theme
        String prefTheme = getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE).getString(PREFERENCE_THEME, "0");
        themeIndex = Integer.parseInt(prefTheme);
    }

    public int getCurrentThemeIndex() {
        return themeIndex;
    }

    public int getCurrentTheme() {
        return themes[themeIndex];
    }

    public void setThemeIndex(int index) {
        this.themeIndex = index;
    }

    public static BaseApplication getInstance() {
        return instance;
    }

    public static SharedPreferences getSharedPreferences() {
        return instance.getSharedPreferences(SHARED_PREFERENCE_KEY, MODE_PRIVATE);
    }
}
