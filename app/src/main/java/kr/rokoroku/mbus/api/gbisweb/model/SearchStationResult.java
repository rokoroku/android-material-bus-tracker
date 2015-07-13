package kr.rokoroku.mbus.api.gbisweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SearchStationResult {

    /**
     * success : true
     */
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
         * stationNm : 야탑역.고속버스터미널
         * count : 0
         * stationId : 206000158
         */
        private List<BusStationInfoEntity> busStationInfo;
        private String stationNm;
        private int count;
        private List<BusArrivalInfoEntity> busArrivalInfo;
        private String stationId;

        public void setBusStationInfo(List<BusStationInfoEntity> busStationInfo) {
            this.busStationInfo = busStationInfo;
        }

        public void setStationNm(String stationNm) {
            this.stationNm = stationNm;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setBusArrivalInfo(List<BusArrivalInfoEntity> busArrivalInfo) {
            this.busArrivalInfo = busArrivalInfo;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }

        public List<BusStationInfoEntity> getBusStationInfo() {
            return busStationInfo;
        }

        public String getStationNm() {
            return stationNm;
        }

        public int getCount() {
            return count;
        }

        public List<BusArrivalInfoEntity> getBusArrivalInfo() {
            return busArrivalInfo;
        }

        public String getStationId() {
            return stationId;
        }

        public class BusStationInfoEntity {
            /**
             * routeId : 234000065
             * routeName : 1005
             */
            private String routeId;
            private String routeName;

            public void setRouteId(String routeId) {
                this.routeId = routeId;
            }

            public void setRouteName(String routeName) {
                this.routeName = routeName;
            }

            public String getRouteId() {
                return routeId;
            }

            public String getRouteName() {
                return routeName;
            }
        }

        public class BusArrivalInfoEntity {
            /**
             * predictTime2 : 19
             * flag : PASS
             * vehId2 : 107017420
             * endBus2 : 0
             * vehId1 : 107017422
             * endBus1 : 0
             * predictTime1 : 9
             * routeName : 9403
             * routeId : 100100390
             * delayYn1 : N
             * stationNm2 : 정든마을.우성아파트
             * delayYn2 : N
             * stationId : 206000158
             * stationNm1 : 이매촌한신.서현역.AK프라자
             * routeDestName : 동대문역사문화공원
             * lowPlate1 : 0
             * crowded1 : 0
             * plateNo1 : 서울75사1090
             * crowded2 : 0
             * plateNo2 : 서울75사1075
             * routeTypeCd : 11
             * locationNo1 : 8
             * drvEnd : N
             * locationNo2 : 16
             * lowPlate2 : 0
             * remainSeatCnt2 : -1
             * remainSeatCnt1 : -1
             */
            private String predictTime2;
            private String flag;
            private String vehId2;
            private String endBus2;
            private String vehId1;
            private String endBus1;
            private String predictTime1;
            private String routeName;
            private String routeId;
            private String delayYn1;
            private String stationNm2;
            private String delayYn2;
            private String stationId;
            private String stationNm1;
            private String routeDestName;
            private String lowPlate1;
            private String crowded1;
            private String plateNo1;
            private String crowded2;
            private String plateNo2;
            private String routeTypeCd;
            private String locationNo1;
            private String drvEnd;
            private String locationNo2;
            private String lowPlate2;
            private String remainSeatCnt2;
            private String remainSeatCnt1;

            public void setPredictTime2(String predictTime2) {
                this.predictTime2 = predictTime2;
            }

            public void setFlag(String flag) {
                this.flag = flag;
            }

            public void setVehId2(String vehId2) {
                this.vehId2 = vehId2;
            }

            public void setEndBus2(String endBus2) {
                this.endBus2 = endBus2;
            }

            public void setVehId1(String vehId1) {
                this.vehId1 = vehId1;
            }

            public void setEndBus1(String endBus1) {
                this.endBus1 = endBus1;
            }

            public void setPredictTime1(String predictTime1) {
                this.predictTime1 = predictTime1;
            }

            public void setRouteName(String routeName) {
                this.routeName = routeName;
            }

            public void setRouteId(String routeId) {
                this.routeId = routeId;
            }

            public void setDelayYn1(String delayYn1) {
                this.delayYn1 = delayYn1;
            }

            public void setStationNm2(String stationNm2) {
                this.stationNm2 = stationNm2;
            }

            public void setDelayYn2(String delayYn2) {
                this.delayYn2 = delayYn2;
            }

            public void setStationId(String stationId) {
                this.stationId = stationId;
            }

            public void setStationNm1(String stationNm1) {
                this.stationNm1 = stationNm1;
            }

            public void setRouteDestName(String routeDestName) {
                this.routeDestName = routeDestName;
            }

            public void setLowPlate1(String lowPlate1) {
                this.lowPlate1 = lowPlate1;
            }

            public void setCrowded1(String crowded1) {
                this.crowded1 = crowded1;
            }

            public void setPlateNo1(String plateNo1) {
                this.plateNo1 = plateNo1;
            }

            public void setCrowded2(String crowded2) {
                this.crowded2 = crowded2;
            }

            public void setPlateNo2(String plateNo2) {
                this.plateNo2 = plateNo2;
            }

            public void setRouteTypeCd(String routeTypeCd) {
                this.routeTypeCd = routeTypeCd;
            }

            public void setLocationNo1(String locationNo1) {
                this.locationNo1 = locationNo1;
            }

            public void setDrvEnd(String drvEnd) {
                this.drvEnd = drvEnd;
            }

            public void setLocationNo2(String locationNo2) {
                this.locationNo2 = locationNo2;
            }

            public void setLowPlate2(String lowPlate2) {
                this.lowPlate2 = lowPlate2;
            }

            public void setRemainSeatCnt2(String remainSeatCnt2) {
                this.remainSeatCnt2 = remainSeatCnt2;
            }

            public void setRemainSeatCnt1(String remainSeatCnt1) {
                this.remainSeatCnt1 = remainSeatCnt1;
            }

            public String getPredictTime2() {
                return predictTime2;
            }

            public String getFlag() {
                return flag;
            }

            public String getVehId2() {
                return vehId2;
            }

            public String getEndBus2() {
                return endBus2;
            }

            public String getVehId1() {
                return vehId1;
            }

            public String getEndBus1() {
                return endBus1;
            }

            public String getPredictTime1() {
                return predictTime1;
            }

            public String getRouteName() {
                return routeName;
            }

            public String getRouteId() {
                return routeId;
            }

            public String getDelayYn1() {
                return delayYn1;
            }

            public String getStationNm2() {
                return stationNm2;
            }

            public String getDelayYn2() {
                return delayYn2;
            }

            public String getStationId() {
                return stationId;
            }

            public String getStationNm1() {
                return stationNm1;
            }

            public String getRouteDestName() {
                return routeDestName;
            }

            public String getLowPlate1() {
                return lowPlate1;
            }

            public String getCrowded1() {
                return crowded1;
            }

            public String getPlateNo1() {
                return plateNo1;
            }

            public String getCrowded2() {
                return crowded2;
            }

            public String getPlateNo2() {
                return plateNo2;
            }

            public String getRouteTypeCd() {
                return routeTypeCd;
            }

            public String getLocationNo1() {
                return locationNo1;
            }

            public String getDrvEnd() {
                return drvEnd;
            }

            public String getLocationNo2() {
                return locationNo2;
            }

            public String getLowPlate2() {
                return lowPlate2;
            }

            public String getRemainSeatCnt2() {
                return remainSeatCnt2;
            }

            public String getRemainSeatCnt1() {
                return remainSeatCnt1;
            }
        }
    }
}
