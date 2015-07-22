package kr.rokoroku.mbus.api.tago.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.api.ApiMethodNotSupportedException;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
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
import kr.rokoroku.mbus.api.tago.model.ArrivalInfoResult;
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
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Response;

/**
 * Created by rok on 2015. 4. 22..
 */
public class TagoRestClient implements ApiWrapperInterface {

    private static final String BASE_URL = "http://openapi.tago.go.kr/openapi/service";

    private int cityCode;
    private String apiKey;
    private Client client;
    private Provider provider;
    private TagoRestInterface adapter;

    public TagoRestClient(Client client, int cityCode, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.cityCode = cityCode;
        this.provider = Provider.valueOf(cityCode);
    }

    public TagoRestInterface getAdapter() {
        if (adapter == null) {
            adapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setClient(client)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setConverter(new TagoXmlConverter())
                    .setRequestInterceptor(request -> {
                        if (apiKey != null) {
                            request.addEncodedQueryParam("serviceKey", apiKey);
                        }
                        request.addQueryParam("cityCode", String.valueOf(cityCode));
                    })
                    .build()
                    .create(TagoRestInterface.class);
        }
        return adapter;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    public int getCityCode() {
        return cityCode;
    }

    @Override
    public void getRouteBaseInfo(String routeId, Callback<Route> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback) {
        getAdapter().getArrivalInfo(stationId, new retrofit.Callback<ArrivalInfoResult>() {
            @Override
            public void success(ArrivalInfoResult arrivalInfoResult, Response response) {
                List<ArrivalInfo> result = null;
                if(arrivalInfoResult != null && arrivalInfoResult.getItems() != null) {
                    result = new ArrayList<>();
                    for (ArrivalInfoResult.ResultEntity resultEntity : arrivalInfoResult.getItems()) {
                        result.add(new ArrivalInfo(resultEntity));
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
    public void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback) {
        getAdapter().getArrivalInfo(stationId, new retrofit.Callback<ArrivalInfoResult>() {
            @Override
            public void success(ArrivalInfoResult arrivalInfoResult, Response response) {
                ArrivalInfo result = null;
                if (arrivalInfoResult != null && arrivalInfoResult.getItems() != null) {
                    for (ArrivalInfoResult.ResultEntity resultEntity : arrivalInfoResult.getItems()) {
                        result = new ArrivalInfo(resultEntity);
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
    public void getStationBaseInfo(String stationId, Callback<Station> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void searchRouteByKeyword(String keyword, Callback<List<Route>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void searchStationByKeyword(String keyword, Callback<List<Station>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }

    @Override
    public void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback) {
        callback.onFailure(new ApiMethodNotSupportedException());
    }
}
