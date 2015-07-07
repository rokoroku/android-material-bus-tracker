package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root(name = "itemList", strict = false)
public class SeoulBusRouteInfo {
    @Element
    String busRouteId;  // 노선 ID
    @Element
    String busRouteNm;  // 노선명
    @Element
    String length;      // 노선 길이 (Km)
    @Element
    String routeType;   // 노선 유형 (1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
    @Element
    String stStationNm; // 기점
    @Element
    String edStationNm; // 종점
    @Element
    String term;        // 배차간격 (분)
    @Element
    String lastBusYn;   // 막차운행여부
    @Element
    String lastBusTm;   // 금일막차시간
    @Element
    String firstBusTm;  // 금일첫차시간
    @Element
    String lastLowTm;   // 금일저상첫차시간
    @Element
    String firstLowTm;  // 금일저상막차시간

    public String getBusRouteId() {
        return busRouteId;
    }

    public void setBusRouteId(String busRouteId) {
        this.busRouteId = busRouteId;
    }

    public String getBusRouteNm() {
        return busRouteNm;
    }

    public void setBusRouteNm(String busRouteNm) {
        this.busRouteNm = busRouteNm;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getStStationNm() {
        return stStationNm;
    }

    public void setStStationNm(String stStationNm) {
        this.stStationNm = stStationNm;
    }

    public String getEdStationNm() {
        return edStationNm;
    }

    public void setEdStationNm(String edStationNm) {
        this.edStationNm = edStationNm;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getLastBusYn() {
        return lastBusYn;
    }

    public void setLastBusYn(String lastBusYn) {
        this.lastBusYn = lastBusYn;
    }

    public String getLastBusTm() {
        return lastBusTm;
    }

    public void setLastBusTm(String lastBusTm) {
        this.lastBusTm = lastBusTm;
    }

    public String getFirstBusTm() {
        return firstBusTm;
    }

    public void setFirstBusTm(String firstBusTm) {
        this.firstBusTm = firstBusTm;
    }

    public String getLastLowTm() {
        return lastLowTm;
    }

    public void setLastLowTm(String lastLowTm) {
        this.lastLowTm = lastLowTm;
    }

    public String getFirstLowTm() {
        return firstLowTm;
    }

    public void setFirstLowTm(String firstLowTm) {
        this.firstLowTm = firstLowTm;
    }

    @Override
    public String toString() {
        return "SeoulBusRouteInfo{" +
                "busRouteId='" + busRouteId + '\'' +
                ", busRouteNm='" + busRouteNm + '\'' +
                ", length='" + length + '\'' +
                ", routeType='" + routeType + '\'' +
                ", stStationNm='" + stStationNm + '\'' +
                ", edStationNm='" + edStationNm + '\'' +
                ", term='" + term + '\'' +
                ", lastBusYn='" + lastBusYn + '\'' +
                ", lastBusTm='" + lastBusTm + '\'' +
                ", firstBusTm='" + firstBusTm + '\'' +
                ", lastLowTm='" + lastLowTm + '\'' +
                ", firstLowTm='" + firstLowTm + '\'' +
                '}';
    }
}
