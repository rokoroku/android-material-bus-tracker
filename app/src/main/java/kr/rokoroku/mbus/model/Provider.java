package kr.rokoroku.mbus.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;

public enum Provider {

    SEOUL(11),
    GYEONGGI(31),
    INCHEON(23);

    private final int cityCode;
    private String cityName;

    Provider(int cityCode) {
        this.cityCode = cityCode;
    }

    @Nullable
    public static Provider valueOf(int cityCode) {
        for (Provider provider : Provider.values()) {
            if (provider.cityCode == cityCode) return provider;
        }
        return null;
    }

    public int getCityCode() {
        return cityCode;
    }

    public String getCityName() {
        if(cityName == null) {
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
            cityName = BaseApplication.getInstance().getString(strRes);
        }
        return cityName;
    }
}
