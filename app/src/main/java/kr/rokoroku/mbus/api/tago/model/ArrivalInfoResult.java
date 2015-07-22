package kr.rokoroku.mbus.api.tago.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 7. 16..
 */
@Root(name = "items")
public class ArrivalInfoResult {

    @ElementList(entry = "item", inline = true, required = false)
    private List<ResultEntity> items;

    public List<ResultEntity> getItems() {
        return items;
    }

    public void setItems(List<ResultEntity> items) {
        this.items = items;
    }

    public static class ResultEntity {

        /**
         * <arrprevstationcnt>9</arrprevstationcnt>
         * <arrtime>1098</arrtime>
         * <nodeid>ICB163000099</nodeid>
         * <nodenm>인하대정문</nodenm>
         * <routeid>ICB165000334</routeid>
         * <routeno>9</routeno>
         * <routetp>간선</routetp>
         * <vehicletp>저상버스</vehicletp>
         */
        @Element(name = "routeid")
        public String routeId;
        @Element(name = "routeno")
        public String routeName;
        @Element(name = "routetp")
        public String routeType;

        @Element(name = "nodeid")
        public String stationId;
        @Element(name = "nodenm")
        public String stationName;

        @Element(name = "vehicletp")
        public String vehicleType;

        @Element(name = "arrtime")
        public int arrivalTime;
        @Element(name = "arrprevstationcnt")
        public int prevStationCount;
    }
}