package kr.rokoroku.mbus.api;

import java.util.List;

import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.MapLine;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.Station;

/**
 * Created by rok on 2015. 7. 13..
 */
public interface ApiWrapperInterface {

    Provider getProvider();

    void searchRouteByKeyword(String keyword, Callback<List<Route>> callback);
    void searchStationByKeyword(String keyword, Callback<List<Station>> callback);
    void searchStationByLocation(double latitude, double longitude, int radius, Callback<List<Station>> callback);

    void getRouteBaseInfo(String routeId, Callback<Route> callback);
    void getRouteRealtimeInfo(String routeId, Callback<List<BusLocation>> callback);
    void getRouteMaplineInfo(String routeId, Callback<List<MapLine>> callback);

    void getStationBaseInfo(String stationId, Callback<Station> callback);
    void getStationArrivalInfo(String stationId, Callback<List<ArrivalInfo>> callback);
    void getStationArrivalInfo(String stationId, String routeId, Callback<ArrivalInfo> callback);

    interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Throwable t);
    }
}
