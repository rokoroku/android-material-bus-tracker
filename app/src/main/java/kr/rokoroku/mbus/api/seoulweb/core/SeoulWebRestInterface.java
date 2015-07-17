package kr.rokoroku.mbus.api.seoulweb.core;

import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationByPositionResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisRealtimeResult;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface SeoulWebRestInterface {

    /**
     * 서울버스 (모바일) 노선검색
     *
     * @param keyword
     * @param callback
     */
    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getBusRouteList.bms")
    void searchRoute(@Field("strSrch") String keyword,
                     Callback<SearchRouteResult> callback);

    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getSearchByName.bms")
    void searchStation(@Field("stSrch") String keyword,
                       Callback<SearchStationResult> callback);

    @GET("//bus.go.kr/xmlRequest/getStationByPos.jsp")
    void searchStationByPos(@Query("busY") double latitude,
                            @Query("busX") double longitude,
                            @Query("radius") int radius,
                            Callback<StationByPositionResult> callback);

    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getRouteAndPos.bms")
    void getRouteInfo(@Field("busRouteId") String routeId,
                      Callback<RouteStationResult> callback);

    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getStationByUid.bms")
    void getStationInfo(@Field("arsId") String arsId,
                        Callback<StationRouteResult> callback);

    @GET("//topis.seoul.go.kr/renewal/ajaxData/getBusData.jsp?mode=routLine")
    void getTopisRouteMapLine(@Query("rout_id") String routeId,
                              Callback<TopisMapLineResult> callback);

    @GET("//topis.seoul.go.kr/renewal/ajaxData/getRemoteData_4.jsp" +
            "?remoteUrl=http%3A%2F%2F210.96.13.82%3A8901%2Fapi%2Frest%2Fbuspos%2FgetBusPosByRtid")
    void getTopisRealtimeRoute(@Query("busRouteId") String routeId,
                               Callback<TopisRealtimeResult> callback);
}
