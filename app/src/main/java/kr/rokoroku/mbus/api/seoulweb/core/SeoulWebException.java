package kr.rokoroku.mbus.api.seoulweb.core;

import kr.rokoroku.mbus.api.seoulweb.model.ResponseHeader;
import retrofit.converter.ConversionException;

/**
 * Created by rok on 2015. 4. 22..
 */
public class SeoulWebException extends ConversionException {

    public static final int RESULT_OK = 0;
    public static final int ERROR_SYSTEM = 1;
    public static final int ERROR_MISSING_PARAMETER = 2;
    public static final int ERROR_INVALID_PARAMETER = 3;
    public static final int ERROR_NO_RESULT = 4;
    public static final int ERROR_NO_SERVICE_KEY = 5;
    public static final int ERROR_INVALID_SERVICE_KEY = 6;
    public static final int ERROR_HALTED_SERVICE_KEY = 7;
    public static final int ERROR_SERVICE_ACCESS_FLOODED = 8;
    public static final int ERROR_INVALID_GPS_POSITION = 20;

    int errorCode;

    public SeoulWebException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SeoulWebException(ResponseHeader responseHeader) {
        this(responseHeader.errorCode, responseHeader.errorMessage);
    }


    public int getErrorCode() {
        return errorCode;
    }
}
