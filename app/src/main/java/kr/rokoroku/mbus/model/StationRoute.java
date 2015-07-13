package kr.rokoroku.mbus.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import kr.rokoroku.mbus.api.gbisweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteByStation;
import kr.rokoroku.mbus.core.Database;

public class StationRoute implements Parcelable, Serializable, Comparable<StationRoute> {

    private String routeId;
    private String routeName;
    private String firstStationName;
    private String lastStationName;
    private String stationLocalId;
    private Provider provider;
    private RouteType routeType;
    private int sequence = -1;

    private transient Route route;
    private transient ArrivalInfo arrivalInfo;

    public StationRoute() {

    }

    public StationRoute(Route route, String stationLocalId) {
        this.route = route;
        this.routeId = route.getId();
        this.routeName = route.getName();
        this.provider = route.getProvider();
        this.stationLocalId = stationLocalId;
        this.firstStationName = route.getStartStationName();
        this.lastStationName = route.getEndStationName();
        this.routeType = route.getType();
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

    public StationRoute(SearchStationResult.ResultEntity.BusStationInfoEntity busStationInfoEntity, String stationLocalId) {
        this.routeId = busStationInfoEntity.getRouteId();
        this.routeName = busStationInfoEntity.getRouteName();
        this.stationLocalId = stationLocalId;
        this.provider = Provider.GYEONGGI;
    }

    public Route getRoute() {
        if (route == null && routeId != null)
            route = Database.getInstance().getRoute(provider, routeId);
        return route;
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

    public String getStationLocalId() {
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
        dest.writeString(this.firstStationName);
        dest.writeString(this.lastStationName);
        dest.writeInt(this.routeType == null ? -1 : this.routeType.ordinal());
        dest.writeInt(this.provider == null ? -1 : this.provider.ordinal());
        dest.writeInt(this.sequence);
        dest.writeParcelable(this.arrivalInfo, 0);
    }

    protected StationRoute(Parcel in) {
        this.routeId = in.readString();
        this.routeName = in.readString();
        this.firstStationName = in.readString();
        this.lastStationName = in.readString();
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
}
