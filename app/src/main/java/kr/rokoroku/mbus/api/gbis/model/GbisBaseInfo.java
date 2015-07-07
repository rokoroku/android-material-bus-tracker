package kr.rokoroku.mbus.api.gbis.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 5. 31..
 */
@Root
public class GbisBaseInfo {
    /**
     * areaVersion : 20150531
     * routeLineDownloadUrl : http://smart.gbis.go.kr/ws/download?routeline20150531.txt
     * routeLineVersion : 20150531
     * routeStationVersion : 20150531
     * routeVersion : 20150531
     * stationDownloadUrl : http://smart.gbis.go.kr/ws/download?station20150531.txt
     * areaDownloadUrl : http://smart.gbis.go.kr/ws/download?area20150531.txt
     * routeDownloadUrl : http://smart.gbis.go.kr/ws/download?route20150531.txt
     * routeStationDownloadUrl : http://smart.gbis.go.kr/ws/download?routestation20150531.txt
     * stationVersion : 20150531
     */
    @Element
    private String areaVersion;
    @Element
    private String routeLineDownloadUrl;
    @Element
    private String routeLineVersion;
    @Element
    private String routeStationVersion;
    @Element
    private String routeVersion;
    @Element
    private String stationDownloadUrl;
    @Element
    private String areaDownloadUrl;
    @Element
    private String routeDownloadUrl;
    @Element
    private String routeStationDownloadUrl;
    @Element
    private String stationVersion;

    public void setAreaVersion(String areaVersion) {
        this.areaVersion = areaVersion;
    }

    public void setRouteLineDownloadUrl(String routeLineDownloadUrl) {
        this.routeLineDownloadUrl = routeLineDownloadUrl;
    }

    public void setRouteLineVersion(String routeLineVersion) {
        this.routeLineVersion = routeLineVersion;
    }

    public void setRouteStationVersion(String routeStationVersion) {
        this.routeStationVersion = routeStationVersion;
    }

    public void setRouteVersion(String routeVersion) {
        this.routeVersion = routeVersion;
    }

    public void setStationDownloadUrl(String stationDownloadUrl) {
        this.stationDownloadUrl = stationDownloadUrl;
    }

    public void setAreaDownloadUrl(String areaDownloadUrl) {
        this.areaDownloadUrl = areaDownloadUrl;
    }

    public void setRouteDownloadUrl(String routeDownloadUrl) {
        this.routeDownloadUrl = routeDownloadUrl;
    }

    public void setRouteStationDownloadUrl(String routeStationDownloadUrl) {
        this.routeStationDownloadUrl = routeStationDownloadUrl;
    }

    public void setStationVersion(String stationVersion) {
        this.stationVersion = stationVersion;
    }

    public String getAreaVersion() {
        return areaVersion;
    }

    public String getRouteLineDownloadUrl() {
        return routeLineDownloadUrl;
    }

    public String getRouteLineVersion() {
        return routeLineVersion;
    }

    public String getRouteStationVersion() {
        return routeStationVersion;
    }

    public String getRouteVersion() {
        return routeVersion;
    }

    public String getStationDownloadUrl() {
        return stationDownloadUrl;
    }

    public String getAreaDownloadUrl() {
        return areaDownloadUrl;
    }

    public String getRouteDownloadUrl() {
        return routeDownloadUrl;
    }

    public String getRouteStationDownloadUrl() {
        return routeStationDownloadUrl;
    }

    public String getStationVersion() {
        return stationVersion;
    }

}
