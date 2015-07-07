package kr.rokoroku.mbus.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.adapter.RouteDataProvider;
import kr.rokoroku.mbus.adapter.SearchDataProvider;
import kr.rokoroku.mbus.adapter.StationDataProvider;
import kr.rokoroku.mbus.api.gbis.core.GbisRestClient;
import kr.rokoroku.mbus.api.gbis.core.GbisRestInterface;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrival;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocation;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestClient;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestInterface;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusStationResult;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestClient;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestInterface;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrival;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfo;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;
import kr.rokoroku.mbus.model.ArrivalInfo;
import kr.rokoroku.mbus.model.BusLocation;
import kr.rokoroku.mbus.model.District;
import kr.rokoroku.mbus.model.Provider;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import kr.rokoroku.mbus.model.StationRoute;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 6. 3..
 */
public class ApiCaller {

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
        ApiCaller.client = new OkClient(okHttpClient);
    }

    private static ApiCaller instance;

    public static ApiCaller getInstance() {
        if (instance == null) {
            String apiKey = BaseApplication.getInstance().getString(R.string.data_gov_openapi_key);
            instance = new ApiCaller(apiKey);
        }
        return instance;
    }

    private Handler handler;
    private SeoulBusRestClient seoulBusRestClient;
    private GbisRestClient gbisRestClient;
    private GbisWebRestClient gbisWebRestClient;

    private ApiCaller(String apiKey) {
        if (client == null) client = new AndroidApacheClient();

        this.apiKey = apiKey;
        this.handler = new Handler(Looper.getMainLooper());
        this.gbisRestClient = new GbisRestClient(client, apiKey);
        this.gbisWebRestClient = new GbisWebRestClient(client);
        this.seoulBusRestClient = new SeoulBusRestClient(client, apiKey);
    }

    public SeoulBusRestInterface getSeoulBusRestClient() {
        return seoulBusRestClient.getAdapter();
    }

    public GbisRestInterface getGbisRestClient() {
        return gbisRestClient.getAdapter();
    }

    public GbisWebRestInterface getGbisWebRestClient() {
        return gbisWebRestClient.getAdapter();
    }

    public void fillRouteData(@NonNull String routeId,
                              @NonNull RouteDataProvider routeDataProvider,
                              @NonNull ProgressCallback callback) {

        final int[] progress = {0, 1};

        // 1. retrieve base info (incl. station info)
        // 1-1. use local data if exist
        final Provider provider = routeDataProvider.getRoute().getProvider();
        Route storedRoute = DatabaseHelper.getInstance().getRoute(provider, routeId);
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
                    getGbisWebRestClient().getRouteInfo(route.getId(),
                            new ProgressWrappedCallback<GbisWebSearchBusRouteResult>(callback, progress) {
                                @Override
                                public void success(GbisWebSearchBusRouteResult result) {
                                    if (result != null && result.isSuccess()) {
                                        GbisWebSearchBusRouteResult.ResultEntity resultEntity = result.getResult();
                                        routeDataProvider.setGbisRouteEntity(resultEntity);
                                        routeDataProvider.setGbisStationEntity(resultEntity);
                                        if (routeDataProvider.getBusPositionList() == null) {
                                            routeDataProvider.setGbisRealtimeBusEntity(resultEntity);
                                        }
                                        DatabaseHelper.getInstance().putRoute(provider, routeDataProvider.getRoute());
                                    }
                                }
                            });
                    break;

                case SEOUL:
                    progress[1]++;

                    // 1-2-1. retrieve base info
                    SeoulBusRestInterface seoulBusRestClient = getSeoulBusRestClient();
                    if (!routeDataProvider.isRouteInfoAvailable()) {
                        seoulBusRestClient.getRouteInfo(routeId, new ProgressWrappedCallback<SeoulBusRouteInfoList>(callback, progress) {
                            @Override
                            void success(SeoulBusRouteInfoList seoulBusRouteInfoList) {
                                if (seoulBusRouteInfoList != null && seoulBusRouteInfoList.getItems().size() > 0) {
                                    route.setSeoulBusInfo(seoulBusRouteInfoList.getItems().get(0));
                                }
                                if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                                    DatabaseHelper.getInstance().putRoute(provider, route);
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
                                            DatabaseHelper.getInstance().putRoute(provider, route);
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

    public void fillRouteRealtimeLocation(@NonNull RouteDataProvider routeDataProvider,
                                          @NonNull ProgressCallback callback) {
        final int[] progress = {0, 1};

        Route route = routeDataProvider.getRoute();
        if (route != null && route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
            switch (route.getProvider()) {
                case GYEONGGI:
                    getGbisRestClient().getBusLocationList(route.getId(),
                            new ProgressWrappedCallback<GbisBusLocationList>(callback, progress) {
                                @Override
                                public void success(GbisBusLocationList busInfoList) {
                                    if (busInfoList != null) {
                                        ArrayList<BusLocation> busLocationList = new ArrayList<>();
                                        for (GbisBusLocation gbisBusLocation : busInfoList.getItems()) {
                                            BusLocation busLocation = new BusLocation(gbisBusLocation);
                                            busLocation.setType(route.getType());
                                            busLocationList.add(busLocation);
                                        }
                                        routeDataProvider.setBusPositionList(busLocationList);
                                    }
                                }
                            });
                    break;

                case SEOUL:
                    getSeoulBusRestClient().getRouteBusPositionList(String.valueOf(route.getId()),
                            new ProgressWrappedCallback<SeoulBusLocationList>(callback, progress) {
                                @Override
                                void success(SeoulBusLocationList result) {
                                    if (result != null) {
                                        List<BusLocation> busLocationList = new ArrayList<>();
                                        for (SeoulBusLocation entity : result.getItems()) {
                                            BusLocation busLocation = new BusLocation(entity);
                                            busLocationList.add(busLocation);
                                        }
                                        routeDataProvider.setBusPositionList(busLocationList);
                                    }
                                }
                            });
                    break;
            }
        } else if (route != null) {
            fillRouteData(route.getId(), routeDataProvider, new SimpleProgressCallback() {
                @Override
                public void onComplete(boolean success) {
                    if (success && route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                        runOnUiThread(() -> callback.onProgressUpdate(progress[0], progress[1]));
                        fillRouteRealtimeLocation(routeDataProvider, callback);
                    } else {
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(++progress[0], progress[1]);
                            callback.onComplete(false);
                        });
                    }
                }

                @Override
                public void onError(int progress, Throwable t) {
                    callback.onError(progress, t);
                    callback.onComplete(false);
                }
            });
        } else {
            runOnUiThread(() -> {
                callback.onError(progress[1], new Throwable("Route's base infomation is not available!"));
                callback.onComplete(false);
            });
        }

    }

    public void fillStationData(@NonNull String stationId,
                                @NonNull StationDataProvider stationDataProvider,
                                @NonNull ProgressCallback callback) {
        final int[] progress = {0, 1};
        final Provider dataProvider = stationDataProvider.getProvider();
        final Station storedStation = DatabaseHelper.getInstance().getStation(dataProvider, stationId);

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
                    if (station.isLinkedRouteInfoAvailable(linkEntryProvider)) {
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(++progress[0], progress[1]);
                            if (progress[0] >= progress[1]) callback.onComplete(true);
                        });

                    } else {
                        // 1-4. retrieve meta data by recursion
                        Station linkStation = DatabaseHelper.getInstance().getStationWithSecondaryKey(linkEntryProvider, externalEntry.getKey());
                        if (linkStation == null)
                            linkStation = new Station(externalEntry.getKey(), linkEntryProvider);

                        if (linkEntryProvider.equals(Provider.GYEONGGI)) {
                            getGbisWebRestClient().searchAll(externalEntry.getKey(), 1, 1, new Callback<GbisWebSearchAllResult>() {
                                @Override
                                public void success(GbisWebSearchAllResult gbisWebSearchAllResult, Response response) {
                                    if (gbisWebSearchAllResult.isSuccess()) {
                                        for (GbisWebSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity : gbisWebSearchAllResult.getResult().getBusStation().getList()) {
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
                                                                DatabaseHelper.getInstance().putStation(stationDataProvider.getProvider(), stationDataProvider.getStation());
                                                                DatabaseHelper.getInstance().putStation(linkStationDataProvider.getProvider(), linkStationDataProvider.getStation());
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
                                            DatabaseHelper.getInstance().putStation(stationDataProvider.getProvider(), stationDataProvider.getStation());
                                            DatabaseHelper.getInstance().putStation(linkStationDataProvider.getProvider(), linkStationDataProvider.getStation());
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
                    GbisWebRestInterface gbisWebRestClient = getGbisWebRestClient();
                    gbisWebRestClient.getStationInfo(stationId, new Callback<GbisWebSearchBusStationResult>() {
                        @Override
                        public void success(GbisWebSearchBusStationResult result, Response response) {
                            if (result != null && result.isSuccess()) {
                                GbisWebSearchBusStationResult.ResultEntity resultEntity = result.getResult();

                                // 2-1. put every route of station
                                Map<String, StationRoute> stationRoutes = new TreeMap<>();
                                for (GbisWebSearchBusStationResult.ResultEntity.BusStationInfoEntity busStationInfoEntity : resultEntity.getBusStationInfo()) {
                                    StationRoute stationRoute = new StationRoute(busStationInfoEntity);
                                    stationRoutes.put(stationRoute.getRouteId(), stationRoute);
                                }

                                // 2-2. set arrivalinfo if presents
                                for (GbisWebSearchBusStationResult.ResultEntity.BusArrivalInfoEntity busArrivalInfoEntity : resultEntity.getBusArrivalInfo()) {
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
                                                    DatabaseHelper.getInstance().putStation(dataProvider, stationDataProvider.getStation());
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
                                    DatabaseHelper.getInstance().putStation(dataProvider, stationDataProvider.getStation());
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
                    getSeoulBusRestClient().getRouteByStation(seoulStationId, new Callback<SeoulBusRouteByStationList>() {
                        @Override
                        public void success(SeoulBusRouteByStationList result, Response response) {
                            if (result != null) {
                                // put every route of station
                                // TODO: filter gbis routes
                                List<StationRoute> stationRoutes = new ArrayList<>();
                                for (SeoulBusRouteByStation routeByStation : result.getItems()) {
                                    StationRoute stationRoute = new StationRoute(routeByStation);
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
                                                    DatabaseHelper.getInstance().putStation(dataProvider, stationDataProvider.getStation());
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
                                    DatabaseHelper.getInstance().putStation(dataProvider, stationDataProvider.getStation());
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
                GbisRestInterface gbisRestClient = getGbisRestClient();
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
                    Station secondaryKey = DatabaseHelper.getInstance().getStationWithSecondaryKey(serviceProvider, publicId);
                    if (secondaryKey != null) stationId = secondaryKey.getId();
                }

                if (stationRoute == null) {
                    // retrieve all
                    gbisRestClient.getBusArrivalList(stationId, gbisCallback);

                } else {
                    // retrieve single
                    String routeId = stationRoute.getRouteId();
                    gbisRestClient.getBusArrivalListByRouteId(stationId, routeId, gbisCallback);
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
                                            if(station.getProvider().equals(Provider.SEOUL)) {
                                                seoulStationId = station.getId();
                                            } else {
                                                seoulStation = DatabaseHelper.getInstance().getStationWithSecondaryKey(Provider.SEOUL, station.getLocalIdByProvider(Provider.SEOUL));
                                                if(seoulStation != null) {
                                                    seoulStationId = seoulStation.getId();
                                                }
                                            }
                                            if (seoulStationId.equals(arrival.getStId())) {
                                                stationRoute.setSequence(Integer.parseInt(arrival.getStaOrd()));
                                                ArrivalInfo arrivalInfo = new ArrivalInfo(arrival);
                                                station.putArrivalInfo(arrivalInfo);
                                                station.setLastUpdateTime(new Date());
                                                if(seoulStation != null) {
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
                                getSeoulBusRestClient().getArrivalInfo(routeId, singleCallback);
                            } else {
                                stationId = station.getId();
                                if (!station.getProvider().equals(serviceProvider)) {
                                    String externalProviderLocalId = station.getLocalIdByProvider(serviceProvider);
                                    Station secondaryKey = DatabaseHelper.getInstance().getStationWithSecondaryKey(serviceProvider, externalProviderLocalId);
                                    if (secondaryKey != null) stationId = secondaryKey.getId();
                                }
                                getSeoulBusRestClient().getArrivalInfo(stationId, routeId, String.valueOf(sequence), singleCallback);
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

    public void searchByKeyword(@NonNull String keyword,
                                @NonNull SearchDataProvider searchDataProvider,
                                @NonNull ProgressCallback callback) {

        final int[] progress = {0, 3};
        final Map<String, Station> seoulStationMap = new HashMap<>();

        //search seoulbus route
        SeoulBusRestInterface seoulBusRestClient = getSeoulBusRestClient();
        seoulBusRestClient.searchRouteListByName(keyword,
                new ProgressWrappedCallback<SeoulBusRouteInfoList>(callback, progress) {
                    @Override
                    public void success(SeoulBusRouteInfoList seoulBusRouteInfoList) {
                        if (seoulBusRouteInfoList != null && !seoulBusRouteInfoList.getItems().isEmpty()) {
                            List<Route> routeList = new ArrayList<>();
                            for (SeoulBusRouteInfo seoulBusRouteInfo : seoulBusRouteInfoList.getItems()) {
                                Route route = new Route(seoulBusRouteInfo);

                                //exclude non-Seoul route
                                if (!RouteType.UNKNOWN.equals(route.getType()) && !RouteType.RED_GYEONGGI.equals(route.getType())) {
                                    routeList.add(route);
                                }
                            }
                            searchDataProvider.addRouteData(routeList);
                        }
                    }
                });

        //search seoulbus station
        seoulBusRestClient.searchStationListByName(keyword,
                new ProgressWrappedCallback<SeoulStationInfoList>(callback, progress) {
                    @Override
                    public void success(SeoulStationInfoList seoulStationInfoList) {
                        if (seoulStationInfoList != null && !seoulStationInfoList.getItems().isEmpty()) {
                            for (SeoulStationInfo seoulStationInfo : seoulStationInfoList.getItems()) {
                                Station station = new Station(seoulStationInfo);

                                //exclude non seoul & non stop station
                                String seoulId = station.getLocalId();
                                if (seoulId != null) {
                                    seoulStationMap.put(seoulId, station);
                                }
                            }
                            Collection<Station> values = seoulStationMap.values();
                            searchDataProvider.addStationData(values);
                        }
                        searchGbisAll(1);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progress[0]++;
                        runOnUiThread(() -> {
                            callback.onProgressUpdate(progress[0], progress[1]);
                            callback.onError(progress[0], error);
                        });
                        searchGbisAll(1);
                    }

                    private void searchGbisAll(int page) {
                        //search via gbis web interface
                        getGbisWebRestClient().searchAll(keyword, page, page, new Callback<GbisWebSearchAllResult>() {
                            @Override
                            public void success(GbisWebSearchAllResult gbisWebSearchAllResult, Response response) {
                                // put route entities
                                if (gbisWebSearchAllResult != null && gbisWebSearchAllResult.isSuccess()) {
                                    GbisWebSearchAllResult.ResultEntity.BusRouteEntity busRouteEntity = gbisWebSearchAllResult.getResult().getBusRoute();
                                    List<Route> routeList = new ArrayList<>();
                                    if (busRouteEntity.getCount() > 0) {
                                        for (GbisWebSearchAllResult.ResultEntity.BusRouteEntity.ListEntity listEntity : busRouteEntity.getList()) {
                                            Route route = new Route(listEntity);

                                            // exclude SEOUL route from GBIS result
                                            if (District.SEOUL.equals(route.getDistrict()))
                                                continue;

                                            routeList.add(route);
                                        }
                                    }

                                    // put station entities
                                    List<Station> stationList = new ArrayList<>();
                                    GbisWebSearchAllResult.ResultEntity.BusStationEntity busStationEntity = gbisWebSearchAllResult.getResult().getBusStation();
                                    if (busStationEntity.getCount() > 0) {
                                        for (GbisWebSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity : busStationEntity.getList()) {
                                            Station station = new Station(listEntity);

                                            // exclude non-stop station
                                            if (TextUtils.isEmpty(station.getLocalId()))
                                                continue;

                                            String seoulId = station.getLocalIdByProvider(Provider.SEOUL);
                                            if (seoulId != null) {
                                                Station seoulStation = seoulStationMap.get(seoulId);
                                                if (seoulStation != null) {
                                                    seoulStation.addExternalEntry(new Station.ExternalEntry(Provider.GYEONGGI, station.getLocalId()));
                                                } else {
                                                    stationList.add(station);
                                                }
                                            } else {
                                                stationList.add(station);
                                            }
                                        }
                                    }

                                    searchDataProvider.addRouteData(routeList);
                                    searchDataProvider.addStationData(stationList);

                                    int routeCount = Integer.parseInt(gbisWebSearchAllResult.getResult().getBusRoute().getTotalCount());
                                    int stationCount = Integer.parseInt(gbisWebSearchAllResult.getResult().getBusStation().getTotalCount());
                                    if (routeCount > 10 || stationCount > 10) {
                                        if (progress[1] < 4) {
                                            progress[1]++;
                                            searchGbisAll(page + 1);
                                        }
                                    } else {
                                        searchDataProvider.addStationData(seoulStationMap.values());
                                        DatabaseHelper.getInstance().putStations(Provider.SEOUL, seoulStationMap.values());
                                    }
                                } else {
                                    searchDataProvider.addStationData(seoulStationMap.values());
                                    DatabaseHelper.getInstance().putStations(Provider.SEOUL, seoulStationMap.values());
                                }
                                runOnUiThread(() -> {
                                    callback.onProgressUpdate(++progress[0], progress[1]);
                                    if (progress[0] >= progress[1]) callback.onComplete(true);
                                });
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                runOnUiThread(() -> {
                                    callback.onError(++progress[0], error);
                                    callback.onProgressUpdate(progress[0], progress[1]);
                                    if (progress[0] >= progress[1]) callback.onComplete(false);
                                });
                            }
                        });
                    }
                });
    }


    public interface ProgressCallback {
        void onComplete(boolean success);

        void onProgressUpdate(int current, int target);

        void onError(int progress, Throwable t);
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

    private void runOnUiThread(Runnable runnable) {
        this.handler.post(runnable);
    }
}
