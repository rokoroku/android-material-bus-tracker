package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 22..
 */
@Root(name = "msgHeader")
public class SeoulBusResponseHeader {

    @Element(name = "headerCd")
    private int responseCode;

    @Element(name = "headerMsg")
    private String message;

    @Element(name = "itemCount", required = false)
    private int itemCount;

    public SeoulBusResponseHeader() {
    }

    public SeoulBusResponseHeader(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
