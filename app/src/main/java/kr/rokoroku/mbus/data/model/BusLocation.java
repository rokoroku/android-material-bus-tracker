package kr.rokoroku.mbus.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import kr.rokoroku.mbus.api.gbis.model.GbisBusLocation;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusLocation;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisRealtimeResult;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.util.GeoUtils;

public class BusLocation implements Comparable<BusLocation>, Parcelable {

    private String id;
    private String routeId;
    private String routeName;
    private String plateNumber;
    private RouteType type;
    private Direction direction;
    private int stationSeq = -1;
    private int remainSeat = -1;
    private String currentStationId;
    private String targetStationId;
    private boolean isLowPlate = false;
    private boolean isLastBus = false;
    private LatLng latLng;

    public BusLocation(SeoulBusLocation entity) {
        this.id = entity.getVehId();
        this.plateNumber = entity.getPlainNo();
        this.currentStationId = entity.getSectionId();
        this.isLowPlate = "1".equals(entity.getBusType());  //차량유형 (0:일반버스, 1:저상버스, 2:굴절버스)
        this.stationSeq = entity.getSectOrd();
        this.isLastBus = "Y".equalsIgnoreCase(entity.getLstbusyn());
        this.latLng = new LatLng(entity.getGpsY(), entity.getGpsX());
    }

    public BusLocation(GbisSearchRouteResult.ResultEntity.RealTimeEntity.BusEntity busEntity) {
        this.id = busEntity.getVehId();
        this.routeName = busEntity.getRouteNm();
        this.plateNumber = busEntity.getBusNo().get(0);
        this.currentStationId = busEntity.getFromStationId();
        this.targetStationId = busEntity.getToStationId();
        this.direction = busEntity.getBusDirList().isEmpty() ? null :
                busEntity.getBusDirList().get(0).equals("D") ? Direction.DOWN : Direction.UP;
        this.isLowPlate = !busEntity.getLowBusYn().isEmpty() &&
                busEntity.getLowBusYn().get(0).equals("Y");
        Double mercatorX = busEntity.getBusXList().get(0);
        Double mercatorY = busEntity.getBusYList().get(0);
        this.latLng = GeoUtils.convertEPSG3857(mercatorY, mercatorX);
    }

    public BusLocation(GbisBusLocation gbisBusLocation) {
        this.routeId = gbisBusLocation.getRouteId();
        this.plateNumber = gbisBusLocation.getPlateNo();
        this.stationSeq = gbisBusLocation.getStationSeq();
        this.remainSeat = gbisBusLocation.getRemainSeatCnt();
        this.isLowPlate = gbisBusLocation.getLowPlate() == 1;
        this.isLastBus = gbisBusLocation.getEndBus() == 1;
    }

    public BusLocation(TopisRealtimeResult.ResultEntity resultEntity) {
        this.id = resultEntity.vehId;
        this.plateNumber = resultEntity.plainNo;
        this.currentStationId = resultEntity.sectionId;
        this.stationSeq = resultEntity.sectOrd;
        this.isLowPlate = "1".equals(resultEntity.busType);
        this.isLastBus = "Y".equalsIgnoreCase(resultEntity.lstbusyn);
        this.latLng = new LatLng(resultEntity.gpsY, resultEntity.gpsX);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getStationSeq() {
        return stationSeq;
    }

    public void setStationSeq(int stationSeq) {
        this.stationSeq = stationSeq;
    }

    public int getRemainSeat() {
        return remainSeat;
    }

    public void setRemainSeat(int remainSeat) {
        this.remainSeat = remainSeat;
    }

    public String getCurrentStationId() {
        return currentStationId;
    }

    public void setCurrentStationId(String currentStationId) {
        this.currentStationId = currentStationId;
    }

    public String getTargetStationId() {
        return targetStationId;
    }

    public void setTargetStationId(String targetStationId) {
        this.targetStationId = targetStationId;
    }

    public boolean isLowPlate() {
        return isLowPlate;
    }

    public void setLowPlate(boolean isLowPlate) {
        this.isLowPlate = isLowPlate;
    }

    public boolean isLastBus() {
        return isLastBus;
    }

    public void setLastBus(boolean isLastBus) {
        this.isLastBus = isLastBus;
    }

    @Override
    public int compareTo(BusLocation another) {
        return stationSeq - another.stationSeq;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.routeId);
        dest.writeString(this.routeName);
        dest.writeString(this.plateNumber);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeInt(this.direction == null ? -1 : this.direction.ordinal());
        dest.writeInt(this.stationSeq);
        dest.writeInt(this.remainSeat);
        dest.writeString(this.currentStationId);
        dest.writeString(this.targetStationId);
        dest.writeByte(isLowPlate ? (byte) 1 : (byte) 0);
        dest.writeByte(isLastBus ? (byte) 1 : (byte) 0);
    }

    private BusLocation(Parcel in) {
        this.id = in.readString();
        this.routeId = in.readString();
        this.routeName = in.readString();
        this.plateNumber = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : RouteType.values()[tmpType];
        int tmpDirection = in.readInt();
        this.direction = tmpDirection == -1 ? null : Direction.values()[tmpDirection];
        this.stationSeq = in.readInt();
        this.remainSeat = in.readInt();
        this.currentStationId = in.readString();
        this.targetStationId = in.readString();
        this.isLowPlate = in.readByte() != 0;
        this.isLastBus = in.readByte() != 0;
    }

    public static final Creator<BusLocation> CREATOR = new Creator<BusLocation>() {
        public BusLocation createFromParcel(Parcel source) {
            return new BusLocation(source);
        }

        public BusLocation[] newArray(int size) {
            return new BusLocation[size];
        }
    };
}
