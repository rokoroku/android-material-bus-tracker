package kr.rokoroku.mbus.api.seoulweb.model;

/**
 * Created by rok on 2015. 7. 13..
 */
public class ErrorEntity {
    /**
     * errorMessage : 성공
     * errorCode : 0000
     */
    public String errorMessage;
    public String errorCode;

    public boolean isSuccess() {
        return "0000".equals(errorCode);
    }
}
