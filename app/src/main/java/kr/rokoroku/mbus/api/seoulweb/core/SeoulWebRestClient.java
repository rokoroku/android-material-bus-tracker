package kr.rokoroku.mbus.api.seoulweb.core;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
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
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
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
        getAdapter().searchRoute(keyword, new retrofit.Callback<SearchRouteResult>() {
            @Override
            public void success(SearchRouteResult searchRouteResult, Response response) {
                List<Route> result = null;
                if (searchRouteResult != null && searchRouteResult.error.errorCode.equals("0000")) {
                    result = new ArrayList<>();
                    for (SearchRouteResult.ResultListEntity resultEntity : searchRouteResult.resultList) {
                        Route route = new Route(resultEntity.busRouteId, resultEntity.busRouteNm, provider);
                        route.setType(RouteType.valueOfTopis(resultEntity.routeType));

                        if (!RouteType.RED_GYEONGGI.equals(route.getType())) {
                            route.setAllocNormal(resultEntity.term);
                            try {
                                route.setFirstUpTime(TimeUtils.getGbisDateFormat().format(TimeUtils.getSeoulBusDateFormat().parse(resultEntity.firstBusTm)));
                                route.setLastUpTime(TimeUtils.getGbisDateFormat().format(TimeUtils.getSeoulBusDateFormat().parse(resultEntity.firstBusTm)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            route.setStartStationName(resultEntity.stStationNm);
                            route.setEndStationName(resultEntity.edStationNm);
                            String[] split = resultEntity.corpNm.split(" ");
                            route.setCompanyName(split[0]);
                            route.setCompanyTel(split[1]);
                            route.setRegionName("서울");
                            route.setDistrict(District.SEOUL);
                            route.setProvider(Provider.SEOUL);
                            result.add(route);
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
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        getAdapter().searchStation(keyword, new retrofit.Callback<SearchStationResult>() {
            @Override
            public void success(SearchStationResult searchStationResult, Response response) {
                List<Station> result = null;
                if(searchStationResult != null && searchStationResult.error.isSuccess()) {
                    result = new ArrayList<Station>();
                    for (SearchStationResult.BusListEntity busListEntity : searchStationResult.busList) {
                        Station station = new Station(busListEntity.stId, provider);
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
                    if(route != null) {
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
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
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
