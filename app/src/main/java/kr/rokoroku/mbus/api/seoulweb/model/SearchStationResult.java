package kr.rokoroku.mbus.api.seoulweb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SearchStationResult {

    @SerializedName("error")
    public ResponseHeader header;

    @SerializedName("busList")
    public List<StationEntity> result;

    public class StationEntity {
        /**
         * posX : 200563.9
         * arsId : 08368
         * posY : 455515.7
         * tmY : 37.59917445517374
         * stNm : 508단지입구
         * stId : 32474
         * tmX : 127.00638624895606
         */
        public String stId;
        public String stNm;
        public String arsId;
        public double posX;
        public double posY;
        @SerializedName("tmX")
        public double gpsX;
        @SerializedName("tmY")
        public double gpsY;
    }

}
