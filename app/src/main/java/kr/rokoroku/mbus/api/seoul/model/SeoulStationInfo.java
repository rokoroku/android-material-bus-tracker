package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root(name = "itemList", strict = false)
public class SeoulStationInfo {
    @Element
    String stId;    // 정류소 ID
    @Element
    String stNm;    // 정류소명
    @Element
    String tmX;     // 정류소 좌표X
    @Element
    String tmY;     // 정류소 좌표Y
    @Element
    String arsId;   // 정류소고유번호

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getStNm() {
        return stNm;
    }

    public void setStNm(String stNm) {
        this.stNm = stNm;
    }

    public String getTmX() {
        return tmX;
    }

    public void setTmX(String tmX) {
        this.tmX = tmX;
    }

    public String getTmY() {
        return tmY;
    }

    public void setTmY(String tmY) {
        this.tmY = tmY;
    }

    public String getArsId() {
        return arsId;
    }

    public void setArsId(String arsId) {
        this.arsId = arsId;
    }

    @Override
    public String toString() {
        return "SeoulStationInfo{" +
                "stId='" + stId + '\'' +
                ", stNm='" + stNm + '\'' +
                ", tmX='" + tmX + '\'' +
                ", tmY='" + tmY + '\'' +
                ", arsId='" + arsId + '\'' +
                '}';
    }
}
