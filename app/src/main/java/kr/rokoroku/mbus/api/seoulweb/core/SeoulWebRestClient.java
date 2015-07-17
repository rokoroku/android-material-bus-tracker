package kr.rokoroku.mbus.api.seoulweb.core;

import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationByPositionResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisRealtimeResult;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.Direction;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.GeoUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Client;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 4. 22..
 */
public class SeoulWebRestClient implements ApiWrapperInterface {

    private Client client;
    private Provider provider;
    private SeoulWebRestInterface adapter;

    private Map<String, WeakReference<StationRouteResult>> stationRouteCache;
    private Timer cacheTimer;

    public SeoulWebRestClient(Client client) {
        this.client = client;
        this.provider = Provider.SEOUL;
    }

    public SeoulWebRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint("http:")
                    .setClient(client)
                    .setLog(new AndroidLog("SeoulWebRestClient"))
                    .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                    .setConverter(new SeoulWebResponseConverter())
                    .build()
                    .create(SeoulWebRestInterface.class);
        }
        return adapter;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        String encoded;
        try {
            encoded = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encoded = keyword;
        }
        getAdapter().searchRoute(encoded, new retrofit.Callback<SearchRouteResult>() {
            @Override
            public void success(SearchRouteResult searchRouteResult, Response response) {
                if (searchRouteResult != null) {
                    if (searchRouteResult.header.isSuccess()) {
                        List<Route> result = new ArrayList<>();
                        if (searchRouteResult.resultList != null) {
                            for (SearchRouteResult.RouteEntity resultEntity : searchRouteResult.resultList) {
                                Route route = new Route(resultEntity);
                                if (RouteType.checkSeoulRoute(route.getType())) {
                                    result.add(route);
                                }
                            }
                        }
                        callback.onSuccess(result);

                    } else {
                        callback.onFailure(new SeoulWebException(searchRouteResult.header));
                    }
                } else {
                    callback.onFailure(new SeoulWebException(SeoulBusException.ERROR_NO_RESULT, "NO RESPONSE"));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        String encoded;
        try {
            encoded = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encoded = keyword;
        }
        getAdapter().searchStation(encoded, new retrofit.Callback<SearchStationResult>() {
            @Override
            public void success(SearchStationResult searchStationResult, Response response) {
                List<Station> result = null;
                if (searchStationResult != null && searchStationResult.header.isSuccess()) {
                    result = new ArrayList<>();
                    if (searchStationResult.result != null) {
                        for (SearchStationResult.StationEntity stationEntity : searchStationResult.result) {
                            Station station = new Station(stationEntity);
                            result.add(station);
                        }
                    }
                } else if(searchStationResult != null) {
                    callback.onFailure(new SeoulWebException(searchStationResult.header));
                }
                callback.onSuccess(result);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });

    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback) {
        getAdapter().searchStationByPos(latitude, longitude, 1000, new retrofit.Callback<StationByPositionResult>() {
            @Override
            public void success(StationByPositionResult searchStationResult, Response response) {
                List<Station> stationList = null;
                if (searchStationResult != null) {
                    stationList = new ArrayList<>();
                    if (searchStationResult.getItems() != null) {
                        for (StationByPositionResult.ResultEntity resultEntity : searchStationResult.getItems()) {
                            Station station = new Station(resultEntity);
                            stationList.add(station);
                        }
                    }
                }
                callback.onSuccess(stationList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        final retrofit.Callback<RouteStationResult> resultCallback = new retrofit.Callback<RouteStationResult>() {
            @Override
            public void success(RouteStationResult routeStationResult, Response response) {
                if (routeStationResult != null && routeStationResult.header.isSuccess()) {
                    Route route = DatabaseFacade.getInstance().getRoute(getProvider(), routeId);
                    if (route == null) route = new Route(routeId, null, getProvider());
                    final Route finalRoute = route;

                    final ArrayList<RouteStation> routeStationList = new ArrayList<>();
                    for (RouteStationResult.StationEntity stationEntity : routeStationResult.resultList) {
                        if (route.getName() == null) route.setName(stationEntity.busRouteNm);
                        RouteStation routeStation = new RouteStation(stationEntity);
                        routeStationList.add(routeStation);
                    }
                    route.setRouteStationList(routeStationList);

                    if (!route.isRouteBaseInfoAvailable()) {
                        searchRouteByKeyword(route.getName().replaceAll("[^0-9]", ""), new Callback<List<Route>>() {
                            @Override
                            public void onSuccess(List<Route> result) {
                                if (result != null) {
                                    for (Route retrievedRoute : result) {
                                        if (retrievedRoute.getId().equals(routeId)) {
                                            retrievedRoute.setRouteStationList(routeStationList);
                                            callback.onSuccess(retrievedRoute);
                                            return;
                                        }
                                    }
                                }
                                callback.onSuccess(finalRoute);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                callback.onSuccess(finalRoute);
                            }
                        });
                    } else {
                        callback.onSuccess(route);
                    }
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        getAdapter().getRouteInfo(routeId, resultCallback);
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        getAdapter().getTopisRealtimeRoute(routeId, new retrofit.Callback<TopisRealtimeResult>() {
            @Override
            public void success(TopisRealtimeResult topisRealtimeResult, Response response) {
                List<BusLocation> busLocationList = null;
                if (topisRealtimeResult != null && topisRealtimeResult.result != null) {
                    RouteType routeType = null;
                    Route route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                    if (route != null) routeType = route.getType();

                    busLocationList = new ArrayList<>();
                    for (TopisRealtimeResult.ResultEntity resultEntity : topisRealtimeResult.result) {
                        BusLocation busLocation = new BusLocation(resultEntity);
                        busLocation.setRouteId(routeId);
                        busLocation.setType(routeType);
                        busLocationList.add(busLocation);
                    }
                }
                callback.onSuccess(busLocationList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getAdapter().getTopisRouteMapLine(routeId, new retrofit.Callback<TopisMapLineResult>() {
            @Override
            public void success(TopisMapLineResult topisMapLineResult, Response response) {
                List<MapLine> mapLineList = null;
                if (topisMapLineResult != null && topisMapLineResult.result != null) {
                    mapLineList = new ArrayList<>();

                    //get turn station if available
                    RouteStation turnStation = null;
                    Route route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                    if (route != null) {
                        List<RouteStation> routeStationList = route.getRouteStationList();
                        if (routeStationList != null && route.getTurnStationSeq() != -1) {
                            for (RouteStation routeStation : routeStationList) {
                                if (route.getTurnStationSeq() == routeStation.getSequence()) {
                                    turnStation = routeStation;
                                    break;
                                }
                            }
                        }
                    }

                    Direction direction = Direction.UP;
                    for (TopisMapLineResult.ResultEntity resultEntity : topisMapLineResult.result) {
                        MapLine mapLine = new MapLine();
                        LatLng latLng = GeoUtils.convertTm(resultEntity.x, resultEntity.y);
                        mapLine.setLatitude(latLng.latitude);
                        mapLine.setLongitude(latLng.longitude);

                        if (turnStation != null) {
                            LatLng turnLatLng = new LatLng(turnStation.getLatitude(), turnStation.getLongitude());
                            if (GeoUtils.calculateDistanceInMeter(turnLatLng, latLng) < 100) {
                                direction = Direction.DOWN;
                                turnStation = null;
                            }
                        }
                        mapLine.setDirection(direction);
                        mapLineList.add(mapLine);
                    }
                }
                callback.onSuccess(mapLineList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getStationBaseInfo(String arsId, Callback<Station> callback) {
        final StationRouteResult cachedRouteStationResult = getCachedStationResult(arsId);
        final retrofit.Callback<StationRouteResult> resultCallback = new retrofit.Callback<StationRouteResult>() {
            @Override
            public void success(StationRouteResult stationRouteResult, Response response) {
                if (stationRouteResult != null && stationRouteResult.header.isSuccess()) {
                    putStationResultCache(arsId, stationRouteResult);
                    Station station = new Station(stationRouteResult);
                    callback.onSuccess(station);

                } else if (stationRouteResult != null) {
                    callback.onFailure(new SeoulWebException(stationRouteResult.header));

                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedRouteStationResult != null) {
            resultCallback.success(cachedRouteStationResult, null);

        } else {
            getAdapter().getStationInfo(arsId, resultCallback);
        }
    }

    @Override
    public void getStationArrivalInfo(String arsId, Callback<List<ArrivalInfo>> callback) {
        final StationRouteResult cachedRouteStationResult = getCachedStationResult(arsId);
        final retrofit.Callback<StationRouteResult> resultCallback = new retrofit.Callback<StationRouteResult>() {
            @Override
            public void success(StationRouteResult stationRouteResult, Response response) {
                if (stationRouteResult != null && stationRouteResult.header.isSuccess()) {
                    putStationResultCache(arsId, stationRouteResult);
                    Station station = new Station(stationRouteResult);
                    callback.onSuccess(station.getArrivalInfoList());

                } else if (stationRouteResult != null) {
                    callback.onFailure(new SeoulWebException(stationRouteResult.header));

                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedRouteStationResult != null) {
            resultCallback.success(cachedRouteStationResult, null);
        } else {
            getAdapter().getStationInfo(arsId, resultCallback);
        }
    }

    @Override
    public void getStationArrivalInfo(String arsId, String routeId, Callback<ArrivalInfo> callback) {
        final StationRouteResult cachedRouteStationResult = getCachedStationResult(arsId);
        final retrofit.Callback<StationRouteResult> resultCallback = new retrofit.Callback<StationRouteResult>() {
            @Override
            public void success(StationRouteResult stationRouteResult, Response response) {
                if (stationRouteResult != null && stationRouteResult.header.isSuccess()) {
                    putStationResultCache(arsId, stationRouteResult);
                    Station station = new Station(stationRouteResult);
                    callback.onSuccess(station.getArrivalInfo(routeId));

                } else if (stationRouteResult != null) {
                    callback.onFailure(new SeoulWebException(stationRouteResult.header));

                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedRouteStationResult != null) {
            resultCallback.success(cachedRouteStationResult, null);
        } else {
            getAdapter().getStationInfo(arsId, resultCallback);
        }
    }


    private synchronized void putStationResultCache(String routeId, StationRouteResult routeStationResult) {
        if (stationRouteCache == null) {
            stationRouteCache = new HashMap<>();
        }
        if (!stationRouteCache.containsKey(routeId)) {
            stationRouteCache.put(routeId, new WeakReference<>(routeStationResult));

            if (cacheTimer == null) {
                cacheTimer = new Timer();
            }

            int TTL = BaseApplication.REFRESH_INTERVAL / 2;
            //noinspection ConstantConditions
            if (TTL < 5000) TTL = 5000;

            cacheTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (SeoulWebRestClient.this) {
                        stationRouteCache.remove(routeId);
                    }
                }
            }, TTL);
        }
    }

    private StationRouteResult getCachedStationResult(String routeId) {
        if (stationRouteCache != null) {
            WeakReference<StationRouteResult> reference = stationRouteCache.get(routeId);
            if (reference != null) {
                StationRouteResult cachedResult = reference.get();
                if (cachedResult != null) {
                    return cachedResult;
                } else synchronized (this) {
                    stationRouteCache.remove(routeId);
                }
            }
        }
        return null;
    }
}
