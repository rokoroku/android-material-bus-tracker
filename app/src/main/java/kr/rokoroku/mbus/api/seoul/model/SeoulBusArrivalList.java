package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root
public class SeoulBusArrivalList {
    @ElementList(name = "msgBody")
    private List<SeoulBusArrival> items;

    public List<SeoulBusArrival> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "BusArrivalList{" +
                "items=" + items +
                '}';
    }
}
