package kr.rokoroku.mbus.api.incheon.core;

import java.util.List;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchStationByPosResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.incheon.data.IncheonArrivalInfo;
import kr.rokoroku.mbus.api.incheon.data.IncheonBusPosition;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.StationRoute;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface IncheonWebRestInterface {

    /**
     * 도착정보가 조회되지 않을 경우,
     * 버스 노선 타입 (RouteType)이 반환되지 않음.
     * 서울-경기 통합 정류장일 경우 처리가 필요.
     *
     * @param stationId
     * @param callback
     */
    @GET("//121.172.227.24/BusAPI/services/busStopArriveInfo.asp")
    void getArrivalInfo(@Query("bstopId") String stationId,
                        Callback<IncheonArrivalInfo> callback);

    @GET("//121.172.227.24/web/sub/getBusRoutePathJson.asp")
    void getRouteRealtime(@Query("routeid") String routeId,
                          Callback<IncheonBusPosition> callback);

    @GET("//bus.incheon.go.kr/iwcm/retrieverouteruninfolist.laf")
    void getRouteStations(@Query("routeid") String routeId,
                          Callback<List<RouteStation>> callback);

    @FormUrlEncoded
    @POST("//bus.incheon.go.kr/iw/pda/03/retrievePdaRouteBstop.laf")
    void getStationRoutes(@Field("nodeid") String stationId,
                          Callback<List<StationRoute>> callback);

}
