package kr.rokoroku.mbus.api;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbis.core.GbisRestInterface;
import kr.rokoroku.mbus.api.gbis.model.GbisBaseInfo;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestInterface;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchStationByPosResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestInterface;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;
import kr.rokoroku.mbus.api.seoulweb.core.SeoulWebRestInterface;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationByPositionResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisRealtimeResult;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 7. 24..
 */
@SuppressWarnings("ALL")
public class AnswerWrapper {

    private static final String KEY_METHOD = "Method";
    private static final String KEY_RESULT = "Result";
    private static final String KEY_VALUE = "Value";

    public static CustomEvent createCustomEvent(String tag, String method) {
        CustomEvent customEvent = new CustomEvent(tag);
        if (method != null) customEvent.putCustomAttribute(KEY_METHOD, method);
        return customEvent;
    }

    public static CustomEvent createCustomEvent(String tag, String method, String result) {
        CustomEvent customEvent = new CustomEvent(tag);
        if (method != null) customEvent.putCustomAttribute(KEY_METHOD, method);
        if (result != null) customEvent.putCustomAttribute(KEY_RESULT, result);
        return customEvent;
    }

    public static CustomEvent createCustomEvent(String tag, String method, Number value) {
        CustomEvent customEvent = new CustomEvent(tag);
        if (method != null) customEvent.putCustomAttribute(KEY_METHOD, method);
        if (value != null) customEvent.putCustomAttribute(KEY_VALUE, value);
        return customEvent;
    }

    public static Callback createWrappedCallback(Callback callback, String tag, String method) {
        return new Callback() {
            @Override
            public void success(Object o, Response response) {
                callback.success(o, response);
                Answers.getInstance().logCustom(createCustomEvent(tag, method, "Success"));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
                String failReason = "Failure";
                if (error != null) try {
                    Throwable errorCause = error.getCause();
                    RetrofitError.Kind errorKind = error.getKind();
                    if (errorKind != null) {
                        failReason = errorKind.name();
                    }
                    if (errorCause == null) {
                        failReason += ":" + errorCause.getClass().getName();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Answers.getInstance().logCustom(createCustomEvent(tag, method, failReason));
            }
        };
    }

    public static SeoulBusRestInterface wrap(SeoulBusRestInterface seoulBusRestInterface) {
        return new SeoulBusRestInterface() {
            @Override
            public void getArrivalInfo(@Query("busRouteId") String routeId, Callback<SeoulBusArrivalList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getArrivalInfo");
                seoulBusRestInterface.getArrivalInfo(routeId, callback);
            }

            @Override
            public void getArrivalInfo(@Query("stId") String stationId,
                                       @Query("busRouteId") String routeId,
                                       @Query("ord") int stationSeq,
                                       Callback<SeoulBusArrivalList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getArrivalInfoByStation");
                seoulBusRestInterface.getArrivalInfo(stationId, routeId, stationSeq, callback);
            }

            @Override
            public void searchRouteListByName(@Query("strSrch") String busNumber,
                                              Callback<SeoulBusRouteInfoList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "searchRouteByName");
                seoulBusRestInterface.searchRouteListByName(busNumber, callback);
            }

            @Override
            public void getRouteInfo(@Query("busRouteId") String busRouteId,
                                     Callback<SeoulBusRouteInfoList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getRouteInfo");
                seoulBusRestInterface.searchRouteListByName(busRouteId, callback);
            }

            @Override
            public void getRouteStationList(@Query("busRouteId") String busRouteId,
                                            Callback<SeoulBusRouteStationList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getRouteStationList");
                seoulBusRestInterface.getRouteStationList(busRouteId, callback);
            }

            @Override
            public void getRouteBusPositionList(@Query("busRouteId") String busRouteId,
                                                Callback<SeoulBusLocationList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getRouteBusPositionList");
                seoulBusRestInterface.getRouteBusPositionList(busRouteId, callback);
            }

            @Override
            public void searchStationListByName(@Query("stSrch") String stationName,
                                                Callback<SeoulStationInfoList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "searchStationByName");
                seoulBusRestInterface.searchStationListByName(stationName, callback);
            }

            @Override
            public void searchStationListByPos(@Query("tmX") double longitude,
                                               @Query("tmY") double latitude,
                                               @Query("radius") int radius,
                                               Callback<SeoulStationInfoList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "searchStationByPos");
                seoulBusRestInterface.searchStationListByPos(longitude, latitude, radius, callback);
            }

            @Override
            public void getRouteByStation(@Query("arsId") String arsId, Callback<SeoulBusRouteByStationList> callback) {
                callback = createWrappedCallback(callback, "SeoulRestClient", "getRouteByStation");
                seoulBusRestInterface.getRouteByStation(arsId, callback);
            }
        };
    }

    public static SeoulWebRestInterface wrap(SeoulWebRestInterface seoulWebRestInterface) {
        return new SeoulWebRestInterface() {
            @Override
            public void searchRoute(@Field("strSrch") String keyword,
                                    Callback<kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "searchRoute");
                seoulWebRestInterface.searchRoute(keyword, callback);
            }

            @Override
            public void searchStation(@Field("stSrch") String keyword,
                                      Callback<SearchStationResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "searchStation");
                seoulWebRestInterface.searchStation(keyword, callback);
            }

            @Override
            public void searchStationByPos(@Query("busY") double latitude,
                                           @Query("busX") double longitude,
                                           @Query("radius") int radius,
                                           Callback<StationByPositionResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "searchStationByPos");
                seoulWebRestInterface.searchStationByPos(latitude, longitude, radius, callback);
            }

            @Override
            public void getRouteInfo(@Field("busRouteId") String routeId,
                                     Callback<RouteStationResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "getRouteInfo");
                seoulWebRestInterface.getRouteInfo(routeId, callback);
            }

            @Override
            public void getStationInfo(@Field("arsId") String arsId, Callback<StationRouteResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "getStationInfo");
                seoulWebRestInterface.getStationInfo(arsId, callback);
            }

            @Override
            public void getTopisRouteMapLine(@Query("rout_id") String routeId,
                                             Callback<TopisMapLineResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "getTopisRouteMapLine");
                seoulWebRestInterface.getTopisRouteMapLine(routeId, callback);
            }

            @Override
            public void getTopisRealtimeRoute(@Query("busRouteId") String routeId, Callback<TopisRealtimeResult> callback) {
                callback = createWrappedCallback(callback, "SeoulWebRestClient", "getTopisRealtimeRoute");
                seoulWebRestInterface.getTopisRealtimeRoute(routeId, callback);
            }
        };
    }

    public static GbisRestInterface wrap(GbisRestInterface gbisRestInterface) {
        return new GbisRestInterface() {
            @Override
            public void getBaseInfo(Callback<GbisBaseInfo> callback) {
                callback = createWrappedCallback(callback, "GbisRestClient", "getBaseInfo");
                gbisRestInterface.getBaseInfo(callback);
            }

            @Override
            public void getBusLocationList(@Query("routeId") String routeId,
                                           Callback<GbisBusLocationList> callback) {
                callback = createWrappedCallback(callback, "GbisRestClient", "getBusLocationList");
                gbisRestInterface.getBusLocationList(routeId, callback);
            }

            @Override
            public void getBusArrivalList(@Query("stationId") String stationId,
                                          Callback<GbisBusArrivalList> callback) {
                callback = createWrappedCallback(callback, "GbisRestClient", "getBusArrivalList");
                gbisRestInterface.getBusArrivalList(stationId, callback);
            }

            @Override
            public void getBusArrivalList(@Query("stationId") String stationId,
                                          @Query("routeId") String routeId,
                                          Callback<GbisBusArrivalList> callback) {
                callback = createWrappedCallback(callback, "GbisRestClient", "getBusArrivalListByRoute");
                gbisRestInterface.getBusArrivalList(stationId, routeId, callback);
            }
        };
    }

    public static GbisWebRestInterface wrap(GbisWebRestInterface gbisWebRestInterface) {
        return new GbisWebRestInterface() {
            @Override
            public void searchAll(@Field("searchKeyword") String keyword,
                                  @Field("pageOfRoute") int pageOfRoute,
                                  @Field("pageOfBus") int pageOfBus,
                                  Callback<GbisSearchAllResult> callback) {
                callback = createWrappedCallback(callback, "GbisWebRestClient", "searchAll");
                gbisWebRestInterface.searchAll(keyword, pageOfRoute, pageOfBus, callback);
            }

            @Override
            public void searchStationByPos(@Field("lat") double EPSGlatitude,
                                           @Field("lon") double EPSGlongitude,
                                           @Field("radius") int radius,
                                           Callback<GbisSearchStationByPosResult> callback) {
                callback = createWrappedCallback(callback, "GbisWebRestClient", "searchStationByPos");
                gbisWebRestInterface.searchStationByPos(EPSGlatitude, EPSGlongitude, radius, callback);
            }

            @Override
            public void getRouteInfo(@Field("routeId") String routeId,
                                     Callback<GbisSearchRouteResult> callback) {
                callback = createWrappedCallback(callback, "GbisWebRestClient", "getRouteInfo");
                gbisWebRestInterface.getRouteInfo(routeId, callback);
            }

            @Override
            public void getStationInfo(@Field("stationId") String stationId,
                                       Callback<GbisStationRouteResult> callback) {
                callback = createWrappedCallback(callback, "GbisWebRestClient", "getStationInfo");
                gbisWebRestInterface.getStationInfo(stationId, callback);
            }

            @Override
            public void getRouteMapLine(@Field("routeId") String routeId,
                                        Callback<GbisSearchMapLineResult> callback) {
                callback = createWrappedCallback(callback, "GbisWebRestClient", "getRouteMapLine");
                gbisWebRestInterface.getRouteMapLine(routeId, callback);
            }
        };
    }
}
