package kr.rokoroku.mbus.data.model;

import android.content.Context;
import android.support.annotation.Nullable;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.util.ThemeUtils;

/**
 * Created by rok on 2015. 6. 3..
 */
@SuppressWarnings("OctalInteger")
public enum RouteType {

    // Local Area
    GREEN(00),              // 지선 버스
    GREEN_SEOUL_LOCAL(01),  // 서울 마을 버스
    GREEN_GYEONGGI(02),     // 경기 일반 버스
    GREEN_INCHEON(03),      // 인천 지선 버스
    GREEN_SUBURB(04),       // 경기 농어촌 버스

    // Intercourse
    BLUE(10),               // 간선 버스
    BLUE_INCHEON(12),       // 인천 간선 버스

    // Local Shuttle
    YELLOW(20),             // 순환 버스
    YELLOW_GYEONGGI(21),    // 경기 마을 버스
    YELLOW_INCHEON(22),     // 인천 순환 버스

    // Metropolitan & Express
    RED(30),                // 광역 버스
    RED_GYEONGGI(31),       // 경기 직행(좌석) 버스
    RED_INCHEON(32),        // 인천 광역 버스
    METROPOLITAN(33),       // 국토부 광역 급행 버스 (경기)

    // Intercity
    PURPLE(40),             // 시외 버스
    PURPLE_GYEONGGI(41),    // 경기 시외 버스

    // Airport Limousine
    AIRPORT(50),            // 공항 리무진

    // Other
    UNKNOWN(-1);

    private int value;

    RouteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static RouteType valueOf(int value) {
        for (RouteType routeType : RouteType.values()) {
            if(routeType.value == value) return routeType;
        }
        return null;
    }

    public static RouteType valueOfGbis(String type) {
        if (type == null) return UNKNOWN;
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
        if (type == null) return UNKNOWN;
        switch (type) {
            case "1":
                return AIRPORT;
            case "2":
                return GREEN_SEOUL_LOCAL;
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
        int digit = value / 10;
        switch (digit) {
            case 0:
                return ThemeUtils.getThemeColor(context, R.attr.busColorGreenLine);
            case 1:
                return ThemeUtils.getThemeColor(context, R.attr.busColorBlueLine);
            case 2:
                return ThemeUtils.getThemeColor(context, R.attr.busColorYellowLine);
            case 3:
                return ThemeUtils.getThemeColor(context, R.attr.busColorRedLine);
            case 4:
                return ThemeUtils.getThemeColor(context, R.attr.busColorPurpleLine);
            case 5:
                return ThemeUtils.getThemeColor(context, R.attr.busColorRedLine);
            default:
                return ThemeUtils.getThemeColor(context, android.R.attr.textColorPrimary);
        }
    }

    public String getDescription(Context context) {
        switch (this) {
            case RED:
            case RED_INCHEON:
                return context.getString(R.string.route_type_red);
            case GREEN:
            case GREEN_INCHEON:
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
            case GREEN_SEOUL_LOCAL:
                return context.getString(R.string.route_type_local);
            default:
                return context.getString(R.string.route_type_general);
        }
    }


    public static boolean checkSeoulRoute(RouteType routeType) {
        if (routeType != null) {
            switch (routeType) {
                case RED:
                case GREEN:
                case BLUE:
                case YELLOW:
                case AIRPORT:
                case GREEN_SEOUL_LOCAL:
                    return true;
            }
        }
        return false;
    }

    public static boolean checkIncheonRoute(RouteType routeType) {
        if (routeType != null) {
            switch (routeType) {
                case RED_INCHEON:
                case GREEN_INCHEON:
                    return true;
            }
        }
        return false;
    }

    public static final Serializer<RouteType> SERIALIZER = new Serializer<RouteType>() {
        @Override
        public void serialize(DataOutput out, RouteType value) throws IOException {
            if(value == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeInt(value.value);
            }
        }

        @Override
        public RouteType deserialize(DataInput in, int available) throws IOException {
            boolean isNull = in.readByte() == 0;
            if(!isNull) {
                int value = in.readInt();
                return RouteType.valueOf(value);
            } else {
                return null;
            }
        }
    };
}
