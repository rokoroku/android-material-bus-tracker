package kr.rokoroku.mbus.api.seoul.core;

import com.google.android.gms.analytics.HitBuilders;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbis.model.GbisBaseInfo;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface SeoulBusRestInterface {

    /**
     * 버스 노선이름 검색
     *
     * @param busNumber
     * @param callback
     */
    @GET("/busRouteInfo/getBusRouteList")
    void searchRouteListByName(@Query("strSrch") String busNumber,
                               Callback<SeoulBusRouteInfoList> callback);

    /**
     * 특정 노선의 기본 정보 조회
     *
     * @param busRouteId
     * @param callback
     */
    @GET("/busRouteInfo/getRouteInfo")
    void getRouteInfo(@Query("busRouteId") String busRouteId,
                      Callback<SeoulBusRouteInfoList> callback);

    /**
     * 특정 노선의 경유 정류장 조회
     *
     * @param busRouteId
     * @param callback
     */
    @GET("/busRouteInfo/getStaionByRoute")
    void getRouteStationList(@Query("busRouteId") String busRouteId,
                             Callback<SeoulBusRouteStationList> callback);

    /**
     * 특정 노선의 현재 위치 조회
     *
     * @param busRouteId
     * @param callback
     */
    @GET("/buspos/getBusPosByRtid")
    void getRouteBusPositionList(@Query("busRouteId") String busRouteId,
                                 Callback<SeoulBusLocationList> callback);

    /**
     * 명칭기반 정류소 검색
     *
     * @param stationName
     * @param callback
     */
    @GET("/stationinfo/getStationByName")
    void searchStationListByName(@Query("stSrch") String stationName,
                                 Callback<SeoulStationInfoList> callback);

    /**
     * 좌표기반 정류소 검색
     *
     * @param longitude
     * @param latitude
     * @param radius
     * @param callback
     */
    @GET("/stationinfo/getStationByPos")
    void searchStationListByPos(@Query("tmX") double longitude,
                                @Query("tmY") double latitude,
                                @Query("radius") int radius,
                                Callback<SeoulStationInfoList> callback);

    /**
     * 정류소 경유노선 검색
     *
     * @param arsId      서울시 조회용 정류소 ID
     * @param callback
     */
    @GET("/stationinfo/getRouteByStation")
    void getRouteByStation(@Query("arsId") String arsId,
                           Callback<SeoulBusRouteByStationList> callback);

    /**
     * 도착정보 검색 (특정 노선, 특정 정류소)
     * 순번까지 알아야함. ㅁㅊ
     *
     * @param stationId  정보제공처별 고유 ID
     * @param routeId
     * @param stationSeq
     * @param callback
     */
    @GET("/arrive/getArrInfoByRoute")
    void getArrivalInfo(@Query("stId") String stationId,
                        @Query("busRouteId") String routeId,
                        @Query("ord") int stationSeq,
                        Callback<SeoulBusArrivalList> callback);

    /**
     * 도착정보 검색 (특정 노선, 모든 정류소)
     *
     * @param routeId
     * @param callback
     */
    @GET("/arrive/getArrInfoByRouteAll")
    void getArrivalInfo(@Query("busRouteId") String routeId,
                        Callback<SeoulBusArrivalList> callback);

}
