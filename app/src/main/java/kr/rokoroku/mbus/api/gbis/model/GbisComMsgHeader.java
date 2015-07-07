package kr.rokoroku.mbus.api.gbis.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rok on 2015. 4. 22..
 */
@Root(name = "comMsgHeader")
public class GbisComMsgHeader {

    @Element(name = "returnCode")
    private int responseCode;

    @Element(name = "errMsg")
    private String message;

    public GbisComMsgHeader() {
    }

    public GbisComMsgHeader(int responseCode, String message) {
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
