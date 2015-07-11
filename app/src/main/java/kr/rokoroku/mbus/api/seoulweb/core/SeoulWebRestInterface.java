package kr.rokoroku.mbus.api.seoulweb.core;

import kr.rokoroku.mbus.api.seoulweb.model.SeoulWebSearchRouteResult;
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
     * 경기버스 통합검색
     *
     * @param keyword
     * @param pageOfRoute
     * @param pageOfBus
     * @param callback
     */
    @FormUrlEncoded
    @POST("//m.bus.go.kr/mBus/bus/getBusRouteList.bms")
    void searchRoute(@Field("strSrch") String keyword,
                     @Field("pageOfRoute") int pageOfRoute,
                     @Field("pageOfBus") int pageOfBus,
                     Callback<SeoulWebSearchRouteResult> callback);

    @GET("//topis.seoul.go.kr/renewal/ajaxData/getBusData.jsp?mode=routLine")
    void getRouteMapLine(@Query("rout_id") String routeId,
                         Callback<TopisMapLineResult> callback);

}
