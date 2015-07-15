package kr.rokoroku.mbus.api.seoulweb.core;

import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.core.Database;
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
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setConverter(new TopisJsonConverter())
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
                        SeoulBusException exception = new SeoulBusException(
                                Integer.parseInt(searchRouteResult.header.errorCode),
                                searchRouteResult.header.errorMessage);
                        callback.onFailure(exception);
                    }
                } else {
                    callback.onFailure(new SeoulBusException(SeoulBusException.ERROR_NO_RESULT, "NO RESPONSE"));
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
                        for (SearchStationResult.BusListEntity busListEntity : searchStationResult.result) {
                            Station station = new Station(busListEntity.stId, provider);
                            station.setName(busListEntity.stNm);
                            if (!"0".equals(busListEntity.arsId)) {
                                station.setLocalId(busListEntity.arsId);
                            }
                            Double x = Double.valueOf(busListEntity.tmX);
                            Double y = Double.valueOf(busListEntity.tmY);
                            LatLng latLng = GeoUtils.convertTm(x, y);
                            station.setLatitude(latLng.latitude);
                            station.setLongitude(latLng.longitude);
                            station.setCity("서울시");
                            result.add(station);
                        }
                    }
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
    public void searchStationByLocation(double latitude, double longitude, Callback<List<Station>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getAdapter().getRouteMapLine(routeId, new retrofit.Callback<TopisMapLineResult>() {
            @Override
            public void success(TopisMapLineResult topisMapLineResult, Response response) {
                List<MapLine> mapLineList = null;
                if (topisMapLineResult != null && topisMapLineResult.result != null) {
                    mapLineList = new ArrayList<>();

                    //get turn station if available
                    RouteStation turnStation = null;
                    Route route = Database.getInstance().getRoute(provider, routeId);
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
        getAdapter().getStationInfos(arsId, new retrofit.Callback<StationRouteResult>() {
            @Override
            public void success(StationRouteResult stationRouteResult, Response response) {
                if (stationRouteResult != null && stationRouteResult.header.isSuccess()) {
                    final Station station = new Station(stationRouteResult);
                    callback.onSuccess(station);

//                    // filter routes where RouteType is unknown
//                    List<StationRoute> unknownRoutes = new ArrayList<>();
//                    for (StationRoute stationRoute : station.getStationRouteList()) {
//                        if (stationRoute.getRouteType() == null) {
//                            unknownRoutes.add(stationRoute);
//                        }
//                    }
//
//                    // retrieve unknown typed route's meta data
//                    final ProgressCallback.ProgressRunner progressRunner = new ProgressCallback.ProgressRunner(new ApiFacade.SimpleProgressCallback() {
//                        @Override
//                        public void onComplete(boolean success) {
//                            callback.onSuccess(station);
//                        }
//
//                        @Override
//                        public void onError(int progress, Throwable t) {
//                            callback.onFailure(t);
//                        }
//                    }, unknownRoutes.size());
//                    for (StationRoute unknownTypeRoute : unknownRoutes) {
//                        getRouteBaseInfo(unknownTypeRoute.getRouteId(), new Callback<Route>() {
//                            @Override
//                            public void onSuccess(Route result) {
//                                if(result != null && unknownTypeRoute.getRouteId().equals(result.getId())) {
//                                    unknownTypeRoute.setRoute(result);
//                                    Database.getInstance().putRoute(result.getProvider(), result);
//                                }
//                                progressRunner.progress();
//                            }
//
//                            @Override
//                            public void onFailure(Throwable t) {
//                                progressRunner.error(t);
//                            }
//                        });
//                    }
                } else if (stationRouteResult != null) {
                    callback.onFailure(new SeoulBusException(Integer.parseInt(stationRouteResult.header.errorCode), stationRouteResult.header.errorMessage));
                } else {
                    callback.onSuccess(null);
                }
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
}
