package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.core.ApiCaller;
import kr.rokoroku.mbus.core.DatabaseHelper;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.model.Direction;
import kr.rokoroku.mbus.model.MapLine;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;
import kr.rokoroku.mbus.util.ThemeUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener, LocationClient.Listener {

    public static final String EXTRA_KEY_STATION = "station";
    public static final String EXTRA_KEY_ROUTE = "route";

    private final int DEFAULT_ZOOM_LEVEL = 16;
    private final int INITIAL_ZOOM_LEVEL = 12;

    private Route mRouteExtra;
    private Station mStationExtra;

    private Toolbar mToolbar;
    private SupportMapFragment mMapFragment;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mMyLocationButton;

    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private ArrayList<Marker> markers;

    private int mPreviousZoom = 0;


    public Bitmap getScaledIcon(int dp) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stop_marker);
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return Bitmap.createScaledBitmap(bitmap, pixel, pixel, false);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }

        initToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mMyLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mMyLocationButton.setShowProgressBackground(false);
        mMyLocationButton.setOnClickListener(v -> {
            if (mMap.getMyLocation() == null) {
                requestLocationUpdate(false);

            } else {
                requestLocationUpdate(true);
            }
        });

        onNewIntent(getIntent());
    }

    private void requestLocationUpdate(boolean force) {
        int color = ThemeUtils.getThemeColor(this, R.attr.colorAccent);
        mMyLocationButton.setDrawableTint(color);
        mMyLocationButton.setIndeterminate(true);

        if (mMap != null && mMap.getMyLocation() != null) {
            Location myLocation = mMap.getMyLocation();
            onLocationUpdate(myLocation);

        } else {
            if (mLocationClient == null) {
                initLocationClient();
            }
            mLocationClient.start(force);
        }
    }

    private void initLocationClient() {
        mLocationClient = LocationClient.with(this).listener(this).build();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        this.mMap = map;

        int paddingTop = ThemeUtils.getStatusBarHeight(this) + ThemeUtils.getDimension(this, android.R.attr.actionBarSize);
        map.setPadding(0, paddingTop, 0, 0);
        map.setMyLocationEnabled(true);
        map.setOnCameraChangeListener(this);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        LatLng initialLatLng = new LatLng(126.99, 37.55);

        Location lastKnownLocation = LocationClient.getLastKnownLocation();
        if (lastKnownLocation != null) {
            initialLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, INITIAL_ZOOM_LEVEL));

            CameraPosition cameraPosition = new CameraPosition(initialLatLng, DEFAULT_ZOOM_LEVEL, 0, 0);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, INITIAL_ZOOM_LEVEL));
        }

        if (mRouteExtra != null) {
            List<MapLine> mapLineList = mRouteExtra.getMapLineList();
            if (mapLineList == null) {
                ApiCaller.getInstance().getRouteMapLine(mRouteExtra, new Callback<List<MapLine>>() {
                    @Override
                    public void success(List<MapLine> mapLines, Response response) {
                        drawRoute(mRouteExtra);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Snackbar.make(mCoordinatorLayout, "지도 정보를 가져오는데 실패하였습니다.", Snackbar.LENGTH_LONG).show();
                    }
                });
            } else {
                new Handler().postDelayed(() -> drawRoute(mRouteExtra), 1000);
            }

        } else if (mStationExtra != null) {
            // center camera to station
            LatLng latLng = new LatLng(mStationExtra.getLatitude(), mStationExtra.getLongitude());
            map.addMarker(new MarkerOptions().position(latLng).title(mStationExtra.getName()).snippet(mStationExtra.getLocalId()));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, DEFAULT_ZOOM_LEVEL, 0, 0)));
        }

    }

    private void drawRoute(Route route) {

        List<RouteStation> routeStationList = route.getRouteStationList();
        List<MapLine> mapLineList = route.getMapLineList();
        if (routeStationList != null) {
            markers = new ArrayList<>();

            RouteStation startStation = routeStationList.get(0);
            RouteStation turnStation = null;
            RouteStation lastStation = null;

            double southWestLatitude = Double.MAX_VALUE;
            double southWestLongitude = Double.MAX_VALUE;
            double northEastLatitude = 0;
            double northEastLongitude = 0;

            for (RouteStation routeStation : routeStationList) {
                if (turnStation == null && routeStation.getSequence() == route.getTurnStationSeq()) {
                    turnStation = routeStation;
                }
                if (lastStation == null && routeStation.getId().equals(route.getEndStationId())) {
                    lastStation = routeStation;
                }

                southWestLatitude = Math.min(southWestLatitude, routeStation.getLatitude());
                southWestLongitude = Math.min(southWestLongitude, routeStation.getLongitude());
                northEastLatitude = Math.max(northEastLatitude, routeStation.getLatitude());
                northEastLongitude = Math.max(northEastLongitude, routeStation.getLongitude());

                LatLng latLng = new LatLng(routeStation.getLatitude(), routeStation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(routeStation.getName())
                        .snippet(routeStation.getLocalId())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker))
                        .visible(false);
                Marker marker = mMap.addMarker(markerOptions);
                markers.add(marker);
            }

            if (lastStation == null) {
                lastStation = routeStationList.get(routeStationList.size() - 1);
            }


            LatLng southWest = new LatLng(southWestLatitude, southWestLongitude);
            LatLng northEast = new LatLng(northEastLatitude, northEastLongitude);
            LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));

        }

        if (mapLineList != null) {
            PolylineOptions upLine = new PolylineOptions().color(Color.BLUE);
            PolylineOptions downLine = new PolylineOptions().color(Color.RED);
            for (MapLine mapLine : mapLineList) {
                if (mapLine.getDirection().equals(Direction.UP)) {
                    upLine.add(new LatLng(mapLine.getLatitude(), mapLine.getLongitude()));
                } else {
                    downLine.add(new LatLng(mapLine.getLatitude(), mapLine.getLongitude()));
                }
            }

            mMap.addPolyline(upLine);
            mMap.addPolyline(downLine);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mRouteExtra = intent.getParcelableExtra(EXTRA_KEY_ROUTE);
        mStationExtra = intent.getParcelableExtra(EXTRA_KEY_STATION);

        if (mRouteExtra != null) {
            Route storedRoute = DatabaseHelper.getInstance().getRoute(mRouteExtra.getProvider(), mRouteExtra.getId());
            if (storedRoute != null) mRouteExtra = storedRoute;
            setTitle(mRouteExtra.getName());

            RouteType routeType = mRouteExtra.getType();
            if (routeType != null && !RouteType.UNKNOWN.equals(routeType)) {
                mToolbar.setTitleTextColor(routeType.getColor(this));
            }

        } else if (mStationExtra != null) {
            Station storedStation = DatabaseHelper.getInstance().getStation(mStationExtra.getProvider(), mStationExtra.getId());
            if (storedStation != null) mStationExtra = storedStation;
            setTitle(mStationExtra.getName());
        }

        setIntent(intent);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        int newZoom = (int) cameraPosition.zoom;
        if (mPreviousZoom != newZoom) {
            if (markers != null) {
                boolean visible = true;
                Bitmap bitmap = null;
                if (cameraPosition.zoom < 11) {
                    visible = false;
                } else {
                    int size = 10 + (newZoom - 11) * 2;
                    bitmap = MapsActivity.this.getScaledIcon(size);
                }
                for (Marker marker : markers) {
                    boolean isInfoWindowShown = marker.isInfoWindowShown();
                    marker.setVisible(visible);
                    if (bitmap != null) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    }
                    if (isInfoWindowShown) {
                        marker.showInfoWindow();
                    }
                }
            }
        }
        mPreviousZoom = newZoom;
    }

    @Override
    public void onLocationUpdate(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null) {
            CameraPosition cameraPosition = new CameraPosition(latLng, DEFAULT_ZOOM_LEVEL, 0, 0);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        mMyLocationButton.postDelayed(() -> {
            mMyLocationButton.setDrawableTint(Color.BLACK);
            mMyLocationButton.setIndeterminate(false);
            mMyLocationButton.setProgress(0, false);
        }, 1000);
    }

    @Override
    public void onError(String failReason, ConnectionResult connectionResult) {
        Snackbar.make(mCoordinatorLayout, failReason, Snackbar.LENGTH_LONG).show();
        mMyLocationButton.postDelayed(() -> {
            mMyLocationButton.setDrawableTint(Color.BLACK);
            mMyLocationButton.setIndeterminate(false);
            mMyLocationButton.setProgress(0, false);
        }, 1000);
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


    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(mToolbar);

            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mToolbar.getLayoutParams();
            layoutParams.topMargin = ThemeUtils.getStatusBarHeight(this);

            // init marquee animation to  toolbar title
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);

            TextView titleTextView = null;
            titleTextView = (TextView) f.get(mToolbar);
            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);

            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
            float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, getResources().getDisplayMetrics());
            titleTextView.setShadowLayer(radius, offset, offset, Color.BLACK);

        } catch (Exception ignored) {

        }
    }
}