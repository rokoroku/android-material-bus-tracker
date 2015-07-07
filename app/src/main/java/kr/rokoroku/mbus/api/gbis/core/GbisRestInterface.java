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
    void getBusArrivalListByRouteId(@Query("stationId") String stationId,
                                    @Query("routeId") String routeId,
                                    Callback<GbisBusArrivalList> callback);

    /**
     * 기반정보 조회
     *
     * @param callback
     */
    @GET("/baseinfoservice")
    void getBaseInfo(Callback<GbisBaseInfo> callback);


    /**
     * Google analytics Wrapper
     */
    abstract class GAWrappedClient implements GbisRestInterface {

        private GbisRestInterface gbisRestInterface;

        public GAWrappedClient(GbisRestInterface gbisRestInterface) {
            this.gbisRestInterface = gbisRestInterface;
        }

        @Override
        public void getBaseInfo(Callback<GbisBaseInfo> callback) {
            if (BaseApplication.tracker != null)
                BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("GBIS")
                        .setLabel("Base Info Service")
                        .setAction("getBaseInfo")
                        .build());
            gbisRestInterface.getBaseInfo(callback);
        }

        @Override
        public void getBusLocationList(@Query("routeId") String routeId, Callback<GbisBusLocationList> callback) {
            if (BaseApplication.tracker != null)
                BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("GBIS")
                        .setLabel("Bus Location Service")
                        .setAction("getBusLocationList")
                        .build());
            gbisRestInterface.getBusLocationList(routeId, callback);
        }

        @Override
        public void getBusArrivalList(@Query("stationId") String stationId, Callback<GbisBusArrivalList> callback) {
            if (BaseApplication.tracker != null)
                BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("GBIS")
                        .setLabel("Bus Arrival Service")
                        .setAction("getBusArrivalList")
                        .build());
            gbisRestInterface.getBusArrivalList(stationId, callback);
        }

        @Override
        public void getBusArrivalListByRouteId(@Query("stationId") String stationId, @Query("routeId") String routeId, Callback<GbisBusArrivalList> callback) {
            if (BaseApplication.tracker != null)
                BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("GBIS")
                        .setLabel("Bus Arrival Service")
                        .setAction("getBusArrivalListByRouteId")
                        .build());
            gbisRestInterface.getBusArrivalListByRouteId(stationId, routeId, callback);
        }
    }
}
