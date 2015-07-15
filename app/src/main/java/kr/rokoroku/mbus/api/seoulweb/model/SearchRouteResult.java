package kr.rokoroku.mbus.api.seoulweb.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SearchRouteResult {

    @SerializedName("error")
    public ResponseHeader header;

    @SerializedName("resultList")
    public List<RouteEntity> resultList;

    public class RouteEntity {
        /**
         * firstLowTm :
         * lastLowTm :
         * length : 112
         * corpNm : 공항리무진  02-2664-9898
         * lastBusTm : 20150710210000
         * busRouteNm : 6008
         * stStationNm : 영등포역
         * lastBusYn :
         * edStationNm : 경방타임스퀘어
         * term : 20
         * firstBusTm : 20150710044000
         * routeType : 1
         * busRouteId : 3600800
         */
        public String busRouteId;
        public String busRouteNm;
        public String routeType;
        public String edStationNm;
        public String stStationNm;
        public String corpNm;
        public String term;
        public String length;
        public String lastBusTm;
        public String lastLowTm;
        public String lastBusYn;
        public String firstLowTm;
        public String firstBusTm;
    }
}
