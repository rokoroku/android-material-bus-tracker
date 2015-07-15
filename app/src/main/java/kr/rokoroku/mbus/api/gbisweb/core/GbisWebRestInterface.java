package kr.rokoroku.mbus.api.gbisweb.core;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchStationByPosResult;
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
     * @param pageOfStation
     * @param callback
     */
    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchAllJson&pageOfPOI=0&pageOfSubway=0")
    void searchAll(@Field(value = "searchKeyword", encodeValue = false) String keyword,
                   @Field("pageOfRoute") int pageOfRoute,
                   @Field("pageOfBus") int pageOfStation,
                   Callback<GbisSearchAllResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchAroundBusStationJson")
    void searchStationByPos(@Field("lat") double EPSGlatitude,
                            @Field("lon") double EPSGlongitude,
                            @Field("radius") int radius,
                            Callback<GbisSearchStationByPosResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchRouteJson")
    void getRouteInfo(@Field("routeId") String routeId,
                      Callback<GbisSearchRouteResult> callback);

    /**
     * 도착정보가 조회되지 않을 경우,
     * 버스 노선 타입 (RouteType)이 반환되지 않음.
     * 서울-경기 통합 정류장일 경우 처리가 필요.
     *
     * @param stationId
     * @param callback
     */
    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchBusStationJson")
    void getStationInfo(@Field("stationId") String stationId,
                        Callback<GbisStationRouteResult> callback);

    @FormUrlEncoded
    @POST("/schBusAPI.action?cmd=searchMapLineJson")
    void getRouteMapLine(@Field("routeId") String routeId,
                         Callback<GbisSearchMapLineResult> callback);

}
