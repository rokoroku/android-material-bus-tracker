package kr.rokoroku.mbus.api.gbis.core;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.api.gbis.model.GbisBusArrival;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocation;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisRestClient implements ApiWrapperInterface {

    private static final String BASE_URL = "http://openapi.gbis.go.kr/ws/rest";

    private Client client;
    private String apiKey;
    private Provider provider;
    private GbisRestInterface adapter;

    public GbisRestClient(Client client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.provider = Provider.GYEONGGI;
    }

    public GbisRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new GbisXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("serviceKey", apiKey);
                        }
                    })
                    .build()
                    .create(GbisRestInterface.class);
        }
        return adapter;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        ApiFacade.getInstance().getGbisWebRestClient().searchRouteByKeyword(keyword, callback);
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        ApiFacade.getInstance().getGbisWebRestClient().searchStationByKeyword(keyword, callback);
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, Callback<List<Station>> callback) {
        ApiFacade.getInstance().getGbisWebRestClient().searchStationByLocation(latitude, longitude, callback);
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        ApiFacade.getInstance().getGbisWebRestClient().getRouteBaseInfo(routeId, callback);
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        getAdapter().getBusLocationList(routeId, new retrofit.Callback<GbisBusLocationList>() {
            @Override
            public void success(GbisBusLocationList locationList, Response response) {
                List<BusLocation> busLocationList = null;
                if (locationList != null) {
                    busLocationList = new ArrayList<>();
                    RouteType routeType = null;
                    Route route = DatabaseFacade.getInstance().getRoute(provider, routeId);
                    if (route != null) routeType = route.getType();

                    for (GbisBusLocation gbisBusLocation : locationList.getItems()) {
                        BusLocation busLocation = new BusLocation(gbisBusLocation);
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
        ApiFacade.getInstance().getGbisWebRestClient().getRouteMaplineInfo(routeId, callback);
    }

    @Override
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        ApiFacade.getInstance().getGbisWebRestClient().getStationBaseInfo(stationId, callback);
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        getAdapter().getBusArrivalList(stationId, new retrofit.Callback<GbisBusArrivalList>() {
            @Override
            public void success(GbisBusArrivalList gbisBusArrivalList, Response response) {
                ArrayList<ArrivalInfo> arrivalInfoList = null;
                if (gbisBusArrivalList != null) {
                    arrivalInfoList = new ArrayList<>();
                    for (GbisBusArrival gbisBusArrival : gbisBusArrivalList.getItems()) {
                        ArrivalInfo arrivalInfo = new ArrivalInfo(gbisBusArrival);
                        arrivalInfoList.add(arrivalInfo);
                    }
                }
                callback.onSuccess(arrivalInfoList);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        getAdapter().getBusArrivalList(stationId, routeId, new retrofit.Callback<GbisBusArrivalList>() {
            @Override
            public void success(GbisBusArrivalList gbisBusArrivalList, Response response) {
                ArrivalInfo arrivalInfo = null;
                if (gbisBusArrivalList != null && gbisBusArrivalList.getItems() != null) {
                    List<GbisBusArrival> busArrivalList = gbisBusArrivalList.getItems();
                    if(!busArrivalList.isEmpty()) {
                        arrivalInfo = new ArrivalInfo(busArrivalList.get(0));
                    }
                }
                callback.onSuccess(arrivalInfo);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onFailure(error);
            }
        });
    }
}
