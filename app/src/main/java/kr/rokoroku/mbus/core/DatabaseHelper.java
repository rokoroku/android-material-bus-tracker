package kr.rokoroku.mbus.core;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;

import kr.rokoroku.mbus.model.Favorite;
import kr.rokoroku.mbus.model.FavoriteGroup;
import kr.rokoroku.mbus.model.Provider;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.SearchHistory;
import kr.rokoroku.mbus.model.Station;

/**
 * Created by rok on 2015. 6. 7..
 */
public class DatabaseHelper {

    public static final String TABLE_ROUTE_PREFIX = "route_";
    public static final String TABLE_STATION_PREFIX = "station_";
    public static final String TABLE_HIDDEN_ROUTE = "hidden_route_";
    public static final String TABLE_HIDDEN_STATION = "hidden_station_";
    public static final String TABLE_FAVORITE = "favorite";
    public static final String TABLE_SEARCH_HISTORY = "search_history";
    public static final String DEFAULT_FAVORITE_ID = "Default";

    private static final String TAG = "DBHelper";

    private static DatabaseHelper instance;

    public static void init(Context context) {
        instance = new DatabaseHelper(context);
    }

    public static DatabaseHelper getInstance() {
        return instance;
    }

    private DB db;
    private Map<Provider, BTreeMap<String, Route>> routeTable;
    private Map<Provider, BTreeMap<String, Station>> stationTable;
    private Map<Provider, Map<String, String>> secondaryStationKeyTable;

    private Map<String, Favorite> bookmarkTable;
    private Map<Provider, Set<String>> hiddenRouteStationTable;
    private Map<Provider, Set<String>> hiddenStationRouteTable;
    private Set<SearchHistory> searchHistoryTable;

    private DatabaseHelper(Context context) {
        //open (or create) database
        File file = new File(context.getCacheDir().getAbsolutePath(), "data.db");
        try {
            this.db = DBMaker
                    .fileDB(file)
                    .asyncWriteEnable()
                    .cacheHardRefEnable()
                    .make();
        } catch (Exception e) {
            Log.e(TAG, "Exception in DatabaseHelper", e);
            if(file.delete()) {
                this.db = DBMaker
                        .fileDB(file)
                        .asyncWriteEnable()
                        .cacheHardRefEnable()
                        .make();
            }
        }
        createOrLoadTables();

        //printAll();
    }

    private void createOrLoadTables() {

        this.routeTable = new HashMap<>();
        this.stationTable = new HashMap<>();
        this.secondaryStationKeyTable = new HashMap<>();
        this.hiddenRouteStationTable = new HashMap<>();
        this.hiddenStationRouteTable = new HashMap<>();

        for (Provider provider : Provider.values()) {
            BTreeMap<String, Route> routeBTreeMap = db.treeMap(TABLE_ROUTE_PREFIX + provider.getCityCode());
            BTreeMap<String, Station> stationBTreeMap = db.treeMap(TABLE_STATION_PREFIX + provider.getCityCode());
            Map<String, String> secondaryKeyTable = createSecondaryKeyTable(stationBTreeMap);

            NavigableSet<String> hiddenStationSet = db.treeSet(TABLE_HIDDEN_STATION + provider.getCityCode());
            NavigableSet<String> hiddenRouteSet = db.treeSet(TABLE_HIDDEN_ROUTE + provider.getCityCode());

            this.routeTable.put(provider, routeBTreeMap);
            this.stationTable.put(provider, stationBTreeMap);
            this.secondaryStationKeyTable.put(provider, secondaryKeyTable);
            this.hiddenRouteStationTable.put(provider, hiddenStationSet);
            this.hiddenStationRouteTable.put(provider, hiddenRouteSet);
        }

        this.searchHistoryTable = db.hashSetCreate(TABLE_SEARCH_HISTORY)
                .serializer(SearchHistory.serializer)
                .expireMaxSize(5)
                .makeOrGet();

        this.bookmarkTable = db.treeMapCreate(TABLE_FAVORITE)
                .keySerializer(BTreeKeySerializer.STRING)
                .valueSerializer(Favorite.serializer)
                .makeOrGet();

        if (bookmarkTable.get(DEFAULT_FAVORITE_ID) == null) {
            bookmarkTable.put(DEFAULT_FAVORITE_ID, new Favorite(DEFAULT_FAVORITE_ID));
        }

    }

    private Map<String, String> createSecondaryKeyTable(Bind.MapWithModificationListener<String, Station> primary) {

        // stores value hash from primary map
        Map<String, String> secondary = new HashMap<>();

        // bind secondary to primary so it contains secondary key
        Bind.secondaryKey(primary, secondary, (key, value) -> {
            return value.getLocalId();
        });

        return secondary;
    }

    public synchronized void clear() {
        for (Provider provider : Provider.values()) {
            db.delete(TABLE_ROUTE_PREFIX + provider.getCityCode());
            db.delete(TABLE_STATION_PREFIX + provider.getCityCode());
            db.delete(TABLE_HIDDEN_STATION + provider.getCityCode());
            db.delete(TABLE_HIDDEN_ROUTE + provider.getCityCode());
            db.delete(TABLE_FAVORITE);
        }
        db.commit();
        createOrLoadTables();
    }

    public Route getRoute(Provider provider, String routeId) {
        Map<String, Route> routeMap = routeTable.get(provider);
        if (routeMap != null) return routeMap.get(routeId);
        else return null;
    }

    public Route getRandomRoute() {

        Random random = new Random();
        for (BTreeMap<String, Route> stringRouteBTreeMap : routeTable.values()) {
            Object[] list = stringRouteBTreeMap.values().toArray();
            if (list.length > 1) {
                return (Route) list[random.nextInt(list.length - 1)];
            }
        }
        return null;
    }

    public synchronized void putRoute(Provider provider, Route route) {
        Map<String, Route> routeMap = routeTable.get(provider);
        routeMap.put(route.getId(), route);
    }

    public Station getStation(Provider provider, String stationId) {
        Map<String, Station> stationMap = stationTable.get(provider);
        if (stationMap != null) return stationMap.get(stationId);
        else return null;
    }

    public Station getStationWithSecondaryKey(Provider provider, String localId) {
        Map<String, Station> primaryMap = stationTable.get(provider);
        Map<String, String> secondaryMap = secondaryStationKeyTable.get(provider);
        String stationId = secondaryMap.get(localId);
        if (stationId != null) return primaryMap.get(stationId);
        else return null;
    }

    public void putStation(Station station) {
        putStation(station.getProvider(), station);

        List<Station.ExternalEntry> externalEntries = station.getExternalEntries();
        if (externalEntries != null && !externalEntries.isEmpty()) {
            for (Station.ExternalEntry externalEntry : externalEntries) {
                Station externalStation = getStationWithSecondaryKey(externalEntry.getProvider(), externalEntry.getKey());

                if (externalStation != null) {
                    Station newStation = new Station(externalStation);
                    newStation.addExternalEntry(new Station.ExternalEntry(station.getProvider(), station.getLocalId()));
                    newStation.setStationRouteList(station.getStationRouteList());
                    putStation(newStation.getProvider(), newStation);
                }
            }
        }
    }

    public synchronized void putStation(Provider provider, Station station) {
        Log.d("DBHelper", "putStation: " + provider.getCityName() + "/" + station.toString());
        Map<String, Station> stationMap = stationTable.get(provider);
        stationMap.put(station.getId(), station);
    }

    public synchronized void putStations(Provider provider, Collection<Station> stations) {
        Map<String, Station> stationMap = stationTable.get(provider);
        for (Station station : stations) {
            String stationId = station.getId();
            if (!stationMap.containsKey(stationId)) {
                stationMap.put(stationId, station);
            }
        }
    }

    public Favorite getBookmark(String id) {
        return bookmarkTable.get(id);
    }

    public synchronized void putBookmark(String id, Favorite favorite) {
        bookmarkTable.put(id, favorite);
    }

    public Set<SearchHistory> getSearchHistoryTable() {
        return searchHistoryTable;
    }

    AsyncTask<Void, Void, Void> commitTask;

    public synchronized void commitAsync() {
        if (commitTask == null) {
            commitTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void[] params) {
                    synchronized (DatabaseHelper.this) {
                        try {
                            db.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    commitTask = null;
                }
            };
            commitTask.execute();
        }
    }

    public synchronized void rollback() {
        db.rollback();
    }

    private void printAll() {

        for (Favorite favorite : bookmarkTable.values()) {
            Log.d(TAG, "favorite: " + favorite.toString());
            for (FavoriteGroup favoriteGroup : favorite.getFavoriteGroups()) {
                Log.d(TAG, "favoriteGroup: "+ favoriteGroup.toString());
                for (int i=0; i<favoriteGroup.size(); i++) {
                    FavoriteGroup.FavoriteItem favoriteItem =favoriteGroup.get(i);
                    Log.d(TAG, "favoriteGroupItem: "+ favoriteItem.toString());
                }
            }
            for (Map<String, Integer> stringIntegerMap : favorite.getColoredRouteTable().values()) {
                for (Map.Entry<String, Integer> stringIntegerEntry : stringIntegerMap.entrySet()) {
                    Log.d(TAG, "favoriteColorRoute: " + stringIntegerEntry.getKey() + "/" + stringIntegerEntry.getValue());
                }
            }
            for (Map<String, Integer> stringIntegerMap : favorite.getColoredStationTable().values()) {
                for (Map.Entry<String, Integer> stringIntegerEntry : stringIntegerMap.entrySet()) {
                    Log.d(TAG, "favoriteColorStation:" + stringIntegerEntry.getKey() + "/" + stringIntegerEntry.getValue());
                }
            }
        }
        for (BTreeMap<String, Route> stringRouteBTreeMap : routeTable.values()) {
            for (Route route : stringRouteBTreeMap.values()) {
                Log.d(TAG, "route: " + route.toString());
            }
        }
        for (BTreeMap<String, Station> stringRouteBTreeMap : stationTable.values()) {
            for (Station route : stringRouteBTreeMap.values()) {
                Log.d(TAG, "station: " + route.toString());
            }
        }
        for (SearchHistory searchHistory : searchHistoryTable) {
            Log.d(TAG, "searchHistory: " + searchHistory.toString());
        }

    }
}
