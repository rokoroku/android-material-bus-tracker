package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 23..
 */
@Root(name = "itemList", strict = false)
public class SeoulBusRouteStation {
    @Element
    String busRouteId;      // 노선 ID
    @Element
    String busRouteNm;      // 노선명
    @Element
    int seq;                // 순번
    @Element(required = false)
    String section;         // 구간 ID
    @Element
    String station;         // 정류소 ID
    @Element
    String stationNm;       // 정류소 이름
    @Element
    String gpsX;            // X좌표 (WGS 84)
    @Element
    String gpsY;            // Y좌표 (WGS 84)
    @Element(required = false)
    String direction;       // 진행방향
    @Element(required = false)
    String fullSectDist;    // 정류소간 거리
    @Element
    String stationNo;       // 정류소 고유번호
    @Element
    String routeType;       // 노선 유형 (1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
    @Element(required = false)
    String beginTm;         // 첫차 시간
    @Element(required = false)
    String lastTm;          // 막차 시간
    @Element(required = false)
    String trnstnid;        // 회차지 정류소ID

    @Override
    public String toString() {
        return "Station{" +
                "busRouteId='" + busRouteId + '\'' +
                ", busRouteNm='" + busRouteNm + '\'' +
                ", seq='" + seq + '\'' +
                ", section='" + section + '\'' +
                ", station='" + station + '\'' +
                ", stationNm='" + stationNm + '\'' +
                ", gpsX='" + gpsX + '\'' +
                ", gpsY='" + gpsY + '\'' +
                ", direction='" + direction + '\'' +
                ", fullSectDist='" + fullSectDist + '\'' +
                ", stationNo='" + stationNo + '\'' +
                ", routeType='" + routeType + '\'' +
                ", beginTm='" + beginTm + '\'' +
                ", lastTm='" + lastTm + '\'' +
                ", trnstnid='" + trnstnid + '\'' +
                '}';
    }

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

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStationId() {
        return station;
    }

    public void setStationId(String station) {
        this.station = station;
    }

    public String getStationNm() {
        return stationNm;
    }

    public void setStationNm(String stationNm) {
        this.stationNm = stationNm;
    }

    public String getGpsX() {
        return gpsX;
    }

    public void setGpsX(String gpsX) {
        this.gpsX = gpsX;
    }

    public String getGpsY() {
        return gpsY;
    }

    public void setGpsY(String gpsY) {
        this.gpsY = gpsY;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getFullSectDist() {
        return fullSectDist;
    }

    public void setFullSectDist(String fullSectDist) {
        this.fullSectDist = fullSectDist;
    }

    public String getStationNo() {
        return stationNo;
    }

    public void setStationNo(String stationNo) {
        this.stationNo = stationNo;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getBeginTm() {
        return beginTm;
    }

    public void setBeginTm(String beginTm) {
        this.beginTm = beginTm;
    }

    public String getLastTm() {
        return lastTm;
    }

    public void setLastTm(String lastTm) {
        this.lastTm = lastTm;
    }

    public String getTrnstnid() {
        return trnstnid;
    }

    public void setTrnstnid(String trnstnid) {
        this.trnstnid = trnstnid;
    }
}
