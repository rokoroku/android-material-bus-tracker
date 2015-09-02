package kr.rokoroku.mbus.data.model;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.rokoroku.mbus.util.SerializeUtil;

/**
 * Created by rok on 2015. 6. 8..
 */
public class Favorite implements Serializable {

    private String name;
    private Map<Provider, Map<String, Integer>> coloredStationTable;
    private Map<Provider, Map<String, Integer>> coloredRouteTable;
    private List<FavoriteGroup> favoriteGroups;

    public Favorite(String name) {
        this.name = name;
        this.coloredStationTable = new HashMap<>();
        this.coloredRouteTable = new HashMap<>();
        this.favoriteGroups = new ArrayList<>();
    }

    public Favorite(Favorite another) {
        this.name = another.name;
        this.coloredRouteTable = new HashMap<>(another.coloredRouteTable);
        this.coloredStationTable = new HashMap<>(another.coloredStationTable);
        this.favoriteGroups = new ArrayList<>();
        for (FavoriteGroup favoriteGroup : another.favoriteGroups) {
            FavoriteGroup group = new FavoriteGroup(favoriteGroup.getName(), favoriteGroup.getItems());
            this.favoriteGroups.add(group);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Provider, Map<String, Integer>> getColoredRouteTable() {
        return coloredRouteTable;
    }

    public Map<Provider, Map<String, Integer>> getColoredStationTable() {
        return coloredStationTable;
    }

    public Integer getStationColor(Provider provider, String stationId) {
        Map<String, Integer> stringIntegerPair = coloredStationTable.get(provider);
        if (stringIntegerPair != null) return stringIntegerPair.get(stationId);
        else return null;
    }

    public Integer getRouteColor(Provider provider, String routeId) {
        Map<String, Integer> stringIntegerPair = coloredRouteTable.get(provider);
        if (stringIntegerPair != null) return stringIntegerPair.get(routeId);
        else return null;
    }

    public void setStationColor(Provider provider, String stationId, Integer color) {
        Map<String, Integer> stationTable = coloredStationTable.get(provider);
        if (color != null) {
            if (stationTable == null) {
                stationTable = new HashMap<>();
                coloredStationTable.put(provider, stationTable);
            }
            stationTable.put(stationId, color);

        } else if (stationTable != null) {
            stationTable.remove(stationId);
            if (stationTable.size() == 0) coloredRouteTable.remove(provider);
        }
    }

    public void setRouteColor(Provider provider, String routeId, Integer color) {
        Map<String, Integer> routeTable = coloredRouteTable.get(provider);
        if (color != null) {
            if (routeTable == null) {
                routeTable = new HashMap<>();
                coloredRouteTable.put(provider, routeTable);
            }
            routeTable.put(routeId, color);

        } else if (routeTable != null) {
            routeTable.remove(routeId);
            if (routeTable.size() == 0) coloredRouteTable.remove(provider);
        }
    }

    public List<FavoriteGroup> getFavoriteGroups() {
        return favoriteGroups;
    }

    public void addFavoriteGroup(int position, FavoriteGroup favoriteGroup) {
        favoriteGroups.add(position, favoriteGroup);
    }

    public boolean removeFavoriteGroup(FavoriteGroup favoriteGroup) {
        return favoriteGroups.remove(favoriteGroup);
    }

    public FavoriteGroup removeFavoriteGroup(int position) {
        return favoriteGroups.remove(position);
    }


    public static Serializer<Favorite> SERIALIZER = new Serializer<Favorite>() {

        private Serializer<Map<String, Integer>> stringIntegerMapSerializer = new Serializer<Map<String, Integer>>() {
            @Override
            public void serialize(DataOutput out, Map<String, Integer> value) throws IOException {
                SerializeUtil.writeMap(out, value, Serializer.STRING, Serializer.INTEGER);
            }

            @Override
            public Map<String, Integer> deserialize(DataInput in, int available) throws IOException {
                return SerializeUtil.readMap(in, Serializer.STRING, Serializer.INTEGER);
            }
        };

        @Override
        public void serialize(DataOutput out, Favorite value) throws IOException {
            out.writeUTF(value.name);
            SerializeUtil.writeMap(out, value.coloredRouteTable, Provider.SERIALIZER, stringIntegerMapSerializer);
            SerializeUtil.writeMap(out, value.coloredStationTable, Provider.SERIALIZER, stringIntegerMapSerializer);
            SerializeUtil.writeList(out, value.favoriteGroups, FavoriteGroup.SERIALIZER);
        }

        @Override
        public Favorite deserialize(DataInput in, int available) throws IOException {
            Favorite favorite = new Favorite(in.readUTF());
            favorite.coloredRouteTable = SerializeUtil.readMap(in, Provider.SERIALIZER, stringIntegerMapSerializer);
            favorite.coloredStationTable = SerializeUtil.readMap(in, Provider.SERIALIZER, stringIntegerMapSerializer);
            favorite.favoriteGroups = SerializeUtil.readList(in, FavoriteGroup.SERIALIZER);
            return favorite;
        }
    };
}
