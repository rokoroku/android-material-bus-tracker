package kr.rokoroku.mbus.api.gbisweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class GbisSearchAllResult {

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
        /**
         * busRoute : {"pageOfRoute":"1","count":10,"totalCount":"212","list":[{"startLat":"4551118.437066","routeTp":"13","endLat":"4530660.037654","routeId":"207000021","siFlag":"2","startLon":"14148881.20899","endLon":"14144059.974327","routeRegoin":"서울, 양주, 의정부","routeNm":"7"},{"startLat":"4474602.872936","routeTp":"13","endLat":"4488001.393139","routeId":"200000039","siFlag":"2","startLon":"14133347.297399","endLon":"14149472.246621","routeRegoin":"성남, 수원, 용인","routeNm":"7"},{"startLat":"4525698.653667","routeTp":"13","endLat":"4504880.076589","routeId":"165000010","siFlag":"3","startLon":"14097049.297164","endLon":"14104327.67455","routeRegoin":"김포, 인천","routeNm":"7"},{"startLat":"4533891.037029","routeTp":"14","endLat":"4517190.854079","routeId":"218000012","siFlag":"2","startLon":"14109216.375551","endLon":"14134584.461173","routeRegoin":"고양, 서울","routeNm":"M7106"},{"startLat":"4541614.036378","routeTp":"14","endLat":"4517190.854079","routeId":"229000102","siFlag":"2","startLon":"14108155.82456","endLon":"14134584.461173","routeRegoin":"서울, 파주","routeNm":"M7111"},{"startLat":"4534447.095405","routeTp":"14","endLat":"4517190.854079","routeId":"218000015","siFlag":"2","startLon":"14116718.117041","endLon":"14134584.461173","routeRegoin":"고양, 서울","routeNm":"M7119"},{"startLat":"4536220.233059","routeTp":"14","endLat":"4508497.027452","routeId":"234001241","siFlag":"2","startLon":"14112957.357398","endLon":"14140735.360897","routeRegoin":"고양, 서울","routeNm":"M7412"},{"startLat":"4541751.135009","routeTp":"14","endLat":"4506905.415294","routeId":"229000111","siFlag":"2","startLon":"14110842.7122","endLon":"14141728.321558","routeRegoin":"서울, 파주","routeNm":"M7426"},{"startLat":"4536220.233059","routeTp":"14","endLat":"4513482.428143","routeId":"234001242","siFlag":"2","startLon":"14112957.357398","endLon":"14129019.609119","routeRegoin":"고양, 서울","routeNm":"M7613"},{"startLat":"4541751.135009","routeTp":"14","endLat":"4512010.683976","routeId":"229000112","siFlag":"2","startLon":"14110842.7122","endLon":"14129224.824173","routeRegoin":"서울, 파주","routeNm":"M7625"}]}
         * subwayStation : {"pageOfSubway":"1","count":0,"list":[]}
         * poi : {"pageOfPOI":"1","count":10,"list":[{"poiTelNo":"","poiAddrName":"서울 서초구 방배동 ","poiNm":"이수역  7번출구","poiBizName":"","lon":"14135552.4982647","poiUpperBizName":"교통편의","poiId":"1134705","poiSeq":"1","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4506882.8250636"},{"poiTelNo":"","poiAddrName":"서울 서초구 방배동 ","poiNm":"내방역  7번출구","poiBizName":"","lon":"14136851.0968007","poiUpperBizName":"교통편의","poiId":"1134734","poiSeq":"2","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4507358.2249258"},{"poiTelNo":"","poiAddrName":"서울 서초구 반포동 ","poiNm":"논현역  7번출구","poiBizName":"","lon":"14139949.1329142","poiUpperBizName":"교통편의","poiId":"1134783","poiSeq":"3","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4510674.7993237"},{"poiTelNo":"","poiAddrName":"서울 광진구 중곡동 ","poiNm":"군자역  7번출구","poiBizName":"","lon":"14146383.2897972","poiUpperBizName":"교통편의","poiId":"1134947","poiSeq":"4","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4517002.9892919"},{"poiTelNo":"","poiAddrName":"서울 중랑구 묵동 ","poiNm":"먹골역  7번출구","poiBizName":"","lon":"14146188.3244746","poiUpperBizName":"교통편의","poiId":"1519251","poiSeq":"5","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4524716.4413820"},{"poiTelNo":"","poiAddrName":"서울 강남구 청담동 ","poiNm":"청담역  7번출구","poiBizName":"","lon":"14143173.9913523","poiUpperBizName":"교통편의","poiId":"1134864","poiSeq":"6","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4511610.3865754"},{"poiTelNo":"","poiAddrName":"서울 노원구 상계동 ","poiNm":"노원역  7번출구","poiBizName":"","lon":"14144268.1029523","poiUpperBizName":"교통편의","poiId":"1134671","poiSeq":"7","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4530709.2763376"},{"poiTelNo":"","poiAddrName":"서울 강남구 논현동 ","poiNm":"학동역  7번출구","poiBizName":"","lon":"14140966.3687136","poiUpperBizName":"교통편의","poiId":"1134820","poiSeq":"8","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4511033.4262557"},{"poiTelNo":"","poiAddrName":"서울 노원구 상계동 ","poiNm":"마들역  7번출구","poiBizName":"","lon":"14143955.7820578","poiUpperBizName":"교통편의","poiId":"1135113","poiSeq":"9","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4532318.3667972"},{"poiTelNo":"02-2686-6607","poiAddrName":"경기 광명시 광명동  720","poiNm":"광명7동주민센터","poiBizName":"","lon":"14121267.8457751","poiUpperBizName":"공공편의","poiId":"221946","poiSeq":"10","poiMiddleBizName":"행정기관","poiRpFlag":"8","lat":"4504731.9162339"}]}
         * busStation : {"count":10,"totalCount":"170","list":[{"stationNm":"도건설본부.축산위생연구소.칠보마을7단지","stationNo":"02009","lon":"14130958.760027","stationSeq":"1","centerYn":"N","stationNoSi":"","lat":"4476462.091216","districtGnm":"수원시","stationId":"201000178"},{"stationNm":"도건설본부.축산위생연구소.칠보마을7단지","stationNo":"02840","lon":"14131070.164243","stationSeq":"2","centerYn":"N","stationNoSi":"","lat":"4476486.134981","districtGnm":"수원시","stationId":"201000337"},{"stationNm":"칠보마을7단지","stationNo":"02915","lon":"14131194.947569","stationSeq":"3","centerYn":"N","stationNoSi":"","lat":"4476558.266605","districtGnm":"수원시","stationId":"201000416"},{"stationNm":"칠보마을7단지","stationNo":"02916","lon":"14131240.658356","stationSeq":"4","centerYn":"N","stationNoSi":"","lat":"4476718.560871","districtGnm":"수원시","stationId":"201000417"},{"stationNm":"망포역7번출구.벽적골8단지","stationNo":"04082","lon":"14144082.476637","stationSeq":"5","centerYn":"N","stationNoSi":"","lat":"4473465.064968","districtGnm":"수원시","stationId":"203000140"},{"stationNm":"도촌7단지.8단지","stationNo":"06187","lon":"14155573.854125","stationSeq":"6","centerYn":"N","stationNoSi":"","lat":"4496702.330222","districtGnm":"성남시","stationId":"205000220"},{"stationNm":"도촌7단지.8단지","stationNo":"06190","lon":"14155587.244241","stationSeq":"7","centerYn":"N","stationNoSi":"","lat":"4496662.180197","districtGnm":"성남시","stationId":"205000214"},{"stationNm":"주공7단지앞","stationNo":"07088","lon":"14150335.909477","stationSeq":"8","centerYn":"N","stationNoSi":"","lat":"4490697.592062","districtGnm":"성남시","stationId":"206000182"},{"stationNm":"주공7단지","stationNo":"07092","lon":"14150370.934202","stationSeq":"9","centerYn":"N","stationNoSi":"","lat":"4490561.160622","districtGnm":"성남시","stationId":"206000323"},{"stationNm":"매화마을사거리.장미마을7단지","stationNo":"07221","lon":"14152463.465823","stationSeq":"10","centerYn":"N","stationNoSi":"","lat":"4496967.324237","districtGnm":"성남시","stationId":"206000116"}],"pageOfBus":"1"}
         */
        private BusRouteEntity busRoute;
        private SubwayStationEntity subwayStation;
        private PoiEntity poi;
        private BusStationEntity busStation;

        public void setBusRoute(BusRouteEntity busRoute) {
            this.busRoute = busRoute;
        }

        public void setSubwayStation(SubwayStationEntity subwayStation) {
            this.subwayStation = subwayStation;
        }

        public void setPoi(PoiEntity poi) {
            this.poi = poi;
        }

        public void setBusStation(BusStationEntity busStation) {
            this.busStation = busStation;
        }

        public BusRouteEntity getBusRoute() {
            return busRoute;
        }

        public SubwayStationEntity getSubwayStation() {
            return subwayStation;
        }

        public PoiEntity getPoi() {
            return poi;
        }

        public BusStationEntity getBusStation() {
            return busStation;
        }

        public class BusRouteEntity {
            /**
             * pageOfRoute : 1
             * count : 10
             * totalCount : 212
             * list : [{"startLat":"4551118.437066","routeTp":"13","endLat":"4530660.037654","routeId":"207000021","siFlag":"2","startLon":"14148881.20899","endLon":"14144059.974327","routeRegoin":"서울, 양주, 의정부","routeNm":"7"},{"startLat":"4474602.872936","routeTp":"13","endLat":"4488001.393139","routeId":"200000039","siFlag":"2","startLon":"14133347.297399","endLon":"14149472.246621","routeRegoin":"성남, 수원, 용인","routeNm":"7"},{"startLat":"4525698.653667","routeTp":"13","endLat":"4504880.076589","routeId":"165000010","siFlag":"3","startLon":"14097049.297164","endLon":"14104327.67455","routeRegoin":"김포, 인천","routeNm":"7"},{"startLat":"4533891.037029","routeTp":"14","endLat":"4517190.854079","routeId":"218000012","siFlag":"2","startLon":"14109216.375551","endLon":"14134584.461173","routeRegoin":"고양, 서울","routeNm":"M7106"},{"startLat":"4541614.036378","routeTp":"14","endLat":"4517190.854079","routeId":"229000102","siFlag":"2","startLon":"14108155.82456","endLon":"14134584.461173","routeRegoin":"서울, 파주","routeNm":"M7111"},{"startLat":"4534447.095405","routeTp":"14","endLat":"4517190.854079","routeId":"218000015","siFlag":"2","startLon":"14116718.117041","endLon":"14134584.461173","routeRegoin":"고양, 서울","routeNm":"M7119"},{"startLat":"4536220.233059","routeTp":"14","endLat":"4508497.027452","routeId":"234001241","siFlag":"2","startLon":"14112957.357398","endLon":"14140735.360897","routeRegoin":"고양, 서울","routeNm":"M7412"},{"startLat":"4541751.135009","routeTp":"14","endLat":"4506905.415294","routeId":"229000111","siFlag":"2","startLon":"14110842.7122","endLon":"14141728.321558","routeRegoin":"서울, 파주","routeNm":"M7426"},{"startLat":"4536220.233059","routeTp":"14","endLat":"4513482.428143","routeId":"234001242","siFlag":"2","startLon":"14112957.357398","endLon":"14129019.609119","routeRegoin":"고양, 서울","routeNm":"M7613"},{"startLat":"4541751.135009","routeTp":"14","endLat":"4512010.683976","routeId":"229000112","siFlag":"2","startLon":"14110842.7122","endLon":"14129224.824173","routeRegoin":"서울, 파주","routeNm":"M7625"}]
             */
            private String pageOfRoute;
            private int count;
            private String totalCount;
            private List<ListEntity> list;

            public void setPageOfRoute(String pageOfRoute) {
                this.pageOfRoute = pageOfRoute;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public void setTotalCount(String totalCount) {
                this.totalCount = totalCount;
            }

            public void setList(List<ListEntity> list) {
                this.list = list;
            }

            public String getPageOfRoute() {
                return pageOfRoute;
            }

            public int getCount() {
                return count;
            }

            public String getTotalCount() {
                return totalCount;
            }

            public List<ListEntity> getList() {
                return list;
            }

            public class ListEntity {
                /**
                 * startLat : 4551118.437066
                 * routeTp : 13
                 * endLat : 4530660.037654
                 * routeId : 207000021
                 * siFlag : 2
                 * startLon : 14148881.20899
                 * endLon : 14144059.974327
                 * routeRegoin : 서울, 양주, 의정부
                 * routeNm : 7
                 */
                private String startLat;
                private String routeTp;
                private String endLat;
                private String routeId;
                private String siFlag;
                private String startLon;
                private String endLon;
                private String routeRegoin;
                private String routeNm;

                public void setStartLat(String startLat) {
                    this.startLat = startLat;
                }

                public void setRouteTp(String routeTp) {
                    this.routeTp = routeTp;
                }

                public void setEndLat(String endLat) {
                    this.endLat = endLat;
                }

                public void setRouteId(String routeId) {
                    this.routeId = routeId;
                }

                public void setSiFlag(String siFlag) {
                    this.siFlag = siFlag;
                }

                public void setStartLon(String startLon) {
                    this.startLon = startLon;
                }

                public void setEndLon(String endLon) {
                    this.endLon = endLon;
                }

                public void setRouteRegoin(String routeRegoin) {
                    this.routeRegoin = routeRegoin;
                }

                public void setRouteNm(String routeNm) {
                    this.routeNm = routeNm;
                }

                public String getStartLat() {
                    return startLat;
                }

                public String getRouteTp() {
                    return routeTp;
                }

                public String getEndLat() {
                    return endLat;
                }

                public String getRouteId() {
                    return routeId;
                }

                public String getSiFlag() {
                    return siFlag;
                }

                public String getStartLon() {
                    return startLon;
                }

                public String getEndLon() {
                    return endLon;
                }

                public String getRouteRegion() {
                    return routeRegoin;
                }

                public String getRouteNm() {
                    return routeNm;
                }
            }
        }

        public class SubwayStationEntity {
            /**
             * pageOfSubway : 1
             * count : 0
             * list : []
             */
            private String pageOfSubway;
            private int count;
            private List<?> list;

            public void setPageOfSubway(String pageOfSubway) {
                this.pageOfSubway = pageOfSubway;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public void setList(List<?> list) {
                this.list = list;
            }

            public String getPageOfSubway() {
                return pageOfSubway;
            }

            public int getCount() {
                return count;
            }

            public List<?> getList() {
                return list;
            }
        }

        public class PoiEntity {
            /**
             * pageOfPOI : 1
             * count : 10
             * list : [{"poiTelNo":"","poiAddrName":"서울 서초구 방배동 ","poiNm":"이수역  7번출구","poiBizName":"","lon":"14135552.4982647","poiUpperBizName":"교통편의","poiId":"1134705","poiSeq":"1","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4506882.8250636"},{"poiTelNo":"","poiAddrName":"서울 서초구 방배동 ","poiNm":"내방역  7번출구","poiBizName":"","lon":"14136851.0968007","poiUpperBizName":"교통편의","poiId":"1134734","poiSeq":"2","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4507358.2249258"},{"poiTelNo":"","poiAddrName":"서울 서초구 반포동 ","poiNm":"논현역  7번출구","poiBizName":"","lon":"14139949.1329142","poiUpperBizName":"교통편의","poiId":"1134783","poiSeq":"3","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4510674.7993237"},{"poiTelNo":"","poiAddrName":"서울 광진구 중곡동 ","poiNm":"군자역  7번출구","poiBizName":"","lon":"14146383.2897972","poiUpperBizName":"교통편의","poiId":"1134947","poiSeq":"4","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4517002.9892919"},{"poiTelNo":"","poiAddrName":"서울 중랑구 묵동 ","poiNm":"먹골역  7번출구","poiBizName":"","lon":"14146188.3244746","poiUpperBizName":"교통편의","poiId":"1519251","poiSeq":"5","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4524716.4413820"},{"poiTelNo":"","poiAddrName":"서울 강남구 청담동 ","poiNm":"청담역  7번출구","poiBizName":"","lon":"14143173.9913523","poiUpperBizName":"교통편의","poiId":"1134864","poiSeq":"6","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4511610.3865754"},{"poiTelNo":"","poiAddrName":"서울 노원구 상계동 ","poiNm":"노원역  7번출구","poiBizName":"","lon":"14144268.1029523","poiUpperBizName":"교통편의","poiId":"1134671","poiSeq":"7","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4530709.2763376"},{"poiTelNo":"","poiAddrName":"서울 강남구 논현동 ","poiNm":"학동역  7번출구","poiBizName":"","lon":"14140966.3687136","poiUpperBizName":"교통편의","poiId":"1134820","poiSeq":"8","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4511033.4262557"},{"poiTelNo":"","poiAddrName":"서울 노원구 상계동 ","poiNm":"마들역  7번출구","poiBizName":"","lon":"14143955.7820578","poiUpperBizName":"교통편의","poiId":"1135113","poiSeq":"9","poiMiddleBizName":"교통시설","poiRpFlag":"8","lat":"4532318.3667972"},{"poiTelNo":"02-2686-6607","poiAddrName":"경기 광명시 광명동  720","poiNm":"광명7동주민센터","poiBizName":"","lon":"14121267.8457751","poiUpperBizName":"공공편의","poiId":"221946","poiSeq":"10","poiMiddleBizName":"행정기관","poiRpFlag":"8","lat":"4504731.9162339"}]
             */
            private String pageOfPOI;
            private int count;
            private List<ListEntity> list;

            public void setPageOfPOI(String pageOfPOI) {
                this.pageOfPOI = pageOfPOI;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public void setList(List<ListEntity> list) {
                this.list = list;
            }

            public String getPageOfPOI() {
                return pageOfPOI;
            }

            public int getCount() {
                return count;
            }

            public List<ListEntity> getList() {
                return list;
            }

            public class ListEntity {
                /**
                 * poiTelNo : 
                 * poiAddrName : 서울 서초구 방배동 
                 * poiNm : 이수역  7번출구
                 * poiBizName : 
                 * lon : 14135552.4982647
                 * poiUpperBizName : 교통편의
                 * poiId : 1134705
                 * poiSeq : 1
                 * poiMiddleBizName : 교통시설
                 * poiRpFlag : 8
                 * lat : 4506882.8250636
                 */
                private String poiTelNo;
                private String poiAddrName;
                private String poiNm;
                private String poiBizName;
                private String lon;
                private String poiUpperBizName;
                private String poiId;
                private String poiSeq;
                private String poiMiddleBizName;
                private String poiRpFlag;
                private String lat;

                public void setPoiTelNo(String poiTelNo) {
                    this.poiTelNo = poiTelNo;
                }

                public void setPoiAddrName(String poiAddrName) {
                    this.poiAddrName = poiAddrName;
                }

                public void setPoiNm(String poiNm) {
                    this.poiNm = poiNm;
                }

                public void setPoiBizName(String poiBizName) {
                    this.poiBizName = poiBizName;
                }

                public void setLon(String lon) {
                    this.lon = lon;
                }

                public void setPoiUpperBizName(String poiUpperBizName) {
                    this.poiUpperBizName = poiUpperBizName;
                }

                public void setPoiId(String poiId) {
                    this.poiId = poiId;
                }

                public void setPoiSeq(String poiSeq) {
                    this.poiSeq = poiSeq;
                }

                public void setPoiMiddleBizName(String poiMiddleBizName) {
                    this.poiMiddleBizName = poiMiddleBizName;
                }

                public void setPoiRpFlag(String poiRpFlag) {
                    this.poiRpFlag = poiRpFlag;
                }

                public void setLat(String lat) {
                    this.lat = lat;
                }

                public String getPoiTelNo() {
                    return poiTelNo;
                }

                public String getPoiAddrName() {
                    return poiAddrName;
                }

                public String getPoiNm() {
                    return poiNm;
                }

                public String getPoiBizName() {
                    return poiBizName;
                }

                public String getLon() {
                    return lon;
                }

                public String getPoiUpperBizName() {
                    return poiUpperBizName;
                }

                public String getPoiId() {
                    return poiId;
                }

                public String getPoiSeq() {
                    return poiSeq;
                }

                public String getPoiMiddleBizName() {
                    return poiMiddleBizName;
                }

                public String getPoiRpFlag() {
                    return poiRpFlag;
                }

                public String getLat() {
                    return lat;
                }
            }
        }

        public class BusStationEntity {
            /**
             * count : 10
             * totalCount : 170
             * list : [{"stationNm":"도건설본부.축산위생연구소.칠보마을7단지","stationNo":"02009","lon":"14130958.760027","stationSeq":"1","centerYn":"N","stationNoSi":"","lat":"4476462.091216","districtGnm":"수원시","stationId":"201000178"},{"stationNm":"도건설본부.축산위생연구소.칠보마을7단지","stationNo":"02840","lon":"14131070.164243","stationSeq":"2","centerYn":"N","stationNoSi":"","lat":"4476486.134981","districtGnm":"수원시","stationId":"201000337"},{"stationNm":"칠보마을7단지","stationNo":"02915","lon":"14131194.947569","stationSeq":"3","centerYn":"N","stationNoSi":"","lat":"4476558.266605","districtGnm":"수원시","stationId":"201000416"},{"stationNm":"칠보마을7단지","stationNo":"02916","lon":"14131240.658356","stationSeq":"4","centerYn":"N","stationNoSi":"","lat":"4476718.560871","districtGnm":"수원시","stationId":"201000417"},{"stationNm":"망포역7번출구.벽적골8단지","stationNo":"04082","lon":"14144082.476637","stationSeq":"5","centerYn":"N","stationNoSi":"","lat":"4473465.064968","districtGnm":"수원시","stationId":"203000140"},{"stationNm":"도촌7단지.8단지","stationNo":"06187","lon":"14155573.854125","stationSeq":"6","centerYn":"N","stationNoSi":"","lat":"4496702.330222","districtGnm":"성남시","stationId":"205000220"},{"stationNm":"도촌7단지.8단지","stationNo":"06190","lon":"14155587.244241","stationSeq":"7","centerYn":"N","stationNoSi":"","lat":"4496662.180197","districtGnm":"성남시","stationId":"205000214"},{"stationNm":"주공7단지앞","stationNo":"07088","lon":"14150335.909477","stationSeq":"8","centerYn":"N","stationNoSi":"","lat":"4490697.592062","districtGnm":"성남시","stationId":"206000182"},{"stationNm":"주공7단지","stationNo":"07092","lon":"14150370.934202","stationSeq":"9","centerYn":"N","stationNoSi":"","lat":"4490561.160622","districtGnm":"성남시","stationId":"206000323"},{"stationNm":"매화마을사거리.장미마을7단지","stationNo":"07221","lon":"14152463.465823","stationSeq":"10","centerYn":"N","stationNoSi":"","lat":"4496967.324237","districtGnm":"성남시","stationId":"206000116"}]
             * pageOfBus : 1
             */
            private int count;
            private String totalCount;
            private List<ListEntity> list;
            private String pageOfBus;

            public void setCount(int count) {
                this.count = count;
            }

            public void setTotalCount(String totalCount) {
                this.totalCount = totalCount;
            }

            public void setList(List<ListEntity> list) {
                this.list = list;
            }

            public void setPageOfBus(String pageOfBus) {
                this.pageOfBus = pageOfBus;
            }

            public int getCount() {
                return count;
            }

            public String getTotalCount() {
                return totalCount;
            }

            public List<ListEntity> getList() {
                return list;
            }

            public String getPageOfBus() {
                return pageOfBus;
            }

            public class ListEntity {
                /**
                 * stationNm : 도건설본부.축산위생연구소.칠보마을7단지
                 * stationNo : 02009
                 * lon : 14130958.760027
                 * stationSeq : 1
                 * centerYn : N
                 * stationNoSi : 
                 * lat : 4476462.091216
                 * districtGnm : 수원시
                 * stationId : 201000178
                 */
                private String stationNm;
                private String stationNo;
                private String stationSeq;
                private String centerYn;
                private String stationNoSi;
                private Double lon;
                private Double lat;
                private String districtGnm;
                private String stationId;

                public void setStationNm(String stationNm) {
                    this.stationNm = stationNm;
                }

                public void setStationNo(String stationNo) {
                    this.stationNo = stationNo;
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

                public void setStationNoSi(String stationNoSi) {
                    this.stationNoSi = stationNoSi;
                }

                public void setLat(Double lat) {
                    this.lat = lat;
                }

                public void setDistrictGnm(String districtGnm) {
                    this.districtGnm = districtGnm;
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

                public Double getLon() {
                    return lon;
                }

                public String getStationSeq() {
                    return stationSeq;
                }

                public String getCenterYn() {
                    return centerYn;
                }

                public String getStationNoSi() {
                    return stationNoSi;
                }

                public Double getLat() {
                    return lat;
                }

                public String getDistrictGnm() {
                    return districtGnm;
                }

                public String getStationId() {
                    return stationId;
                }
            }
        }
    }
}
