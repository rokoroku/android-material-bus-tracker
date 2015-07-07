package kr.rokoroku.mbus.api.gbis.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 5. 30..
 */
@Root(name = "busLocationList")
public class GbisBusLocation {
    /**
     * endBus : 0
     * routeId : 200000085
     * plateNo : 경기70바1429
     * remainSeatCnt : -1
     * stationSeq : 1
     * lowPlate : 0
     * stationId : 200000165
     */

    @Element
    private String routeId;
    @Element
    private String stationId;
    @Element
    private String plateNo;
    @Element
    private int remainSeatCnt;
    @Element
    private int stationSeq;
    @Element
    private int lowPlate;
    @Element
    private int endBus;

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

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public int getRemainSeatCnt() {
        return remainSeatCnt;
    }

    public void setRemainSeatCnt(int remainSeatCnt) {
        this.remainSeatCnt = remainSeatCnt;
    }

    public int getStationSeq() {
        return stationSeq;
    }

    public void setStationSeq(int stationSeq) {
        this.stationSeq = stationSeq;
    }

    public int getLowPlate() {
        return lowPlate;
    }

    public void setLowPlate(int lowPlate) {
        this.lowPlate = lowPlate;
    }

    public int getEndBus() {
        return endBus;
    }

    public void setEndBus(int endBus) {
        this.endBus = endBus;
    }
}
