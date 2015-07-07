package kr.rokoroku.mbus.api.gbis.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 5. 30..
 */
@Root
//@Root(name = "busArrivalList")
//@Root(name = "busArrivalItem")
public class GbisBusArrival {

    /**
     * predictTime2 : 60
     * flag : PASS
     * lowPlate1 : 0
     * plateNo1 : 경기70바1735
     * plateNo2 : 경기70바1249
     * predictTime1 : 18
     * locationNo1 : 17
     * drvEnd : N
     * routeId : 200000042
     * locationNo2 : 50
     * lowPlate2 : 0
     * delayYn1 : N
     * remainSeatCnt2 : -1
     * remainSeatCnt1 : -1
     * delayYn2 : N
     * stationId : 233001450
     */
    @Element
    private String routeId;
    @Element
    private String stationId;
    @Element (required = false)
    private String flag;
    @Element
    private String drvEnd;
    @Element
    private String lowPlate1;
    @Element
    private String plateNo1;
    @Element
    private String predictTime1;
    @Element
    private String locationNo1;
    @Element
    private String delayYn1;
    @Element
    private String remainSeatCnt1;
    @Element (required = false)
    private String plateNo2;
    @Element (required = false)
    private String predictTime2;
    @Element (required = false)
    private String locationNo2;
    @Element (required = false)
    private String lowPlate2;
    @Element (required = false)
    private String delayYn2;
    @Element (required = false)
    private String remainSeatCnt2;

    public void setPredictTime2(String predictTime2) {
        this.predictTime2 = predictTime2;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setLowPlate1(String lowPlate1) {
        this.lowPlate1 = lowPlate1;
    }

    public void setPlateNo1(String plateNo1) {
        this.plateNo1 = plateNo1;
    }

    public void setPlateNo2(String plateNo2) {
        this.plateNo2 = plateNo2;
    }

    public void setPredictTime1(String predictTime1) {
        this.predictTime1 = predictTime1;
    }

    public void setLocationNo1(String locationNo1) {
        this.locationNo1 = locationNo1;
    }

    public void setDrvEnd(String drvEnd) {
        this.drvEnd = drvEnd;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setLocationNo2(String locationNo2) {
        this.locationNo2 = locationNo2;
    }

    public void setLowPlate2(String lowPlate2) {
        this.lowPlate2 = lowPlate2;
    }

    public void setDelayYn1(String delayYn1) {
        this.delayYn1 = delayYn1;
    }

    public void setRemainSeatCnt2(String remainSeatCnt2) {
        this.remainSeatCnt2 = remainSeatCnt2;
    }

    public void setRemainSeatCnt1(String remainSeatCnt1) {
        this.remainSeatCnt1 = remainSeatCnt1;
    }

    public void setDelayYn2(String delayYn2) {
        this.delayYn2 = delayYn2;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getPredictTime2() {
        return predictTime2;
    }

    public String getFlag() {
        return flag;
    }

    public String getLowPlate1() {
        return lowPlate1;
    }

    public String getPlateNo1() {
        return plateNo1;
    }

    public String getPlateNo2() {
        return plateNo2;
    }

    public String getPredictTime1() {
        return predictTime1;
    }

    public String getLocationNo1() {
        return locationNo1;
    }

    public String getDrvEnd() {
        return drvEnd;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getLocationNo2() {
        return locationNo2;
    }

    public String getLowPlate2() {
        return lowPlate2;
    }

    public String getDelayYn1() {
        return delayYn1;
    }

    public String getRemainSeatCnt2() {
        return remainSeatCnt2;
    }

    public String getRemainSeatCnt1() {
        return remainSeatCnt1;
    }

    public String getDelayYn2() {
        return delayYn2;
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public String toString() {
        return "GbisBusArrival{" +
                "predictTime2='" + predictTime2 + '\'' +
                ", flag='" + flag + '\'' +
                ", lowPlate1='" + lowPlate1 + '\'' +
                ", plateNo1='" + plateNo1 + '\'' +
                ", plateNo2='" + plateNo2 + '\'' +
                ", predictTime1='" + predictTime1 + '\'' +
                ", locationNo1='" + locationNo1 + '\'' +
                ", drvEnd='" + drvEnd + '\'' +
                ", routeId='" + routeId + '\'' +
                ", locationNo2='" + locationNo2 + '\'' +
                ", lowPlate2='" + lowPlate2 + '\'' +
                ", delayYn1='" + delayYn1 + '\'' +
                ", remainSeatCnt2='" + remainSeatCnt2 + '\'' +
                ", remainSeatCnt1='" + remainSeatCnt1 + '\'' +
                ", delayYn2='" + delayYn2 + '\'' +
                ", stationId='" + stationId + '\'' +
                '}';
    }

}
