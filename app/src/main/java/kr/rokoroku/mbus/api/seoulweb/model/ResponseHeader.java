package kr.rokoroku.mbus.api.seoulweb.model;

/**
 * Created by rok on 2015. 7. 13..
 */
public class ResponseHeader {
    /**
     * errorMessage : 성공
     * errorCode : 0000
     */
    public int errorCode = -1;
    public String errorMessage;

    public boolean isSuccess() {
        return errorCode == 0;
    }
}
