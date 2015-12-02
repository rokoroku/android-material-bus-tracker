package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root(name = "itemList", strict = false)
public class SeoulBusRouteByStation {

    /**
     * stEnd : 노들역
     * lastBusTmLow : 20150616215000
     * busRouteNm : 350
     * firstBusTmLow : 20150616043000
     * busRouteType : 3
     * length : 62.3
     * stBegin : 송파공영차고지
     * nextBus : 0
     * term : 7
     * firstBusTm : 20150616040000
     * lastBusTm : 20150616223000
     * busRouteId : 3035001
     */

    @Element
    private String busRouteId;
    @Element
    private String busRouteNm;
    @Element
    private String busRouteType;// 노선유형 (1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
    @Element(required = false)
    private String stBegin;     // 기점
    @Element(required = false)
    private String stEnd;       // 종점
    @Element(required = false)
    private String length;      // 노선길이
    @Element(required = false)
    private String term;        // 배차간격
    @Element(required = false)
    private String nextBus;     // 막차여부 (0,1)
    @Element(required = false)
    private String firstBusTmLow;
    @Element(required = false)
    private String lastBusTmLow;
    @Element(required = false)
    private String firstBusTm;
    @Element(required = false)
    private String lastBusTm;

    public void setStEnd(String stEnd) {
        this.stEnd = stEnd;
    }

    public void setLastBusTmLow(String lastBusTmLow) {
        this.lastBusTmLow = lastBusTmLow;
    }

    public void setBusRouteNm(String busRouteNm) {
        this.busRouteNm = busRouteNm;
    }

    public void setFirstBusTmLow(String firstBusTmLow) {
        this.firstBusTmLow = firstBusTmLow;
    }

    public void setBusRouteType(String busRouteType) {
        this.busRouteType = busRouteType;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setStBegin(String stBegin) {
        this.stBegin = stBegin;
    }

    public void setNextBus(String nextBus) {
        this.nextBus = nextBus;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setFirstBusTm(String firstBusTm) {
        this.firstBusTm = firstBusTm;
    }

    public void setLastBusTm(String lastBusTm) {
        this.lastBusTm = lastBusTm;
    }

    public void setBusRouteId(String busRouteId) {
        this.busRouteId = busRouteId;
    }

    public String getStEnd() {
        return stEnd;
    }

    public String getLastBusTmLow() {
        return lastBusTmLow;
    }

    public String getBusRouteNm() {
        return busRouteNm;
    }

    public String getFirstBusTmLow() {
        return firstBusTmLow;
    }

    public String getBusRouteType() {
        return busRouteType;
    }

    public String getLength() {
        return length;
    }

    public String getStBegin() {
        return stBegin;
    }

    public String getNextBus() {
        return nextBus;
    }

    public String getTerm() {
        return term;
    }

    public String getFirstBusTm() {
        return firstBusTm;
    }

    public String getLastBusTm() {
        return lastBusTm;
    }

    public String getBusRouteId() {
        return busRouteId;
    }
}
