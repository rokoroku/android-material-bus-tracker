package kr.rokoroku.mbus.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.api.ApiNotAvailableException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.gbis.core.GbisRestClient;
import kr.rokoroku.mbus.api.gbis.core.GbisRestInterface;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestClient;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestInterface;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestClient;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestInterface;
import kr.rokoroku.mbus.api.seoulweb.core.SeoulWebRestClient;
import kr.rokoroku.mbus.api.seoulweb.core.SeoulWebRestInterface;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.data.RouteDataProvider;
import kr.rokoroku.mbus.data.SearchDataProvider;
import kr.rokoroku.mbus.data.StationDataProvider;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.Direction;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.ProgressCallback;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ValueCallback;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Response;

import static kr.rokoroku.mbus.util.ViewUtils.runOnUiThread;

/**
 * Created by rok on 2015. 6. 3..
 */
public class ApiFacade {

    private String apiKey;
    private static Client client;

    public static void init(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(10 * 1000, TimeUnit.MILLISECONDS);
        okHttpClient.setReadTimeout(15 * 1000, TimeUnit.MILLISECONDS);
        if (context != null) try {
            File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "HttpCache");
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(cacheDirectory, cacheSize);
            okHttpClient.setCache(cache);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ApiFacade.client = new OkClient(okHttpClient);
    }

    private static ApiFacade instance;

    public static ApiFacade getInstance() {
        if (instance == null) {
            String apiKey = BaseApplication.getInstance().getString(R.string.data_gov_openapi_key);
            instance = new ApiFacade(apiKey);
        }
        return instance;
    }

    private Handler handler;
    private GbisRestClient gbisRestClient;
    private GbisWebRestClient gbisWebRestClient;
    private SeoulBusRestClient seoulBusRestClient;
    private SeoulWebRestClient seoulWebRestClient;

    private ApiFacade(String apiKey) {
        if (client == null) client = new AndroidApacheClient();

        this.apiKey = apiKey;
        this.handler = new Handler(Looper.getMainLooper());
        this.gbisRestClient = new GbisRestClient(client, apiKey);
        this.gbisWebRestClient = new GbisWebRestClient(client);
        this.seoulBusRestClient = new SeoulBusRestClient(client, apiKey);
        this.seoulWebRestClient = new SeoulWebRestClient(client);
    }

    public ApiWrapperInterface getWrappedClient(Provider provider) {
        switch (provider) {
            case SEOUL:
                return seoulBusRestClient;
            case GYEONGGI:
                return gbisRestClient;
            case INCHEON:
        }
        return null;
    }

    public GbisRestClient getGbisRestClient() {
        return gbisRestClient;
    }

    public GbisWebRestClient getGbisWebRestClient() {
        return gbisWebRestClient;
    }

    public SeoulBusRestClient getSeoulBusRestClient() {
        return seoulBusRestClient;
    }

    public SeoulWebRestClient getSeoulWebRestClient() {
        return seoulWebRestClient;
    }

    public SeoulBusRestInterface getSeoulBusRestAdapter() {
        return seoulBusRestClient.getAdapter();
    }

    public GbisRestInterface getGbisRestAdapter() {
        return gbisRestClient.getAdapter();
    }

    public GbisWebRestInterface getGbisWebRestAdapter() {
        return gbisWebRestClient.getAdapter();
    }

    public SeoulWebRestInterface getSeoulWebRestAdapter() {
        return seoulWebRestClient.getAdapter();
    }

    public RouteDataProvider getRouteData(@NonNull Route route,
                                          @Nullable RouteDataProvider routeDataProvider,
                                          @Nullable ProgressCallback<Route> progressCallback) {
        return getRouteData(route.getProvider(), route.getId(), routeDataProvider, progressCallback);
    }

    /**
     * @param provider          data provider
     * @param routeId           route's id
     * @param routeDataProvider existing data provider
     * @param progressCallback  callback
     * @return new or existing RouteDataProvider object
     */
    public RouteDataProvider getRouteData(@NonNull Provider provider,
                                          @NonNull String routeId,
                                          @Nullable RouteDataProvider routeDataProvider,
                                          @Nullable ProgressCallback<Route> progressCallback) {
        // prepare data provider
        RouteDataProvider tempRouteDataProvider = routeDataProvider;
        if (tempRouteDataProvider == null) {
            Route route = new Route(routeId, null, provider);
            tempRouteDataProvider = new RouteDataProvider(route);
        }

        // retrieve stored data if exist
        Route storedRoute = DatabaseFacade.getInstance().getRoute(provider, routeId);
        if (storedRoute != null) {
            tempRouteDataProvider.setRoute(storedRoute);
        }

        final RouteDataProvider resultDataProvider = tempRouteDataProvider;
        final Route route = resultDataProvider.getRoute();
        final ApiWrapperInterface wrappedClient = getWrappedClient(provider);
        final ProgressCallback.ProgressRunner<Route> progressRunner
                = new ProgressCallback.ProgressRunner<>(progressCallback, 1);

        // check whether the data already has the information
        if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
            progressRunner.end(true, route);

        } else {
            // get wrapped interface for api call
            if (wrappedClient != null) {
                wrappedClient.getRouteBaseInfo(routeId, new ApiWrapperInterface.Callback<Route>() {
                    @Override
                    public void onSuccess(Route result) {
                        if (result != null) {
                            resultDataProvider.setRoute(result);
                            DatabaseFacade.getInstance().putRoute(provider, result);
                            if (result.getRouteStationList() != null) {
                                for (RouteStation station : result.getRouteStationList()) {
                                    DatabaseFacade.getInstance().putStation(station.getProvider(), station);
                                }
                            }
                        }
                        progressRunner.end(true, result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressRunner.error(t);
                    }
                });

            } else {
                progressRunner.error(new ApiNotAvailableException(provider));
            }
        }
        return resultDataProvider;
    }

    public StationDataProvider getStationData(@NonNull Station station,
                                              @Nullable StationDataProvider stationDataProvider,
                                              @Nullable ProgressCallback<Station> progressCallback) {
        return getStationData(station.getProvider(), station.getId(), stationDataProvider, progressCallback);
    }

    public StationDataProvider getStationData(@NonNull Provider provider,
                                              @NonNull String stationId,
                                              @Nullable StationDataProvider stationDataProvider,
                                              @Nullable ProgressCallback<Station> progressCallback) {
        // prepare data provider
        StationDataProvider tempStationDataProvider = stationDataProvider;
        if (tempStationDataProvider == null) {
            Station station = new Station(stationId, provider);
            tempStationDataProvider = new StationDataProvider(station);
        }

        // retrieve stored data if exist
        Station storedStation = DatabaseFacade.getInstance().getStation(provider, stationId);
        if (storedStation != null) {
            tempStationDataProvider.setStation(storedStation);
        }

        final StationDataProvider resultDataProvider = tempStationDataProvider;
        final Station station = resultDataProvider.getStation();
        final ProgressCallback.ProgressRunner<Station> outerProgressRunner
                = new ProgressCallback.ProgressRunner<>(progressCallback, 1);

        // check whether the data already has the information
        if (station.isEveryRouteInfoAvailable()) {
            outerProgressRunner.end(true, station);

        } else {
            // prepare station entry list to retrieve
            List<Pair<Provider, String>> localStationIdEntries = new ArrayList<>();
            String stationLocalIdByProvider = station.getLocalIdByProvider(provider);
            if (stationLocalIdByProvider != null) {
                localStationIdEntries.add(new Pair<>(provider, stationLocalIdByProvider));
            }

            List<Station.RemoteEntry> externalEntries = station.getRemoteEntries();
            if (externalEntries != null) {
                for (Station.RemoteEntry remoteEntry : externalEntries) {
                    String externalKey = remoteEntry.getKey();
                    Provider externalProvider = remoteEntry.getProvider();
                    localStationIdEntries.add(new Pair<>(externalProvider, externalKey));
                }
            }

            // prepare callback interfaces
            final List<Station> retrievedStationEntries = new ArrayList<>();
            final ProgressCallback<Station> finalWrappedCallback = new ProgressCallback<Station>() {
                @Override
                public void onComplete(boolean success, Station value) {
                    // get external entries from all
                    final Set<Station.RemoteEntry> remoteEntryList = new HashSet<>();
                    final Set<StationRoute> finalStationRoutes = new HashSet<>();
                    for (Station retrievedStation : retrievedStationEntries) {
                        final Provider provider = retrievedStation.getProvider();
                        final String localId = retrievedStation.getLocalId();
                        if (provider != null && localId != null) {
                            Station.RemoteEntry remoteEntry = new Station.RemoteEntry(provider, localId);
                            remoteEntryList.add(remoteEntry);
                        }
                        finalStationRoutes.addAll(retrievedStation.getStationRouteList());
                    }

                    // set external entries to all
                    for (Station externalStation : retrievedStationEntries) {
                        for (Station.RemoteEntry externalEntry : remoteEntryList) {
                            externalStation.addRemoteEntry(externalEntry);
                        }
                        externalStation.setStationRouteList(finalStationRoutes);
                        DatabaseFacade.getInstance().putStation(station.getProvider(), station);
                    }

                    station.setStationRouteList(finalStationRoutes);
                    resultDataProvider.setStation(station);

                    if (progressCallback != null) {
                        runOnUiThread(() -> progressCallback.onComplete(success, station));
                    }
                }

                @Override
                public void onProgressUpdate(int current, int target) {
                    if (progressCallback != null) {
                        runOnUiThread(() -> progressCallback.onProgressUpdate(current, target));
                    }
                }

                @Override
                public void onError(int progress, Throwable t) {
                    if (progressCallback != null) {
                        runOnUiThread(() -> progressCallback.onError(progress, t));
                    }
                }
            };
            final ProgressCallback.ProgressRunner innerProgressRunner =
                    new ProgressCallback.ProgressRunner<>(finalWrappedCallback, localStationIdEntries.size(), false);
            final ApiWrapperInterface.Callback<Station> stationEntryCallback = new ApiWrapperInterface.Callback<Station>() {
                @Override
                public void onSuccess(Station result) {
                    if (result != null && result.getLocalId() != null) {
                        retrievedStationEntries.add(result);
                    }
                    innerProgressRunner.progress();
                }

                @Override
                public void onFailure(Throwable t) {
                    innerProgressRunner.error(t);
                }
            };

            // retrieve base station data for every entries
            for (Pair<Provider, String> stationLocalIdEntry : localStationIdEntries) {
                Provider externalProvider = stationLocalIdEntry.first;
                String externalKey = stationLocalIdEntry.second;
                ApiWrapperInterface innerClient = getWrappedClient(externalProvider);

                // check local data if exists
                Station storedExternalStation = DatabaseFacade.getInstance().getStationWithSecondaryKey(externalProvider, externalKey);

                if (innerClient == null) {
                    innerProgressRunner.error(new ApiNotAvailableException(externalProvider));

                } else if (storedExternalStation != null) {
                    if (storedExternalStation.isLocalRouteInfoAvailable()) {
                        stationEntryCallback.onSuccess(storedExternalStation);

                    } else {
                        innerClient.getStationBaseInfo(stationId, stationEntryCallback);
                    }
                } else {
                    // search by given keyword (localId)
                    if (externalProvider.equals(Provider.SEOUL)) {
                        getSeoulWebRestClient().getStationBaseInfo(externalKey, stationEntryCallback);

                    } else {
                        innerClient.searchStationByKeyword(externalKey, new ApiWrapperInterface.Callback<List<Station>>() {
                            @Override
                            public void onSuccess(List<Station> result) {
                                DatabaseFacade.getInstance().putStations(innerClient.getProvider(), result);
                                if (result != null && !result.isEmpty()) {
                                    Station retrievedStation = null;
                                    for (Station resultEntry : result) {
                                        if (externalKey.equals(resultEntry.getLocalId())) {
                                            retrievedStation = resultEntry;
                                            break;
                                        }
                                    }
                                    if (retrievedStation != null) {
                                        innerClient.getStationBaseInfo(retrievedStation.getId(), stationEntryCallback);
                                        return;
                                    }
                                }
                                innerProgressRunner.progress();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                innerProgressRunner.error(t);
                            }
                        });
                    }
                }
            }
        }
        return resultDataProvider;
    }

    public RouteDataProvider getRouteRealtimeData(@NonNull Route route,
                                                  @Nullable RouteDataProvider routeDataProvider,
                                                  @Nullable ProgressCallback<List<BusLocation>> progressCallback) {
        return getRouteRealtimeData(route.getProvider(), route.getId(), routeDataProvider, progressCallback);
    }

    public RouteDataProvider getRouteRealtimeData(@NonNull Provider provider,
                                                  @NonNull String routeId,
                                                  @Nullable RouteDataProvider routeDataProvider,
                                                  @Nullable ProgressCallback<List<BusLocation>> progressCallback) {

        // prepare data provider
        RouteDataProvider tempRouteDataProvider = routeDataProvider;
        if (tempRouteDataProvider == null) {
            Route route = new Route(routeId, null, provider);
            tempRouteDataProvider = new RouteDataProvider(route);
        }

        // retrieve stored data if exist
        Route storedRoute = DatabaseFacade.getInstance().getRoute(provider, routeId);
        if (storedRoute != null) {
            tempRouteDataProvider.setRoute(storedRoute);
        }

        final RouteDataProvider resultDataProvider = tempRouteDataProvider;
        final ProgressCallback.ProgressRunner<List<BusLocation>> progressRunner
                = new ProgressCallback.ProgressRunner<>(progressCallback, 2);

        // 1. call getRouteData to ensure route base data is available
        getRouteData(provider, routeId, resultDataProvider, new SimpleProgressCallback<Route>() {
            @Override
            public void onComplete(boolean success, Route value) {
                progressRunner.progress();

                // 2. retrieve realtime info only when having station infomations
                Route route = resultDataProvider.getRoute();
                if (route.isRouteStationInfoAvailable()) {
                    ApiWrapperInterface wrappedClient = getWrappedClient(route.getProvider());
                    wrappedClient.getRouteRealtimeInfo(routeId, new ApiWrapperInterface.Callback<List<BusLocation>>() {
                        @Override
                        public void onSuccess(List<BusLocation> result) {
                            if (result != null) {
                                resultDataProvider.setBusPositionList(result);
                                progressRunner.setResult(result);
                            }
                            progressRunner.progress();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            progressRunner.error(t);
                        }
                    });
                } else {
                    progressRunner.end(false, null);
                }
            }

            @Override
            public void onError(int progress, Throwable t) {
                progressRunner.error(t);
            }
        });

        return resultDataProvider;
    }

    public SearchDataProvider searchByKeyword(@NonNull String keyword,
                                              @Nullable SearchDataProvider searchDataProvider,
                                              @Nullable ProgressCallback callback) {

        final String finalKeyword = keyword.replaceAll("-", "").toUpperCase();
        if (searchDataProvider == null) searchDataProvider = new SearchDataProvider();

        final SearchDataProvider resultDataProvider = searchDataProvider;
        final ProgressCallback.ProgressRunner progressRunner = new ProgressCallback.ProgressRunner(callback, 4);

        // search route
        getGbisRestClient().searchRouteByKeyword(finalKeyword, new ApiWrapperInterface.Callback<List<Route>>() {
            @Override
            public void onSuccess(List<Route> result) {
                if (result != null) {
                    resultDataProvider.addRouteData(result);
                    DatabaseFacade.getInstance().putRoutes(Provider.GYEONGGI, result);
                }
                progressRunner.progress();
                searchNextProvider();
            }

            @Override
            public void onFailure(Throwable t) {
                progressRunner.error(t);
                searchNextProvider();
            }

            public void searchNextProvider() {
                getSeoulBusRestClient().searchRouteByKeyword(finalKeyword, new ApiWrapperInterface.Callback<List<Route>>() {
                    @Override
                    public void onSuccess(List<Route> result) {
                        if (result != null) {
                            resultDataProvider.addRouteData(result);
                            DatabaseFacade.getInstance().putRoutes(Provider.SEOUL, result);
                        }
                        progressRunner.progress();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressRunner.error(t);
                    }
                });
            }
        });

        // search station
        getSeoulBusRestClient().searchStationByKeyword(finalKeyword, new ApiWrapperInterface.Callback<List<Station>>() {
            final Map<String, Station> seoulStationMap = new HashMap<>();
            final Set<Station> resultSet = new HashSet<>();

            @Override
            public void onSuccess(List<Station> result) {
                if (result != null) {
                    for (Station station : result) {
                        if (!TextUtils.isEmpty(station.getLocalId())) {
                            seoulStationMap.put(station.getLocalId(), station);
                            resultSet.add(station);
                        }
                    }
                }
                resultDataProvider.addStationData(resultSet);
                progressRunner.progress();
                searchNextProvider();
            }

            @Override
            public void onFailure(Throwable t) {
                progressRunner.error(t);
                searchNextProvider();
            }

            public void searchNextProvider() {
                getGbisRestClient().searchStationByKeyword(finalKeyword, new ApiWrapperInterface.Callback<List<Station>>() {
                    @Override
                    public void onSuccess(List<Station> result) {
                        if (result != null) {
                            for (Station station : result) {
                                String seoulId = station.getLocalIdByProvider(Provider.SEOUL);
                                if (seoulId != null && seoulStationMap.containsKey(seoulId)) {
                                    Station seoulStation = seoulStationMap.get(seoulId);
                                    seoulStation.addRemoteEntry(new Station.RemoteEntry(station.getProvider(), station.getLocalId()));

                                } else if (!TextUtils.isEmpty(station.getLocalId())) {
                                    resultSet.add(station);
                                }
                            }
                        }
                        resultDataProvider.addStationData(resultSet);
                        DatabaseFacade.getInstance().putStationsForEachProvider(resultSet);
                        progressRunner.progress();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressRunner.error(t);
                    }
                });
            }
        });

        // return result data provider
        return resultDataProvider;
    }

    public void searchByPosition(double latitude, double longitude, int radius,
                                 @Nullable ProgressCallback<List<Station>> callback) {
        final List<Station> resultList = new ArrayList<>();
        final ProgressCallback.ProgressRunner<List<Station>> progressRunner = new ProgressCallback.ProgressRunner<>(callback, 2);
        final ApiWrapperInterface.Callback<List<Station>> resultCallback = new ApiWrapperInterface.Callback<List<Station>>() {
            @Override
            public void onSuccess(List<Station> result) {
                if (result != null) synchronized (resultList) {
                    resultList.addAll(result);
                    progressRunner.setResult(resultList);
                }
                progressRunner.progress();
            }

            @Override
            public void onFailure(Throwable t) {
                progressRunner.error(t);
            }
        };
        if (radius < 0) radius = 1000;

        getSeoulWebRestClient().searchStationByLocation(latitude, longitude, radius, resultCallback);
        getGbisWebRestClient().searchStationByLocation(latitude, longitude, radius, resultCallback);
    }

    public void fillArrivalInfo(@NonNull Station station,
                                @Nullable StationRoute stationRoute,
                                @Nullable ProgressCallback<List<ArrivalInfo>> progressCallback) {

        final List<ArrivalInfo> resultArrivalInfos = new ArrayList<>();
        if (stationRoute == null) {
            // retrieve all route's arrival info
            final List<Station.RemoteEntry> stationEntries = new ArrayList<>();
            stationEntries.add(new Station.RemoteEntry(station.getProvider(), station.getLocalId()));
            if (station.getRemoteEntries() != null)
                stationEntries.addAll(station.getRemoteEntries());

            final ProgressCallback.ProgressRunner<List<ArrivalInfo>> progressRunner
                    = new ProgressCallback.ProgressRunner<>(progressCallback, stationEntries.size());

            for (Station.RemoteEntry remoteEntry : stationEntries) {
                final Provider provider = remoteEntry.getProvider();
                final String localId = remoteEntry.getKey();
                final Station externalStation = DatabaseFacade.getInstance().getStationWithSecondaryKey(provider, localId);
                final ApiWrapperInterface wrappedClient = getWrappedClient(provider);
                if (externalStation != null) {
                    if (wrappedClient != null) {
                        wrappedClient.getStationArrivalInfo(externalStation.getId(), new ApiWrapperInterface.Callback<List<ArrivalInfo>>() {
                            @Override
                            public void onSuccess(List<ArrivalInfo> result) {
                                if (result != null) {
                                    externalStation.setArrivalInfos(result);
                                    resultArrivalInfos.addAll(result);
                                }
                                progressRunner.setResult(resultArrivalInfos);
                                progressRunner.progress();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                progressRunner.error(t);
                            }
                        });
                    } else {
                        progressRunner.error(new ApiNotAvailableException(provider));
                    }
                } else {
                    getStationData(station, null, new SimpleProgressCallback<Station>() {
                        @Override
                        public void onComplete(boolean success, Station value) {
                            if (success && DatabaseFacade.getInstance().getStationWithSecondaryKey(provider, localId) != null) {
                                fillArrivalInfo(station, stationRoute, progressCallback);
                                progressRunner.progress();
                            } else {
                                progressRunner.end(success, null);
                            }
                        }

                        @Override
                        public void onError(int progress, Throwable t) {
                            progressRunner.error(t);
                        }
                    });
                }
            }

        } else {
            // retrieve specific route's arrival info
            final Provider provider = stationRoute.getProvider();
            final String routeId = stationRoute.getRouteId();
            final ApiWrapperInterface wrappedClient = getWrappedClient(provider);
            final ProgressCallback.ProgressRunner<List<ArrivalInfo>> progressRunner
                    = new ProgressCallback.ProgressRunner<>(progressCallback, 1);

            if (wrappedClient != null) {
                wrappedClient.getStationArrivalInfo(station.getId(), routeId, new ApiWrapperInterface.Callback<ArrivalInfo>() {
                    @Override
                    public void onSuccess(ArrivalInfo result) {
                        if (result != null) {
                            stationRoute.setArrivalInfo(result);
                            resultArrivalInfos.add(result);
                        }
                        progressRunner.setResult(resultArrivalInfos);
                        progressRunner.progress();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressRunner.error(t);
                    }
                });
            } else {
                progressRunner.error(new ApiNotAvailableException(provider));
            }
        }

    }

    public void getRouteMapLine(Route route, Callback<List<MapLine>> callback) {
//        Provider provider = route.getProvider();
//        ApiWrapperInterface wrappedClient = getWrappedClient(provider);
//        wrappedClient.getRouteMaplineInfo(route.getId(), new ApiWrapperInterface.Callback<List<MapLine>>() {
//            @Override
//            public void onSuccess(List<MapLine> result) {
//                callback.success(result, null);
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                callback.success(null, null);
//            }
//        });

        Provider provider = route.getProvider();
        switch (provider) {
            case GYEONGGI:
                getGbisWebRestAdapter().getRouteMapLine(route.getId(), new Callback<GbisSearchMapLineResult>() {
                    @Override
                    public void success(GbisSearchMapLineResult gbisSearchMapLineResult, Response response) {
                        List<MapLine> mapLineList = null;
                        if (gbisSearchMapLineResult != null && gbisSearchMapLineResult.isSuccess()) {
                            GbisSearchMapLineResult.ResultEntity.GgEntity resultEntity = gbisSearchMapLineResult.getResult().getGg();

                            mapLineList = new ArrayList<MapLine>();
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
                            route.setMapLineList(mapLineList);
                        }
                        if (callback != null) {
                            final List<MapLine> finalMapLineList = mapLineList;
                            handler.post(() -> callback.success(finalMapLineList, response));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.post(() -> callback.failure(error));
                    }
                });
                break;

            case SEOUL:
                getSeoulWebRestAdapter().getTopisRouteMapLine(route.getId(), new Callback<TopisMapLineResult>() {
                    @Override
                    public void success(TopisMapLineResult topisMapLineResult, Response response) {
                        List<MapLine> mapLineList = null;
                        if (topisMapLineResult != null && topisMapLineResult.result != null) {
                            mapLineList = new ArrayList<>();

                            //get turn station
                            List<RouteStation> routeStationList = route.getRouteStationList();
                            RouteStation turnStation = null;
                            if (routeStationList != null && route.getTurnStationSeq() != -1) {
                                for (RouteStation routeStation : routeStationList) {
                                    if (route.getTurnStationSeq() == routeStation.getSequence()) {
                                        turnStation = routeStation;
                                        break;
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
                        if (mapLineList != null) {
                            route.setMapLineList(mapLineList);
                        }
                        if (callback != null) {
                            final List<MapLine> finalMapLineList = mapLineList;
                            handler.post(() -> callback.success(finalMapLineList, response));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.post(() -> callback.failure(error));
                    }
                });
                break;
        }
    }

}
