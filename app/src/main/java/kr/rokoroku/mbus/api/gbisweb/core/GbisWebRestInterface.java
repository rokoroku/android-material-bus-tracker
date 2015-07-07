package kr.rokoroku.mbus.api.gbisweb.core;

import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusStationResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchMapLineResult;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface GbisWebRestInterface {

    /**
     * 경기버스 통합검색
     *
     * @param keyword
     * @param pageOfRoute
     * @param pageOfBus
     * @param callback
     */
    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchAllJson")
    void searchAll(@Field("searchKeyword") String keyword,
                   @Field("pageOfRoute") int pageOfRoute,
                   @Field("pageOfBus") int pageOfBus,
                   Callback<GbisWebSearchAllResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchMapLineJson")
    void getRouteMapLine(@Field("routeId") String routeId,
                         Callback<GbisWebSearchMapLineResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchRouteJson")
    void getRouteInfo(@Field("routeId") String routeId,
                      Callback<GbisWebSearchBusRouteResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchBusStationJson")
    void getStationInfo(@Field("stationId") String stationId,
                        Callback<GbisWebSearchBusStationResult> callback);

}
