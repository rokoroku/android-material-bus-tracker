package kr.rokoroku.mbus.api.incheon.data;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 15. 8. 20..
 */
@Root(name = "root")
public class IncheonArrivalInfo {

    /**
     * <errCode>000</errCode>
     * <errMessage>성공</errMessage>
     * <bstopName>대보아파트</bstopName>
     */
    @Element(name = "errCode")
    public String errorCode;
    @Element(name = "errMessage", required = false)
    public String errorMessage;
    @Element(name = "bstopName", required = false)
    public String stationName;

    @ElementList(entry = "list", inline = true, required = false)
    private List<ResultEntity> items;

    public List<ResultEntity> getItems() {
        return items;
    }

    public void setItems(List<ResultEntity> items) {
        this.items = items;
    }

    public static class ResultEntity {

        /**
         * <routeId>165000036</routeId>
         * <routeNo>37</routeNo>
         * <curBstopName>KT중동지사</curBstopName>
         * <restBusStopCnt>10</restBusStopCnt>
         * <arrPlanTime>12분 15초</arrPlanTime>
         */
        @Element(name = "routeId")
        public String routeId;
        @Element(name = "routeNo")
        public String routeName;
        @Element(name = "curBstopName")
        public String currentStationName;
        @Element(name = "restBusStopCnt")
        public int remainStation;
        @Element(name = "arrPlanTime")
        public String predictedTime;
    }
}
