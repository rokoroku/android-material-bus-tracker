package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root
public class SeoulBusRouteInfoList {
    @ElementList(name = "msgBody")
    private List<SeoulBusRouteInfo> items;

    public List<SeoulBusRouteInfo> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "BusRouteInfoList{" +
                "items=" + items +
                '}';
    }
}
