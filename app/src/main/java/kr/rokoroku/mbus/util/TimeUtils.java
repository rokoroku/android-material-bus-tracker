package kr.rokoroku.mbus.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.rokoroku.mbus.BaseApplication;

/**
 * Created by rok on 2015. 6. 3..
 */
public class TimeUtils {

    public static boolean checkShouldUpdate(Date lastUpdateTime) {
        return lastUpdateTime == null || checkShouldUpdate(lastUpdateTime.getTime());
    }

    public static boolean checkShouldUpdate(Long timestamp) {
        return timestamp < 0 ||
                System.currentTimeMillis() - timestamp > (BaseApplication.REFRESH_INTERVAL / 2);
    }

    public static DateFormat getSeoulBusDateFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmSS", Locale.getDefault()) {
            @Override
            public Date parse(String s) throws ParseException {
                return super.parse((s + "000000").substring(0, 14));
            }
        };
    }

    public static DateFormat getGbisDateFormat() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
}
