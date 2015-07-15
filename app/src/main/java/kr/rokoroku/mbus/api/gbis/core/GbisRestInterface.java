package kr.rokoroku.mbus.api.gbis.core;

import com.google.android.gms.analytics.HitBuilders;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbis.model.GbisBaseInfo;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 4. 22..
 */
public interface GbisRestInterface {

    /**
     * 특정 노선의 버스 위치 정보 조회
     *
     * @param routeId
     * @param callback
     */
    @GET("/buslocationservice")
    void getBusLocationList(@Query("routeId") String routeId,
                            Callback<GbisBusLocationList> callback);

    /**
     * 특정 정류장의 버스 도착 정보 조회
     *
     * @param stationId
     * @param callback
     */
    @GET("/busarrivalservice/station")
    void getBusArrivalList(@Query("stationId") String stationId,
                           Callback<GbisBusArrivalList> callback);

    /**
     * 특정 정류장의 특정 노선 도착 정보 조회
     *
     * @param routeId
     * @param stationId
     * @param callback
     */
    @GET("/busarrivalservice")
    void getBusArrivalList(@Query("stationId") String stationId,
                           @Query("routeId") String routeId,
                           Callback<GbisBusArrivalList> callback);

    /**
     * 기반정보 조회
     *
     * @param callback
     */
    @GET("/baseinfoservice")
    void getBaseInfo(Callback<GbisBaseInfo> callback);

}
