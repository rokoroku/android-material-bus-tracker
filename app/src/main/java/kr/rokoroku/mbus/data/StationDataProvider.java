package kr.rokoroku.mbus.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;

/**
 * Created by rok on 2015. 5. 29..
 */
public class StationDataProvider {

    private Station station;
    private List<StationListItemData> adapterItemList;
    private List<StationRoute> favoritedStationRouteList;
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
        return station != null && station.getRemoteEntries() != null && !station.getRemoteEntries().isEmpty();
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
                case GREEN_INCHEON:
                    routeType = RouteType.GREEN;
                    break;

                case RED_GYEONGGI:
                case RED_INCHEON:
                    routeType = RouteType.RED;
                    break;
            }
        } else {
            switch (routeType) {
                case GREEN_INCHEON:
                    routeType = RouteType.GREEN_GYEONGGI;
                    break;

                case RED_INCHEON:
                    routeType = RouteType.RED_GYEONGGI;
                    break;
            }
        }
        List<StationRoute> typedTable = typedStationRouteTable.get(routeType);
        if (typedTable == null) {
            typedTable = new ArrayList<>();
            typedStationRouteTable.put(routeType, typedTable);
        }
        return typedTable;
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
        if (favoritedStationRouteList == null) {
            favoritedStationRouteList = new ArrayList<>();
        } else {
            favoritedStationRouteList.clear();
        }
    }

    public boolean checkFavoritedRoute(StationRoute stationRoute) {
        Favorite favorite = FavoriteFacade.getInstance().getCurrentFavorite();
        for (FavoriteGroup favoriteGroup : favorite.getFavoriteGroups()) {
            for (int i = 0; i < favoriteGroup.size(); i++) {
                FavoriteItem favoriteItem = favoriteGroup.get(i);
                FavoriteItem.Type type = favoriteItem.getType();
                if (type == FavoriteItem.Type.ROUTE) {
                    Route data = favoriteItem.getData(Route.class);
                    if (data != null && stationRoute.getRouteId().equals(data.getId())) {
                        return true;
                    }

                } else if(type == FavoriteItem.Type.STATION) {
                    StationRoute route = favoriteItem.getExtraData(StationRoute.class);
                    if(route != null && stationRoute.getRouteId().equals(route.getRouteId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void organize() {
        clear();

        // group by route type
        if (getRawStationRouteList() != null) {
            for (StationRoute stationRoute : getRawStationRouteList()) {
                RouteType routeType = stationRoute.getRouteType();
                if (routeType == null) routeType = RouteType.UNKNOWN;

                List<StationRoute> targetStationRouteList;
                if (checkFavoritedRoute(stationRoute)) {
                    targetStationRouteList = getFavoritedStationRouteList();
                } else {
                    targetStationRouteList = getTypedStationRouteList(routeType);
                }

                boolean conflict = false;
                for (StationRoute route : targetStationRouteList) {
                    if (route.getRouteName().equals(stationRoute.getRouteName())) {
                        if (!Provider.SEOUL.equals(stationRoute.getProvider())) {
                            conflict = true;
                        }
                    }
                }

                if (!conflict) {
                    targetStationRouteList.add(stationRoute);
                }
            }
        }

        // sort
        for (List typedStationRouteList : typedStationRouteTable.values()) {
            Collections.sort(typedStationRouteList);
        }

        // put into adapter item list (visible item list)
        if(favoritedStationRouteList != null && !favoritedStationRouteList.isEmpty()) {
            Collections.sort(favoritedStationRouteList);
            String favoriteRoute = BaseApplication.getInstance().getString(R.string.route_type_favorite);
            adapterItemList.add(new StationListItemData(favoriteRoute));
            for (StationRoute stationRoute : favoritedStationRouteList) {
                adapterItemList.add(new StationListItemData(stationRoute));
            }
        }

        for (Map.Entry<RouteType, List<StationRoute>> entry : typedStationRouteTable.entrySet()) {
            adapterItemList.add(new StationListItemData(entry.getKey()));
            for (StationRoute stationRoute : entry.getValue()) {
                adapterItemList.add(new StationListItemData(stationRoute));
            }
        }
    }

    public List<StationRoute> getFavoritedStationRouteList() {
        return favoritedStationRouteList;
    }

    public static class StationListItemData {

        public enum Type {
            SECTION, ROUTE
        }

        private Type type;
        private Object data;

        public StationListItemData(String title) {
            this.type = Type.SECTION;
            this.data = title;
        }

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
            if (data instanceof StationRoute) {
                return (StationRoute) data;
            } else {
                return null;
            }
        }

        public RouteType getRouteType() {
            if (data instanceof RouteType) {
                return (RouteType) data;
            } else {
                return null;
            }
        }

        public int getId() {
            if (type.equals(Type.ROUTE)) {
                return getStationRoute().getRouteId().hashCode();
            } else if (data instanceof RouteType) {
                return getRouteType().ordinal();
            } else {
                return data.hashCode();
            }
        }

        public String getTitle(Context context) {
            RouteType routeType = getRouteType();
            if (routeType != null) {
                return routeType.getDescription(context);
            } else if (data instanceof String) {
                return (String) data;
            } else {
                return null;
            }
        }
    }
}
