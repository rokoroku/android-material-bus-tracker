package kr.rokoroku.mbus.api;

import com.google.android.gms.analytics.HitBuilders;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.api.gbis.core.GbisRestInterface;
import kr.rokoroku.mbus.api.gbis.model.GbisBaseInfo;
import kr.rokoroku.mbus.api.gbis.model.GbisBusArrivalList;
import kr.rokoroku.mbus.api.gbis.model.GbisBusLocationList;
import kr.rokoroku.mbus.api.gbisweb.core.GbisWebRestInterface;
import kr.rokoroku.mbus.api.gbisweb.model.SearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchMapLineResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchStationByPosResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusRestInterface;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrivalList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfoList;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStationList;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;
import kr.rokoroku.mbus.api.seoulweb.core.SeoulWebRestInterface;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.Query;

/**
 * Created by rok on 2015. 7. 14..
 */
public class GAWrapper {

    public static SeoulBusRestInterface wrap(SeoulBusRestInterface seoulBusRestInterface) {
        return new SeoulBusRestInterface() {
            @Override
            public void getArrivalInfo(@Query("busRouteId") String routeId, Callback<SeoulBusArrivalList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Arrival Service")
                            .setAction("getArrInfoByRoute")
                            .build());
                seoulBusRestInterface.getArrivalInfo(routeId, callback);
            }

            @Override
            public void getArrivalInfo(@Query("stId") String stationId,
                                       @Query("busRouteId") String routeId,
                                       @Query("ord") int stationSeq,
                                       Callback<SeoulBusArrivalList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Arrival Service")
                            .setAction("getArrInfoByRouteAll")
                            .build());
                seoulBusRestInterface.getArrivalInfo(stationId, routeId, stationSeq, callback);
            }

            @Override
            public void searchRouteListByName(@Query("strSrch") String busNumber,
                                              Callback<SeoulBusRouteInfoList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Route Info Service")
                            .setAction("getBusRouteList")
                            .build());
                seoulBusRestInterface.searchRouteListByName(busNumber, callback);
            }

            @Override
            public void getRouteInfo(@Query("busRouteId") String busRouteId,
                                     Callback<SeoulBusRouteInfoList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Route Info Service")
                            .setAction("getRouteInfo")
                            .build());
                seoulBusRestInterface.searchRouteListByName(busRouteId, callback);
            }

            @Override
            public void getRouteStationList(@Query("busRouteId") String busRouteId,
                                            Callback<SeoulBusRouteStationList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Bus Route Info Service")
                            .setAction("getStationByRoute")
                            .build());
                seoulBusRestInterface.getRouteStationList(busRouteId, callback);
            }

            @Override
            public void getRouteBusPositionList(@Query("busRouteId") String busRouteId,
                                                Callback<SeoulBusLocationList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Bus Route Info Service")
                            .setAction("getBusPosByRtid")
                            .build());
                seoulBusRestInterface.getRouteBusPositionList(busRouteId, callback);
            }

            @Override
            public void searchStationListByName(@Query("stSrch") String stationName,
                                                Callback<SeoulStationInfoList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Bus Station Info Service")
                            .setAction("getStationByName")
                            .build());
                seoulBusRestInterface.searchStationListByName(stationName, callback);
            }

            @Override
            public void searchStationListByPos(@Query("tmX") double longitude,
                                               @Query("tmY") double latitude,
                                               @Query("radius") int radius,
                                               Callback<SeoulStationInfoList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Bus Station Info Service")
                            .setAction("getStationByPos")
                            .build());
                seoulBusRestInterface.searchStationListByPos(longitude, latitude, radius, callback);
            }

            @Override
            public void getRouteByStation(@Query("arsId") String arsId, Callback<SeoulBusRouteByStationList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("SEOUL")
                            .setLabel("Bus Station Info Service")
                            .setAction("getRouteByStation")
                            .build());
                seoulBusRestInterface.getRouteByStation(arsId, callback);
            }
        };
    }

    public static SeoulWebRestInterface wrap(SeoulWebRestInterface seoulWebRestInterface) {
        return new SeoulWebRestInterface() {
            @Override
            public void searchRoute(@Field("strSrch") String keyword,
                                    Callback<kr.rokoroku.mbus.api.seoulweb.model.SearchRouteResult> callback) {
                seoulWebRestInterface.searchRoute(keyword, callback);
            }

            @Override
            public void searchStation(@Field("stSrch") String keyword,
                                      Callback<kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult> callback) {
                seoulWebRestInterface.searchStation(keyword, callback);
            }

            @Override
            public void getRouteStations(@Field("busRouteId") String routeId,
                                         Callback<RouteStationResult> callback) {
                seoulWebRestInterface.getRouteStations(routeId, callback);
            }

            @Override
            public void getStationRoutes(@Field("arsId") String stationLocalId,
                                         Callback<RouteStationResult> callback) {
                seoulWebRestInterface.getStationRoutes(stationLocalId, callback);
            }

            @Override
            public void getRouteMapLine(@Query("rout_id") String routeId,
                                        Callback<TopisMapLineResult> callback) {
                seoulWebRestInterface.getRouteMapLine(routeId, callback);
            }
        };
    }

    public static GbisRestInterface wrap(GbisRestInterface gbisRestInterface) {
        return new GbisRestInterface() {
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
            public void getBusLocationList(@Query("routeId") String routeId,
                                           Callback<GbisBusLocationList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("GBIS")
                            .setLabel("Bus Location Service")
                            .setAction("getBusLocationList")
                            .build());
                gbisRestInterface.getBusLocationList(routeId, callback);
            }

            @Override
            public void getBusArrivalList(@Query("stationId") String stationId,
                                          Callback<GbisBusArrivalList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("GBIS")
                            .setLabel("Bus Arrival Service")
                            .setAction("getBusArrivalList")
                            .build());
                gbisRestInterface.getBusArrivalList(stationId, callback);
            }

            @Override
            public void getBusArrivalList(@Query("stationId") String stationId,
                                          @Query("routeId") String routeId,
                                          Callback<GbisBusArrivalList> callback) {
                if (BaseApplication.tracker != null)
                    BaseApplication.tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("GBIS")
                            .setLabel("Bus Arrival Service")
                            .setAction("getBusArrivalList")
                            .build());
                gbisRestInterface.getBusArrivalList(stationId, routeId, callback);
            }
        };
    }

    public static GbisWebRestInterface wrap(GbisWebRestInterface gbisWebRestInterface) {
        return new GbisWebRestInterface() {
            @Override
            public void searchAll(@Field("searchKeyword") String keyword,
                                  @Field("pageOfRoute") int pageOfRoute,
                                  @Field("pageOfBus") int pageOfBus,
                                  Callback<SearchAllResult> callback) {
                gbisWebRestInterface.searchAll(keyword, pageOfRoute, pageOfBus, callback);
            }

            @Override
            public void searchStationByPos(@Field("lat") double EPSGlatitude,
                                           @Field("lon") double EPSGlongitude,
                                           @Field("radius") int radius,
                                           Callback<SearchStationByPosResult> callback) {
                gbisWebRestInterface.searchStationByPos(EPSGlatitude, EPSGlongitude, radius, callback);
            }

            @Override
            public void getRouteInfo(@Field("routeId") String routeId,
                                     Callback<SearchRouteResult> callback) {
                gbisWebRestInterface.getRouteInfo(routeId, callback);
            }

            @Override
            public void getStationInfo(@Field("stationId") String stationId,
                                       Callback<SearchStationResult> callback) {
                gbisWebRestInterface.getStationInfo(stationId, callback);
            }

            @Override
            public void getRouteMapLine(@Field("routeId") String routeId,
                                        Callback<SearchMapLineResult> callback) {
                gbisWebRestInterface.getRouteMapLine(routeId, callback);
            }
        };
    }

}
