package kr.rokoroku.mbus.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;

/**
 * Created by rok on 2015. 5. 29..
 */
public class StationDataProvider {

    private Station station;
    private List<StationListItemData> adapterItemList;
    private SortedMap<RouteType, List<StationRoute>> typedStationRouteTable;

    public StationDataProvider(Station station) {
        setStation(station);
    }

    public void setStation(Station station) {
        this.station = station;
        organize();
    }

    public void setArrivalInfos(Collection<ArrivalInfo> arrivalInfoList) {
        this.station.setArrivalInfos(arrivalInfoList);
        organize();
    }

    public void putStationRouteList(Collection<StationRoute> routeList) {
        this.station.putStationRouteList(routeList);
        organize();
    }

    public Station getStation() {
        return station;
    }

    public Provider getProvider() {
        return station.getProvider();
    }

    public boolean hasLinkedStation() {
        return station != null && station.getExternalEntries() != null && !station.getExternalEntries().isEmpty();
    }

    public boolean isRouteInfoAvailable() {
        return station != null && station.isLocalRouteInfoAvailable();
    }

    public boolean isArrivalInfoAvailable() {
        return station != null && station.isEveryArrivalInfoAvailable();
    }

    public List<StationListItemData> getAdapterItemList() {
        return adapterItemList;
    }

    public List<StationRoute> getRawStationRouteList() {
        return station.getStationRouteList();
    }

    private List<StationRoute> getTypedStationRouteList(RouteType routeType) {
        if (hasLinkedStation()) {
            switch (routeType) {
                case GREEN_GYEONGGI:
                    routeType = RouteType.GREEN;
                    break;

                case RED_GYEONGGI:
                    routeType = RouteType.RED;
                    break;
            }
        }
        return typedStationRouteTable.get(routeType);
    }

    public Date getLastUpdateTime() {
        return station.getLastUpdateTime();
    }

    public int getCount() {
        int count = adapterItemList == null ? 0 : adapterItemList.size();
        return count;
    }

    public StationListItemData getItem(int index) {
        if (adapterItemList != null && index < adapterItemList.size()) {
            return adapterItemList.get(index);
        }
        return null;
    }

    public void clear() {
        if (adapterItemList == null) {
            adapterItemList = new ArrayList<>();
        } else {
            adapterItemList.clear();
        }
        if (typedStationRouteTable == null) {
            typedStationRouteTable = new TreeMap<>((lhs, rhs) -> lhs.ordinal() - rhs.ordinal());
        } else {
            typedStationRouteTable.clear();
        }
    }

    public void organize() {

        clear();

        // group by route type
        if (getRawStationRouteList() != null) {
            for (StationRoute stationRoute : getRawStationRouteList()) {
                RouteType routeType = stationRoute.getRouteType();
                if (routeType == null) routeType = RouteType.UNKNOWN;

                List<StationRoute> typedStationRouteList = getTypedStationRouteList(routeType);
                if (typedStationRouteList == null) {
                    typedStationRouteList = new ArrayList<>();
                    typedStationRouteTable.put(routeType, typedStationRouteList);
                }

                boolean conflict = false;
                for (StationRoute route : typedStationRouteList) {
                    if (route.getRouteName().equals(stationRoute.getRouteName())) {
                        if (!Provider.SEOUL.equals(stationRoute.getProvider()) && !stationRoute.getRouteName().startsWith("N")) {
                            conflict = true;
                        }
                    }
                }
                if (!conflict) typedStationRouteList.add(stationRoute);
            }
        }

        // sort
        for (List typedStationRouteList : typedStationRouteTable.values()) {
            Collections.sort(typedStationRouteList);
        }

        // put into adapter item list (visible item list)
        for (Map.Entry<RouteType, List<StationRoute>> entry : typedStationRouteTable.entrySet()) {
            adapterItemList.add(new StationListItemData(entry.getKey()));
            for (StationRoute stationRoute : entry.getValue()) {
                adapterItemList.add(new StationListItemData(stationRoute));
            }
        }
    }

    public static class StationListItemData {

        public enum Type {
            SECTION, ROUTE
        }

        private Type type;
        private Object data;

        public StationListItemData(StationRoute stationRoute) {
            this.type = Type.ROUTE;
            this.data = stationRoute;
        }

        public StationListItemData(RouteType routeType) {
            this.type = Type.SECTION;
            this.data = routeType;
        }

        public Type getType() {
            return type;
        }

        public StationRoute getStationRoute() {
            if (type.equals(Type.ROUTE)) {
                return (StationRoute) data;
            } else {
                return null;
            }
        }

        public RouteType getRouteType() {
            if (type.equals(Type.SECTION)) {
                return (RouteType) data;
            } else {
                return null;
            }
        }

        public int getId() {
            if (type.equals(Type.ROUTE)) {
                return getStationRoute().getRouteId().hashCode();
            } else {
                return getRouteType().ordinal();
            }
        }
    }
}
