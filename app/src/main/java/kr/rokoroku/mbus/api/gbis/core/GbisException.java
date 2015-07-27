package kr.rokoroku.mbus.api.gbis.core;

import retrofit.converter.ConversionException;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisException extends ConversionException {

    /**
     * 0	정상적으로 처리되었습니다.
     * 1	시스템 에러가 발생하였습니다.
     * 2 	필수 요청 Parameter가 존재하지 않습니다.
     * 3	필수 요청 Parameter가 잘못되었습니다.
     * 4	결과가 존재하지 않습니다.
     * 5	필수 요청 Parameter(인증키)가 존재하지 않습니다.
     * 6	등록되지 않은 키 입니다.
     * 7	사용할 수 없는(등록은 되었으나, 일시적으로 사용중지된) 키입니다.
     * 8	요청 제한을 초과하였습니다.
     * 20	잘못된 위치로 요청하였습니다. 위경도 좌표값이 정확한지 확인하십시오.
     * 21	노선번호는 1자리 이상 입력하세요.
     * 22	정류소 명/번호는 1자리 이상 입력하세요
     * 23	버스 도착 정보가 존재하지 않습니다.
     * 31	존재하지 않는 출발 정류소 아이디(ID)/번호 입니다.
     * 32	존재하지 않는 도착 정류소 아이디(ID)/번호 입니다.
     * 99	API서비스 준비중입니다.
     */

    public static final int RESULT_OK = 0;
    public static final int ERROR_SYSTEM = 1;
    public static final int ERROR_MISSING_PARAMETER = 2;
    public static final int ERROR_INVALID_PARAMETER = 3;
    public static final int ERROR_NO_RESULT = 4;
    public static final int ERROR_NO_SERVICE_KEY = 5;
    public static final int ERROR_SERVICE_ACCESS_FLOODED = 22;
    public static final int ERROR_INVALID_SERVICE_KEY = 30;
    public static final int ERROR_INVALID_GPS_POSITION = 20;
    public static final int ERROR_REFER_COM_MSG_HEADER = 50;


    int errorCode;

    public GbisException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
