package kr.rokoroku.mbus.api.tago.core;

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

import kr.rokoroku.mbus.api.tago.model.ArrivalInfoResult;
import kr.rokoroku.mbus.api.tago.model.SearchStationResult;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface TagoRestInterface {

    /**
     * 정류소 검색 (명칭)
     *
     * @param keyword
     * @param callback
     */
    @GET("/BusSttnInfoInqireService/getSttnNoList")
    void searchStationByName(@Query("nodeNm") String keyword,
                             Callback<SearchStationResult> callback);

    /**
     * 도착정보 조회 (정류장)
     *
     * @param stationId
     * @param callback
     */
    @GET("/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList")
    void getArrivalInfo(@Query("nodeId") String stationId,
                        Callback<ArrivalInfoResult> callback);

    /**
     * 도착정보 조회 2
     *
     * @param stationId
     * @param routeId
     * @param callback
     */
    @GET("/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList")
    void getArrivalInfo(@Query("nodeId") String stationId,
                        @Query("routeId") String routeId,
                        Callback<ArrivalInfoResult> callback);

}
