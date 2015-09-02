package kr.rokoroku.mbus.api.incheon.core;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;

import com.crashlytics.android.Crashlytics;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.GeoUtils;

/**
 * Created by rok on 15. 8. 19..
 */
public class IncheonDbHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "incheon.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ROUTE = "route";
    private static final String TABLE_STATION = "station";

    private static IncheonDbHelper instance;

    public static IncheonDbHelper getInstance() {
        if(instance == null) {
            instance = new IncheonDbHelper(BaseApplication.getInstance());
        }
        return instance;
    }

    public IncheonDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
    }

    public Route getRoute(String id) {
        String[] args = {id};
        Cursor cursor = getReadableDatabase()
                .query(TABLE_ROUTE, null, "id=?", args, null, null, null);

        if (cursor.moveToFirst()) return parseRoute(cursor);
        else return null;
    }

    public List<Route> getRoutesByName(String name) {
        String[] args = {"%" + name + "%"};
        Cursor cursor = getReadableDatabase()
                .query(TABLE_ROUTE, null, "name like ?", args, null, null, null);

        List<Route> routes = new ArrayList<>();
        if (cursor.moveToFirst()) do {
            Route route = parseRoute(cursor);
            if (route != null) routes.add(route);
        } while (cursor.moveToNext());

        return routes;
    }

    public Station getStation(String id) {
        String[] args = {id};
        Cursor cursor = getReadableDatabase()
                .query(TABLE_STATION, null, "id=?", args, null, null, null);

        if (cursor.moveToFirst()) return parseStation(cursor);
        else return null;
    }

    public Station getStationByLocalId(String localId) {
        String[] args = {localId};
        Cursor cursor = getReadableDatabase()
                .query(TABLE_STATION, null, "no=?", args, null, null, null);

        if (cursor.moveToFirst()) return parseStation(cursor);
        else return null;
    }

    public List<Station> getStationsByName(String name) {
        String[] args = {"%" + name + "%"};
        Cursor cursor = getReadableDatabase()
                .query(TABLE_ROUTE, null, "name like ?", args, null, null, null);

        List<Station> stations = new ArrayList<>();
        if (cursor.moveToFirst()) do {
            Station station = parseStation(cursor);
            if (station != null) stations.add(station);
        } while (cursor.moveToNext());

        return stations;
    }


    private Station parseStation(Cursor cursor) {
        try {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String no = cursor.getString(cursor.getColumnIndexOrThrow("no"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            Double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            Double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

            Station station = new Station(id, Provider.INCHEON);
            station.setName(name);
            station.setLocalId(no);
            station.setLatitude(latitude);
            station.setLongitude(longitude);
            return station;

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return null;
    }


    private Route parseRoute(Cursor cursor) {
        try {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String startStationId = cursor.getString(cursor.getColumnIndexOrThrow("startStationId"));
            String endStationId = cursor.getString(cursor.getColumnIndexOrThrow("endStationId"));
            String startStationName = cursor.getString(cursor.getColumnIndexOrThrow("startStationName"));
            String endStationName = cursor.getString(cursor.getColumnIndexOrThrow("endStationName"));
            String firstUpTime = cursor.getString(cursor.getColumnIndexOrThrow("firstUpTime"));
            String lastUpTime = cursor.getString(cursor.getColumnIndexOrThrow("lastUpTime"));
            String firstDownTime = cursor.getString(cursor.getColumnIndexOrThrow("firstDownTime"));
            String lastDownTime = cursor.getString(cursor.getColumnIndexOrThrow("lastDownTime"));
            String allocNormal = cursor.getString(cursor.getColumnIndexOrThrow("allocNormal"));
            String allocWeekend = cursor.getString(cursor.getColumnIndexOrThrow("allocWeekend"));
            String companyName = cursor.getString(cursor.getColumnIndexOrThrow("companyName"));
            String companyTel = cursor.getString(cursor.getColumnIndexOrThrow("companyTel"));

            Route route = new Route(id, name, Provider.INCHEON);
            route.setType(RouteType.valueOfIncheon(type));
            route.setStartStationId(startStationId);
            route.setStartStationName(startStationName);
            route.setEndStationId(endStationId);
            route.setEndStationName(endStationName);
            route.setFirstUpTime(firstUpTime);
            route.setLastUpTime(lastUpTime);
            route.setFirstDownTime(firstDownTime);
            route.setLastDownTime(lastDownTime);
            route.setAllocNormal(allocNormal);
            route.setAllocWeekend(allocWeekend);
            route.setCompanyName(companyName);
            route.setCompanyTel(companyTel);
            return route;

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return null;
    }


    public List<Station> getStationsByPos(double latitude, double longitude, int radius) {
        final PointF center = new PointF((float) latitude, (float) longitude);
        final double mult = 1.1;
        PointF p1 = GeoUtils.calculateDerivedPosition(center, mult * radius, 0);
        PointF p2 = GeoUtils.calculateDerivedPosition(center, mult * radius, 90);
        PointF p3 = GeoUtils.calculateDerivedPosition(center, mult * radius, 180);
        PointF p4 = GeoUtils.calculateDerivedPosition(center, mult * radius, 270);

        String condition = "latitude" + " > " + String.valueOf(p3.x) + " AND "
                + "latitude" + " < " + String.valueOf(p1.x) + " AND "
                + "longitude" + " < " + String.valueOf(p2.y) + " AND "
                + "longitude" + " > " + String.valueOf(p4.y);

        Cursor cursor = getReadableDatabase()
                .query(TABLE_ROUTE, null, condition, null, null, null, null);

        List<Station> stations = new ArrayList<>();
        if (cursor.moveToFirst()) do {
            Station station = parseStation(cursor);
            if (station != null) stations.add(station);
        } while (cursor.moveToNext());

        return stations;
    }
}
