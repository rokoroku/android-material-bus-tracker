package kr.rokoroku.mbus.api.seoul.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrival;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfo;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.seoulweb.core.SeoulWebRestClient;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.ProgressCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 4. 22..
 */
public class SeoulBusRestClient implements ApiWrapperInterface {

    private static final String BASE_URL = "http://ws.bus.go.kr/api/rest";

    private String apiKey;
    private Client client;
    private Provider provider;
    private SeoulBusRestInterface adapter;
    private SeoulWebRestClient webClient;

    public SeoulBusRestClient(Client client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.provider = Provider.SEOUL;
    }

    public SeoulBusRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new SeoulBusXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("ServiceKey", apiKey);
                        }
                    })
                    .build()
                    .create(SeoulBusRestInterface.class);
        }
        return adapter;
    }

    public SeoulWebRestClient getWebClient() {
        if(webClient == null) {
            webClient = ApiFacade.getInstance().getSeoulWebRestClient();
            if(webClient == null) {
                webClient = new SeoulWebRestClient(client);
            }
        }
        return webClient;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        getWebClient().searchRouteByKeyword(keyword, new Callback<List<Route>>() {
            @Override
            public void onSuccess(List<Route> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                getAdapter().searchRouteListByName(keyword, new retrofit.Callback<SeoulBusRouteInfoList>() {
                    @Override
                    public void success(SeoulBusRouteInfoList seoulBusRouteInfoList, Response response) {
                        List<Route> routeList = null;
                        if (seoulBusRouteInfoList != null && seoulBusRouteInfoList.getItems() != null) {
                            routeList = new ArrayList<>();
                            for (SeoulBusRouteInfo seoulBusRouteInfo : seoulBusRouteInfoList.getItems()) {
                                Route route = new Route(seoulBusRouteInfo);

                                // exclude non-Seoul route
                                if (RouteType.checkSeoulRoute(route.getType())) {
                                    routeList.add(route);
                                }
                            }
                        }
                        callback.onSuccess(routeList);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onFailure(error);
                    }
                });
            }
        });
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        getWebClient().searchStationByKeyword(keyword, new Callback<List<Station>>() {
            @Override
            public void onSuccess(List<Station> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                getAdapter().searchStationListByName(keyword, new retrofit.Callback<SeoulStationInfoList>() {
                    @Override
                    public void success(SeoulStationInfoList seoulStationInfoList, Response response) {
                        List<Station> stationList = null;
                        if (seoulStationInfoList != null && seoulStationInfoList.getItems() != null) {
                            stationList = new ArrayList<>();
                            for (SeoulStationInfo seoulStationInfo : seoulStationInfoList.getItems()) {
                                Station station = new Station(seoulStationInfo);

                                // exclude non seoul & non stop station
                                if (station.getLocalId() != null) {
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
        });
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, Callback<List<Station>> callback) {
        getAdapter().searchStationListByPos(latitude, longitude, 1000, new retrofit.Callback<SeoulStationInfoList>() {
            @Override
            public void success(SeoulStationInfoList seoulStationInfoList, Response response) {
                List<Station> stationList = null;
                if (seoulStationInfoList != null && seoulStationInfoList.getItems() != null) {
                    stationList = new ArrayList<>();
                    for (SeoulStationInfo seoulStationInfo : seoulStationInfoList.getItems()) {
                        Station station = new Station(seoulStationInfo);
                        stationList.add(station);
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
        getWebClient().getRouteBaseInfo(routeId, new Callback<Route>() {
            @Override
            public void onSuccess(Route result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Route route = DatabaseFacade.getInstance().getRoute(getProvider(), routeId);
                if(route != null && shouldUseWebInterface(route.getType())) {
                    callback.onFailure(t);

                } else {
                    getAdapter().getRouteInfo(routeId, new retrofit.Callback<SeoulBusRouteInfoList>() {
                        @Override
                        public void success(SeoulBusRouteInfoList seoulBusRouteInfoList, Response response) {
                            Route route = null;
                            if (seoulBusRouteInfoList != null && seoulBusRouteInfoList.getItems() != null) {
                                route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                                if (route == null) route = new Route(routeId, null, provider);

                                if (!seoulBusRouteInfoList.getItems().isEmpty()) {
                                    route.setSeoulBusInfo(seoulBusRouteInfoList.getItems().get(0));
                                }
                            }

                            if (route != null) {
                                final Route resultRoute = route;
                                getAdapter().getRouteStationList(routeId, new retrofit.Callback<SeoulBusRouteStationList>() {
                                    @Override
                                    public void success(SeoulBusRouteStationList seoulBusRouteStationList, Response response) {
                                        if (seoulBusRouteStationList != null) {
                                            List<RouteStation> routeStationList = new ArrayList<>();
                                            for (SeoulBusRouteStation entity : seoulBusRouteStationList.getItems()) {
                                                RouteStation routeStation = new RouteStation(entity);
                                                routeStationList.add(routeStation);
                                                if (entity.getTrnstnid().equals(entity.getStationId())) {
                                                    resultRoute.setTurnStationSeq(entity.getSeq());
                                                    resultRoute.setTurnStationId(entity.getTrnstnid());
                                                }
                                            }
                                            resultRoute.setRouteStationList(routeStationList);
                                        }
                                        callback.onSuccess(resultRoute);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        callback.onFailure(error);
                                    }
                                });
                            } else {
                                callback.onFailure(new SeoulBusException(SeoulBusException.ERROR_NO_RESULT, "ROUTE NOT FOUND"));
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            callback.onFailure(error);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        Route route = DatabaseFacade.getInstance().getRoute(getProvider(), routeId);
        if(route != null && shouldUseWebInterface(route.getType())) {
            getWebClient().getRouteRealtimeInfo(routeId, callback);
        } else {
            getAdapter().getRouteBusPositionList(routeId, new retrofit.Callback<SeoulBusLocationList>() {
                @Override
                public void success(SeoulBusLocationList seoulBusLocationList, Response response) {
                    List<BusLocation> busLocationList = null;
                    if (seoulBusLocationList != null) {
                        RouteType routeType = null;
                        Route route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                        if (route != null) routeType = route.getType();

                        busLocationList = new ArrayList<>();
                        for (SeoulBusLocation entity : seoulBusLocationList.getItems()) {
                            BusLocation busLocation = new BusLocation(entity);
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
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getWebClient().getRouteMaplineInfo(routeId, callback);
    }

    @Override
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        Station station = DatabaseFacade.getInstance().getStation(provider, stationId);
        if (station != null && station.getLocalId() != null && !station.isLocalRouteInfoAvailable()) {
            final String arsId = station.getLocalId();
            getWebClient().getStationBaseInfo(arsId, new Callback<Station>() {
                @Override
                public void onSuccess(Station result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    getAdapter().getRouteByStation(arsId, new retrofit.Callback<SeoulBusRouteByStationList>() {
                        @Override
                        public void success(SeoulBusRouteByStationList routeByStationList, Response response) {
                            Station station = null;
                            if (routeByStationList != null && routeByStationList.getItems() != null) {
                                station = DatabaseFacade.getInstance().getStation(provider, stationId);
                                if (station == null) station = new Station(stationId, provider);

                                // 1. parse retrieved data
                                List<StationRoute> stationRoutes = new ArrayList<>();
                                for (SeoulBusRouteByStation routeByStation : routeByStationList.getItems()) {
                                    StationRoute stationRoute = new StationRoute(routeByStation, arsId);

                                    // exclude non-Seoul route
                                    if (RouteType.checkSeoulRoute(stationRoute.getRouteType())) {
                                        stationRoutes.add(stationRoute);
                                    }
                                }

                                // 2. retrieve route's meta data if not presented
                                List<StationRoute> unknownRoutes = new ArrayList<>();
                                for (StationRoute stationRoute : stationRoutes) {
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
                                                    finalStation.setStationRouteList(stationRoutes);
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
            });

        } else if (station != null) {
            // non-stop station (arsId == null)
            callback.onSuccess(station);

        } else {
            SeoulBusException exception = new SeoulBusException(SeoulBusException.ERROR_INVALID_PARAMETER, "ILLEGAL_STATION_ID");
            Log.e("SeoulBusRestClient", "getStationBaseInfo", exception);
            callback.onFailure(exception);
        }
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        getWebClient().getStationArrivalInfo(stationId, new Callback<List<ArrivalInfo>>() {
            @Override
            public void onSuccess(List<ArrivalInfo> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {

                final Station station = DatabaseFacade.getInstance().getStation(provider, stationId);
                final List<ArrivalInfo> arrivalInfoList = new ArrayList<>();
                final List<StationRoute> stationRouteList = station.getStationRouteList();
                final ProgressCallback.ProgressRunner progressRunner
                        = new ProgressCallback.ProgressRunner(new ApiFacade.SimpleProgressCallback() {

                    private Throwable throwable;

                    @Override
                    public void onComplete(boolean success) {
                        if (success) {
                            callback.onSuccess(arrivalInfoList);
                        } else {
                            callback.onFailure(throwable);
                        }
                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        this.throwable = t;
                    }
                }, stationRouteList.size());

                for (StationRoute stationRoute : stationRouteList) {
                    if (stationRoute.getProvider().equals(getProvider())) {
                        getStationArrivalInfo(stationId, stationRoute.getRouteId(), new Callback<ArrivalInfo>() {
                            @Override
                            public void onSuccess(ArrivalInfo result) {
                                if (result != null) {
                                    stationRoute.setArrivalInfo(result);
                                    arrivalInfoList.add(result);
                                }
                                progressRunner.progress();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                progressRunner.error(t);
                            }
                        });
                    } else {
                        progressRunner.progress();
                    }
                }
            }
        });
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {

        Station station = DatabaseFacade.getInstance().getStation(getProvider(), stationId);
        if (station != null) {
            getWebClient().getStationArrivalInfo(station.getLocalId(), routeId, new Callback<ArrivalInfo>() {
                @Override
                public void onSuccess(ArrivalInfo result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    int sequence = -1;
                    final retrofit.Callback<SeoulBusArrivalList> innerCallback = new retrofit.Callback<SeoulBusArrivalList>() {
                        @Override
                        public void success(SeoulBusArrivalList seoulBusArrivalList, Response response) {
                            ArrivalInfo arrivalInfo = null;
                            if (seoulBusArrivalList != null) {
                                for (SeoulBusArrival seoulBusArrival : seoulBusArrivalList.getItems()) {
                                    if (routeId.equals(seoulBusArrival.getBusRouteId())) {
                                        arrivalInfo = new ArrivalInfo(seoulBusArrival);
                                        break;
                                    }
                                }
                            }
                            callback.onSuccess(arrivalInfo);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            callback.onFailure(error);
                        }
                    };
                    final StationRoute stationRoute = station.getStationRoute(routeId);

                    if (stationRoute != null) {
                        sequence = stationRoute.getSequence();
                        RouteType routeType = stationRoute.getRouteType();
                        if (shouldUseWebInterface(routeType)) {
                            callback.onFailure(t);
                            return;
                        }
                    }
                    if (sequence != -1) {
                        getAdapter().getArrivalInfo(stationId, routeId, sequence, innerCallback);
                    } else {
                        getAdapter().getArrivalInfo(routeId, innerCallback);
                    }

                }
            });

        } else {
            getStationBaseInfo(stationId, new Callback<Station>() {
                @Override
                public void onSuccess(Station result) {
                    if (result != null) {
                        DatabaseFacade.getInstance().putStation(provider, result);
                        getStationArrivalInfo(stationId, routeId, callback);
                    } else {
                        onFailure(new SeoulBusException(SeoulBusException.ERROR_NO_RESULT, "STATION NOT FOUND"));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
        }

    }

    private boolean shouldUseWebInterface(RouteType routeType) {
        return RouteType.GREEN_SEOUL_LOCAL.equals(routeType);
    }
}
