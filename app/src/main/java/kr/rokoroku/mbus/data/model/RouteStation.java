package kr.rokoroku.mbus.data.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.mapdb.Serializer;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.util.SerializeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class RouteStation extends Station implements Parcelable, Serializable {

    static final long serialVersionUID = 1L;

    private int sequence;
    private String routeId;
    private District district;
    private Direction direction;

    public RouteStation() {

    }

    public RouteStation(Station station, String routeId, int sequence, District district) {
        super(station);
        this.routeId = routeId;
        this.sequence = sequence;
        this.district = district;
    }

    public RouteStation(GbisSearchRouteResult.ResultEntity.GgEntity.StationEntity entity, String routeId, int sequence) {
        super(entity);
        this.routeId = routeId;
        this.sequence = sequence;
        this.district = District.GYEONGGI;
    }

    public RouteStation(GbisSearchAllResult.ResultEntity.BusStationEntity.ListEntity entity, String routeId, int sequence) {
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

    public RouteStation(RouteStationResult.StationEntity stationEntity) {
        super(stationEntity);
        this.routeId = stationEntity.busRouteId;
        this.sequence = stationEntity.seq;
        this.district = District.SEOUL;
    }

    public boolean isBusStop() {
        return !TextUtils.isEmpty(getLocalId());
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

    public static final Serializer<RouteStation> SERIALIZER = new Serializer<RouteStation>() {
        @Override
        public void serialize(DataOutput out, RouteStation value) throws IOException {

            //Serialize Station Properties
            SerializeUtil.writeString(out, value.getId());
            SerializeUtil.writeString(out, value.getName());
            SerializeUtil.writeString(out, value.getCity());
            SerializeUtil.writeString(out, value.getLocalId());
            SerializeUtil.writeDouble(out, value.getLatitude());
            SerializeUtil.writeDouble(out, value.getLongitude());
            Provider.SERIALIZER.serialize(out, value.getProvider());

            //Serialize RouteStation Properties
            SerializeUtil.writeInt(out, value.sequence);
            SerializeUtil.writeString(out, value.routeId);
            District.SERIALIZER.serialize(out, value.district);
            Direction.SERIALIZER.serialize(out, value.direction);
        }

        @Override
        public RouteStation deserialize(DataInput in, int available) throws IOException {

            //Deserialize Station Properties
            String id = SerializeUtil.readString(in);
            String name = SerializeUtil.readString(in);
            String city = SerializeUtil.readString(in);
            String localId = SerializeUtil.readString(in);
            Double latitude = SerializeUtil.readDouble(in);
            Double longitude = SerializeUtil.readDouble(in);
            Provider provider = Provider.SERIALIZER.deserialize(in, available);

            RouteStation routeStation = new RouteStation();
            routeStation.setId(id);
            routeStation.setName(name);
            routeStation.setCity(city);
            routeStation.setLocalId(localId);
            routeStation.setLatitude(latitude);
            routeStation.setLongitude(longitude);
            routeStation.setProvider(provider);

            //Deserialize RouteStation Properties
            routeStation.sequence = SerializeUtil.readInt(in);
            routeStation.routeId = SerializeUtil.readString(in);
            routeStation.district = District.SERIALIZER.deserialize(in, available);
            routeStation.direction = Direction.SERIALIZER.deserialize(in, available);

            return routeStation;
        }
    };
}
