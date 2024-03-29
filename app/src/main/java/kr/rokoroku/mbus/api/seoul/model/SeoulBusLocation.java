package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 23..
 */
@Root(name = "itemList", strict = false)
public class SeoulBusLocation {
    @Element(required = false)
    private String stopFlag;    // 정류소도착여부 (0:운행중, 1:도착)
    @Element(required = false)
    private int isFullFlag;     // ??
    @Element(required = false)
    private String rtDist;      // 노선옵셋거리 (Km)
    @Element(required = false)
    private String lastStnId;
    @Element(required = false)
    private String plainNo;     // 차량번호
    @Element(required = false)
    private String sectDist;    // 구간옵셋거리 (Km)
    @Element(required = false)
    private String fullSectDist;// 정류소간 거리
    @Element(required = false)
    private String nextStTm;    // 다음정류소도착소요시간
    @Element(required = false)
    private String sectionId;   // 구간ID
    @Element
    private int sectOrd;        // 구간순번
    @Element(required = false)
    private String dataTm;
    @Element(required = false)
    private String lastStTm;    // 종점도착소요시간
    @Element(required = false)
    private String trnstnid;    // 회차지 정류소ID
    @Element
    private String vehId;       // 버스 ID
    @Element
    private String busType;     // 차량유형 (0:일반버스, 1:저상버스, 2:굴절버스)
    @Element
    private Double gpsX;
    @Element
    private Double gpsY;
    @Element(required = false)
    private Double posX;
    @Element(required = false)
    private Double posY;
    @Element(required = false)
    private String islastyn;    // 해당차량 운행여부
    @Element(required = false)
    private String isrunyn;     // 막차여부

    public void setStopFlag(String stopFlag) {
        this.stopFlag = stopFlag;
    }

    public void setRtDist(String rtDist) {
        this.rtDist = rtDist;
    }

    public void setLastStnId(String lastStnId) {
        this.lastStnId = lastStnId;
    }

    public void setPlainNo(String plainNo) {
        this.plainNo = plainNo;
    }

    public void setSectDist(String sectDist) {
        this.sectDist = sectDist;
    }

    public void setFullSectDist(String fullSectDist) {
        this.fullSectDist = fullSectDist;
    }

    public void setNextStTm(String nextStTm) {
        this.nextStTm = nextStTm;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public void setIslastyn(String islastyn) {
        this.islastyn = islastyn;
    }

    public void setSectOrd(int sectOrd) {
        this.sectOrd = sectOrd;
    }

    public void setDataTm(String dataTm) {
        this.dataTm = dataTm;
    }

    public void setLastStTm(String lastStTm) {
        this.lastStTm = lastStTm;
    }

    public void setTrnstnid(String trnstnid) {
        this.trnstnid = trnstnid;
    }

    public void setVehId(String vehId) {
        this.vehId = vehId;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public void setGpsX(Double gpsX) {
        this.gpsX = gpsX;
    }

    public void setIsrunyn(String isrunyn) {
        this.isrunyn = isrunyn;
    }

    public void setGpsY(Double gpsY) {
        this.gpsY = gpsY;
    }

    public String getStopFlag() {
        return stopFlag;
    }

    public String getRtDist() {
        return rtDist;
    }

    public String getLastStnId() {
        return lastStnId;
    }

    public String getPlainNo() {
        return plainNo;
    }

    public String getSectDist() {
        return sectDist;
    }

    public String getFullSectDist() {
        return fullSectDist;
    }

    public String getNextStTm() {
        return nextStTm;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getIslastyn() {
        return islastyn;
    }

    public int getSectOrd() {
        return sectOrd;
    }

    public String getDataTm() {
        return dataTm;
    }

    public String getLastStTm() {
        return lastStTm;
    }

    public String getTrnstnid() {
        return trnstnid;
    }

    public String getVehId() {
        return vehId;
    }

    public String getBusType() {
        return busType;
    }

    public Double getGpsX() {
        return gpsX;
    }

    public String getIsrunyn() {
        return isrunyn;
    }

    public Double getGpsY() {
        return gpsY;
    }
}
