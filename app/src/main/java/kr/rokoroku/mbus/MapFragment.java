package kr.rokoroku.mbus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.data.model.Direction;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ValueCallback;
import kr.rokoroku.mbus.util.ViewUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 7. 17..
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener,
        LocationClient.Listener {

    public static final String EXTRA_KEY_STATION = "station";
    public static final String EXTRA_KEY_ROUTE = "route";
    public static final String EXTRA_ENABLE_SEARCH = "enable_search";

    private final int DEFAULT_ZOOM_LEVEL = 16;
    private final int INITIAL_ZOOM_LEVEL = 12;

    private CoordinatorLayout mCoordinatorLayout;

    private LocationClient mLocationClient;
    private CameraPosition mPreviousPosition;
    private OnEventListener mListener;

    private ArrayList<Marker> markers;
    private Map<String, Station> mStationTable;

    private Route mRouteExtra;
    private Station mStationExtra;
    private boolean isSearchEnable;

    public MapFragment() {

    }

    public static MapFragment newInstance(Bundle args) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if(args != null) {
            mRouteExtra = args.getParcelable(EXTRA_KEY_ROUTE);
            mStationExtra = args.getParcelable(EXTRA_KEY_STATION);
            isSearchEnable = args.getBoolean(EXTRA_ENABLE_SEARCH, false);

            if (mRouteExtra != null) {
                setRoute(mRouteExtra);

            } else if (mStationExtra != null) {
                setStation(mStationExtra);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.getMapAsync(this);
    }

    public void setRoute(Route route) {
        Route cachedRoute = DatabaseFacade.getInstance().getRoute(mRouteExtra.getProvider(), mRouteExtra.getId());
        if (cachedRoute != null) route = cachedRoute;
        isSearchEnable = false;
        mStationExtra = null;
        mRouteExtra = route;

        if(getMap() != null) {
            initRoute(route);
        }
    }

    public void setStation(Station station) {
        Station cachedStation = DatabaseFacade.getInstance().getStation(mStationExtra.getProvider(), mStationExtra.getId());
        if (cachedStation != null) station = cachedStation;
        isSearchEnable = true;
        mStationExtra = station;
        mRouteExtra = null;

        if(getMap() != null) {
            initStation(station);
        }
    }

    public void updateLocation(boolean force) {
        GoogleMap map = getMap();
        if (map != null && map.getMyLocation() != null) {
            Location myLocation = map.getMyLocation();
            onLocationUpdate(myLocation);

        } else {
            if (mLocationClient == null) {
                initLocationClient(getActivity());
            }
            mLocationClient.start(force);
        }
    }

    public void setOnEventListener(OnEventListener listener) {
        this.mListener = listener;
    }

    private synchronized void initRoute(Route route) {
        GoogleMap map = getMap();
        if (map != null && route != null) {
            map.clear();
            markers = null;
            mStationTable = null;

            List<MapLine> mapLineList = route.getMapLineList();
            if (mapLineList == null) {
                ApiFacade.getInstance().getRouteMapLine(route, new Callback<List<MapLine>>() {
                    @Override
                    public void success(List<MapLine> mapLines, Response response) {
                        drawRoute(route);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mListener != null) {
                            mListener.onErrorMessage(getString(R.string.error_failed_to_retrieve_map_line));
                        }
                    }
                });
            } else {
                new Handler().postDelayed(() -> drawRoute(route), 1000);
            }
        }
    }

    private synchronized void initStation(Station station) {
        GoogleMap map = getMap();
        if (map != null && station != null) {
            map.clear();
            markers = null;
            mStationTable = null;

            Marker marker = addStationMarker(station);
            LatLng latLng = new LatLng(station.getLatitude(), station.getLongitude());
            map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, DEFAULT_ZOOM_LEVEL, 0, 0)), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    if(marker != null) {
                        marker.showInfoWindow();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    private synchronized void initSearch() {
        if (!isSearchEnable) {
            isSearchEnable = true;
            GoogleMap map = getMap();
            LatLng latLng = map.getCameraPosition().target;
            ApiFacade.getInstance().searchByPosition(latLng.latitude, latLng.longitude, 1000, new ValueCallback<List<Station>>() {
                @Override
                public void onSuccess(List<Station> value) {
                    drawStations(value);
                }

                @Override
                public void onFailure(Throwable t) {
                    if (mListener != null) {
                        mListener.onErrorMessage(t.getMessage());
                    }
                }
            });
        }
    }

    private synchronized void initLocationClient(Context context) {
        if (context == null) context = BaseApplication.getInstance();
        mLocationClient = LocationClient.with(context).listener(this).build();
    }

    @Override
    public synchronized void onMapReady(GoogleMap map) {

        Context context = getActivity();

        int paddingTop = ViewUtils.getStatusBarHeight(context) + ThemeUtils.getDimension(context, android.R.attr.actionBarSize);
        map.setPadding(0, paddingTop, 0, 0);
        map.setMyLocationEnabled(true);
        map.setOnCameraChangeListener(this);
        map.setOnInfoWindowClickListener(this);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        Location lastKnownLocation = LocationClient.getLastKnownLocation();
        if (lastKnownLocation != null) {
            LatLng initialLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, INITIAL_ZOOM_LEVEL));

            CameraPosition cameraPosition = new CameraPosition(initialLatLng, DEFAULT_ZOOM_LEVEL, 0, 0);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            LatLng seoul = new LatLng(126.99, 37.55);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, INITIAL_ZOOM_LEVEL));
        }

        if (mRouteExtra != null) {
            initRoute(mRouteExtra);

        } else if (mStationExtra != null) {
            initStation(mStationExtra);
            initSearch();

        } else {
            initSearch();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        boolean updateNearbyStation = isSearchEnable && (
                (mPreviousPosition == null || GeoUtils.calculateDistanceInMeter(cameraPosition.target, mPreviousPosition.target) > 500));

        boolean updateMarker = mPreviousPosition == null
                || (int) mPreviousPosition.zoom != (int) cameraPosition.zoom;

        if (updateNearbyStation) {
            LatLng latLng = cameraPosition.target;
            int radius = (int) (cameraPosition.zoom * 1000);
            if (radius > 2000) radius = 2000;

            if (cameraPosition.zoom < 14) {
                if (mListener != null) {
                    mListener.onErrorMessage(getString(R.string.warning_zoom_too_low));
                }
            } else {
                ApiFacade.getInstance().searchByPosition(latLng.latitude, latLng.longitude, radius, new ValueCallback<List<Station>>() {
                    @Override
                    public void onSuccess(List<Station> value) {
                        drawStations(value);
                        updateMarkers(cameraPosition);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (mListener != null) {
                            mListener.onErrorMessage(getString(R.string.error_with_reason, t.getMessage()));
                        }
                    }
                });
            }
        }
        if (updateMarker) {
            updateMarkers(cameraPosition);
        }
        mPreviousPosition = cameraPosition;
    }

    @Override
    public void onLocationUpdate(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        GoogleMap map = getMap();
        if (map != null) {
            CameraPosition cameraPosition = new CameraPosition(latLng, DEFAULT_ZOOM_LEVEL, 0, 0);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        if (mListener != null) {
            mListener.onLocationUpdate(location);
        }
    }

    @Override
    public void onError(String failReason, ConnectionResult connectionResult) {
        if (mListener != null) {
            mListener.onLocationUpdate(null);
            mListener.onErrorMessage(failReason);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String arsId = marker.getSnippet();
        if (arsId != null && !TextUtils.isEmpty(arsId) && mStationTable != null) {
            Station markerStation = mStationTable.get(arsId);
            if (markerStation != null && mListener != null) {
                mListener.onStationClick(markerStation);
            }
        }
    }

    private void drawRoute(Route route) {
        GoogleMap map = getMap();
        if (map != null) {
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
                    Marker marker = map.addMarker(markerOptions);
                    markers.add(marker);
                }

                if (lastStation == null) {
                    lastStation = routeStationList.get(routeStationList.size() - 1);
                }


                LatLng southWest = new LatLng(southWestLatitude, southWestLongitude);
                LatLng northEast = new LatLng(northEastLatitude, northEastLongitude);
                LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));

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

                map.addPolyline(upLine);
                map.addPolyline(downLine);
            }
        }
    }

    private void drawStations(Collection<Station> stations) {
        GoogleMap map = getMap();
        if (stations != null && map != null) {
            for (Station station : stations) {
                addStationMarker(station);
            }
        }
    }

    private Marker addStationMarker(Station station) {
        String key = station.getLocalId();
        if (key == null || TextUtils.isEmpty(key)) {
            key = station.getId();
        }
        if (markers == null) markers = new ArrayList<>();
        if (mStationTable == null) mStationTable = new HashMap<>();
        if (key != null && !mStationTable.containsKey(key)) {
            LatLng latLng = null;
            if (station.getLatitude() != null && station.getLongitude() != null) {
                latLng = new LatLng(station.getLatitude(), station.getLongitude());
            }

            if (latLng != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(station.getName())
                        .snippet(station.getLocalId())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker))
                        .visible(false);
                GoogleMap map = getMap();
                if(map != null) {
                    Marker marker = map.addMarker(markerOptions);
                    markers.add(marker);
                    mStationTable.put(key, station);
                    return marker;
                }
            }
        }
        return null;
    }
    private void updateMarkers(CameraPosition cameraPosition) {
        if (markers != null) {
            boolean visible = true;
            Bitmap bitmap = null;

            if (isSearchEnable && cameraPosition.zoom < 14) {
                visible = false;
            } else if (cameraPosition.zoom < 11) {
                visible = false;
            } else {
                int size = 10 + (int) (cameraPosition.zoom - 11) * 2;
                bitmap = getScaledStationIcon(size);
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

    private Bitmap getScaledStationIcon(int dp) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stop_marker);
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return Bitmap.createScaledBitmap(bitmap, pixel, pixel, false);
    }

    public interface OnEventListener {
        void onLocationUpdate(Location location);

        void onStationClick(Station station);

        void onErrorMessage(String error);
    }
}
