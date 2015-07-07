package kr.rokoroku.mbus.api.gbis.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 5. 30..
 */
@Root
public class GbisBusLocationList {
    @ElementList(name = "msgBody")
    private List<GbisBusLocation> items;

    public List<GbisBusLocation> getItems() {
        return items;
    }

}
