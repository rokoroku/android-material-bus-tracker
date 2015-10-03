package kr.rokoroku.mbus.api.incheon.core;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.AnswerWrapper;
import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.incheon.data.IncheonArrivalInfo;
import kr.rokoroku.mbus.api.incheon.data.IncheonBusPosition;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
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
public class IncheonWebRestClient implements ApiWrapperInterface {

    private Client client;
    private Provider provider;
    private IncheonDbHelper incheonDbHelper;
    private IncheonWebRestInterface adapter;

    public IncheonWebRestClient(Client client) {
        this.provider = Provider.INCHEON;
        this.client = client;
        this.incheonDbHelper = new IncheonDbHelper(BaseApplication.getInstance());
    }

    public IncheonWebRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint("http:")
                    .setClient(client)
                    .setLog(new AndroidLog("IncheonWebRestClient"))
                    .setLogLevel(BaseApplication.logLevel)
                    .setConverter(new IncheonResponseConverter())
                    .build()
                    .create(IncheonWebRestInterface.class);
            adapter = AnswerWrapper.wrap(adapter);
        }
        return adapter;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }


    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        AsyncTask.execute(() -> {
            List<Route> routesByName = incheonDbHelper.getRoutesByName(keyword);
            callback.onSuccess(routesByName);
        });
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        AsyncTask.execute(() -> {
            List<Station> stationByName = incheonDbHelper.getStationsByName(keyword);
            callback.onSuccess(stationByName);
        });
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback) {
        AsyncTask.execute(() -> {
            List<Station> stationByName = incheonDbHelper.getStationsByPos(latitude, longitude, radius);
            callback.onSuccess(stationByName);
        });
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        AsyncTask.execute(() -> {
            Route route = incheonDbHelper.getRoute(routeId);
            if (route != null) {
                getAdapter().getRouteStations(routeId, new retrofit.Callback<List<RouteStation>>() {
                    @Override
                    public void success(List<RouteStation> routeStations, Response response) {
                        if (routeStations != null) {
                            route.setRouteStationList(routeStations);
                        }
                        callback.onSuccess(route);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onFailure(error);
                    }
                });
            } else {
                callback.onFailure(new IllegalArgumentException("no route found for routeId: " + routeId));
            }
        });
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        getAdapter().getRouteRealtime(routeId, new retrofit.Callback<IncheonBusPosition>() {
            @Override
            public void success(IncheonBusPosition incheonBusPosition, Response response) {
                List<BusLocation> busLocationList = null;
                if(incheonBusPosition != null && incheonBusPosition.result != null) {
                    busLocationList = new ArrayList<>();
                    for (IncheonBusPosition.ResultEntity resultEntity : incheonBusPosition.result) {
                        BusLocation busLocation = new BusLocation(resultEntity);
                        busLocation.setRouteId(routeId);
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
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        AsyncTask.execute(() -> {
            Station station = incheonDbHelper.getStation(stationId);
            if(station != null) {
                getAdapter().getStationRoutes(stationId, new retrofit.Callback<List<StationRoute>>() {
                    @Override
                    public void success(List<StationRoute> stationRoutes, Response response) {
                        station.setStationRouteList(stationRoutes);
                        callback.onSuccess(station);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onFailure(error);
                    }
                });
            } else {
                callback.onFailure(new IllegalArgumentException("no station found for stationId: " + stationId));
            }
        });

    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        getAdapter().getArrivalInfo(stationId, new retrofit.Callback<IncheonArrivalInfo>() {
            @Override
            public void success(IncheonArrivalInfo incheonArrivalInfo, Response response) {
                List<ArrivalInfo> arrivalInfos = null;
                if(incheonArrivalInfo != null && incheonArrivalInfo.errorCode.equals("000")) {
                    arrivalInfos = new ArrayList<>();
                    for (IncheonArrivalInfo.ResultEntity resultEntity : incheonArrivalInfo.getItems()) {
                        ArrivalInfo arrivalInfo = new ArrivalInfo(resultEntity, stationId);
                        arrivalInfos.add(arrivalInfo);
                    }
                }
                callback.onSuccess(arrivalInfos);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        getStationArrivalInfo(stationId, new Callback<List<ArrivalInfo>>() {
            @Override
            public void onSuccess(List<ArrivalInfo> result) {
                if(result != null) {
                    for (ArrivalInfo arrivalInfo : result) {
                        if(arrivalInfo.getRouteId().equals(routeId)) {
                            callback.onSuccess(arrivalInfo);
                            return;
                        }
                    }
                }
                callback.onSuccess(new ArrivalInfo(routeId, stationId));
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

}
