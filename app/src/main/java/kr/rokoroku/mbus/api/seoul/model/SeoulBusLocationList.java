package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root
public class SeoulBusLocationList {
    @ElementList(name = "msgBody")
    private List<SeoulBusLocation> items;

    public List<SeoulBusLocation> getItems() {
        return items;
    }

}
