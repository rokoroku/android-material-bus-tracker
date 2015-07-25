package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.GoogleMap;

import java.lang.reflect.Field;

import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;


public class MapsActivity extends AppCompatActivity implements MapFragment.OnEventListener {

    public static final String EXTRA_KEY_STATION = MapFragment.EXTRA_KEY_STATION;
    public static final String EXTRA_KEY_ROUTE = MapFragment.EXTRA_KEY_ROUTE;

    private final String MAP_FRAGMENT_TAG = "MAP";

    private Route mRouteExtra;
    private Station mStationExtra;

    private Toolbar mToolbar;
    private MapFragment mMapFragment;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mMyLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        initToolbar();
        initMapFragment(savedInstanceState);
        initFloatingActionButton();

        onNewIntent(getIntent());
    }

    private void initFloatingActionButton() {
        mMyLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mMyLocationButton.setDrawableTint(Color.BLACK);
        mMyLocationButton.setIndeterminate(false);
        mMyLocationButton.setShowProgressBackground(false);
        mMyLocationButton.setOnClickListener(v -> {
            if (mMapFragment != null) {
                mMyLocationButton.setClickable(false);
                mMyLocationButton.setDrawableTint(ThemeUtils.getThemeColor(this, R.attr.colorAccent));
                mMyLocationButton.setIndeterminate(true);
                mMapFragment.updateLocation(true);
            }
        });
        mMyLocationButton.requestLayout();
    }

    private void initMapFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMapFragment = (MapFragment) fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance(savedInstanceState);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_frame, mMapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
        mMapFragment.setOnEventListener(this);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(mToolbar);
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mToolbar.getLayoutParams();
                layoutParams.topMargin = ViewUtils.getStatusBarHeight(this);
                mToolbar.requestLayout();
            }

            // init marquee animation to  toolbar title
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);

            TextView titleTextView = null;
            titleTextView = (TextView) f.get(mToolbar);
            if (titleTextView != null) {
                titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleTextView.setFocusable(true);
                titleTextView.setFocusableInTouchMode(true);
                titleTextView.requestFocus();
                titleTextView.setSingleLine(true);
                titleTextView.setSelected(true);
                titleTextView.setMarqueeRepeatLimit(-1);
                titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
            }

        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mRouteExtra = intent.getParcelableExtra(EXTRA_KEY_ROUTE);
        mStationExtra = intent.getParcelableExtra(EXTRA_KEY_STATION);

        if (mRouteExtra != null) {
            RouteType routeType = mRouteExtra.getType();
            setTitle(mRouteExtra.getName());
            if (routeType != null && !RouteType.UNKNOWN.equals(routeType)) {
                mToolbar.setTitleTextColor(routeType.getColor(this));
            }
            if (mMapFragment != null) {
                mMapFragment.setRoute(mRouteExtra);
            }
        } else if (mStationExtra != null) {
            setTitle(mStationExtra.getName());

            if (mMapFragment != null) {
                mMapFragment.setStation(mStationExtra);
            }
        }

        setIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapLoaded(GoogleMap map) {

    }

    @Override
    public void onLocationUpdate(Location location) {
        mMyLocationButton.postDelayed(() -> {
            mMyLocationButton.setClickable(true);
            mMyLocationButton.setDrawableTint(Color.BLACK);
            mMyLocationButton.setIndeterminate(false);
            mMyLocationButton.setProgress(0, false);
        }, 1000);
    }

    @Override
    public void onStationClick(Station station) {
        Intent intent = new Intent(MapsActivity.this, StationActivity.class);
        intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
        if (mRouteExtra != null) {
            intent.putExtra(StationActivity.EXTRA_KEY_REDIRECT_ROUTE_ID, mRouteExtra.getId());
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onErrorMessage(String error) {
        Snackbar.make(mCoordinatorLayout, error, Snackbar.LENGTH_LONG).show();
    }
}