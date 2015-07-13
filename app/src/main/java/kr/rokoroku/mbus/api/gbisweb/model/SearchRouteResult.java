package kr.rokoroku.mbus.api.gbisweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SearchRouteResult {

    private ResultEntity result;
    private boolean success;

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResultEntity getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public class ResultEntity {

        private RealTimeEntity realTime;
        private GgEntity gg;

        public void setRealTime(RealTimeEntity realTime) {
            this.realTime = realTime;
        }

        public void setGg(GgEntity gg) {
            this.gg = gg;
        }

        public RealTimeEntity getRealTime() {
            return realTime;
        }

        public GgEntity getGg() {
            return gg;
        }

        public class RealTimeEntity {

            private int count;
            private List<BusEntity> list;

            public void setCount(int count) {
                this.count = count;
            }

            public void setList(List<BusEntity> list) {
                this.list = list;
            }

            public int getCount() {
                return count;
            }

            public List<BusEntity> getList() {
                return list;
            }

            public class BusEntity {
                /**
                 * busDirList : ["U"]
                 * busXList : [14146370]
                 * busYList : [4480374]
                 * fromStationId : 228002014
                 * vehId : 270003361
                 * busNo : ["경기77바1561"]
                 * lowBusYn : ["N"]
                 * busPosition : ["2"]
                 * toStationId : 228002012
                 * routeNm : 720
                 */
                private List<String> busDirList;
                private List<Double> busXList;
                private List<Double> busYList;
                private String fromStationId;
                private String vehId;
                private List<String> busNo;
                private List<String> lowBusYn;
                private List<String> busPosition;
                private String toStationId;
                private String routeNm;

                public void setBusDirList(List<String> busDirList) {
                    this.busDirList = busDirList;
                }

                public void setBusXList(List<Double> busXList) {
                    this.busXList = busXList;
                }

                public void setBusYList(List<Double> busYList) {
                    this.busYList = busYList;
                }

                public void setFromStationId(String fromStationId) {
                    this.fromStationId = fromStationId;
                }

                public void setVehId(String vehId) {
                    this.vehId = vehId;
                }

                public void setBusNo(List<String> busNo) {
                    this.busNo = busNo;
                }

                public void setLowBusYn(List<String> lowBusYn) {
                    this.lowBusYn = lowBusYn;
                }

                public void setBusPosition(List<String> busPosition) {
                    this.busPosition = busPosition;
                }

                public void setToStationId(String toStationId) {
                    this.toStationId = toStationId;
                }

                public void setRouteNm(String routeNm) {
                    this.routeNm = routeNm;
                }

                public List<String> getBusDirList() {
                    return busDirList;
                }

                public List<Double> getBusXList() {
                    return busXList;
                }

                public List<Double> getBusYList() {
                    return busYList;
                }

                public String getFromStationId() {
                    return fromStationId;
                }

                public String getVehId() {
                    return vehId;
                }

                public List<String> getBusNo() {
                    return busNo;
                }

                public List<String> getLowBusYn() {
                    return lowBusYn;
                }

                public List<String> getBusPosition() {
                    return busPosition;
                }

                public String getToStationId() {
                    return toStationId;
                }

                public String getRouteNm() {
                    return routeNm;
                }
            }
        }

        public class GgEntity {
            private String middlePnt;
            private RouteEntity route;
            private UpEntity up;
            private String routeTypeCd;
            private DownEntity down;

            public void setMiddlePnt(String middlePnt) {
                this.middlePnt = middlePnt;
            }

            public void setRoute(RouteEntity route) {
                this.route = route;
            }

            public void setUp(UpEntity up) {
                this.up = up;
            }

            public void setRouteTypeCd(String routeTypeCd) {
                this.routeTypeCd = routeTypeCd;
            }

            public void setDown(DownEntity down) {
                this.down = down;
            }

            public String getMiddlePnt() {
                return middlePnt;
            }

            public RouteEntity getRoute() {
                return route;
            }

            public UpEntity getUp() {
                return up;
            }

            public String getRouteTypeCd() {
                return routeTypeCd;
            }

            public DownEntity getDown() {
                return down;
            }

            public class RouteEntity {
                /**
                 * upLastTime : 23:50
                 * npeekAlloc2 : 20
                 * edStaNm : 남한산성입구.양지파출소
                 * garageNm : 광교영업소
                 * peekAlloc : 8
                 * npeekAlloc : 15
                 * upFirstTime : 04:40
                 * companyTel : 02-455-2114
                 * downLastTime2 : 23:20
                 * downFirstTime : 04:50
                 * upFirstTime2 : 04:40
                 * maxHead :
                 * upLastTime2 : 23:50
                 * downFirstTime2 : 04:50
                 * turnSeq : 67
                 * downLastTime : 23:20
                 * routeId : 234000047
                 * garageTel : 031-212-6657
                 * companyNm : 경기고속
                 * minHead :
                 * stStaNm : 광교차고지
                 * peekAlloc2 : 10
                 * routeNm : 720
                 */
                private String upLastTime;
                private String npeekAlloc2;
                private String edStaNm;
                private String garageNm;
                private String peekAlloc;
                private String npeekAlloc;
                private String upFirstTime;
                private String companyTel;
                private String downLastTime2;
                private String downFirstTime;
                private String upFirstTime2;
                private String maxHead;
                private String upLastTime2;
                private String downFirstTime2;
                private String turnSeq;
                private String downLastTime;
                private String routeId;
                private String garageTel;
                private String companyNm;
                private String minHead;
                private String stStaNm;
                private String peekAlloc2;
                private String routeNm;

                public void setUpLastTime(String upLastTime) {
                    this.upLastTime = upLastTime;
                }

                public void setNpeekAlloc2(String npeekAlloc2) {
                    this.npeekAlloc2 = npeekAlloc2;
                }

                public void setEdStaNm(String edStaNm) {
                    this.edStaNm = edStaNm;
                }

                public void setGarageNm(String garageNm) {
                    this.garageNm = garageNm;
                }

                public void setPeekAlloc(String peekAlloc) {
                    this.peekAlloc = peekAlloc;
                }

                public void setNpeekAlloc(String npeekAlloc) {
                    this.npeekAlloc = npeekAlloc;
                }

                public void setUpFirstTime(String upFirstTime) {
                    this.upFirstTime = upFirstTime;
                }

                public void setCompanyTel(String companyTel) {
                    this.companyTel = companyTel;
                }

                public void setDownLastTime2(String downLastTime2) {
                    this.downLastTime2 = downLastTime2;
                }

                public void setDownFirstTime(String downFirstTime) {
                    this.downFirstTime = downFirstTime;
                }

                public void setUpFirstTime2(String upFirstTime2) {
                    this.upFirstTime2 = upFirstTime2;
                }

                public void setMaxHead(String maxHead) {
                    this.maxHead = maxHead;
                }

                public void setUpLastTime2(String upLastTime2) {
                    this.upLastTime2 = upLastTime2;
                }

                public void setDownFirstTime2(String downFirstTime2) {
                    this.downFirstTime2 = downFirstTime2;
                }

                public void setTurnSeq(String turnSeq) {
                    this.turnSeq = turnSeq;
                }

                public void setDownLastTime(String downLastTime) {
                    this.downLastTime = downLastTime;
                }

                public void setRouteId(String routeId) {
                    this.routeId = routeId;
                }

                public void setGarageTel(String garageTel) {
                    this.garageTel = garageTel;
                }

                public void setCompanyNm(String companyNm) {
                    this.companyNm = companyNm;
                }

                public void setMinHead(String minHead) {
                    this.minHead = minHead;
                }

                public void setStStaNm(String stStaNm) {
                    this.stStaNm = stStaNm;
                }

                public void setPeekAlloc2(String peekAlloc2) {
                    this.peekAlloc2 = peekAlloc2;
                }

                public void setRouteNm(String routeNm) {
                    this.routeNm = routeNm;
                }

                public String getUpLastTime() {
                    return upLastTime;
                }

                public String getNpeekAlloc2() {
                    return npeekAlloc2;
                }

                public String getEdStaNm() {
                    return edStaNm;
                }

                public String getGarageNm() {
                    return garageNm;
                }

                public String getPeekAlloc() {
                    return peekAlloc;
                }

                public String getNpeekAlloc() {
                    return npeekAlloc;
                }

                public String getUpFirstTime() {
                    return upFirstTime;
                }

                public String getCompanyTel() {
                    return companyTel;
                }

                public String getDownLastTime2() {
                    return downLastTime2;
                }

                public String getDownFirstTime() {
                    return downFirstTime;
                }

                public String getUpFirstTime2() {
                    return upFirstTime2;
                }

                public String getMaxHead() {
                    return maxHead;
                }

                public String getUpLastTime2() {
                    return upLastTime2;
                }

                public String getDownFirstTime2() {
                    return downFirstTime2;
                }

                public String getTurnSeq() {
                    return turnSeq;
                }

                public String getDownLastTime() {
                    return downLastTime;
                }

                public String getRouteId() {
                    return routeId;
                }

                public String getGarageTel() {
                    return garageTel;
                }

                public String getCompanyNm() {
                    return companyNm;
                }

                public String getMinHead() {
                    return minHead;
                }

                public String getStStaNm() {
                    return stStaNm;
                }

                public String getPeekAlloc2() {
                    return peekAlloc2;
                }

                public String getRouteNm() {
                    return routeNm;
                }
            }

            public class UpEntity {

                private int count;
                private List<StationEntity> list;

                public void setCount(int count) {
                    this.count = count;
                }

                public void setList(List<StationEntity> list) {
                    this.list = list;
                }

                public int getCount() {
                    return count;
                }

                public List<StationEntity> getList() {
                    return list;
                }


            }

            public class DownEntity {
                private int count;
                private List<StationEntity> list;

                public void setCount(int count) {
                    this.count = count;
                }

                public void setList(List<StationEntity> list) {
                    this.list = list;
                }

                public int getCount() {
                    return count;
                }

                public List<StationEntity> getList() {
                    return list;
                }

            }

            public class StationEntity {
                /**
                 * stationNm : 광교차고지
                 * stationNo : 04254
                 * gnm : 수원시
                 * lon : 14146440.193254
                 * stationSeq : 0
                 * centerYn : N
                 * mobileNoSi :
                 * lat : 4479291.617732
                 * stationId : 203000254
                 */
                private String stationNm;
                private String stationNo;
                private String gnm;
                private Double lon;
                private String stationSeq;
                private String centerYn;
                private String mobileNoSi;
                private Double lat;
                private String stationId;

                public void setStationNm(String stationNm) {
                    this.stationNm = stationNm;
                }

                public void setStationNo(String stationNo) {
                    this.stationNo = stationNo;
                }

                public void setGnm(String gnm) {
                    this.gnm = gnm;
                }

                public void setLon(Double lon) {
                    this.lon = lon;
                }

                public void setStationSeq(String stationSeq) {
                    this.stationSeq = stationSeq;
                }

                public void setCenterYn(String centerYn) {
                    this.centerYn = centerYn;
                }

                public void setMobileNoSi(String mobileNoSi) {
                    this.mobileNoSi = mobileNoSi;
                }

                public void setLat(Double lat) {
                    this.lat = lat;
                }

                public void setStationId(String stationId) {
                    this.stationId = stationId;
                }

                public String getStationNm() {
                    return stationNm;
                }

                public String getStationNo() {
                    return stationNo;
                }

                public String getGnm() {
                    return gnm;
                }

                public Double getLon() {
                    return lon;
                }

                public String getStationSeq() {
                    return stationSeq;
                }

                public String getCenterYn() {
                    return centerYn;
                }

                public String getMobileNoSi() {
                    return mobileNoSi;
                }

                public Double getLat() {
                    return lat;
                }

                public String getStationId() {
                    return stationId;
                }
            }
        }
    }
}
