package kr.rokoroku.mbus.model;

import android.content.Context;

import java.io.Serializable;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.util.ThemeUtils;

/**
 * Created by rok on 2015. 6. 3..
 */
public enum RouteType {

    // Local Area
    GREEN,
    GREEN_GYEONGGI,
    GREEN_INCHEON,
    GREEN_SUBURB,

    // Intercourse
    BLUE,
    BLUE_INCHEON,
    BLUE_RESERVED,

    // Local Shuttle
    YELLOW,
    YELLOW_INCHEON,
    YELLOW_RESERVED,

    // Intercity & Express
    RED,
    RED_GYEONGGI,
    RED_INCHEON,
    RED_RESERVED,

    // Intercity (Gyeonggi)
    PURPLE_GYEONGGI,
    PURPLE_RESERVED,

    // M bus
    MBUS,

    // Airport Limousine
    AIRPORT,

    // Other
    UNKNOWN;

    public static RouteType valueOfGbis(String type) {
        if(type == null) return UNKNOWN;
        switch (type) {
            case "11":    // 광역버스
            case "14":    // m버스
                return RED_GYEONGGI;

            case "12":    // 서울지역 버스
                return BLUE;

            case "13":    // 일반 버스
                return GREEN_GYEONGGI;

            case "23":    // 농어촌 버스 (군내/외곽 버스)
                return GREEN_SUBURB;

            case "43":    // 시외버스 (경기도)
                return PURPLE_GYEONGGI;

            case "51":    // 공항버스
                return AIRPORT;

            default:
                return UNKNOWN;
        }
    }

    public static RouteType valueOfTopis(String type) {
        //3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용
        if(type == null) return UNKNOWN;
        switch (type) {
            case "1":
                return AIRPORT;
            case "3":
                return BLUE;
            case "4":
                return GREEN;
            case "5":
                return YELLOW;
            case "6":
                return RED;
            case "7":
                return RED_INCHEON;
            case "8":
                return RED_GYEONGGI;

            default:
                return UNKNOWN;
        }
    }

    public int getColor(Context context) {
        switch (this) {
            case RED:
            case RED_GYEONGGI:
                return ThemeUtils.getThemeColor(context, R.attr.busColorRedLine);
            case GREEN:
            case GREEN_GYEONGGI:
            case GREEN_SUBURB:
                return ThemeUtils.getThemeColor(context, R.attr.busColorGreenLine);
            case BLUE:
                return ThemeUtils.getThemeColor(context, R.attr.busColorBlueLine);
            case YELLOW:
                return ThemeUtils.getThemeColor(context, R.attr.busColorYellowLine);
            case AIRPORT:
                return ThemeUtils.getThemeColor(context, R.attr.busColorRedLine);
            case PURPLE_GYEONGGI:
                return ThemeUtils.getThemeColor(context, R.attr.busColorPurpleLine);
            case MBUS:
                return ThemeUtils.getThemeColor(context, R.attr.busColorPurpleLine);
            default:
                return ThemeUtils.getThemeColor(context, android.R.attr.textColorPrimary);
        }
    }

    public String getDescription(Context context) {
        switch (this) {
            case RED:
                return context.getString(R.string.route_type_red);
            case GREEN:
                return context.getString(R.string.route_type_green);
            case BLUE:
                return context.getString(R.string.route_type_blue);
            case YELLOW:
                return context.getString(R.string.route_type_yellow);
            case AIRPORT:
                return context.getString(R.string.route_type_airport);
            case RED_GYEONGGI:
                return context.getString(R.string.route_type_metropolitan);
            case GREEN_GYEONGGI:
                return context.getString(R.string.route_type_general);
            case PURPLE_GYEONGGI:
                return context.getString(R.string.route_type_purple);
            case GREEN_SUBURB:
                return context.getString(R.string.route_type_suburb);
            default:
                return context.getString(R.string.route_type_general);
        }
    }
}
