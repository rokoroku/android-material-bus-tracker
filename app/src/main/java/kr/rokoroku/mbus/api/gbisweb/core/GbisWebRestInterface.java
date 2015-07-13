package kr.rokoroku.mbus.api.gbisweb.core;

import kr.rokoroku.mbus.api.gbisweb.model.SearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchMapLineResult;

import kr.rokoroku.mbus.api.gbisweb.model.SearchStationByPosResult;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

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
                   Callback<SearchAllResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchAroundBusStationJson")
    void searchStationByPos(@Field("lat") double EPSGlatitude,
                            @Field("lon") double EPSGlongitude,
                            @Field("radius") int radius,
                            Callback<SearchStationByPosResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchRouteJson")
    void getRouteInfo(@Field("routeId") String routeId,
                      Callback<SearchRouteResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchBusStationJson")
    void getStationInfo(@Field("stationId") String stationId,
                        Callback<SearchStationResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchMapLineJson")
    void getRouteMapLine(@Field("routeId") String routeId,
                         Callback<SearchMapLineResult> callback);

}
