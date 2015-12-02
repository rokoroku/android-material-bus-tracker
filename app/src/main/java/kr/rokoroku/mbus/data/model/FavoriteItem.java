package kr.rokoroku.mbus.data.model;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.util.SerializeUtil;

/**
 * Created by rok on 15. 8. 29..
 */
public class FavoriteItem implements Serializable {

    static final long serialVersionUID = 1L;

    public enum Type {
        STATION, ROUTE
    }

    private Type type;
    private AccessKey dataKey;
    private AccessKey extraKey;

    private transient Object data;
    private transient Object extraData;

    private FavoriteItem(Type type, AccessKey key) {
        this.type = type;
        this.dataKey = key;
    }

    public FavoriteItem(Station station) {
        this.type = Type.STATION;
        this.data = station;
        this.dataKey = new AccessKey(station.getProvider(), station.getId());
    }

    public FavoriteItem(Station station, StationRoute stationRoute) {
        this(station);
        setExtraData(stationRoute);
    }

    public FavoriteItem(Route route) {
        this.type = Type.ROUTE;
        this.data = route;
        this.dataKey = new AccessKey(route.getProvider(), route.getId());
    }

    public FavoriteItem(Route route, RouteStation routeStation) {
        this(route);
        setExtraData(routeStation);
    }

    public void setExtraData(RouteStation routeStation) {
        if (type.equals(Type.ROUTE)) {
            if (routeStation != null) {
                this.extraKey = new AccessKey(routeStation.getProvider(), routeStation.getLocalId());
                this.extraData = routeStation;
            } else {
                this.extraKey = null;
                this.extraData = null;
            }
        }
    }

    public void setExtraData(StationRoute stationRoute) {
        if (type.equals(Type.STATION)) {
            if (stationRoute != null) {
                this.extraKey = new AccessKey(stationRoute.getProvider(), stationRoute.getRouteId());
                this.extraData = stationRoute;
            } else {
                this.extraKey = null;
                this.extraData = null;
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
            DatabaseFacade databaseFacade = DatabaseFacade.getInstance();
            if (type.equals(Type.ROUTE)) {
                data = databaseFacade.getRoute(dataKey.provider, dataKey.id);
            } else {
                data = databaseFacade.getStation(dataKey.provider, dataKey.id);
            }
        }
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        } else {
            return null;
        }
    }

    public <T> T getExtraData(Class<T> clazz) {
        if (dataKey != null && extraKey != null) {
            if (extraData == null) {
                if (type.equals(Type.ROUTE)) {
                    Route route = getData(Route.class);
                    if (route != null) {
                        List<RouteStation> routeStationList = route.getRouteStationList();
                        if (routeStationList != null) {
                            for (RouteStation routeStation : routeStationList) {
                                if (routeStation.getProvider().equals(extraKey.provider) && routeStation.getLocalId().equals(extraKey.id)) {
                                    extraData = routeStation;
                                    break;
                                }
                            }
                        }
                    }

                } else if (type.equals(Type.STATION)) {
                    Station station = getData(Station.class);
                    if (station != null) {
                        extraData = station.getStationRoute(extraKey.id);
                    }
                }
            }
            if (clazz.isInstance(extraData)) {
                return clazz.cast(extraData);
            }
        }
        return null;
    }

    public boolean contains(Route route) {
        if (dataKey != null) {
            if (dataKey.provider == route.getProvider() && dataKey.id.equals(route.getId())) return true;
        }
        if (extraKey != null) {
            if (extraKey.id.equals(route.getId())) return true;
        }
        return false;
    }

    public boolean contains(Station station) {
        if (dataKey != null) {
            if (dataKey.provider == station.getProvider() && dataKey.id.equals(station.getId())) return true;
        }
        if (extraKey != null) {
            if (extraKey.id.equals(station.getId())) return true;
        }
        return false;
    }

    public static class AccessKey implements Serializable {
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

        public static final Serializer<AccessKey> SERIALIZER = new Serializer<AccessKey>() {
            @Override
            public void serialize(DataOutput out, AccessKey value) throws IOException {
                if (value != null) {
                    out.writeByte(1);
                    SerializeUtil.writeString(out, value.id);
                    Provider.SERIALIZER.serialize(out, value.provider);
                } else {
                    out.writeByte(0);
                }
            }

            @Override
            public AccessKey deserialize(DataInput in, int available) throws IOException {
                if (in.readByte() == 1) {
                    String id = SerializeUtil.readString(in);
                    Provider provider = Provider.SERIALIZER.deserialize(in, available);
                    return new AccessKey(provider, id);
                } else {
                    return null;
                }
            }
        };

    }

    public static final Serializer<FavoriteItem> SERIALIZER = new Serializer<FavoriteItem>() {
        @Override
        public void serialize(DataOutput out, FavoriteItem value) throws IOException {
            out.writeInt(value.type.ordinal());
            AccessKey.SERIALIZER.serialize(out, value.dataKey);
            AccessKey.SERIALIZER.serialize(out, value.extraKey);
        }

        @Override
        public FavoriteItem deserialize(DataInput in, int available) throws IOException {
            Type type = Type.values()[in.readInt()];
            AccessKey key = AccessKey.SERIALIZER.deserialize(in, available);
            AccessKey extraKey = AccessKey.SERIALIZER.deserialize(in, available);

            FavoriteItem item = new FavoriteItem(type, key);
            item.extraKey = extraKey;
            return item;
        }
    };
}
