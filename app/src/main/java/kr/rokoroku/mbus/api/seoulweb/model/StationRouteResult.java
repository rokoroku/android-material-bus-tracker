package kr.rokoroku.mbus.api.seoulweb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 2015. 7. 14..
 */
public class StationRouteResult {

    @SerializedName("error")
    public ResponseHeader header;

    @SerializedName("resultList")
    public List<RouteEntity> result;

    public class RouteEntity {
        /**
         * stId : 5288
         * arsId : 22297
         * stNm : 시민의숲.양재꽃시장
         * rtNm : 9404
         * busRouteId : 4940400
         * staOrd : 21
         * adirection : 신사역
         * vehId1 : 49845
         * vehId2 : 51703
         * traTime1 : 420
         * traTime2 : 851
         * plainNo1 : 서울74사9845
         * plainNo2 : 서울75사1703
         * isFullFlag1 : 0
         * isFullFlag2 : 0
         * arrmsg1 : 7분후 [6번째 전]
         * arrmsg2 : 14분후 [6번째 전]
         * stationNm1 : 분당구청.경동보일러.수내역.롯데백화점
         * stationNm2 : 분당구청.경동보일러.수내역.롯데백화점
         * sectOrd1 : 15
         * sectOrd2 : 15
         * busType1 : 0
         * busType2 : 0
         * isLast1 : 0
         * isLast2 : 0
         * sectNm : 내곡동주민센터~시민의숲.양재꽃시장
         * term : 8
         * nextBus :
         * routeType : 6
         * isArrive1 : 0
         * isArrive2 : 0
         * busX : 127.03904686738552
         * busY : 37.47120154665851
         * rerdieDiv1 : 1
         * rerdieDiv2 : 1
         * stationTp : 3
         * firstTm : 04:00
         * posX : 203453.7
         * posY : 441314.5
         * rerideNum1 : 26
         * rerideNum2 : 4
         * repTm1 : 2015-07-14 18:27:43.0
         * repTm2 : 2015-07-14 18:27:07.0
         * lastTm : 00:00
         * traSpd1 : 114
         * traSpd2 : 56
         */
        public String busRouteId;
        public String arsId;
        public String plainNo1;
        public String plainNo2;
        public int traTime1;
        public String vehId1;
        public String vehId2;
        public int traTime2;
        public String isFullFlag1;
        public String isFullFlag2;
        public String arrmsg2;
        public String adirection;
        public String arrmsg1;
        public String rtNm;
        public String busType1;
        public String isLast1;
        public String busType2;
        public String sectNm;
        public String term;
        public String isLast2;
        public String nextBus;
        public String routeType;
        public String stationNm2;
        public double gpsX;
        public String isArrive1;
        public String stationNm1;
        public double gpsY;
        public String isArrive2;
        public String stationTp;
        public int sectOrd2;
        public String rerdieDiv1;
        public String rerdieDiv2;
        public int sectOrd1;
        public String stId;
        public int staOrd;
        public String firstTm;
        public double posX;
        public String repTm1;
        public double posY;
        public String traSpd1;
        public String rerideNum2;
        public String stNm;
        public String rerideNum1;
        public String repTm2;
        public String lastTm;
        public String traSpd2;
    }
}
