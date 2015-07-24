package kr.rokoroku.mbus.data.model;

import android.content.Context;
import android.support.annotation.Nullable;

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

    public String getCityName(Context context) {
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
            cityName = context.getString(strRes);
        }
        return cityName;
    }

    public String getProviderText() {
        switch (this) {
            case GYEONGGI:
                return "경기도 버스정보시스템";
            case SEOUL:
                return "서울시 교통정보과";
            case INCHEON:
                return "인천시 버스정보시스템";
        }
        return null;
    }
}
