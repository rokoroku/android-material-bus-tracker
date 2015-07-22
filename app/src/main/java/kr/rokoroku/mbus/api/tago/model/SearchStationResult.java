package kr.rokoroku.mbus.api.tago.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 7. 16..
 */
@Root(name = "items")
public class SearchStationResult {

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
         * <gpslati>37.457421</gpslati>
         * <gpslong>126.633598</gpslong>
         * <nodeid>ICB161000119</nodeid>
         * <nodenm>인하대병원</nodenm>
         */
        @Element(name = "nodeid")
        public String stationId;
        @Element(name = "nodenm")
        public String stationName;

        @Element(name = "gpslati")
        public double gpslati;
        @Element(name = "gpslong")
        public double gpslong;
    }
}