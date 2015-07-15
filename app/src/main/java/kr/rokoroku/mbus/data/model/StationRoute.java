package kr.rokoroku.mbus.data.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import kr.rokoroku.mbus.api.gbisweb.model.GbisStationRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStation;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.core.DatabaseFacade;

public class StationRoute implements Parcelable, Serializable, Comparable<StationRoute> {

    private String routeId;
    private String routeName;
    private String firstStationName;
    private String lastStationName;
    private String stationLocalId;
    private String destination;
    private Provider provider;
    private RouteType routeType;
    private int sequence = -1;

    private transient Route route;
    private transient ArrivalInfo arrivalInfo;

    public StationRoute() {

    }

    public StationRoute(Route route, String stationLocalId) {
        setRoute(route);
        this.stationLocalId = stationLocalId;
    }

    public StationRoute(SeoulBusRouteByStation entity, String stationLocalId) {
        this.routeId = entity.getBusRouteId();
        this.routeName = entity.getBusRouteNm();
        this.firstStationName = entity.getStBegin();
        this.lastStationName = entity.getStEnd();
        this.stationLocalId = stationLocalId;
        this.routeType = RouteType.valueOfTopis(entity.getBusRouteType());
        this.provider = Provider.SEOUL;
    }

    public StationRoute(GbisStationRouteResult.ResultEntity.BusStationInfoEntity busStationInfoEntity, String stationLocalId) {
        this.routeId = busStationInfoEntity.getRouteId();
        this.routeName = busStationInfoEntity.getRouteName();
        this.stationLocalId = stationLocalId;
        this.provider = Provider.GYEONGGI;
    }

    public StationRoute(StationRouteResult.RouteEntity routeEntity) {
        this.routeId = routeEntity.busRouteId;
        this.routeName = routeEntity.rtNm;
        this.stationLocalId = routeEntity.arsId;
        this.routeType = RouteType.valueOfTopis(routeEntity.routeType);
        this.provider = Provider.SEOUL;
        this.sequence = routeEntity.staOrd;
        this.destination = routeEntity.adirection;
        if (routeEntity.sectNm != null) {
            String[] split = routeEntity.sectNm.split("~");
            if (split.length >= 1) this.firstStationName = split[0];
            if (split.length >= 2) this.lastStationName = split[1];
        }
        if (sequence == 0) sequence = -1;
    }

    public Route getRoute() {
        if (route == null && routeId != null)
            route = DatabaseFacade.getInstance().getRoute(provider, routeId);
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
        this.routeId = route.getId();
        this.routeName = route.getName();
        this.provider = route.getProvider();
        this.firstStationName = route.getStartStationName();
        this.lastStationName = route.getEndStationName();
        this.routeType = route.getType();
    }

    public ArrivalInfo getArrivalInfo() {
        return arrivalInfo;
    }

    public void setArrivalInfo(ArrivalInfo arrivalInfo) {
        this.arrivalInfo = arrivalInfo;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getLocalStationId() {
        return stationLocalId;
    }

    public void setStationLocalId(String stationLocalId) {
        this.stationLocalId = stationLocalId;
    }

    public String getFirstStationName() {
        return firstStationName;
    }

    public void setFirstStationName(String firstStationName) {
        this.firstStationName = firstStationName;
    }

    public String getLastStationName() {
        return lastStationName;
    }

    public void setLastStationName(String lastStationName) {
        this.lastStationName = lastStationName;
    }

    public RouteType getRouteType() {
        if (routeType == null && getRoute() != null) {
            routeType = getRoute().getType();
        }
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public int compareTo(StationRoute another) {
        return routeName.compareTo(another.getRouteName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StationRoute that = (StationRoute) o;

        if (routeId != null ? !routeId.equals(that.routeId) : that.routeId != null) return false;
        return provider == that.provider;
    }

    @Override
    public int hashCode() {
        int result = routeId != null ? routeId.hashCode() : 0;
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.routeId);
        dest.writeString(this.routeName);
        dest.writeString(this.stationLocalId);
        dest.writeString(this.firstStationName);
        dest.writeString(this.lastStationName);
        dest.writeString(this.destination);
        dest.writeInt(this.routeType == null ? -1 : this.routeType.ordinal());
        dest.writeInt(this.provider == null ? -1 : this.provider.ordinal());
        dest.writeInt(this.sequence);
        dest.writeParcelable(this.arrivalInfo, 0);
    }

    protected StationRoute(Parcel in) {
        this.routeId = in.readString();
        this.routeName = in.readString();
        this.stationLocalId = in.readString();
        this.firstStationName = in.readString();
        this.lastStationName = in.readString();
        this.destination = in.readString();
        int tmpRouteType = in.readInt();
        this.routeType = tmpRouteType == -1 ? null : RouteType.values()[tmpRouteType];
        int tmpProvider = in.readInt();
        this.provider = tmpProvider == -1 ? null : Provider.values()[tmpProvider];
        this.sequence = in.readInt();
        this.arrivalInfo = in.readParcelable(ArrivalInfo.class.getClassLoader());
    }

    public static final Creator<StationRoute> CREATOR = new Creator<StationRoute>() {
        public StationRoute createFromParcel(Parcel source) {
            return new StationRoute(source);
        }

        public StationRoute[] newArray(int size) {
            return new StationRoute[size];
        }
    };

    public static Serializer<StationRoute> SERIALIZER = new Serializer<StationRoute>() {
        @Override
        public void serialize(DataOutput out, StationRoute value) throws IOException {
            out.writeUTF(value.routeId);
            out.writeUTF(value.routeName);
            out.writeUTF(value.stationLocalId);
            out.writeUTF(value.firstStationName);
            out.writeUTF(value.lastStationName);
            out.writeUTF(value.destination);
            out.writeInt(value.routeType == null ? Integer.MIN_VALUE : value.routeType.getValue());
            out.writeInt(value.provider == null ? Integer.MIN_VALUE : value.provider.getCityCode());
            out.writeInt(value.sequence);
        }

        @Override
        public StationRoute deserialize(DataInput in, int available) throws IOException {
            StationRoute stationRoute = new StationRoute();
            stationRoute.routeId = in.readUTF();
            stationRoute.routeName = in.readUTF();
            stationRoute.stationLocalId = in.readUTF();
            stationRoute.firstStationName = in.readUTF();
            stationRoute.lastStationName = in.readUTF();
            stationRoute.destination = in.readUTF();
            int typeValue = in.readInt();
            stationRoute.routeType = typeValue == Integer.MIN_VALUE ? null : RouteType.valueOf(typeValue);
            int cityCode = in.readInt();
            stationRoute.provider = cityCode == Integer.MIN_VALUE ? null : Provider.valueOf(cityCode);
            stationRoute.sequence = in.readInt();

            return stationRoute;
        }
    };
}
