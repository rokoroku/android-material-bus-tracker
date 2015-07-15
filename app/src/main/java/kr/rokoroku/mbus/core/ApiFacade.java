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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.gbis.core.GbisRestClient;
import kr.rokoroku.mbus.api.gbis.core.GbisRestInterface;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrival;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestClient;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestInterface;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestClient;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestInterface;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrival;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
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
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.ProgressCallback;
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
                                          @Nullable ProgressCallback progressCallback) {
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
                                          @Nullable ProgressCallback progressCallback) {
        // prepare data provider
        RouteDataProvider tempRouteDataProvider = routeDataProvider;
        if (tempRouteDataProvider == null) {
            Route route = new Route(routeId, null, provider);
            tempRouteDataProvider = new RouteDataProvider(route);
        }

        // retrieve stored data if exist
        Route storedRoute = Database.getInstance().getRoute(provider, routeId);
        if (storedRoute != null) {
            tempRouteDataProvider.setRoute(storedRoute);
        }

        final RouteDataProvider resultDataProvider = tempRouteDataProvider;
        final Route route = resultDataProvider.getRoute();
        final ApiWrapperInterface wrappedClient = getWrappedClient(provider);
        final ProgressCallback.ProgressRunner progressRunner
                = new ProgressCallback.ProgressRunner(progressCallback, 1);

        // check whether the data already has the information
        if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
            progressRunner.end(true);

        } else {
            // get wrapped interface for api call
            if (wrappedClient != null) {
                wrappedClient.getRouteBaseInfo(routeId, new ApiWrapperInterface.Callback<Route>() {
                    @Override
                    public void onSuccess(Route result) {
                        if (result != null) {
                            resultDataProvider.setRoute(result);
                            Database.getInstance().putRoute(provider, result);
                        }
                        progressRunner.end(true);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressRunner.error(t);
                    }
                });

            } else {
                progressRunner.error(new ApiMethodNotSupportedException("API not implemented to given provider: " + provider.name()));
            }
        }
        return resultDataProvider;
    }

    public StationDataProvider getStationData(@NonNull Station station,
                                              @Nullable StationDataProvider stationDataProvider,
                                              @Nullable ProgressCallback progressCallback) {
        return getStationData(station.getProvider(), station.getId(), stationDataProvider, progressCallback);
    }

    public StationDataProvider getStationData(@NonNull Provider provider,
                                              @NonNull String stationId,
                                              @Nullable StationDataProvider stationDataProvider,
                                              @Nullable ProgressCallback progressCallback) {
        // prepare data provider
        StationDataProvider tempStationDataProvider = stationDataProvider;
        if (tempStationDataProvider == null) {
            Station station = new Station(stationId, provider);
            tempStationDataProvider = new StationDataProvider(station);
        }

        // retrieve stored data if exist
        Station storedStation = Database.getInstance().getStation(provider, stationId);
        if (storedStation != null) {
            tempStationDataProvider.setStation(storedStation);
        }

        final StationDataProvider resultDataProvider = tempStationDataProvider;
        final Station station = resultDataProvider.getStation();
        final ProgressCallback.ProgressRunner outerProgressRunner
                = new ProgressCallback.ProgressRunner(progressCallback, 1);

        // check whether the data already has the information
        if (station.isEveryRouteInfosAvailable()) {
            outerProgressRunner.end(true);

        } else {
            // prepare station entry list to retrieve
            List<Pair<Provider, String>> localStationIdEntries = new ArrayList<>();
            String stationLocalIdByProvider = station.getLocalIdByProvider(provider);
            if (stationLocalIdByProvider != null) {
                localStationIdEntries.add(new Pair<>(provider, stationLocalIdByProvider));
            }

            List<Station.ExternalEntry> externalEntries = station.getExternalEntries();
            if (externalEntries != null) {
                for (Station.ExternalEntry externalEntry : externalEntries) {
                    String externalKey = externalEntry.getKey();
                    Provider externalProvider = externalEntry.getProvider();
                    localStationIdEntries.add(new Pair<>(externalProvider, externalKey));
                }
            }

            // prepare callback interfaces
            final List<Station> externalStationList = new ArrayList<>();
            final ProgressCallback finalWrappedCallback = new ProgressCallback() {
                @Override
                public void onComplete(boolean success) {
                    // get external entries from all
                    final Set<Station.ExternalEntry> externalEntryList = new HashSet<>();
                    final Set<StationRoute> stationRoutes = new HashSet<>(station.getStationRouteList());
                    for (Station externalStation : externalStationList) {
                        final Provider provider = externalStation.getProvider();
                        final String localId = externalStation.getLocalId();
                        if (provider != null && localId != null) {
                            Station.ExternalEntry externalEntry = new Station.ExternalEntry(provider, localId);
                            externalEntryList.add(externalEntry);
                        }
                        stationRoutes.addAll(externalStation.getStationRouteList());
                    }

                    // set external entries to all
                    for (Station externalStation : externalStationList) {
                        for (Station.ExternalEntry externalEntry : externalEntryList) {
                            externalStation.addExternalEntry(externalEntry);
                        }
                        externalStation.setStationRouteList(stationRoutes);
                        Database.getInstance().putStationForEachProvider(station);
                    }

                    station.setStationRouteList(stationRoutes);
                    Database.getInstance().putStationForEachProvider(station);
                    resultDataProvider.setStation(station);

                    if (progressCallback != null) {
                        runOnUiThread(() -> progressCallback.onComplete(success));
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
                    new ProgressCallback.ProgressRunner(finalWrappedCallback, localStationIdEntries.size(), false);
            final ApiWrapperInterface.Callback<Station> externalStationEntryCallback = new ApiWrapperInterface.Callback<Station>() {
                @Override
                public void onSuccess(Station result) {
                    if (result != null && result.getLocalId() != null) {
                        externalStationList.add(result);
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
                Station storedExternalStation = Database.getInstance().getStationWithSecondaryKey(externalProvider, externalKey);

                if (innerClient == null) {
                    innerProgressRunner.error(new ApiMethodNotSupportedException("API not implemented to given provider:" + externalProvider.name()));

                } else if (storedExternalStation != null) {
                    if (storedExternalStation.isLocalRouteInfoAvailable()) {
                        externalStationList.add(storedExternalStation);
                        innerProgressRunner.progress();

                    } else {
                        innerClient.getStationBaseInfo(stationId, externalStationEntryCallback);
                    }
                } else {
                    // search by given keyword (localId)
                    if (externalProvider.equals(Provider.SEOUL)) {
                        getSeoulWebRestClient().getStationBaseInfo(externalKey, externalStationEntryCallback);

                    } else {
                        innerClient.searchStationByKeyword(externalKey, new ApiWrapperInterface.Callback<List<Station>>() {
                            @Override
                            public void onSuccess(List<Station> result) {
                                Database.getInstance().putStations(innerClient.getProvider(), result);
                                if (result != null && !result.isEmpty()) {
                                    Station retrievedStation = null;
                                    for (Station resultEntry : result) {
                                        if (externalKey.equals(resultEntry.getLocalId())) {
                                            retrievedStation = resultEntry;
                                            break;
                                        }
                                    }
                                    if (retrievedStation != null) {
                                        innerClient.getStationBaseInfo(retrievedStation.getId(), externalStationEntryCallback);
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
                                                  @Nullable ProgressCallback progressCallback) {
        return getRouteRealtimeData(route.getProvider(), route.getId(), routeDataProvider, progressCallback);
    }

    public RouteDataProvider getRouteRealtimeData(@NonNull Provider provider,
                                                  @NonNull String routeId,
                                                  @Nullable RouteDataProvider routeDataProvider,
                                                  @Nullable ProgressCallback progressCallback) {

        // prepare data provider
        RouteDataProvider tempRouteDataProvider = routeDataProvider;
        if (tempRouteDataProvider == null) {
            Route route = new Route(routeId, null, provider);
            tempRouteDataProvider = new RouteDataProvider(route);
        }

        // retrieve stored data if exist
        Route storedRoute = Database.getInstance().getRoute(provider, routeId);
        if (storedRoute != null) {
            tempRouteDataProvider.setRoute(storedRoute);
        }

        final RouteDataProvider resultDataProvider = tempRouteDataProvider;
        final ProgressCallback.ProgressRunner progressRunner
                = new ProgressCallback.ProgressRunner(progressCallback, 2);

        // 1. call getRouteData to ensure route base data is available
        getRouteData(provider, routeId, resultDataProvider, new SimpleProgressCallback() {
            @Override
            public void onComplete(boolean success) {
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
                            }
                            progressRunner.progress();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            progressRunner.error(t);
                        }
                    });
                } else {
                    progressRunner.end(false);
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

        if (searchDataProvider == null) searchDataProvider = new SearchDataProvider();
        final SearchDataProvider resultDataProvider = searchDataProvider;
        final ProgressCallback.ProgressRunner progressRunner = new ProgressCallback.ProgressRunner(callback, 4);

        // search route
        getGbisRestClient().searchRouteByKeyword(keyword, new ApiWrapperInterface.Callback<List<Route>>() {
            @Override
            public void onSuccess(List<Route> result) {
                if (result != null) {
                    resultDataProvider.addRouteData(result);
                    Database.getInstance().putRoutes(Provider.GYEONGGI, result);
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
                getSeoulBusRestClient().searchRouteByKeyword(keyword, new ApiWrapperInterface.Callback<List<Route>>() {
                    @Override
                    public void onSuccess(List<Route> result) {
                        if (result != null) {
                            resultDataProvider.addRouteData(result);
                            Database.getInstance().putRoutes(Provider.SEOUL, result);
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
        getSeoulBusRestClient().searchStationByKeyword(keyword, new ApiWrapperInterface.Callback<List<Station>>() {
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
                getGbisRestClient().searchStationByKeyword(keyword, new ApiWrapperInterface.Callback<List<Station>>() {
                    @Override
                    public void onSuccess(List<Station> result) {
                        if (result != null) {
                            for (Station station : result) {
                                String seoulId = station.getLocalIdByProvider(Provider.SEOUL);
                                if (seoulId != null && seoulStationMap.containsKey(seoulId)) {
                                    Station seoulStation = seoulStationMap.get(seoulId);
                                    seoulStation.addExternalEntry(new Station.ExternalEntry(station.getProvider(), station.getLocalId()));

                                } else if (!TextUtils.isEmpty(station.getLocalId())) {
                                    resultSet.add(station);
                                }
                            }
                        }
                        resultDataProvider.addStationData(resultSet);
                        Database.getInstance().putStationsForEachProvider(resultSet);
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

    public void fillArrivalInfo(@NonNull Station station,
                                @Nullable StationRoute stationRoute,
                                @Nullable ProgressCallback progressCallback) {
        if (stationRoute == null) {
            // retrieve all route's arrival info
            final List<Station.ExternalEntry> stationEntries = new ArrayList<>();
            stationEntries.add(new Station.ExternalEntry(station.getProvider(), station.getLocalId()));
            if (station.getExternalEntries() != null)
                stationEntries.addAll(station.getExternalEntries());

            final ProgressCallback.ProgressRunner progressRunner
                    = new ProgressCallback.ProgressRunner(progressCallback, stationEntries.size());

            for (Station.ExternalEntry externalEntry : stationEntries) {
                final Provider provider = externalEntry.getProvider();
                final String localId = externalEntry.getKey();
                final Station externalStation = Database.getInstance().getStationWithSecondaryKey(provider, localId);
                if (externalStation != null) {
                    getWrappedClient(provider).getStationArrivalInfo(externalStation.getId(), new ApiWrapperInterface.Callback<List<ArrivalInfo>>() {
                        @Override
                        public void onSuccess(List<ArrivalInfo> result) {
                            if (result != null) {
                                externalStation.setArrivalInfos(result);
                            }
                            progressRunner.progress();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            progressRunner.error(t);
                        }
                    });

                } else {
                    ApiFacade.this.getStationData(station, null, new SimpleProgressCallback() {
                        @Override
                        public void onComplete(boolean success) {
                            if (success && Database.getInstance().getStationWithSecondaryKey(provider, localId) != null) {
                                fillArrivalInfo(station, stationRoute, progressCallback);
                                progressRunner.progress();
                            } else {
                                progressRunner.end(success);
                            }
                        }

                        @Override
                        public void onError(int progress, Throwable t) {
                            progressRunner.error(t);
                        }
                    });
                    return;
                }
            }

        } else {
            // retrieve specific route's arrival info
            final Provider provider = stationRoute.getProvider();
            final String routeId = stationRoute.getRouteId();
            final ApiWrapperInterface wrappedClient = getWrappedClient(provider);
            final ProgressCallback.ProgressRunner progressRunner
                    = new ProgressCallback.ProgressRunner(progressCallback, 1);

            String localStationId = stationRoute.getLocalStationId();
            if (localStationId == null) localStationId = station.getLocalIdByProvider(provider);

            wrappedClient.getStationArrivalInfo(localStationId, routeId, new ApiWrapperInterface.Callback<ArrivalInfo>() {
                @Override
                public void onSuccess(ArrivalInfo result) {
                    if (result != null) {
                        stationRoute.setArrivalInfo(result);
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

    public void fillRouteData(@NonNull String routeId,
                              @NonNull RouteDataProvider routeDataProvider,
                              @NonNull ProgressCallback callback) {

        final int[] progress = {0, 1};

        // 1. retrieve base info (incl. station info)
        // 1-1. use local data if exist
        final Provider provider = routeDataProvider.getRoute().getProvider();
        Route storedRoute = Database.getInstance().getRoute(provider, routeId);
        if (storedRoute != null) {
            routeDataProvider.setRoute(storedRoute);
            runOnUiThread(() -> {
                callback.onProgressUpdate(++progress[0], progress[1]);
                if (progress[0] >= progress[1]) callback.onComplete(true);
            });
        }

        final Route route = routeDataProvider.getRoute();
        // 1-2. retrieve data remotely if data is not available
        if (!route.isRouteBaseInfoAvailable() || !route.isRouteStationInfoAvailable()) {
            switch (provider) {
                case GYEONGGI:
                    getGbisWebRestAdapter().getRouteInfo(route.getId(),
                            new ProgressWrappedCallback<GbisSearchRouteResult>(callback, progress) {
                                @Override
                                public void success(GbisSearchRouteResult result) {
                                    if (result != null && result.isSuccess()) {
                                        GbisSearchRouteResult.ResultEntity resultEntity = result.getResult();
                                        routeDataProvider.setGbisRouteEntity(resultEntity);
                                        routeDataProvider.setGbisStationEntity(resultEntity);
                                        if (routeDataProvider.getBusPositionList() == null) {
                                            routeDataProvider.setGbisRealtimeBusEntity(resultEntity);
                                        }
                                        Database.getInstance().putRoute(provider, routeDataProvider.getRoute());
                                    }
                                }
                            });
                    break;

                case SEOUL:
                    progress[1]++;

                    // 1-2-1. retrieve base info
                    SeoulBusRestInterface seoulBusRestClient = getSeoulBusRestAdapter();
                    if (!routeDataProvider.isRouteInfoAvailable()) {
                        seoulBusRestClient.getRouteInfo(routeId, new ProgressWrappedCallback<SeoulBusRouteInfoList>(callback, progress) {
                            @Override
                            void success(SeoulBusRouteInfoList seoulBusRouteInfoList) {
                                if (seoulBusRouteInfoList != null && seoulBusRouteInfoList.getItems().size() > 0) {
                                    route.setSeoulBusInfo(seoulBusRouteInfoList.getItems().get(0));
                                }
                                if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                                    Database.getInstance().putRoute(provider, route);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(++progress[0], progress[1]);
                            if (progress[0] >= progress[1]) callback.onComplete(true);
                        });
                    }

                    // 1-2-2. retrieve station info
                    if (routeDataProvider.getRouteStationList() == null) {
                        seoulBusRestClient.getRouteStationList(route.getId(),
                                new ProgressWrappedCallback<SeoulBusRouteStationList>(callback, progress) {
                                    @Override
                                    public void success(SeoulBusRouteStationList result) {
                                        if (result != null) {
                                            List<RouteStation> routeStationList = new ArrayList<>();
                                            for (SeoulBusRouteStation entity : result.getItems()) {
                                                RouteStation routeStation = new RouteStation(entity);
                                                routeStationList.add(routeStation);
                                                if (entity.getTrnstnid().equals(entity.getStationId())) {
                                                    route.setTurnStationSeq(entity.getSeq());
                                                    route.setTurnStationId(entity.getTrnstnid());
                                                }
                                            }
                                            routeDataProvider.setRouteStationList(routeStationList);
                                        }
                                        if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                                            Database.getInstance().putRoute(provider, route);
                                        }
                                    }
                                });
                    } else {
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(++progress[0], progress[1]);
                            if (progress[0] >= progress[1]) callback.onComplete(true);
                        });
                    }
                    break;
            }
        } else {
            runOnUiThread(() -> {
                callback.onProgressUpdate(++progress[0], progress[1]);
                if (progress[0] >= progress[1]) callback.onComplete(true);
            });
        }
    }

    public void fillStationData(@NonNull String stationId,
                                @NonNull StationDataProvider stationDataProvider,
                                @NonNull ProgressCallback callback) {
        final int[] progress = {0, 1};
        final Provider dataProvider = stationDataProvider.getProvider();
        final Station storedStation = Database.getInstance().getStation(dataProvider, stationId);

        // 1. retrieve base info
        if (storedStation != null) stationDataProvider.setStation(storedStation);

        final Station station = stationDataProvider.getStation();
        if (station.isLocalRouteInfoAvailable()) {

            // 1-1. check local data has external station
            List<Station.ExternalEntry> linkEntries = station.getExternalEntries();
            if (linkEntries == null || linkEntries.isEmpty()) {
                runOnUiThread(() -> {
                    callback.onProgressUpdate(++progress[0], progress[1]);
                    if (progress[0] >= progress[1]) callback.onComplete(true);
                });

            } else {
                // 1-2. prefare for linkEntries.size() tasks
                progress[1] = linkEntries.size();
                for (Station.ExternalEntry externalEntry : linkEntries) {
                    Provider linkEntryProvider = externalEntry.getProvider();

                    // 1-3. check whether some link data are already fetched or not
                    if (station.isExternalRouteInfoAvailable(linkEntryProvider)) {
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(++progress[0], progress[1]);
                            if (progress[0] >= progress[1]) callback.onComplete(true);
                        });

                    } else {
                        // 1-4. retrieve meta data by recursion
                        Station linkStation = Database.getInstance().getStationWithSecondaryKey(linkEntryProvider, externalEntry.getKey());
                        if (linkStation == null)
                            linkStation = new Station(externalEntry.getKey(), linkEntryProvider);

                        if (linkEntryProvider.equals(Provider.GYEONGGI)) {
                            getGbisWebRestAdapter().searchAll(externalEntry.getKey(), 1, 1, new Callback<GbisSearchAllResult>() {
                                @Override
                                public void success(GbisSearchAllResult gbisSearchAllResult, Response response) {
                                    if (gbisSearchAllResult.isSuccess()) {
                                        for (GbisSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity : gbisSearchAllResult.getResult().getBusStation().getList()) {
                                            if (listEntity.getStationNo().equals(externalEntry.getKey())) {
                                                Station station = new Station(listEntity);
                                                StationDataProvider linkStationDataProvider = new StationDataProvider(station);
                                                fillStationData(station.getId(), linkStationDataProvider, new SimpleProgressCallback() {
                                                    @Override
                                                    public void onComplete(boolean success) {
                                                        if (success) {
                                                            List<StationRoute> rawStationRouteList = stationDataProvider.getRawStationRouteList();
                                                            List<StationRoute> linkStationRouteList = linkStationDataProvider.getStation().getStationRouteList();

                                                            if (linkStationRouteList != null) {
                                                                for (StationRoute stationRoute : linkStationRouteList) {
                                                                    if (!rawStationRouteList.contains(stationRoute))
                                                                        rawStationRouteList.add(stationRoute);
                                                                }
                                                                linkStationDataProvider.putStationRouteList(rawStationRouteList);
                                                                linkStationDataProvider.getStation().addExternalEntry(new Station.ExternalEntry(station.getProvider(), station.getLocalId()));
                                                                Database.getInstance().putStation(stationDataProvider.getProvider(), stationDataProvider.getStation());
                                                                Database.getInstance().putStation(linkStationDataProvider.getProvider(), linkStationDataProvider.getStation());
                                                            }
                                                        }
                                                        runOnUiThread(() -> {
                                                            onProgressUpdate(++progress[0], progress[1]);
                                                            if (progress[0] == progress[1])
                                                                callback.onComplete(success);
                                                        });
                                                    }

                                                    @Override
                                                    public void onError(int innerProgress, Throwable t) {
                                                        runOnUiThread(() -> {
                                                            callback.onError(++progress[0], t);
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        runOnUiThread(() -> {
                                            callback.onProgressUpdate(++progress[0], progress[1]);
                                            if (progress[0] == progress[1])
                                                callback.onComplete(true);
                                        });
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    runOnUiThread(() -> {
                                        callback.onError(++progress[0], error);
                                        if (progress[0] == progress[1]) callback.onComplete(true);
                                    });
                                }
                            });

                        } else if (linkEntryProvider.equals(Provider.SEOUL)) {
                            linkStation.setLocalId(externalEntry.getKey());

                            StationDataProvider linkStationDataProvider = new StationDataProvider(linkStation);
                            fillStationData(linkStation.getId(), linkStationDataProvider, new SimpleProgressCallback() {
                                @Override
                                public void onComplete(boolean success) {
                                    if (success) {
                                        List<StationRoute> rawStationRouteList = stationDataProvider.getRawStationRouteList();
                                        List<StationRoute> linkStationRouteList = linkStationDataProvider.getStation().getStationRouteList();
                                        if (linkStationRouteList != null) {
                                            for (StationRoute stationRoute : linkStationRouteList) {
                                                if (!rawStationRouteList.contains(stationRoute))
                                                    rawStationRouteList.add(stationRoute);
                                            }
                                            linkStationDataProvider.putStationRouteList(rawStationRouteList);
                                            linkStationDataProvider.getStation().addExternalEntry(new Station.ExternalEntry(station.getProvider(), station.getLocalId()));
                                            Database.getInstance().putStation(stationDataProvider.getProvider(), stationDataProvider.getStation());
                                            Database.getInstance().putStation(linkStationDataProvider.getProvider(), linkStationDataProvider.getStation());
                                        }
                                    }
                                    runOnUiThread(() -> {
                                        onProgressUpdate(++progress[0], progress[1]);
                                        if (progress[0] == progress[1])
                                            callback.onComplete(success);
                                    });
                                }

                                @Override
                                public void onError(int innerProgress, Throwable t) {
                                    runOnUiThread(() -> callback.onError(progress[0], t));
                                }
                            });
                        }
                    }
                }
            }

        } else {
            // 2. remotely retrieve & store
            switch (dataProvider) {
                case GYEONGGI:
                    GbisWebRestInterface gbisWebRestClient = getGbisWebRestAdapter();
                    gbisWebRestClient.getStationInfo(stationId, new Callback<GbisStationRouteResult>() {
                        @Override
                        public void success(GbisStationRouteResult result, Response response) {
                            if (result != null && result.isSuccess()) {
                                GbisStationRouteResult.ResultEntity resultEntity = result.getResult();

                                // 2-1. put every route of station
                                Map<String, StationRoute> stationRoutes = new TreeMap<>();
                                String stationLocalId = station.getLocalIdByProvider(dataProvider);
                                for (GbisStationRouteResult.ResultEntity.BusStationInfoEntity busStationInfoEntity : resultEntity.getBusStationInfo()) {
                                    StationRoute stationRoute = new StationRoute(busStationInfoEntity, stationLocalId);
                                    stationRoutes.put(stationRoute.getRouteId(), stationRoute);
                                }

                                // 2-2. set arrivalinfo if presents
                                for (GbisStationRouteResult.ResultEntity.BusArrivalInfoEntity busArrivalInfoEntity : resultEntity.getBusArrivalInfo()) {
                                    ArrivalInfo arrivalInfo = new ArrivalInfo(busArrivalInfoEntity);
                                    StationRoute stationRoute = stationRoutes.get(arrivalInfo.getRouteId());
                                    if (stationRoute != null) {
                                        stationRoute.setRouteType(RouteType.valueOfGbis(busArrivalInfoEntity.getRouteTypeCd()));
                                    }
                                }

                                // 2-3. retrieve route's meta data
                                List<StationRoute> unknownTypeRoutes = new ArrayList<>();
                                for (StationRoute stationRoute : stationRoutes.values()) {
                                    if (stationRoute.getRoute() == null) {
                                        unknownTypeRoutes.add(stationRoute);
                                    }
                                }

                                int remainTaskSize = unknownTypeRoutes.size();
                                if (remainTaskSize > 0) {
                                    progress[1] += remainTaskSize;
                                    runOnUiThread(() -> callback.onProgressUpdate(++progress[0], progress[1]));
                                    for (StationRoute unknownTypeRoute : unknownTypeRoutes) {
                                        final Route route = new Route(unknownTypeRoute.getRouteId(), unknownTypeRoute.getRouteName(), unknownTypeRoute.getProvider());
                                        final RouteDataProvider routeDataProvider = new RouteDataProvider(route);
                                        fillRouteData(route.getId(), routeDataProvider, new SimpleProgressCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                progress[0]++;
                                                if (success) {
                                                    List<RouteStation> routeStationList = routeDataProvider.getRouteStationList();
                                                    for (RouteStation routeStation : routeStationList) {
                                                        if (routeStation.getId().equals(stationId)) {
                                                            unknownTypeRoute.setSequence(routeStation.getSequence());
                                                        }
                                                    }

                                                }
                                                if (progress[0] >= progress[1]) {
                                                    stationDataProvider.putStationRouteList(stationRoutes.values());
                                                    Database.getInstance().putStation(dataProvider, stationDataProvider.getStation());
                                                }
                                                runOnUiThread(() -> {
                                                    callback.onProgressUpdate(progress[0], progress[1]);
                                                    if (progress[0] >= progress[1]) {
                                                        callback.onComplete(success);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(int innerProgress, Throwable t) {
                                                runOnUiThread(() -> callback.onError(progress[0], t));
                                            }
                                        });
                                    }
                                } else {
                                    stationDataProvider.putStationRouteList(stationRoutes.values());
                                    Database.getInstance().putStation(dataProvider, stationDataProvider.getStation());
                                    runOnUiThread(() -> {
                                        callback.onProgressUpdate(++progress[0], progress[1]);
                                        if (progress[0] >= progress[1]) callback.onComplete(true);
                                    });
                                }
                            } else {
                                runOnUiThread(() -> {
                                    callback.onProgressUpdate(++progress[0], progress[1]);
                                    if (progress[0] >= progress[1]) callback.onComplete(true);
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            runOnUiThread(() -> {
                                callback.onError(progress[0], error);
                                callback.onProgressUpdate(++progress[0], progress[1]);
                                if (progress[0] >= progress[1]) callback.onComplete(true);
                            });
                        }

                    });
                    break;

                case SEOUL:
                    String seoulStationId = stationDataProvider.getStation().getLocalId();
                    getSeoulBusRestAdapter().getRouteByStation(seoulStationId, new Callback<SeoulBusRouteByStationList>() {
                        @Override
                        public void success(SeoulBusRouteByStationList result, Response response) {
                            if (result != null) {
                                // put every route of station
                                // TODO: filter gbis routes
                                List<StationRoute> stationRoutes = new ArrayList<>();
                                for (SeoulBusRouteByStation routeByStation : result.getItems()) {
                                    String stationLocalId = station.getLocalIdByProvider(dataProvider);
                                    StationRoute stationRoute = new StationRoute(routeByStation, stationLocalId);
                                    if (!stationRoute.getRouteType().equals(RouteType.RED_GYEONGGI)) {
                                        stationRoutes.add(stationRoute);
                                    }
                                }

                                // retrieve route's meta data
                                List<StationRoute> unknownTypeRoutes = new ArrayList<>();
                                for (StationRoute stationRoute : stationRoutes) {
                                    if (stationRoute.getRoute() == null) {
                                        unknownTypeRoutes.add(stationRoute);
                                    }
                                }

                                int remainTaskSize = unknownTypeRoutes.size();
                                if (remainTaskSize > 0) {
                                    progress[1] += remainTaskSize;
                                    runOnUiThread(() -> callback.onProgressUpdate(++progress[0], progress[1]));
                                    for (StationRoute unknownTypeRoute : unknownTypeRoutes) {
                                        final Route route = new Route(unknownTypeRoute.getRouteId(), unknownTypeRoute.getRouteName(), unknownTypeRoute.getProvider());
                                        final RouteDataProvider routeDataProvider = new RouteDataProvider(route);
                                        fillRouteData(route.getId(), routeDataProvider, new SimpleProgressCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                progress[0]++;

                                                if (progress[0] >= progress[1]) {
                                                    stationDataProvider.putStationRouteList(stationRoutes);
                                                    Database.getInstance().putStation(dataProvider, stationDataProvider.getStation());
                                                }

                                                runOnUiThread(() -> {
                                                    callback.onProgressUpdate(progress[0], progress[1]);
                                                    if (progress[0] >= progress[1]) {
                                                        callback.onComplete(success);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(int innerProgress, Throwable t) {
                                                callback.onError(progress[0], t);
                                            }
                                        });
                                    }
                                } else {
                                    stationDataProvider.putStationRouteList(stationRoutes);
                                    Database.getInstance().putStation(dataProvider, stationDataProvider.getStation());
                                    runOnUiThread(() -> {
                                        callback.onProgressUpdate(++progress[0], progress[1]);
                                        callback.onComplete(true);
                                    });
                                }
                            } else {
                                runOnUiThread(() -> {
                                    //callback.onError(++progress[0], new Throwable("Result none"));
                                    callback.onProgressUpdate(++progress[0], progress[1]);
                                    callback.onComplete(false);
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            runOnUiThread(() -> {
                                callback.onError(progress[0], error);
                                callback.onProgressUpdate(++progress[0], progress[1]);
                                if (progress[0] >= progress[1]) callback.onComplete(false);
                            });
                        }
                    });
                    break;

                default:
                    runOnUiThread(() -> {
                        callback.onProgressUpdate(++progress[0], progress[1]);
                        if (progress[0] >= progress[1]) callback.onComplete(false);
                    });
            }
        }
    }

    public void fillArrivalInfoData(@NonNull Station station,
                                    @NonNull ProgressCallback callback) {

        List<Station.ExternalEntry> linkEntries = station.getExternalEntries();
        if (linkEntries != null && linkEntries.size() > 0) {
            Map<Provider, List<StationRoute>> stationRouteMap = new HashMap<>();
            for (StationRoute stationRoute : station.getStationRouteList()) {
                Provider provider = stationRoute.getProvider();
                List<StationRoute> stationRoutes = stationRouteMap.get(provider);
                if (stationRoutes == null) {
                    stationRoutes = new ArrayList<>();
                    stationRouteMap.put(provider, stationRoutes);
                }
                stationRoutes.add(stationRoute);
            }

            final int[] progress = {0, stationRouteMap.size()};
            if (!stationRouteMap.isEmpty()) {
                SimpleProgressCallback innerCallback = new SimpleProgressCallback() {
                    @Override
                    public void onComplete(boolean success) {
                        callback.onProgressUpdate(++progress[0], progress[1]);
                        if (progress[0] >= progress[1]) callback.onComplete(success);
                    }

                    @Override
                    public void onError(int innerProgress, Throwable t) {
                        callback.onError(progress[0], t);
                    }
                };

                for (Provider provider : stationRouteMap.keySet()) {
                    fillArrivalInfoData(station, provider, innerCallback);
                }
            } else {
                callback.onProgressUpdate(++progress[0], progress[1]);
                if (progress[0] >= progress[1]) callback.onComplete(true);
            }
        } else {
            fillArrivalInfoData(station, station.getProvider(), callback);
        }
    }

    public void fillArrivalInfoData(@NonNull Station station,
                                    @NonNull Provider provider,
                                    @NonNull ProgressCallback callback) {
        fillArrivalInfoData(station, provider, null, callback);
    }

    public void fillArrivalInfoData(@NonNull Station station,
                                    @NonNull StationRoute stationRoute,
                                    @NonNull ProgressCallback callback) {
        fillArrivalInfoData(station, stationRoute.getProvider(), stationRoute, callback);
    }

    public void fillArrivalInfoData(@NonNull Station station,
                                    @NonNull Provider serviceProvider,
                                    @Nullable StationRoute stationRoute,
                                    @NonNull ProgressCallback callback) {
        final int[] progress = {0, 1};

        switch (serviceProvider) {
            case GYEONGGI:
                GbisRestInterface gbisRestClient = getGbisRestAdapter();
                Callback<GbisBusArrivalList> gbisCallback = new ProgressWrappedCallback<GbisBusArrivalList>(callback, progress) {
                    @Override
                    public void success(GbisBusArrivalList gbisBusArrivalList) {
                        ArrayList<ArrivalInfo> arrivalInfoList = new ArrayList<>();
                        if (gbisBusArrivalList != null) {
                            for (GbisBusArrival gbisBusArrival : gbisBusArrivalList.getItems()) {
                                ArrivalInfo arrivalInfo = new ArrivalInfo(gbisBusArrival);
                                arrivalInfoList.add(arrivalInfo);
                            }
                        }
                        if (stationRoute == null) {
                            station.setArrivalInfos(arrivalInfoList);
                        } else if (!arrivalInfoList.isEmpty()) {
                            station.putArrivalInfo(arrivalInfoList.get(0));
                        }
                    }
                };

                String stationId = station.getId();
                if (!station.getProvider().equals(serviceProvider)) {
                    String publicId = station.getLocalIdByProvider(serviceProvider);
                    Station secondaryKey = Database.getInstance().getStationWithSecondaryKey(serviceProvider, publicId);
                    if (secondaryKey != null) stationId = secondaryKey.getId();
                }

                if (stationRoute == null) {
                    // retrieve all
                    gbisRestClient.getBusArrivalList(stationId, gbisCallback);

                } else {
                    // retrieve single
                    String routeId = stationRoute.getRouteId();
                    gbisRestClient.getBusArrivalList(stationId, routeId, gbisCallback);
                }
                break;

            case SEOUL:
                List<StationRoute> stationRouteList = station.getStationRouteList();
                if (stationRouteList == null) {

                    // 1. retrieve stationRoute information (required for SEOUL data)
                    stationId = station.getLocalIdByProvider(serviceProvider);
                    StationDataProvider stationDataProvider = new StationDataProvider(station);
                    fillStationData(stationId, stationDataProvider, new SimpleProgressCallback() {
                        @Override
                        public void onComplete(boolean success) {
                            if (success && stationDataProvider.getRawStationRouteList() != null) {
                                fillArrivalInfoData(station, stationRoute, callback);
                            } else {
                                onError(0, new SeoulBusException(SeoulBusException.ERROR_SYSTEM, "StationRoute not found"));
                            }
                        }

                        @Override
                        public void onError(int innerProgress, Throwable t) {
                            runOnUiThread(() -> {
                                callback.onError(++progress[0], t);
                                if (progress[0] >= progress[1]) callback.onComplete(false);
                            });
                        }
                    });

                } else {
                    // 2. retrieve arrivalInfo
                    if (stationRoute == null) {
                        // 2-1. all
                        // this method will call retrieve single method recursively...)
                        progress[1] = stationRouteList.size();
                        if (progress[1] > 0) {
                            StationRoute firstStationRoute = stationRouteList.get(0);
                            if (firstStationRoute.getRouteId() != null) {
                                fillArrivalInfoData(station, firstStationRoute, new SimpleProgressCallback() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                runOnUiThread(() -> {
                                                    callback.onProgressUpdate(++progress[0], progress[1]);
                                                    if (progress[0] >= progress[1])
                                                        callback.onComplete(success);
                                                });
                                                if (success) {
                                                    startRetrieve();
                                                }
                                            }

                                            @Override
                                            public void onError(int innerProgress, Throwable t) {
                                                runOnUiThread(() -> callback.onError(++progress[0], t));
                                            }

                                            public void startRetrieve() {
                                                ProgressCallback innerCallback = new SimpleProgressCallback() {
                                                    @Override
                                                    public void onComplete(boolean success) {
                                                        runOnUiThread(() -> {
                                                            callback.onProgressUpdate(++progress[0], progress[1]);
                                                            if (progress[0] >= progress[1])
                                                                callback.onComplete(success);
                                                        });
                                                    }

                                                    @Override
                                                    public void onError(int innerProgress, Throwable t) {
                                                        runOnUiThread(() -> callback.onError(progress[0], t));
                                                    }
                                                };

                                                for (int i = 1; i < stationRouteList.size(); i++) {
                                                    StationRoute routeEntity = stationRouteList.get(i);
                                                    if (routeEntity.getRouteId() != null) {
                                                        fillArrivalInfoData(station, routeEntity, innerCallback);
                                                    } else {
                                                        runOnUiThread(() -> {
                                                            callback.onProgressUpdate(++progress[0], progress[1]);
                                                            if (progress[0] >= progress[1])
                                                                callback.onComplete(true);
                                                        });
                                                    }
                                                }
                                            }
                                        }

                                );
                            } else {
                                runOnUiThread(() -> {
                                    callback.onProgressUpdate(++progress[0], progress[1]);
                                    if (progress[0] >= progress[1]) callback.onComplete(true);
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                callback.onProgressUpdate(++progress[0], progress[1]);
                                if (progress[0] >= progress[1]) callback.onComplete(true);
                            });
                        }

                    } else {
                        // 2-2. single
                        if (!RouteType.UNKNOWN.equals(stationRoute.getRouteType())) {
                            Callback<SeoulBusArrivalList> singleCallback = new ProgressWrappedCallback<SeoulBusArrivalList>(callback, progress) {
                                @Override
                                public void success(SeoulBusArrivalList seoulBusArrivalList) {
                                    if (seoulBusArrivalList != null) {
                                        for (SeoulBusArrival arrival : seoulBusArrivalList.getItems()) {
                                            String seoulStationId = station.getId();
                                            Station seoulStation = null;
                                            if (station.getProvider().equals(Provider.SEOUL)) {
                                                seoulStationId = station.getId();
                                            } else {
                                                seoulStation = Database.getInstance().getStationWithSecondaryKey(Provider.SEOUL, station.getLocalIdByProvider(Provider.SEOUL));
                                                if (seoulStation != null) {
                                                    seoulStationId = seoulStation.getId();
                                                }
                                            }
                                            if (seoulStationId.equals(arrival.getStId())) {
                                                stationRoute.setSequence(Integer.parseInt(arrival.getStaOrd()));
                                                ArrivalInfo arrivalInfo = new ArrivalInfo(arrival);
                                                station.putArrivalInfo(arrivalInfo);
                                                station.setLastUpdateTime(new Date());
                                                if (seoulStation != null) {
                                                    seoulStation.putArrivalInfo(arrivalInfo);
                                                    seoulStation.setLastUpdateTime(new Date());
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            };
                            int sequence = stationRoute.getSequence();
                            String routeId = stationRoute.getRouteId();

                            if (sequence == -1) {
                                getSeoulBusRestAdapter().getArrivalInfo(routeId, singleCallback);
                            } else {
                                stationId = station.getId();
                                if (!station.getProvider().equals(serviceProvider)) {
                                    String externalProviderLocalId = station.getLocalIdByProvider(serviceProvider);
                                    Station secondaryKey = Database.getInstance().getStationWithSecondaryKey(serviceProvider, externalProviderLocalId);
                                    if (secondaryKey != null) stationId = secondaryKey.getId();
                                }
                                getSeoulBusRestAdapter().getArrivalInfo(stationId, routeId, sequence, singleCallback);
                            }

                        } else {
                            //TODO: redirect to gbis
                            runOnUiThread(() -> {
                                callback.onProgressUpdate(++progress[0], progress[1]);
                                if (progress[0] >= progress[1]) callback.onComplete(true);
                            });
                        }

                    }
                }
                break;

            default:
                break;
        }

    }

    public void getRouteMapLine(Route route, Callback<List<MapLine>> callback) {
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
                getSeoulWebRestAdapter().getRouteMapLine(route.getId(), new Callback<TopisMapLineResult>() {
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

    public static abstract class SimpleProgressCallback implements ProgressCallback {
        @Override
        public void onComplete(boolean success) {

        }

        @Override
        public void onProgressUpdate(int current, int target) {

        }

        @Override
        public void onError(int progress, Throwable t) {

        }
    }

    private abstract class ProgressWrappedCallback<T> implements Callback<T> {

        private int[] progress;
        private ProgressCallback callback;

        public ProgressWrappedCallback(ProgressCallback callback, int[] progress) {
            this.callback = callback;
            this.progress = progress;
        }

        abstract void success(T t);

        @Override
        public void success(T t, Response response) {
            this.success(t);
            runOnUiThread(() -> {
                callback.onProgressUpdate(++progress[0], progress[1]);
                if (progress[0] >= progress[1]) callback.onComplete(true);
            });
        }

        @Override
        public void failure(RetrofitError error) {
            runOnUiThread(() -> {
                callback.onError(++progress[0], error);
                if (progress[0] >= progress[1]) callback.onComplete(false);
            });
        }

    }

}
