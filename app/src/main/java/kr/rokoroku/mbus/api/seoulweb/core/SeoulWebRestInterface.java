package kr.rokoroku.mbus.api.seoulweb.core;

import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
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

    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getRouteAndPos.bms")
    void getRouteStations(@Field("busRouteId") String routeId,
                          Callback<RouteStationResult> callback);

    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getStationByUid.bms")
    void getStationInfos(@Field("arsId") String stationLocalId,
                         Callback<StationRouteResult> callback);

    @GET("//topis.seoul.go.kr/renewal/ajaxData/getBusData.jsp?mode=routLine")
    void getRouteMapLine(@Query("rout_id") String routeId,
                         Callback<TopisMapLineResult> callback);

}
