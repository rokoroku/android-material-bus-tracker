package kr.rokoroku.mbus.api.gbisweb.core;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbis.core.GbisException;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchStationByPosResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.Direction;
import kr.rokoroku.mbus.data.model.District;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.ProgressCallback;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ViewUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Client;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisWebRestClient implements ApiWrapperInterface {

    private static final String BASE_URL = "http://www.gbis.go.kr/gbis2014";

    private Client client;
    private Provider provider;
    private GbisWebRestInterface adapter;
    private Map<String, WeakReference<GbisSearchAllResult>> searchResultCache;
    private Map<String, WeakReference<GbisSearchRouteResult>> searchRouteResultCache;
    private Map<String, WeakReference<GbisStationRouteResult>> stationRouteResultCache;
    private Timer cacheTimer;

    public GbisWebRestClient(Client client) {
        this.provider = Provider.GYEONGGI;
        this.client = client;
    }

    public GbisWebRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLog(new AndroidLog("GbisWebRestClient"))
                    .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                    .setConverter(new GsonConverter(new Gson(), "UTF-8"))
                    .build()
                    .create(GbisWebRestInterface.class);
        }
        return adapter;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        final int[] page = {0, 0};
        final GbisSearchAllResult cachedSearchResult = getCachedSearchResult(keyword);
        final retrofit.Callback<GbisSearchAllResult> searchRouteCallback = new retrofit.Callback<GbisSearchAllResult>() {
            @Override
            public void success(GbisSearchAllResult gbisWebSearchAllResult, Response response) {
                List<Route> routeList = null;
                if (gbisWebSearchAllResult != null && gbisWebSearchAllResult.isSuccess()) {

                    // put route entities
                    routeList = new ArrayList<>();
                    GbisSearchAllResult.ResultEntity.BusRouteEntity busRouteEntity = gbisWebSearchAllResult.getResult().getBusRoute();
                    if (busRouteEntity.getCount() > 0) {
                        for (GbisSearchAllResult.ResultEntity.BusRouteEntity.ListEntity listEntity : busRouteEntity.getList()) {
                            Route route = new Route(listEntity);

                            // exclude SEOUL route from GBIS result
                            if (District.SEOUL.equals(route.getDistrict()))
                                continue;

                            routeList.add(route);
                        }
                    }

                    // search one more time if there exist more entries
                    int routeCount = Integer.parseInt(gbisWebSearchAllResult.getResult().getBusRoute().getTotalCount());
                    if (cachedSearchResult == null && routeCount > 10 && page[0] < 1) {
                        getAdapter().searchAll(keyword, ++page[0], ++page[1], this);
                        return;
                    } else {
                        putSearchResultCache(keyword, gbisWebSearchAllResult);
                    }
                }
                callback.onSuccess(routeList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };

        if (cachedSearchResult != null) {
            searchRouteCallback.success(cachedSearchResult, null);
        } else {
            getAdapter().searchAll(keyword, 1, 1, searchRouteCallback);
        }
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        final int[] page = {1, 1};
        final GbisSearchAllResult cachedSearchResult = getCachedSearchResult(keyword);
        final retrofit.Callback<GbisSearchAllResult> searchStationCallback = new retrofit.Callback<GbisSearchAllResult>() {
            @Override
            public void success(GbisSearchAllResult gbisWebSearchAllResult, Response response) {
                List<Station> stationList = null;
                if (gbisWebSearchAllResult != null && gbisWebSearchAllResult.isSuccess()) {

                    // put station entities
                    stationList = new ArrayList<>();
                    GbisSearchAllResult.ResultEntity.BusStationEntity busStationEntity = gbisWebSearchAllResult.getResult().getBusStation();
                    if (busStationEntity.getCount() > 0) {
                        for (GbisSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity : busStationEntity.getList()) {
                            Station station = new Station(listEntity);

                            // exclude non-stop station
                            String localId = station.getLocalId();
                            if (!TextUtils.isEmpty(localId)) {
                                stationList.add(station);
                            }
                        }
                    }

                    // search one more time if there exist more entries
                    int stationCount = Integer.parseInt(gbisWebSearchAllResult.getResult().getBusStation().getTotalCount());
                    if (cachedSearchResult == null && stationCount > 10 && page[0] <= 1) {
                        getAdapter().searchAll(keyword, ++page[0], ++page[1], this);
                        return;
                    } else {
                        putSearchResultCache(keyword, gbisWebSearchAllResult);
                    }
                }
                callback.onSuccess(stationList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };

        if (cachedSearchResult != null) {
            searchStationCallback.success(cachedSearchResult, null);
        } else {
            getAdapter().searchAll(keyword, page[0], page[1], searchStationCallback);
        }
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback) {
        final Double[] mercator = GeoUtils.toMercator(longitude, latitude);
        getAdapter().searchStationByPos(mercator[1], mercator[0], radius, new retrofit.Callback<GbisSearchStationByPosResult>() {
            @Override
            public void success(GbisSearchStationByPosResult gbisSearchStationByPosResult, Response response) {
                List<Station> stationList = null;
                if (gbisSearchStationByPosResult != null && gbisSearchStationByPosResult.isSuccess()) {
                    stationList = new ArrayList<>();
                    if (gbisSearchStationByPosResult.getResult() != null) {
                        for (GbisSearchStationByPosResult.ResultEntity.ResultMapEntity.ListEntity listEntity :
                                gbisSearchStationByPosResult.getResult()) {
                            Station station = new Station(listEntity);
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
        final GbisSearchRouteResult cachedResult = getCachedSearchRouteResult(routeId);
        final retrofit.Callback<GbisSearchRouteResult> resultCallback = new retrofit.Callback<GbisSearchRouteResult>() {
            @Override
            public void success(GbisSearchRouteResult searchResult, Response response) {
                Route route = null;
                if (searchResult != null && searchResult.isSuccess()) {
                    putSearchRouteResultCache(routeId, searchResult);
                    route = new Route(searchResult.getResult());
                }
                callback.onSuccess(route);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedResult != null) {
            resultCallback.success(cachedResult, null);

        } else {
            getAdapter().getRouteInfo(routeId, resultCallback);
        }
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        final GbisSearchRouteResult cachedResult = getCachedSearchRouteResult(routeId);
        final retrofit.Callback<GbisSearchRouteResult> resultCallback = new retrofit.Callback<GbisSearchRouteResult>() {
            @Override
            public void success(GbisSearchRouteResult searchResult, Response response) {
                if (searchResult != null && searchResult.isSuccess()) {
                    putSearchRouteResultCache(routeId, searchResult);
                    GbisSearchRouteResult.ResultEntity resultEntity = searchResult.getResult();
                    Route route = new Route(resultEntity);
                    callback.onSuccess(route.getBusLocationList());

                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedResult != null) {
            resultCallback.success(cachedResult, null);

        } else {
            getAdapter().getRouteInfo(routeId, resultCallback);
        }
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getAdapter().getRouteMapLine(routeId, new retrofit.Callback<GbisSearchMapLineResult>() {
            @Override
            public void success(GbisSearchMapLineResult gbisSearchMapLineResult, Response response) {
                List<MapLine> mapLineList = null;
                if (gbisSearchMapLineResult != null && gbisSearchMapLineResult.isSuccess()) {
                    GbisSearchMapLineResult.ResultEntity.GgEntity resultEntity = gbisSearchMapLineResult.getResult().getGg();

                    mapLineList = new ArrayList<>();
                    for (GbisSearchMapLineResult.ResultEntity.GgEntity.UpLineEntity.ListEntity listEntity : resultEntity.getUpLine().getList()) {
                        if (TextUtils.isEmpty(listEntity.getLinkId())) {
                            MapLine mapLine = new MapLine();
                            mapLine.setDirection(Direction.UP);
                            double latitude = Double.parseDouble(listEntity.getLat());
                            double longitude = Double.parseDouble(listEntity.getLon());
                            LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
                            mapLine.setLatitude(latLng.latitude);
                            mapLine.setLongitude(latLng.longitude);

                            mapLineList.add(mapLine);
                        }
                    }

                    for (GbisSearchMapLineResult.ResultEntity.GgEntity.DownLineEntity.ListEntity listEntity : resultEntity.getDownLine().getList()) {
                        if (TextUtils.isEmpty(listEntity.getLinkId())) {
                            MapLine mapLine = new MapLine();
                            mapLine.setDirection(Direction.DOWN);
                            double latitude = Double.parseDouble(listEntity.getLat());
                            double longitude = Double.parseDouble(listEntity.getLon());
                            LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
                            mapLine.setLatitude(latLng.latitude);
                            mapLine.setLongitude(latLng.longitude);

                            mapLineList.add(mapLine);
                        }
                    }
                }
                if (mapLineList != null) {
                    Route route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                    if (route != null) route.setMapLineList(mapLineList);
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
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        final GbisStationRouteResult cachedStationRouteResult = getCachedStationRouteResult(stationId);
        final retrofit.Callback<GbisStationRouteResult> resultCallback = new retrofit.Callback<GbisStationRouteResult>() {
            @Override
            public void success(GbisStationRouteResult gbisStationRouteResult, Response response) {
                if (gbisStationRouteResult != null && gbisStationRouteResult.isSuccess()) {
                    putStationRouteResultCache(stationId, gbisStationRouteResult);

                    Station station = DatabaseFacade.getInstance().getStation(getProvider(), stationId);
                    if (station == null) station = new Station(stationId, getProvider());
                    station.setName(gbisStationRouteResult.getResult().getStationNm());

                    //TODO: handle case if localId is null
                    String localId = station.getLocalIdByProvider(provider);
                    if (localId == null) {
                        searchStationByKeyword(station.getName(), new Callback<List<Station>>() {
                            @Override
                            public void onSuccess(List<Station> result) {
                                Station resultStation = null;
                                if (result != null) {
                                    for (Station searchResult : result) {
                                        if (stationId.equals(searchResult.getId())) {
                                            resultStation = searchResult;
                                            break;
                                        }
                                    }
                                }
                                if (resultStation != null) {
                                    GbisStationRouteResult.ResultEntity resultEntity = gbisStationRouteResult.getResult();
                                    handleResult(resultStation, resultEntity, callback);

                                } else {
                                    callback.onFailure(new GbisException(GbisException.ERROR_NO_RESULT, "CANNOT FIND STATION: " + stationId));
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                callback.onFailure(t);
                            }
                        });
                    } else {
                        GbisStationRouteResult.ResultEntity resultEntity = gbisStationRouteResult.getResult();
                        handleResult(station, resultEntity, callback);
                    }
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }

            private void handleResult(Station station, GbisStationRouteResult.ResultEntity resultEntity, Callback<Station> callback) {
                String localId = station.getLocalIdByProvider(provider);

                // 1. parse retrieved data
                final Map<String, StationRoute> stationRouteMap = new HashMap<>();
                final Set<StationRoute> gbisStationRoutes = new HashSet<>();
                for (GbisStationRouteResult.ResultEntity.BusStationInfoEntity busStationInfoEntity : resultEntity.getBusStationInfo()) {
                    StationRoute stationRoute = new StationRoute(busStationInfoEntity, localId);
                    stationRouteMap.put(stationRoute.getRouteId(), stationRoute);
                }

                // 2. set arrivalinfo if presents
                for (GbisStationRouteResult.ResultEntity.BusArrivalInfoEntity busArrivalInfoEntity : resultEntity.getBusArrivalInfo()) {
                    ArrivalInfo arrivalInfo = new ArrivalInfo(busArrivalInfoEntity);
                    StationRoute stationRoute = stationRouteMap.get(arrivalInfo.getRouteId());
                    if (stationRoute != null) {
                        RouteType routeType = RouteType.valueOfGbis(busArrivalInfoEntity.getRouteTypeCd());
                        if (arrivalInfo.getBusArrivalItem1() != null && arrivalInfo.getBusArrivalItem1().getPlateNumber().startsWith("인천")) {
                            stationRoute.setProvider(Provider.INCHEON);
                            if (routeType == RouteType.GREEN_GYEONGGI) {
                                routeType = RouteType.GREEN_INCHEON;
                            } else if (routeType == RouteType.RED_GYEONGGI) {
                                routeType = RouteType.RED_INCHEON;
                            }
                        }
                        stationRoute.setArrivalInfo(arrivalInfo);
                        stationRoute.setRouteType(routeType);
                        stationRoute.setDestination(busArrivalInfoEntity.getRouteDestName());
                        if (!RouteType.checkSeoulRoute(routeType)) {
                            gbisStationRoutes.add(stationRoute);
                        }
                    }
                }

                // 3. filter routes where RouteType is unknown
                final List<StationRoute> unknownRoutes = new ArrayList<>();
                for (StationRoute stationRoute : stationRouteMap.values()) {
                    if (stationRoute.getRouteType() == null) {
                        unknownRoutes.add(stationRoute);
                    }
                }

                // 4. retrieve unknown typed route's meta data
                final Station finalStation = station;
                final ProgressCallback.ProgressRunner progressRunner = new ProgressCallback.ProgressRunner(new SimpleProgressCallback() {
                    @Override
                    public void onComplete(boolean success, Object value) {
                        if (success) {
                            for (StationRoute stationRoute : unknownRoutes) {
                                RouteType routeType = stationRoute.getRouteType();
                                if (!RouteType.checkSeoulRoute(routeType)) {
                                    gbisStationRoutes.add(stationRoute);
                                }
                            }
                            finalStation.putStationRouteList(gbisStationRoutes);
                        }
                        ViewUtils.runOnUiThread(() -> callback.onSuccess(finalStation));
                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        ViewUtils.runOnUiThread(() -> callback.onFailure(t));
                    }
                }, unknownRoutes.size(), false);
                for (StationRoute unknownRoute : unknownRoutes) {
                    getRouteBaseInfo(unknownRoute.getRouteId(), new Callback<Route>() {
                        @Override
                        public void onSuccess(Route result) {
                            if (result != null) {
                                unknownRoute.setRoute(result);

                                List<RouteStation> routeStationList = result.getRouteStationList();
                                if (routeStationList != null) {
                                    for (RouteStation routeStation : routeStationList) {
                                        if (routeStation.getId().equals(stationId)) {
                                            unknownRoute.setSequence(routeStation.getSequence());
                                        }
                                    }
                                }
                            }

                            progressRunner.progress();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            progressRunner.error(t);
                        }
                    });
                }
            }
        };
        if (cachedStationRouteResult != null) {
            resultCallback.success(cachedStationRouteResult, null);
        } else {
            getAdapter().getStationInfo(stationId, resultCallback);
        }
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        getStationBaseInfo(stationId, new Callback<Station>() {
            @Override
            public void onSuccess(Station result) {
                List<ArrivalInfo> resultList = null;
                if (result != null) {
                    resultList = result.getArrivalInfoList();
                }
                callback.onSuccess(resultList);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        getStationBaseInfo(stationId, new Callback<Station>() {
            @Override
            public void onSuccess(Station result) {
                ArrivalInfo resultInfo = null;
                if (result != null) {
                    resultInfo = result.getArrivalInfo(routeId);
                }
                callback.onSuccess(resultInfo);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private synchronized void putSearchResultCache(String keyword, GbisSearchAllResult gbisSearchAllResult) {
        if (searchResultCache == null) {
            searchResultCache = new HashMap<>();
        }
        searchResultCache.put(keyword, new WeakReference<>(gbisSearchAllResult));
    }

    private GbisSearchAllResult getCachedSearchResult(String keyword) {
        if (searchResultCache != null) {
            WeakReference<GbisSearchAllResult> reference = searchResultCache.get(keyword);
            if (reference != null) {
                GbisSearchAllResult gbisSearchAllResult = reference.get();
                if (gbisSearchAllResult != null) {
                    return gbisSearchAllResult;
                } else synchronized (this) {
                    searchResultCache.remove(keyword);
                }
            }
        }
        return null;
    }

    private synchronized void putSearchRouteResultCache(String routeId, GbisSearchRouteResult gbisSearchRouteResult) {
        if (searchRouteResultCache == null) {
            searchRouteResultCache = new HashMap<>();
        }
        if (!searchRouteResultCache.containsKey(routeId)) {
            searchRouteResultCache.put(routeId, new WeakReference<>(gbisSearchRouteResult));

            if (cacheTimer == null) {
                cacheTimer = new Timer();
            }

            int TTL = BaseApplication.REFRESH_INTERVAL / 2;
            //noinspection ConstantConditions
            if (TTL < 5000) TTL = 5000;

            cacheTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (GbisWebRestClient.this) {
                        searchRouteResultCache.remove(routeId);
                    }
                }
            }, TTL);
        }
    }

    private GbisSearchRouteResult getCachedSearchRouteResult(String routeId) {
        if (searchRouteResultCache != null) {
            WeakReference<GbisSearchRouteResult> reference = searchRouteResultCache.get(routeId);
            if (reference != null) {
                GbisSearchRouteResult gbisSearchRouteResult = reference.get();
                if (gbisSearchRouteResult != null) {
                    return gbisSearchRouteResult;
                } else synchronized (this) {
                    searchRouteResultCache.remove(routeId);
                }
            }
        }
        return null;
    }

    private synchronized void putStationRouteResultCache(String stationId, GbisStationRouteResult gbisStationRouteResult) {
        if (stationRouteResultCache == null) {
            stationRouteResultCache = new HashMap<>();
        }
        if (!stationRouteResultCache.containsKey(stationId)) {
            stationRouteResultCache.put(stationId, new WeakReference<>(gbisStationRouteResult));

            if (cacheTimer == null) {
                cacheTimer = new Timer();
            }

            int TTL = BaseApplication.REFRESH_INTERVAL / 2;
            //noinspection ConstantConditions
            if (TTL < 5000) TTL = 5000;

            cacheTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (GbisWebRestClient.this) {
                        stationRouteResultCache.remove(stationId);
                    }
                }
            }, TTL);
        }
    }

    private GbisStationRouteResult getCachedStationRouteResult(String stationId) {
        if (stationRouteResultCache != null) {
            WeakReference<GbisStationRouteResult> reference = stationRouteResultCache.get(stationId);
            if (reference != null) {
                GbisStationRouteResult stationRouteResult = reference.get();
                if (stationRouteResult != null) {
                    return stationRouteResult;
                } else synchronized (this) {
                    stationRouteResultCache.remove(stationId);
                }
            }
        }
        return null;
    }

}
