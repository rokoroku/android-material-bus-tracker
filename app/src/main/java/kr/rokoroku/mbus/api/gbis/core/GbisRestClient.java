package kr.rokoroku.mbus.api.gbis.core;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.AnswerWrapper;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrival;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocation;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestClient;
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
import retrofit.android.AndroidLog;
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
    private boolean isStationArrivalInfoFlooded = false;
    private boolean isStationRouteArrivalInfoFlooded = false;

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
                    .setLog(new AndroidLog("GbisRestClient"))
                    .setLogLevel(BaseApplication.logLevel)
                    .setConverter(new GbisXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("serviceKey", apiKey);
                        }
                    })
                    .build()
                    .create(GbisRestInterface.class);
            adapter = AnswerWrapper.wrap(adapter);
        }
        return adapter;
    }

    private GbisWebRestClient getGbisWebRestClient() {
        return ApiFacade.getInstance().getGbisWebRestClient();
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        getGbisWebRestClient().searchRouteByKeyword(keyword, callback);
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        getGbisWebRestClient().searchStationByKeyword(keyword, callback);
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback) {
        getGbisWebRestClient().searchStationByLocation(latitude, longitude, 1000, callback);
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        getGbisWebRestClient().getRouteBaseInfo(routeId, callback);
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
                getGbisWebRestClient().getRouteRealtimeInfo(routeId, callback);
            }
        });
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        getGbisWebRestClient().getRouteMaplineInfo(routeId, callback);
    }

    @Override
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        getGbisWebRestClient().getStationBaseInfo(stationId, callback);
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        if(isStationArrivalInfoFlooded) {
            getGbisWebRestClient().getStationArrivalInfo(stationId, callback);
        } else {
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
                    Throwable cause = error.getCause();
                    if (cause instanceof GbisException) {
                        GbisException gbisException = (GbisException) cause;
                        if (!isStationArrivalInfoFlooded && gbisException.getErrorCode() == GbisException.ERROR_SERVICE_ACCESS_FLOODED) {
                            isStationArrivalInfoFlooded = true;
                            getStationArrivalInfo(stationId, callback);
                            return;
                        }
                    }
                    getGbisWebRestClient().getStationArrivalInfo(stationId, callback);
                }
            });
        }
    }

    @Override
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        if(isStationRouteArrivalInfoFlooded) {
            getStationArrivalInfo(stationId, new Callback<List<ArrivalInfo>>() {
                @Override
                public void onSuccess(List<ArrivalInfo> result) {
                    ArrivalInfo arrivalInfo = null;
                    if (result != null) {
                        for (ArrivalInfo entry : result) {
                            if (routeId.equals(entry.getRouteId())) {
                                arrivalInfo = entry;
                                break;
                            }
                        }
                    }
                    callback.onSuccess(arrivalInfo);
                }

                @Override
                public void onFailure(Throwable t) {
                    getGbisWebRestClient().getStationArrivalInfo(stationId, routeId, callback);
                }
            });
        } else {
            getAdapter().getBusArrivalList(stationId, routeId, new retrofit.Callback<GbisBusArrivalList>() {
                @Override
                public void success(GbisBusArrivalList gbisBusArrivalList, Response response) {
                    ArrivalInfo arrivalInfo = null;
                    if (gbisBusArrivalList != null && gbisBusArrivalList.getItems() != null) {
                        List<GbisBusArrival> busArrivalList = gbisBusArrivalList.getItems();
                        if (!busArrivalList.isEmpty()) {
                            arrivalInfo = new ArrivalInfo(busArrivalList.get(0));
                        }
                    }
                    callback.onSuccess(arrivalInfo);
                }

                @Override
                public void failure(RetrofitError error) {
                    Throwable cause = error.getCause();
                    if (cause instanceof GbisException) {
                        GbisException gbisException = (GbisException) cause;
                        if (!isStationRouteArrivalInfoFlooded && gbisException.getErrorCode() == GbisException.ERROR_SERVICE_ACCESS_FLOODED) {
                            isStationRouteArrivalInfoFlooded = true;
                            getStationArrivalInfo(stationId, routeId, callback);
                            return;
                        }
                    }
                    getGbisWebRestClient().getStationArrivalInfo(stationId, routeId, callback);
                }
            });
        }
    }
}
