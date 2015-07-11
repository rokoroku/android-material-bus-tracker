package kr.rokoroku.mbus.api.seoulweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SeoulWebRouteStationResult {

    public ErrorEntity error;
    public List<ResultListEntity> resultList;

    public class ErrorEntity {
        /**
         * errorMessage : 성공
         * errorCode : 0000
         */
        public String errorMessage;
        public String errorCode;
    }

    public class ResultListEntity {
        /**
         * stationNm : 서울추모공원
         * sectSpdCol : SpeedRed
         * stationNo : 22099
         * fullSectDist : 0
         * section : 0
         * sectSpd : 0
         * arsId : 22099
         * busRouteNm : 서초08
         * existYn : N
         * trnstnid : 10971
         * lastTm : :
         * station : 13328
         * transYn : N
         * beginTm : 06:00
         * busType : N
         * routeType : 2
         * seq : 1
         * gpsX : 127.04288117001663
         * busRouteId : 3909200
         * gpsY : 37.454268392806995
         * direction : 양재역
         */
        public String stationNm;
        public String sectSpdCol;
        public String stationNo;
        public String fullSectDist;
        public String section;
        public String sectSpd;
        public String arsId;
        public String busRouteNm;
        public String existYn;
        public String trnstnid;
        public String lastTm;
        public String station;
        public String transYn;
        public String beginTm;
        public String busType;
        public String routeType;
        public String seq;
        public String gpsX;
        public String busRouteId;
        public String gpsY;
        public String direction;
    }
}
