package kr.rokoroku.mbus.api.seoulweb.model;

import java.util.List;

/**
 * Created by rok on 2015. 6. 3..
 */
public class SeoulWebSearchRouteResult {

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
        public String firstLowTm;
        public String lastLowTm;
        public String length;
        public String corpNm;
        public String lastBusTm;
        public String busRouteNm;
        public String stStationNm;
        public String lastBusYn;
        public String edStationNm;
        public String term;
        public String firstBusTm;
        public String routeType;
        public String busRouteId;
    }
}
