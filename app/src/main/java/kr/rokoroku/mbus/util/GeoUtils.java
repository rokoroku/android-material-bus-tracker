package kr.rokoroku.mbus.util;

/**
 * Created by rok on 2015. 6. 13..
 */
public class GeoUtils {
    public static Double[] inverseMercator (Double x, Double y) {
        Double lon = (x / 20037508.34) * 180;
        Double lat = (y / 20037508.34) * 180;
        lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

        return new Double[] {lon, lat};
    }

    public static Double[] toMercator (Double longitude, Double latitude) {
        Double x = longitude * 20037508.34 / 180;
        Double y = Math.log(Math.tan((90 + latitude) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;

        return new Double[] {x, y};
    }
}
