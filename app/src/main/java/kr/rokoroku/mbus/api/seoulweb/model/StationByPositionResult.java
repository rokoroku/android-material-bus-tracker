package kr.rokoroku.mbus.api.seoulweb.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 7. 16..
 */
@Root(name = "Msg")
public class StationByPositionResult {

    @ElementList(entry = "BusStationList", inline = true, required = false)
    private List<ResultEntity> items;

    public List<ResultEntity> getItems() {
        return items;
    }

    public void setItems(List<ResultEntity> items) {
        this.items = items;
    }

    public static class ResultEntity {

        @Element(name = "arsId")
        public String arsId;
        @Element(name = "stationNm")
        public String stationName;
        @Element(name = "busX")
        public double gpsX;
        @Element(name = "busY")
        public double gpsY;
    }
}