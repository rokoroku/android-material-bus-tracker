package kr.rokoroku.mbus.api.gbisweb.core;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbisweb.model.SearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchStationByPosResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.core.Database;
import kr.rokoroku.mbus.model.ArrivalInfo;
import kr.rokoroku.mbus.model.BusLocation;
import kr.rokoroku.mbus.model.Direction;
import kr.rokoroku.mbus.model.District;
import kr.rokoroku.mbus.model.MapLine;
import kr.rokoroku.mbus.model.Provider;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;
import kr.rokoroku.mbus.model.StationRoute;
import kr.rokoroku.mbus.util.GeoUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
    private Map<String, WeakReference<SearchAllResult>> searchResultCache;
    private Map<String, WeakReference<SearchRouteResult>> searchRouteResultCache;
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
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
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
        final SearchAllResult cachedSearchResult = getCachedSearchResult(keyword);
        final retrofit.Callback<SearchAllResult> searchRouteCallback = new retrofit.Callback<SearchAllResult>() {
            @Override
            public void success(SearchAllResult gbisWebSearchAllResult, Response response) {
                List<Route> routeList = null;
                if (gbisWebSearchAllResult != null && gbisWebSearchAllResult.isSuccess()) {

                    // put route entities
                    routeList = new ArrayList<>();
                    SearchAllResult.ResultEntity.BusRouteEntity busRouteEntity = gbisWebSearchAllResult.getResult().getBusRoute();
                    if (busRouteEntity.getCount() > 0) {
                        for (SearchAllResult.ResultEntity.BusRouteEntity.ListEntity listEntity : busRouteEntity.getList()) {
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
            getAdapter().searchAll(keyword, 0, 0, searchRouteCallback);
        }
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        final int[] page = {0, 0};
        final SearchAllResult cachedSearchResult = getCachedSearchResult(keyword);
        final retrofit.Callback<SearchAllResult> searchStationCallback = new retrofit.Callback<SearchAllResult>() {
            @Override
            public void success(SearchAllResult gbisWebSearchAllResult, Response response) {
                List<Station> stationList = null;
                if (gbisWebSearchAllResult != null && gbisWebSearchAllResult.isSuccess()) {

                    // put station entities
                    stationList = new ArrayList<>();
                    SearchAllResult.ResultEntity.BusStationEntity busStationEntity = gbisWebSearchAllResult.getResult().getBusStation();
                    if (busStationEntity.getCount() > 0) {
                        for (SearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity : busStationEntity.getList()) {
                            Station station = new Station(listEntity);

                            // exclude non-stop station
                            if (!TextUtils.isEmpty(station.getLocalId())) {
                                stationList.add(station);
                            }
                        }
                    }

                    // search one more time if there exist more entries
                    int stationCount = Integer.parseInt(gbisWebSearchAllResult.getResult().getBusStation().getTotalCount());
                    if (cachedSearchResult == null && stationCount > 10 && page[0] < 1) {
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
            getAdapter().searchAll(keyword, 0, 0, searchStationCallback);
        }
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, Callback<List<Station>> callback) {
        final Double[] mercator = GeoUtils.toMercator(longitude, latitude);
        getAdapter().searchStationByPos(mercator[1], mercator[0], 1000, new retrofit.Callback<SearchStationByPosResult>() {
            @Override
            public void success(SearchStationByPosResult searchStationByPosResult, Response response) {
                List<Station> stationList = null;
                if (searchStationByPosResult != null && searchStationByPosResult.isSuccess()) {
                    List<SearchStationByPosResult.ResultEntity.ResultMapEntity.ListEntity> entityList
                            = searchStationByPosResult.getResult().getResultMap().getList();
                    stationList = new ArrayList<>();
                    if (entityList != null) {
                        for (SearchStationByPosResult.ResultEntity.ResultMapEntity.ListEntity listEntity : entityList) {
                            Station station = new Station(listEntity.getStationId(), Provider.GYEONGGI);
                            String localId = listEntity.getStaNo().trim();
                            if (!TextUtils.isEmpty(localId)) {
                                station.setLocalId(localId);
                            }
                            double latitude = Double.parseDouble(listEntity.getLat());
                            double longitude = Double.parseDouble(listEntity.getLon());
                            LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
                            station.setLatitude(latLng.latitude);
                            station.setLongitude(latLng.longitude);
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
        final SearchRouteResult cachedSearchRouteResult = getCachedSearchRouteResult(routeId);
        final retrofit.Callback<SearchRouteResult> resultCallback = new retrofit.Callback<SearchRouteResult>() {
            @Override
            public void success(SearchRouteResult searchResult, Response response) {
                Route route = Database.getInstance().getRoute(provider, routeId);
                if (route == null) route = new Route(routeId, null, provider);
                if (searchResult != null && searchResult.isSuccess()) {
                    putSearchRouteResultCache(routeId, searchResult);
                    SearchRouteResult.ResultEntity resultEntity = searchResult.getResult();
                    route.setGbisRouteEntity(resultEntity);
                    route.setGbisStationEntity(resultEntity);
                    route.setGbisRealtimeBusEntity(resultEntity);
                }
                callback.onSuccess(route);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        };
        if (cachedSearchRouteResult != null) {
            resultCallback.success(cachedSearchRouteResult, null);
        } else {
            getAdapter().getRouteInfo(routeId, resultCallback);
        }
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        getAdapter().getRouteInfo(routeId, new retrofit.Callback<SearchRouteResult>() {
            @Override
            public void success(SearchRouteResult searchResult, Response response) {
                Route route = Database.getInstance().getRoute(provider, routeId);
                if (route == null) route = new Route(routeId, null, provider);
                if (searchResult != null && searchResult.isSuccess()) {
                    SearchRouteResult.ResultEntity resultEntity = searchResult.getResult();
                    route.setGbisRouteEntity(resultEntity);
                    route.setGbisStationEntity(resultEntity);
                    route.setGbisRealtimeBusEntity(resultEntity);
                }
                callback.onSuccess(route.getBusLocationList());
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getAdapter().getRouteMapLine(routeId, new retrofit.Callback<SearchMapLineResult>() {
            @Override
            public void success(SearchMapLineResult searchMapLineResult, Response response) {
                List<MapLine> mapLineList = null;
                if (searchMapLineResult != null && searchMapLineResult.isSuccess()) {
                    SearchMapLineResult.ResultEntity.GgEntity resultEntity = searchMapLineResult.getResult().getGg();

                    mapLineList = new ArrayList<>();
                    for (SearchMapLineResult.ResultEntity.GgEntity.UpLineEntity.ListEntity listEntity : resultEntity.getUpLine().getList()) {
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

                    for (SearchMapLineResult.ResultEntity.GgEntity.DownLineEntity.ListEntity listEntity : resultEntity.getDownLine().getList()) {
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
                    Route route = Database.getInstance().getRoute(provider, routeId);
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
        getAdapter().getStationInfo(stationId, new retrofit.Callback<SearchStationResult>() {
            @Override
            public void success(SearchStationResult searchStationResult, Response response) {
                Station station = null;
                if (searchStationResult != null && searchStationResult.isSuccess()) {
                    station = Database.getInstance().getStation(provider, stationId);
                    if (station == null) station = new Station(stationId, provider);

                    //TODO: handle case if localId is null
                    String localId = station.getLocalIdByProvider(provider);
                    SearchStationResult.ResultEntity resultEntity = searchStationResult.getResult();

                    // 1. parse retrieved data
                    station.setName(resultEntity.getStationNm());
                    Map<String, StationRoute> stationRouteMap = new HashMap<>();
                    for (SearchStationResult.ResultEntity.BusStationInfoEntity busStationInfoEntity : resultEntity.getBusStationInfo()) {
                        StationRoute stationRoute = new StationRoute(busStationInfoEntity, localId);
                        stationRouteMap.put(stationRoute.getRouteId(), stationRoute);
                    }

                    // 2. set arrivalinfo if presents
                    for (SearchStationResult.ResultEntity.BusArrivalInfoEntity busArrivalInfoEntity : resultEntity.getBusArrivalInfo()) {
                        ArrivalInfo arrivalInfo = new ArrivalInfo(busArrivalInfoEntity);
                        StationRoute stationRoute = stationRouteMap.get(arrivalInfo.getRouteId());
                        if (stationRoute != null) {
                            stationRoute.setRouteType(RouteType.valueOfGbis(busArrivalInfoEntity.getRouteTypeCd()));
                        }
                    }

                    // 3. retrieve route's meta data if not presented
                    List<StationRoute> unknownRoutes = new ArrayList<>();
                    for (StationRoute stationRoute : stationRouteMap.values()) {
                        if (stationRoute.getRoute() == null) {
                            unknownRoutes.add(stationRoute);
                        }
                    }

                    int remainTaskSize = unknownRoutes.size();
                    if (remainTaskSize > 0) {
                        final int[] progress = {0};
                        final Throwable[] error = {null};
                        final Station finalStation = station;
                        for (StationRoute unknownTypeRoute : unknownRoutes) {
                            getRouteBaseInfo(unknownTypeRoute.getRouteId(), new Callback<Route>() {
                                @Override
                                public void onSuccess(Route result) {
                                    if (result != null && result.getRouteStationList() != null) {
                                        for (RouteStation routeStation : result.getRouteStationList()) {
                                            if (routeStation.getId().equals(stationId)) {
                                                unknownTypeRoute.setSequence(routeStation.getSequence());
                                            }
                                        }
                                    }
                                    checkCompletion();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                    error[0] = t;
                                    checkCompletion();
                                }

                                private void checkCompletion() {
                                    int currentTask = ++progress[0];
                                    if (currentTask >= remainTaskSize) {
                                        finalStation.setStationRouteList(stationRouteMap.values());
                                        Throwable throwable = error[0];
                                        if (throwable != null) {
                                            callback.onFailure(throwable);
                                        } else {
                                            callback.onSuccess(finalStation);
                                        }
                                    }
                                }
                            });
                        }
                        return;
                    }
                }
                callback.onSuccess(station);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    private synchronized void putSearchResultCache(String keyword, SearchAllResult searchAllResult) {
        if (searchResultCache == null) {
            searchResultCache = new HashMap<>();
        }
        searchResultCache.put(keyword, new WeakReference<>(searchAllResult));
    }

    private SearchAllResult getCachedSearchResult(String keyword) {
        if (searchResultCache != null) {
            WeakReference<SearchAllResult> reference = searchResultCache.get(keyword);
            if (reference != null) {
                SearchAllResult searchAllResult = reference.get();
                if (searchAllResult != null) {
                    return searchAllResult;
                } else synchronized (this) {
                    searchResultCache.remove(keyword);
                }
            }
        }
        return null;
    }

    private synchronized void putSearchRouteResultCache(String routeId, SearchRouteResult searchRouteResult) {
        if (searchRouteResultCache == null) {
            searchRouteResultCache = new HashMap<>();
        }
        if (!searchResultCache.containsKey(routeId)) {
            searchRouteResultCache.put(routeId, new WeakReference<>(searchRouteResult));

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

    private SearchRouteResult getCachedSearchRouteResult(String routeId) {
        if (searchRouteResultCache != null) {
            WeakReference<SearchRouteResult> reference = searchRouteResultCache.get(routeId);
            if (reference != null) {
                SearchRouteResult searchRouteResult = reference.get();
                if (searchRouteResult != null) {
                    return searchRouteResult;
                } else synchronized (this) {
                    searchRouteResultCache.remove(routeId);
                }
            }
        }
        return null;
    }
}
