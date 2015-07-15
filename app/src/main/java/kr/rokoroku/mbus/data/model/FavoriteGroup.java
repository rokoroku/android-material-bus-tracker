package kr.rokoroku.mbus.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kr.rokoroku.mbus.core.Database;

/**
 * Created by rok on 2015. 7. 7..
 */
public class FavoriteGroup implements Serializable {

    private String name;
    private List<FavoriteItem> list;

    public FavoriteGroup(String name) {
        super();
        this.name = name;
        this.list = new ArrayList<>();
    }

    public FavoriteGroup(String name, Collection<? extends FavoriteItem> collection) {
        this(name);
        this.list.addAll(collection);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FavoriteGroup{" +
                "name='" + name + '\'' +
                ", " + super.toString() +
                '}';
    }

    public void move(int from, int to) {
        FavoriteItem item = remove(from);
        if(item != null) {
            add(to, item);
        }
    }

    public void add(int index, FavoriteItem object) {
        list.add(index, object);
    }

    public boolean add(FavoriteItem object) {
        return list.add(object);
    }

    public FavoriteItem get(int index) {
        return list.get(index);
    }

    public boolean contains(Object object) {
        return list.contains(object);
    }

    public FavoriteItem remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object object) {
        return list.remove(object);
    }

    public FavoriteItem set(int index, FavoriteItem object) {
        return list.set(index, object);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public Collection<? extends FavoriteItem> getItems() {
        return list;
    }

    public long getId() {
        return name.hashCode();
    }

    public static class FavoriteItem implements Serializable {

        public enum Type {
            STATION, ROUTE
        }

        private Type type;
        private AccessKey dataAccessKey;
        private AccessKey extraDataAccessKey;

        private transient Object data;
        private transient Object extraData;

        public FavoriteItem(Station station) {
            this.type = Type.STATION;
            this.data = station;
            this.dataAccessKey = new AccessKey(station.getProvider(), station.getId());
        }

        public FavoriteItem(Station station, StationRoute stationRoute) {
            this(station);
            setExtraData(stationRoute);
        }

        public FavoriteItem(Route route) {
            this.type = Type.ROUTE;
            this.data = route;
            this.dataAccessKey = new AccessKey(route.getProvider(), route.getId());
        }

        public FavoriteItem(Route route, RouteStation routeStation) {
            this(route);
            setExtraData(routeStation);
        }

        public void setExtraData(RouteStation routeStation) {
            if (type.equals(Type.ROUTE)) {
                this.extraData = routeStation;
                if (routeStation != null) {
                    this.extraDataAccessKey = new AccessKey(routeStation.getProvider(), routeStation.getLocalId());
                } else {
                    this.extraDataAccessKey = null;
                }
            }
        }

        public void setExtraData(StationRoute stationRoute) {
            if (type.equals(Type.STATION)) {
                this.extraData = stationRoute;
                if (stationRoute != null) {
                    this.extraDataAccessKey = new AccessKey(stationRoute.getProvider(), stationRoute.getRouteId());
                } else {
                    this.extraDataAccessKey = null;
                }
            }
        }

        public Type getType() {
            return type;
        }

        public long getId() {
            if (data != null) {
                if (data instanceof Route) {
                    return ((Route) data).getId().hashCode() + this.hashCode();
                } else if (data instanceof Station) {
                    return ((Station) data).getId().hashCode() + this.hashCode();
                }
            }
            return this.hashCode();
        }

        public <T> T getData(Class<T> clazz) {
            if (data == null) {
                Database database = Database.getInstance();
                if (type.equals(Type.ROUTE)) {
                    data = database.getRoute(dataAccessKey.provider, dataAccessKey.id);
                } else {
                    data = database.getStation(dataAccessKey.provider, dataAccessKey.id);
                }
            }
            if (clazz.isInstance(data)) {
                return clazz.cast(data);
            } else {
                return null;
            }
        }

        public <T> T getExtraData(Class<T> clazz) {
            if (dataAccessKey != null && extraDataAccessKey != null) {
                if(extraData == null) {
                    if (type.equals(Type.ROUTE)) {
                        Route route = getData(Route.class);
                        if(route != null) {
                            List<RouteStation> routeStationList = route.getRouteStationList();
                            if(routeStationList != null) {
                                for (RouteStation routeStation : routeStationList) {
                                    if(routeStation.getProvider().equals(extraDataAccessKey.provider) && routeStation.getLocalId().equals(extraDataAccessKey.id)) {
                                        extraData = routeStation;
                                        break;
                                    }
                                }
                            }
                        }

                    } else if(type.equals(Type.STATION)){
                        Station station = getData(Station.class);
                        if(station != null) {
                            extraData = station.getStationRoute(extraDataAccessKey.id);
                        }
                    }
                }
                if (clazz.isInstance(extraData)) {
                    return clazz.cast(extraData);
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "FavoriteItem{" +
                    "type=" + type +
                    ", dataAccessKey=" + dataAccessKey +
                    ", extraDataAccessKey=" + extraDataAccessKey +
                    ", data=" + data +
                    ", extraData=" + extraData +
                    '}';
        }

        public class AccessKey implements Serializable {
            Provider provider;
            String id;

            public AccessKey(Provider provider, String id) {
                this.provider = provider;
                this.id = id;
            }

            @Override
            public String toString() {
                return "AccessKey{" +
                        "provider=" + provider +
                        ", id='" + id + '\'' +
                        '}';
            }
        }
    }
}
