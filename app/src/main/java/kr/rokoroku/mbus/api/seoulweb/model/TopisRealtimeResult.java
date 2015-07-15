package kr.rokoroku.mbus.api.seoulweb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 2015. 7. 15..
 */
public class TopisRealtimeResult {

    @SerializedName("result")
    public List<ResultEntity> result;

    public class ResultEntity {
        /**
         * stopFlag : 1
         * rtDist : 10.6
         * lastStnId : 10692
         * sectDist : 12
         * plainNo : 서울70사9128
         * fullSectDist : 210.581
         * nextStTm : 0
         * sectionId : 95238
         * sectOrd : 4
         * lastStnOrd :
         * lstbusyn :
         * lastStTm : 2131
         * dataTm : 20150715171032
         * trnstnid : 10971
         * vehId : 170029128
         * busType : 0
         * gpsX : 127.03664900495765
         * isrunyn : 1
         * gpsY : 37.46214167671144
         */
        public String trnstnid;
        public String vehId;
        public String busType;
        public int sectOrd;
        public String stopFlag;
        public String rtDist;
        public String lastStnId;
        public String sectDist;
        public String lstbusyn;
        public String plainNo;
        public String fullSectDist;
        public String nextStTm;
        public String sectionId;
        public String lastStnOrd;
        public String lastStTm;
        public String dataTm;
        public String isrunyn;
        public double gpsX;
        public double gpsY;
    }
}
