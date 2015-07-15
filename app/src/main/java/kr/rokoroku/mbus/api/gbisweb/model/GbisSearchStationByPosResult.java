package kr.rokoroku.mbus.api.gbisweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 7. 13..
 */
public class GbisSearchStationByPosResult {

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

        private ResultMapEntity resultMap;

        public void setResultMap(ResultMapEntity resultMap) {
            this.resultMap = resultMap;
        }

        public ResultMapEntity getResultMap() {
            return resultMap;
        }

        public class ResultMapEntity {

            private List<ListEntity> list;

            public void setList(List<ListEntity> list) {
                this.list = list;
            }

            public List<ListEntity> getList() {
                return list;
            }

            public class ListEntity {
                /**
                 * stationNm : 서울기록관
                 * lon : 14148852.45025
                 * staNo :  05182
                 * lat : 4497176.112113
                 * stationId : 204000200
                 */
                private String stationNm;
                private String stationId;
                private String staNo;
                private String lat;
                private String lon;

                public void setStationNm(String stationNm) {
                    this.stationNm = stationNm;
                }

                public void setLon(String lon) {
                    this.lon = lon;
                }

                public void setStaNo(String staNo) {
                    this.staNo = staNo;
                }

                public void setLat(String lat) {
                    this.lat = lat;
                }

                public void setStationId(String stationId) {
                    this.stationId = stationId;
                }

                public String getStationNm() {
                    return stationNm;
                }

                public String getLon() {
                    return lon;
                }

                public String getStaNo() {
                    return staNo;
                }

                public String getLat() {
                    return lat;
                }

                public String getStationId() {
                    return stationId;
                }
            }
        }
    }
}
