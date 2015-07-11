package kr.rokoroku.mbus.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rok on 2015. 6. 13..
 */
public class GeoUtils {

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;

    public static Double[] inverseMercator(Double x, Double y) {
        Double lon = (x / 20037508.34) * 180;
        Double lat = (y / 20037508.34) * 180;
        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

        return new Double[]{lon, lat};
    }

    public static Double[] toMercator(Double longitude, Double latitude) {
        Double x = longitude * 20037508.34 / 180;
        Double y = Math.log(Math.tan((90 + latitude) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;

        return new Double[]{x, y};
    }

    public static Angle getIso6709LongitudeAngle(double latitude) {
        //ISO6709 경도 표현
        //도·분·초 그리고 소숫점 이하의 초 : DDMMSS.SS
        int degree = (int) (latitude / 10000);
        int minute = (int) ((latitude / 100) % 100);
        double second = latitude % 100;

        return new Angle(degree, minute, second);
    }

    public static Angle getIso6709LatitudeAngle(double latitude) {
        //ISO6709 위도 표현
        //도·분·초 그리고 소숫점 이하의 초 : DDDMMSS.SS
        int degree = (int) (latitude / 1000);
        int minute = (int) ((latitude / 100) % 100);
        double second = latitude % 100;

        return new Angle(degree, minute, second);
    }

    public static LatLng convertTm(Double x, Double y) {
        GeoTrans.Point inPoint = new GeoTrans.Point(x, y);
        GeoTrans.Point outPoint = GeoTrans.convert(GeoTrans.TM, GeoTrans.GEO, inPoint);
        return new LatLng(outPoint.y - 0.00268, outPoint.x - 0.00074);
    }

    public static LatLng convertKATECH(Double latitude, Double longitude) {
        GeoTrans.Point inPoint = new GeoTrans.Point(longitude, latitude);
        GeoTrans.Point outPoint = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, inPoint);
        return new LatLng(outPoint.y, outPoint.x);
    }

    public static LatLng convertEPSG3857(Double latitude, Double longitude) {
        Double[] geoUnit = inverseMercator(longitude, latitude);
        LatLng latLng = new LatLng(geoUnit[1], geoUnit[0]);
        return latLng;
    }

    public static LatLng convertIso6709(Double latitude, Double longitude) {
        Angle angleLat = getIso6709LatitudeAngle(latitude);
        Angle angleLng = getIso6709LongitudeAngle(longitude);
        return new LatLng(angleLat.toDecimal(), angleLng.toDecimal());
    }

    public static int calculateDistanceInMeter(LatLng from, LatLng to) {

        double latDistance = Math.toRadians(from.latitude - to.latitude);
        double lngDistance = Math.toRadians(from.longitude - to.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c * 1000));
    }


    public static class Angle {
        int degree;
        int minute;
        double second;

        public Angle(int degree, int minute, double second) {
            this.degree = degree;
            this.minute = minute;
            this.second = second;
        }

        public double toDecimal() {
            return degree + (double) (minute / 60) + (second / 3600);
        }
    }

}
