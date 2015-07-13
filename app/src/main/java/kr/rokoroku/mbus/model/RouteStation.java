package kr.rokoroku.mbus.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import kr.rokoroku.mbus.api.gbisweb.model.SearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.SearchRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;

import java.io.Serializable;

public class RouteStation extends Station implements Parcelable, Serializable {

    private int sequence;
    private String routeId;
    private District district;
    private Direction direction;

    public RouteStation() {

    }

    public RouteStation(SearchRouteResult.ResultEntity.GgEntity.StationEntity entity, String routeId, int sequence) {
        super(entity);
        this.routeId = routeId;
        this.sequence = sequence;
        this.district = District.GYEONGGI;
    }

    public RouteStation(SearchAllResult.ResultEntity.BusStationEntity.ListEntity entity, String routeId, int sequence) {
        super(entity);
        this.routeId = routeId;
        this.sequence = sequence;
        this.district = District.GYEONGGI;
    }

    public RouteStation(SeoulStationInfo seoulStationInfo, String routeId, int sequence) {
        super(seoulStationInfo);
        this.routeId = routeId;
        this.sequence = sequence;
        this.district = District.SEOUL;
    }

    public RouteStation(SeoulBusRouteStation seoulBusRouteStation) {
        super(seoulBusRouteStation);
        this.routeId = seoulBusRouteStation.getBusRouteId();
        this.sequence = seoulBusRouteStation.getSeq();
        this.district = District.SEOUL;
    }

    public boolean isBusStop() {
        return !TextUtils.isEmpty(getLocalId());
//        if(Provider.GYEONGGI.equals(getProvider())) {
//            return !TextUtils.isEmpty(getLocalId());
//        } else {
//            return true;
//        }
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public ArrivalInfo getArrivalInfo() {
        return super.getArrivalInfo(routeId);
    }

    @Override
    public String toString() {
        return super.toString() + ", RouteStation{" +
                "sequence=" + sequence +
                ", routeId='" + routeId + '\'' +
                ", district=" + district +
                ", direction=" + direction +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.sequence);
        dest.writeString(this.routeId);
        dest.writeInt(this.district == null ? -1 : this.district.ordinal());
        dest.writeInt(this.direction == null ? -1 : this.direction.ordinal());
    }

    private RouteStation(Parcel in) {
        super(in);
        this.sequence = in.readInt();
        this.routeId = in.readString();
        int tmpDistrict = in.readInt();
        this.district = tmpDistrict == -1 ? null : District.values()[tmpDistrict];
        int tmpDirection = in.readInt();
        this.direction = tmpDirection == -1 ? null : Direction.values()[tmpDirection];
    }

    public static final Creator<RouteStation> CREATOR = new Creator<RouteStation>() {
        public RouteStation createFromParcel(Parcel source) {
            return new RouteStation(source);
        }

        public RouteStation[] newArray(int size) {
            return new RouteStation[size];
        }
    };
}
