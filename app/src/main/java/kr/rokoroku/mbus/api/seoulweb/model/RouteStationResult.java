package kr.rokoroku.mbus.api.seoulweb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class RouteStationResult {

    @SerializedName("error")
    public ResponseHeader header;

    @SerializedName("resultList")
    public List<StationEntity> resultList;

    public class StationEntity {
        /**
         * stationNm : 서울추모공원
         * sectSpdCol : SpeedRed
         * stationNo : 22099
         * fullSectDist : 0
         * section : 0, (95175) -> ?
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
        public String arsId;
        @SerializedName("station")
        public String stationId;
        public String stationNo;
        public String stationNm;
        public String busRouteId;
        public String busRouteNm;
        public String routeType;
        public int seq;
        public String section;
        public String sectSpd;
        public String existYn;
        public String trnstnid;
        public String lastTm;
        public String transYn;
        public String beginTm;
        public String busType;
        public double gpsX;
        public double gpsY;
        public String direction;
        public String sectSpdCol;
        public String fullSectDist;
    }
}
