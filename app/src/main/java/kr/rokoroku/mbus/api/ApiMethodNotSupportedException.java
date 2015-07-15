package kr.rokoroku.mbus.api;

/**
 * Created by rok on 2015. 7. 13..
 */
public class ApiMethodNotSupportedException extends Exception {

    public ApiMethodNotSupportedException() {
        super("Api doesn't support given method.");
    }

    public ApiMethodNotSupportedException(String detailMessage) {
        super(detailMessage);
    }
}
