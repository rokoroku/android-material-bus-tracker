package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rok on 2015. 4. 13..
 */
@Root
public class SeoulStationInfoList {
    @ElementList(name = "msgBody")
    private List<SeoulStationInfo> items;

    public List<SeoulStationInfo> getItems() {
        return items;
    }

}