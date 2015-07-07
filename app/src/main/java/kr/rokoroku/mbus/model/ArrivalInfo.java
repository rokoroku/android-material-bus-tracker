package kr.rokoroku.mbus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import kr.rokoroku.mbus.api.gbis.model.GbisBusArrival;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusStationResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusArrival;

import java.util.Date;

/**
 * Created by rok on 2015. 5. 30..
 */
public class ArrivalInfo implements Parcelable {

    private String routeId;
    private String stationId;
    private boolean isDriveEnd;
    private BusArrivalItem busArrivalItem1;
    private BusArrivalItem busArrivalItem2;
    private long timestamp;

    /**
     * Constructor for Gbis Model
     *
     * @param gbisBusArrival
     */
    public ArrivalInfo(GbisBusArrival gbisBusArrival) {
        this.routeId = gbisBusArrival.getRouteId();
        this.stationId = gbisBusArrival.getStationId();
        this.isDriveEnd = "Y".equals(gbisBusArrival.getDrvEnd());
        this.timestamp = System.currentTimeMillis();

        if(!TextUtils.isEmpty(gbisBusArrival.getPlateNo1())) {
            this.busArrivalItem1 = new BusArrivalItem();
            this.busArrivalItem1.plateNumber = gbisBusArrival.getPlateNo1();
            this.busArrivalItem1.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(gbisBusArrival.getPredictTime1()) * 60000);
            this.busArrivalItem1.behind = Integer.parseInt(gbisBusArrival.getLocationNo1());
            this.busArrivalItem1.remainSeat = Integer.parseInt(gbisBusArrival.getRemainSeatCnt1());
            this.busArrivalItem1.isLowPlate = "Y".equals(gbisBusArrival.getLowPlate1());
            this.busArrivalItem1.isDelayed = "Y".equals(gbisBusArrival.getDelayYn1());
        }

        if(!TextUtils.isEmpty(gbisBusArrival.getPlateNo2())) {
            this.busArrivalItem2 = new BusArrivalItem();
            this.busArrivalItem2.plateNumber = gbisBusArrival.getPlateNo2();
            this.busArrivalItem2.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(gbisBusArrival.getPredictTime2()) * 60000);
            this.busArrivalItem2.behind = Integer.parseInt(gbisBusArrival.getLocationNo2());
            this.busArrivalItem2.remainSeat = Integer.parseInt(gbisBusArrival.getRemainSeatCnt2());
            this.busArrivalItem2.isLowPlate = "Y".equals(gbisBusArrival.getLowPlate2());
            this.busArrivalItem2.isDelayed = "Y".equals(gbisBusArrival.getDelayYn2());
        }
    }

    public ArrivalInfo(GbisWebSearchBusStationResult.ResultEntity.BusArrivalInfoEntity entity) {
        this.routeId = entity.getRouteId();
        this.stationId = entity.getStationId();
        this.isDriveEnd = "Y".equals(entity.getDrvEnd());
        this.timestamp = System.currentTimeMillis();

        if(!TextUtils.isEmpty(entity.getPlateNo1())) {
            this.busArrivalItem1 = new BusArrivalItem();
            this.busArrivalItem1.vehicleId = entity.getVehId1();
            this.busArrivalItem1.plateNumber = entity.getPlateNo1();
            this.busArrivalItem1.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(entity.getPredictTime1()) * 60000);
            this.busArrivalItem1.behind = Integer.parseInt(entity.getLocationNo1());
            this.busArrivalItem1.remainSeat = Integer.parseInt(entity.getRemainSeatCnt1());
            this.busArrivalItem1.isLowPlate = "Y".equals(entity.getLowPlate1());
            this.busArrivalItem1.isDelayed = "Y".equals(entity.getDelayYn1());
        }

        if(!TextUtils.isEmpty(entity.getPlateNo2())) {
            this.busArrivalItem2 = new BusArrivalItem();
            this.busArrivalItem2.vehicleId = entity.getVehId2();
            this.busArrivalItem2.plateNumber = entity.getPlateNo2();
            this.busArrivalItem2.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(entity.getPredictTime2()) * 60000);
            this.busArrivalItem2.behind = Integer.parseInt(entity.getLocationNo2());
            this.busArrivalItem2.remainSeat = Integer.parseInt(entity.getRemainSeatCnt2());
            this.busArrivalItem2.isLowPlate = "Y".equals(entity.getLowPlate2());
            this.busArrivalItem2.isDelayed = "Y".equals(entity.getDelayYn2());
        }
    }

    public ArrivalInfo(SeoulBusArrival entity) {
        this.routeId = entity.getBusRouteId();
        this.stationId = entity.getStId();
        this.isDriveEnd = "Y".equals(entity.getNextBus());
        this.timestamp = System.currentTimeMillis();

        int stationOrd = Integer.parseInt(entity.getStaOrd());
        if(!entity.getVehId1().equals(entity.getVehId2())) {
            if (!"0".equals(entity.getVehId1()) && !entity.getIsLast1().startsWith("-")) {
                this.busArrivalItem1 = new BusArrivalItem();
                this.busArrivalItem1.vehicleId = entity.getVehId1();
                this.busArrivalItem1.plateNumber = entity.getPlainNo1();
                this.busArrivalItem1.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(entity.getKals1()) * 1000);
                this.busArrivalItem1.behind = stationOrd - Integer.parseInt(entity.getSectOrd1());
                this.busArrivalItem1.remainSeat = "1".equals(entity.getFull1()) ? 0 : -1;
                this.busArrivalItem1.isLowPlate = !"0".equals(entity.getBusType1());
                this.busArrivalItem1.isLastBus = !"0".equals(entity.getIsLast1());
                this.isDriveEnd |= this.busArrivalItem1.isLastBus;
            }

            if (!"0".equals(entity.getVehId2()) && !entity.getIsLast2().startsWith("-")) {
                this.busArrivalItem2 = new BusArrivalItem();
                this.busArrivalItem2.vehicleId = entity.getVehId2();
                this.busArrivalItem2.plateNumber = entity.getPlainNo2();
                this.busArrivalItem2.predictTime = new Date(System.currentTimeMillis() + Integer.parseInt(entity.getKals2()) * 1000);
                this.busArrivalItem2.behind = stationOrd - Integer.parseInt(entity.getSectOrd2());
                this.busArrivalItem2.remainSeat = "1".equals(entity.getFull2()) ? 0 : -1;
                this.busArrivalItem2.isLowPlate = !"0".equals(entity.getBusType2());
                this.busArrivalItem2.isLastBus = !"0".equals(entity.getIsLast2());
                this.isDriveEnd |= this.busArrivalItem2.isLastBus;
            }
        }
    }


    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public boolean isDriveEnd() {
        return isDriveEnd;
    }

    public void setIsDriveEnd(boolean isDriveEnd) {
        this.isDriveEnd = isDriveEnd;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public BusArrivalItem getBusArrivalItem1() {
        return busArrivalItem1;
    }

    public void setBusArrivalItem1(BusArrivalItem busArrivalItem1) {
        this.busArrivalItem1 = busArrivalItem1;
    }

    public BusArrivalItem getBusArrivalItem2() {
        return busArrivalItem2;
    }

    public void setBusArrivalItem2(BusArrivalItem busArrivalItem2) {
        this.busArrivalItem2 = busArrivalItem2;
    }

    public static class BusArrivalItem implements Parcelable {

        private String vehicleId;
        private String plateNumber;
        private Date predictTime;
        private int behind = -1;
        private int remainSeat = -1;
        private boolean isLowPlate = false;     //저상버스
        private boolean isDelayed = false;      //회차점 대기중 여부
        private boolean isLastBus = false;      //막차여부

        public String getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public Date getPredictTime() {
            return predictTime;
        }

        public void setPredictTime(Date predictTime) {
            this.predictTime = predictTime;
        }

        public int getBehind() {
            return behind;
        }

        public void setBehind(int behind) {
            this.behind = behind;
        }

        public int getRemainSeat() {
            return remainSeat;
        }

        public void setRemainSeat(int remainSeat) {
            this.remainSeat = remainSeat;
        }

        public boolean isLowPlate() {
            return isLowPlate;
        }

        public void setIsLowPlate(boolean isLowPlate) {
            this.isLowPlate = isLowPlate;
        }

        public boolean isDelayed() {
            return isDelayed;
        }

        public void setIsDelayed(boolean isDelayed) {
            this.isDelayed = isDelayed;
        }

        public boolean isLastBus() {
            return isLastBus;
        }

        public void setIsLastBus(boolean isLastBus) {
            this.isLastBus = isLastBus;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.vehicleId);
            dest.writeString(this.plateNumber);
            dest.writeLong(predictTime != null ? predictTime.getTime() : -1);
            dest.writeInt(this.behind);
            dest.writeInt(this.remainSeat);
            dest.writeByte(isLowPlate ? (byte) 1 : (byte) 0);
            dest.writeByte(isDelayed ? (byte) 1 : (byte) 0);
            dest.writeByte(isLastBus ? (byte) 1 : (byte) 0);
        }

        public BusArrivalItem() {
        }

        private BusArrivalItem(Parcel in) {
            this.vehicleId = in.readString();
            this.plateNumber = in.readString();
            long tmpPredictTime = in.readLong();
            this.predictTime = tmpPredictTime == -1 ? null : new Date(tmpPredictTime);
            this.behind = in.readInt();
            this.remainSeat = in.readInt();
            this.isLowPlate = in.readByte() != 0;
            this.isDelayed = in.readByte() != 0;
            this.isLastBus = in.readByte() != 0;
        }

        public static final Parcelable.Creator<BusArrivalItem> CREATOR = new Parcelable.Creator<BusArrivalItem>() {
            public BusArrivalItem createFromParcel(Parcel source) {
                return new BusArrivalItem(source);
            }

            public BusArrivalItem[] newArray(int size) {
                return new BusArrivalItem[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.routeId);
        dest.writeString(this.stationId);
        dest.writeByte(isDriveEnd ? (byte) 1 : (byte) 0);
        dest.writeLong(timestamp);
        dest.writeParcelable(this.busArrivalItem1, 0);
        dest.writeParcelable(this.busArrivalItem2, 0);
    }

    private ArrivalInfo(Parcel in) {
        this.routeId = in.readString();
        this.stationId = in.readString();
        this.isDriveEnd = in.readByte() != 0;
        this.timestamp = in.readLong();
        this.busArrivalItem1 = in.readParcelable(BusArrivalItem.class.getClassLoader());
        this.busArrivalItem2 = in.readParcelable(BusArrivalItem.class.getClassLoader());
    }

    public static final Parcelable.Creator<ArrivalInfo> CREATOR = new Parcelable.Creator<ArrivalInfo>() {
        public ArrivalInfo createFromParcel(Parcel source) {
            return new ArrivalInfo(source);
        }

        public ArrivalInfo[] newArray(int size) {
            return new ArrivalInfo[size];
        }
    };
}
