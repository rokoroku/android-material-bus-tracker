package kr.rokoroku.mbus.api.incheon.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 15. 8. 20..
 */
public class IncheonBusPosition {

    @SerializedName("result")
    public List<ResultEntity> result;

    public static class ResultEntity {
        /**
         * BUSID : 7116105
         * BUSNUM : 6105
         * PATHSEQ : 1
         */
        @SerializedName("BUSID")
        public String vehId;
        @SerializedName("BUSNUM")
        public String plate;
        @SerializedName("PATHSEQ")
        public int seq;
    }
}
