package kr.rokoroku.mbus.api.tago.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 7. 13..
 */
@Root
public class ResponseHeader {

    @Element
    public int resultCode = -1;
    @Element
    public String resultMsg;

    public boolean isSuccess() {
        return resultCode == 0;
    }
}
