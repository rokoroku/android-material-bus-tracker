package kr.rokoroku.mbus.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kr.rokoroku.mbus.api.gbisweb.model.SearchRouteResult;
import kr.rokoroku.mbus.model.BusLocation;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;

/**
 * Created by rok on 2015. 5. 29..
 */
public class RouteDataProvider {

    private Route route;
    private List<RouteListItemData> routeListItemData;

    public RouteDataProvider(Route route) {
        this.route = route;
    }

    public void setRoute(Route route) {
        this.route = route;
        rearrange();
    }

    public void setBusPositionList(List<BusLocation> busLocationList) {
        this.route.setBusLocationList(busLocationList);
        rearrange();
    }

    public void setRouteStationList(List<RouteStation> routeStationList) {
        this.route.setRouteStationList(routeStationList);
        rearrange();
    }

    public void setGbisRouteEntity(SearchRouteResult.ResultEntity resultEntity) {
        this.route.setGbisRouteEntity(resultEntity);
    }

    public void setGbisStationEntity(SearchRouteResult.ResultEntity resultEntity) {
        this.route.setGbisStationEntity(resultEntity);
        rearrange();
    }

    public void setGbisRealtimeBusEntity(SearchRouteResult.ResultEntity resultEntity) {
        this.route.setGbisRealtimeBusEntity(resultEntity);
        rearrange();
    }

    public Route getRoute() {
        return route;
    }

    public boolean isRouteInfoAvailable() {
        return route != null && route.isRouteBaseInfoAvailable();
    }

    public List<RouteStation> getRouteStationList() {
        return route.getRouteStationList();
    }

    public List<BusLocation> getBusPositionList() {
        return route.getBusLocationList();
    }

    public RouteListItemData getItem(int index) {
        if(routeListItemData != null && index >= 0 && index < routeListItemData.size()) {
            return routeListItemData.get(index);
        } else {
            return null;
        }
    }

    public Date getLastUpdateTime() {
        return route.getLastUpdateTime();
    }

    public int getCount() {
        return routeListItemData != null ? routeListItemData.size() : 0;
    }

    public boolean isExpandable(int index) {
        return getItem(index).type == RouteListItemData.Type.STATION
                && getItem(index).getRouteStation().isBusStop();
    }

    private void rearrange() {
        if (routeListItemData == null) {
            routeListItemData = new ArrayList<>();
        } else {
            routeListItemData.clear();
        }

        if (getRouteStationList() != null) {
            for (RouteStation routeStation : getRouteStationList()) {
                routeListItemData.add(new RouteListItemData(routeStation));
            }
        }
        if (getBusPositionList() != null) {
            for (BusLocation busLocation : getBusPositionList()) {
                routeListItemData.add(new RouteListItemData(busLocation));
            }
        }
        Collections.sort(routeListItemData);
    }

    public static class RouteListItemData implements Comparable<RouteListItemData> {

        public enum Type {
            STATION, BUS
        }

        private Type type;
        private RouteStation routeStation;
        private BusLocation busLocation;

        public RouteListItemData(RouteStation routeStation) {
            this.type = Type.STATION;
            this.routeStation = routeStation;
        }

        public RouteListItemData(BusLocation busLocation) {
            this.type = Type.BUS;
            this.busLocation = busLocation;
        }

        public Type getType() {
            return type;
        }

        public RouteStation getRouteStation() {
            return routeStation;
        }

        public BusLocation getBusLocation() {
            return busLocation;
        }

        public int getId() {
            if (type == Type.STATION) {
                return routeStation.getId() != null ? Integer.parseInt(routeStation.getId()) : routeStation.hashCode();
            } else {
                return busLocation.getId() != null ? Integer.parseInt(busLocation.getId()) : busLocation.hashCode();
            }
        }

        public int getSequence() {
            if (type == Type.STATION) {
                return routeStation.getSequence();
            } else {
                return busLocation.getStationSeq();
            }
        }

        @Override
        public int compareTo(RouteListItemData another) {
            int result = getSequence() - another.getSequence();
            if (result == 0) {
                if (type == Type.STATION && another.type == Type.BUS) {
                    result = -1;
                } else if (type == Type.BUS && another.type == Type.STATION) {
                    result = 1;
                }
            }
            return result;
        }

    }

}
