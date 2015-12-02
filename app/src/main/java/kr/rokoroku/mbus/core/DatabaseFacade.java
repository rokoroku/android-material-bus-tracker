package kr.rokoroku.mbus.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.SearchHistory;
import kr.rokoroku.mbus.data.model.Station;

/**
 * Created by rok on 2015. 6. 7..
 */
public class DatabaseFacade {

    private static final String TAG = "DBFacade";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_FILENAME_USER = "user_data.db";
    private static final String DATABASE_FILENAME_STATIC = "static_data.db";

    public static final String TABLE_ROUTE_PREFIX = "route_";
    public static final String TABLE_STATION_PREFIX = "station_";
    public static final String TABLE_HIDDEN_ROUTE = "hidden_route_";
    public static final String TABLE_HIDDEN_STATION = "hidden_station_";
    public static final String TABLE_FAVORITE = "favorite";
    public static final String TABLE_SEARCH_HISTORY = "search_history";
    public static final String DEFAULT_FAVORITE_ID = "Default";

    private static DatabaseFacade instance;
    private static WeakReference<Context> contextWeakReference;

    public static void init(Context context) {
        contextWeakReference = new WeakReference<>(context);
        instance = new DatabaseFacade();
    }

    public static DatabaseFacade getInstance() {
        return instance;
    }

    private DB userDB;
    private DB dataDB;
    private boolean isDataDbChanged = false;
    private boolean isUserDbChanged = false;
    private Map<Provider, BTreeMap<String, Route>> routeTable;
    private Map<Provider, BTreeMap<String, Station>> stationTable;
    private Map<Provider, Map<String, String>> secondaryStationKeyTable;

    private Map<String, Favorite> bookmarkTable;
    private Map<Provider, Set<String>> hiddenRouteStationTable;
    private Map<Provider, Set<String>> hiddenStationRouteTable;
    private Set<SearchHistory> searchHistoryTable;

    private Map<String, Station> temporalStationCache = new HashMap<>();

    @SuppressLint("CommitPrefEdits")
    private DatabaseFacade() {

        SharedPreferences preferences = BaseApplication.getSharedPreferences();
        int dbVersion = preferences.getInt(BaseApplication.PREFERENCE_DB_VERSION, -1);

        if (dbVersion == -1 || dbVersion == DATABASE_VERSION) try {
            //open (or create) database
            createOrLoadFile();
            createOrLoadTables();

            preferences.edit()
                    .putInt(BaseApplication.PREFERENCE_DB_VERSION, DATABASE_VERSION)
                    .apply();
            //printAll();

        } catch (Exception e) {
            Log.e(TAG, "Exception in DatabaseFacade", e);
//
//            boolean crashedBefore = preferences.getBoolean(BaseApplication.PREFERENCE_DB_CRASHED_BEFORE, false);
//            if (crashedBefore) {
//                recreateFile();
//                preferences.edit()
//                        .putBoolean(BaseApplication.PREFERENCE_DB_CRASHED_BEFORE, false)
//                        .commit();
//
//            } else {
//                preferences.edit()
//                        .putBoolean(BaseApplication.PREFERENCE_DB_CRASHED_BEFORE, true)
//                        .commit();
//
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }
        }
    }

    private synchronized void recreateFile() {
        Context context = contextWeakReference.get();
        if (context == null) context = BaseApplication.getInstance();
        File userfile = new File(context.getCacheDir().getAbsolutePath(), DATABASE_FILENAME_USER);
        File datafile = new File(context.getCacheDir().getAbsolutePath(), DATABASE_FILENAME_STATIC);

        if (userfile.exists()) {
            userfile.delete();
        }
        if (datafile.exists()) {
            datafile.delete();
        }
        createOrLoadFile();
        createOrLoadTables();

        BaseApplication.getSharedPreferences().edit()
                .putInt(BaseApplication.PREFERENCE_DB_VERSION, DATABASE_VERSION)
                .apply();
    }

    private synchronized void createOrLoadFile() {

        Context context = contextWeakReference.get();
        if (context == null) context = BaseApplication.getInstance();
        File userfile = new File(context.getCacheDir().getAbsolutePath(), DATABASE_FILENAME_USER);
        File datafile = new File(context.getCacheDir().getAbsolutePath(), DATABASE_FILENAME_STATIC);
        try {
            this.userDB = DBMaker
                    .fileDB(userfile)
                    .asyncWriteEnable()
                    .cacheHardRefEnable()
                    .closeOnJvmShutdown()
                    .make();
            this.dataDB = DBMaker
                    .fileDB(datafile)
                    .asyncWriteEnable()
                    .cacheHardRefEnable()
                    .closeOnJvmShutdown()
                    .make();
        } catch (Exception e) {
            Log.e(TAG, "Exception in DatabaseFacade", e);
            recreateFile();
        }
    }

    private synchronized void createOrLoadTables() {

        this.routeTable = new HashMap<>();
        this.stationTable = new HashMap<>();
        this.secondaryStationKeyTable = new HashMap<>();
        this.hiddenRouteStationTable = new HashMap<>();
        this.hiddenStationRouteTable = new HashMap<>();

        for (Provider provider : Provider.values()) {
            BTreeMap<String, Route> routeBTreeMap = dataDB.treeMapCreate(TABLE_ROUTE_PREFIX + provider.getCityCode())
                    .keySerializer(BTreeKeySerializer.STRING)
                    .valueSerializer(Route.SERIALIZER)
                    .makeOrGet();
            BTreeMap<String, Station> stationBTreeMap = dataDB.treeMapCreate(TABLE_STATION_PREFIX + provider.getCityCode())
                    .keySerializer(BTreeKeySerializer.STRING)
                    .valueSerializer(Station.SERIALIZER)
                    .makeOrGet();
            Map<String, String> secondaryKeyTable = createSecondaryKeyTable(stationBTreeMap);

            NavigableSet<String> hiddenStationSet = userDB.treeSet(TABLE_HIDDEN_STATION + provider.getCityCode());
            NavigableSet<String> hiddenRouteSet = userDB.treeSet(TABLE_HIDDEN_ROUTE + provider.getCityCode());

            this.routeTable.put(provider, routeBTreeMap);
            this.stationTable.put(provider, stationBTreeMap);
            this.secondaryStationKeyTable.put(provider, secondaryKeyTable);
            this.hiddenRouteStationTable.put(provider, hiddenStationSet);
            this.hiddenStationRouteTable.put(provider, hiddenRouteSet);
        }

        this.searchHistoryTable = userDB.hashSetCreate(TABLE_SEARCH_HISTORY)
                .serializer(SearchHistory.SERIALIZER)
                .expireMaxSize(10)
                .makeOrGet();

        this.bookmarkTable = userDB.treeMapCreate(TABLE_FAVORITE)
                .keySerializer(BTreeKeySerializer.STRING)
                .valueSerializer(Favorite.SERIALIZER)
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
            dataDB.delete(TABLE_ROUTE_PREFIX + provider.getCityCode());
            dataDB.delete(TABLE_STATION_PREFIX + provider.getCityCode());
            userDB.delete(TABLE_HIDDEN_STATION + provider.getCityCode());
            userDB.delete(TABLE_HIDDEN_ROUTE + provider.getCityCode());
            userDB.delete(TABLE_FAVORITE);
        }
        dataDB.commit();
        userDB.commit();
        createOrLoadTables();
    }

    public synchronized Route getRoute(Provider provider, String routeId) {
        Map<String, Route> routeMap = routeTable.get(provider);
        if (routeMap != null) return routeMap.get(routeId);
        else return null;
    }

    public synchronized Route getRandomRoute() {
        Random random = new Random();
        for (BTreeMap<String, Route> stringRouteBTreeMap : routeTable.values()) {
            Object[] list = stringRouteBTreeMap.values().toArray();
            if (list.length > 1) {
                return (Route) list[random.nextInt(list.length - 1)];
            }
        }
        return null;
    }

    public synchronized void putRoute(Route route) {
        putRoute(route.getProvider(), route);
    }

    public synchronized void putRoute(Provider provider, Route route) {
        Map<String, Route> routeMap = routeTable.get(provider);
        final String key = route.getId();
        if (key != null) {
            routeMap.put(key, route);
            isDataDbChanged = true;
        }
    }

    public synchronized void putRoutes(Provider provider, Collection<Route> routes) {
        Map<String, Route> routeMap = routeTable.get(provider);
        for (Route route : routes) {
            String routeId = route.getId();
            if (!routeMap.containsKey(routeId)) {
                routeMap.put(routeId, route);
            }
        }
    }

    public synchronized Station getStation(Provider provider, String stationId) {
        Map<String, Station> stationMap = stationTable.get(provider);
        if (stationMap != null) return stationMap.get(stationId);
        else return temporalStationCache.get(stationId);
    }

    public synchronized Station getStationWithSecondaryKey(Provider provider, String localId) {
        Map<String, Station> primaryMap = stationTable.get(provider);
        Map<String, String> secondaryMap = secondaryStationKeyTable.get(provider);
        String stationId = secondaryMap.get(localId);
        if (stationId != null) return primaryMap.get(stationId);
        else return null;
    }

    public Station getStationFromTemporalCache(String stationId) {
        return temporalStationCache.get(stationId);
    }

    public void putStationsToTemporalCache(Collection<Station> stations) {
        for (Station station : stations) {
            if (station.getId() != null) {
                temporalStationCache.put(station.getId(), station);
            }
        }
    }

    public void putStationToTemporalCache(Station station) {
        if (station.getId() != null) {
            temporalStationCache.put(station.getId(), station);
        }
    }

    public synchronized void putStationForEachProvider(Station station) {
        putStation(station.getProvider(), station);

        List<Station.RemoteEntry> externalEntries = station.getRemoteEntries();
        if (externalEntries != null && !externalEntries.isEmpty()) {
            for (Station.RemoteEntry remoteEntry : externalEntries) {
                Station externalStation = getStationWithSecondaryKey(remoteEntry.getProvider(), remoteEntry.getKey());

                if (externalStation != null) {
                    Station newStation = new Station(externalStation);
                    newStation.addRemoteEntry(new Station.RemoteEntry(station.getProvider(), station.getLocalId()));
                    newStation.setStationRouteList(station.getStationRouteList());
                    putStation(newStation.getProvider(), newStation);
                }
            }
        }
    }

    public synchronized void putStationsForEachProvider(Collection<Station> station) {
        for (Station entry : station) {
            putStationForEachProvider(entry);
        }
    }

    public synchronized void putStation(Provider provider, Station station) {
        Map<String, Station> stationMap = stationTable.get(provider);
        final String key = station.getId();
        if (key != null) {
            stationMap.put(key, station);
            isDataDbChanged = true;
        }
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

    public synchronized Favorite getBookmark(String id) {
        return bookmarkTable.get(id);
    }

    public synchronized void putBookmark(String id, Favorite favorite) {
        bookmarkTable.put(id, favorite);
        isUserDbChanged = true;
    }

    public Set<SearchHistory> getSearchHistoryTable() {
        return searchHistoryTable;
    }

    AsyncTask<Void, Void, Boolean> commitTask;

    public synchronized void commitAsync() {
        if (commitTask == null) {
            commitTask = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void[] params) {
                    boolean success = true;
                    synchronized (DatabaseFacade.this) {

                        if (isDataDbChanged) try {
                            dataDB.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }
                        if (isUserDbChanged) try {
                            userDB.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                            success = false;
                        }
                        isUserDbChanged = false;
                        isDataDbChanged = false;
                    }
                    return success;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (!result) {
                        reopen();
                    }
                    commitTask = null;
                }
            };
            commitTask.execute();
        }
    }

    private synchronized void reopen() {
        Log.d("DatabaseFacade", "reopened");
        userDB.close();
        dataDB.close();
        createOrLoadFile();
        createOrLoadTables();
    }

    public synchronized void rollback() {
        userDB.rollback();
    }

    private void printAll() {

        for (Favorite favorite : bookmarkTable.values()) {
            Log.d(TAG, "favorite: " + favorite.toString());
            for (FavoriteGroup favoriteGroup : favorite.getFavoriteGroups()) {
                Log.d(TAG, "favoriteGroup: " + favoriteGroup.toString());
                for (int i = 0; i < favoriteGroup.size(); i++) {
                    FavoriteItem favoriteItem = favoriteGroup.get(i);
                    Log.d(TAG, "favoriteGroupItem: " + favoriteItem.toString());
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
