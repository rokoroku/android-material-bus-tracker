package kr.rokoroku.mbus.data.model;

import android.content.Context;

import kr.rokoroku.mbus.R;

/**
 * Created by rok on 2015. 6. 3..
 */
public enum District {
    SEOUL(1),
    GYEONGGI(2),
    INCHEON(3),
    UNKNOWN(-1);

    private int value;

    District(int value) {
        this.value = value;
    }

    public static District valueOfGbis(String siflag) {
        int flag = -1;
        if (siflag != null) try {
            flag = Integer.valueOf(siflag);
        } catch (Exception ignored) {

        }
        for (District district : District.values()) {
            if (district.value == flag) return district;
        }
        return UNKNOWN;
    }


    public String getCityName(Context context) {
        int strRes;
        switch (this) {
            case SEOUL:
                strRes = R.string.city_seoul;
                break;

            case GYEONGGI:
                strRes = R.string.city_gyeonggi;
                break;

            case INCHEON:
                strRes = R.string.city_incheon;
                break;

            default:
                strRes = android.R.string.unknownName;
                break;
        }
        return context.getString(strRes);
    }
}
