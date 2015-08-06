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
import android.util.Log;
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
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
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

    private LocationClient mLocationClient;
    private CameraPosition mPreviousPosition;
    private OnEventListener mListener;

    private ArrayList<Marker> markers;
    private Map<String, Station> mStationTable;

    private Route mRouteExtra;
    private Station mStationExtra;
    private boolean isSearchEnable;
    private boolean isExtraInitiated = false;

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

        if (args != null) {
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
        Route cachedRoute = DatabaseFacade.getInstance().getRoute(route.getProvider(), route.getId());
        if (cachedRoute != null) route = cachedRoute;
        isExtraInitiated = false;
        isSearchEnable = false;
        mStationExtra = null;
        mRouteExtra = route;
        if (getMap() != null) {
            initRoute(route);
        }
    }

    public void setStation(Station station) {
        Station cachedStation = DatabaseFacade.getInstance().getStation(station.getProvider(), station.getId());
        if (cachedStation != null) station = cachedStation;
        isExtraInitiated = false;
        isSearchEnable = true;
        mStationExtra = station;
        mRouteExtra = null;

        if (getMap() != null) {
            initStation(station);
            initSearch();
        }
    }

    public void clearExtras() {
        isExtraInitiated = true;
        mStationExtra = null;
        mRouteExtra = null;
        isSearchEnable = true;
        GoogleMap map = getMap();
        if (map != null) {
            map.clear();
            markers = null;
            mStationTable = null;
            mPreviousPosition = null;
        }
        updateLocation(true);
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
        if (!isExtraInitiated) {
            isExtraInitiated = true;

            GoogleMap map = getMap();
            if (map != null && route != null) {
                map.clear();
                markers = null;
                mStationTable = null;

                ApiFacade.getInstance().getRouteData(route, null, new SimpleProgressCallback<Route>() {
                    @Override
                    public void onComplete(boolean success, Route value) {
                        mRouteExtra = value;
                        List<MapLine> mapLineList = value.getMapLineList();
                        if (mapLineList == null) {
                            ApiFacade.getInstance().getRouteMapLine(value, new Callback<List<MapLine>>() {
                                @Override
                                public void success(List<MapLine> mapLines, Response response) {
                                    value.setMapLineList(mapLines);
                                    drawRoute(value);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (mListener != null && getActivity() != null) {
                                        mListener.onErrorMessage(getString(R.string.error_failed_to_retrieve_map_line));
                                    }
                                }

                            });
                        } else {
                            new Handler().postDelayed(() -> drawRoute(route), 1000);
                        }

                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        if (mListener != null && getActivity() != null) {
                            mListener.onErrorMessage(getString(R.string.error_failed_to_retrieve_map_line));
                        }
                    }
                });
            }
        }
    }

    private synchronized void initStation(Station station) {
        if (!isExtraInitiated) {
            isExtraInitiated = true;

            GoogleMap map = getMap();
            if (map != null && station != null) {
                map.clear();
                markers = null;

                Marker marker = addStationMarker(station);
                LatLng latLng = new LatLng(station.getLatitude(), station.getLongitude());
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, DEFAULT_ZOOM_LEVEL, 0, 0)), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (marker != null) {
                            updateMarkers(map.getCameraPosition());
                            marker.setVisible(true);
                            marker.showInfoWindow();
                        }
                        initSearch();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }
    }

    private synchronized void initSearch() {
        isSearchEnable = true;
        GoogleMap map = getMap();
        LatLng latLng = map.getCameraPosition().target;
        ApiFacade.getInstance().searchByPosition(latLng.latitude, latLng.longitude, 1000, new SimpleProgressCallback<List<Station>>() {
            @Override
            public void onComplete(boolean success, List<Station> value) {
                List<Station> validStations = new ArrayList<>();
                if(value != null) {
                    for (Station station : value) {
                        if (!TextUtils.isEmpty(station.getLocalId())) {
                            validStations.add(station);
                        }
                    }
                    drawStations(validStations);
                }
            }

            @Override
            public void onError(int progress, Throwable t) {
                t.printStackTrace();
                if (mListener != null && getActivity() != null) {
                    mListener.onErrorMessage(t.getMessage());
                }
            }
        });
    }

    private synchronized void initLocationClient(Context context) {
        if (context == null) context = BaseApplication.getInstance();
        mLocationClient = LocationClient.with(context).listener(this).build();
    }

    @Override
    public synchronized void onMapReady(GoogleMap map) {

        Context context = getActivity();
        if(mListener != null) mListener.onMapLoaded(map);

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

        } else {
            initSearch();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        boolean updateMarker = mPreviousPosition == null
                || (int) mPreviousPosition.zoom != (int) cameraPosition.zoom
                || GeoUtils.calculateDistanceInMeter(cameraPosition.target, mPreviousPosition.target) > 500;
        boolean updateNearbyStation = isSearchEnable && updateMarker;

        if (updateMarker) {
            updateMarkers(cameraPosition);
        }
        if (updateNearbyStation) {
            LatLng latLng = cameraPosition.target;

            LatLngBounds bounds = getMap().getProjection().getVisibleRegion().latLngBounds;
            Log.d("onCameraChange", String.format("zoom: %f, radius: %.6f", cameraPosition.zoom, GeoUtils.getRadius(latLng, bounds)));
            int radius = (int) (GeoUtils.getRadius(latLng, bounds));

            if (radius > 3000) {
                if (mListener != null && getActivity() != null) {
                    mListener.onErrorMessage(getString(R.string.alert_zoom_to_search_nearby_station));
                }
            } else {
                if(radius > 1000) radius = 1000;
                ApiFacade.getInstance().searchByPosition(latLng.latitude, latLng.longitude, radius, new SimpleProgressCallback<List<Station>>() {
                    @Override
                    public void onComplete(boolean success, List<Station> value) {
                        drawStations(value);
                        updateMarkers(cameraPosition);
                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        t.printStackTrace();
                        if (mListener != null && getActivity() != null) {
                            mListener.onErrorMessage(getString(R.string.error_with_reason, t.getMessage()));
                        }
                    }
                });
            }
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
        if (mListener != null && getActivity() != null) {
            mListener.onLocationUpdate(location);
        }
        LocationClient.setLastKnownLocation(location);
    }

    @Override
    public void onError(String failReason, ConnectionResult connectionResult) {
        if (mListener != null && getActivity() != null) {
            mListener.onLocationUpdate(null);
            mListener.onErrorMessage(failReason);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String arsId = marker.getSnippet();
        if (arsId != null && !TextUtils.isEmpty(arsId) && mStationTable != null) {
            Station markerStation = mStationTable.get(arsId);
            if (markerStation != null && mListener != null && getActivity() != null) {
                mListener.onStationClick(markerStation);
            }
        }
    }

    private void drawRoute(Route route) {
        GoogleMap map = getMap();
        if (map != null) {
            map.clear();

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

                mStationTable = new HashMap<>();
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
                    mStationTable.put(routeStation.getLocalId(), routeStation);
                }

                if (lastStation == null) {
                    lastStation = routeStationList.get(routeStationList.size() - 1);
                }


                LatLng southWest = new LatLng(southWestLatitude, southWestLongitude);
                LatLng northEast = new LatLng(northEastLatitude, northEastLongitude);
                LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));

            } else {
                ApiFacade.getInstance().getRouteData(route, null, new SimpleProgressCallback() {
                    @Override
                    public void onComplete(boolean success, Object value) {
                        drawRoute(route);
                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        super.onError(progress, t);
                    }
                });
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
                if(station.getLocalId() != null) {
                    addStationMarker(station);
                }
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
                if (map != null) {
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
        if (markers != null && getMap() != null) {
            boolean visible = true;
            Bitmap bitmap = null;

            if (isSearchEnable) {
                LatLngBounds bounds = getMap().getProjection().getVisibleRegion().latLngBounds;
                int radius = (int) (GeoUtils.getRadius(cameraPosition.target, bounds));
                if(radius > 3000) {
                    visible = false;
                }
            } else if (cameraPosition.zoom < 11) {
                visible = false;
            }

            int size = (int) cameraPosition.zoom;
            bitmap = getScaledStationIcon(size);

            LatLngBounds bounds = getMap().getProjection().getVisibleRegion().latLngBounds;
            List<Marker> inBoundMarkers = new ArrayList<>(markers);
            for (Marker marker : inBoundMarkers) {
                if (bounds.contains(marker.getPosition())) {
                    boolean isInfoWindowShown = marker.isInfoWindowShown();
                    marker.setVisible(visible);
                    if (bitmap != null) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    }
                    if (isInfoWindowShown) {
                        marker.showInfoWindow();
                    }
                } else if(isSearchEnable) {
                    String localId = marker.getSnippet();
                    if(!TextUtils.isEmpty(localId) && mStationTable.remove(localId) != null) {
                        markers.remove(marker);
                        marker.remove();
                    } else {
                        marker.setVisible(false);
                    }
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
        void onMapLoaded(GoogleMap map);

        void onLocationUpdate(Location location);

        void onStationClick(Station station);

        void onErrorMessage(String error);
    }
}
