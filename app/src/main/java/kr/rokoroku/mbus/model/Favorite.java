package kr.rokoroku.mbus.model;

import android.os.Parcelable;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.rokoroku.mbus.util.SerializationUtil;

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


    public static Serializer<Favorite> serializer = new Serializer<Favorite>() {

        public void writeByteArray(DataOutput out, byte[] bytes) throws IOException {
            int size = bytes != null ? bytes.length : 0;

            out.writeInt(size);
            if(size > 0) {
                out.write(bytes);
            }
        }

        public byte[] readByteArray(DataInput in) throws IOException {
            int size = in.readInt();

            if(size > 0) {
                byte[] bytes = new byte[size];
                in.readFully(bytes);
                return bytes;
            } else {
                return null;
            }
        }

        @Override
        public void serialize(DataOutput out, Favorite value) throws IOException {
            out.writeUTF(value.name);
            writeByteArray(out, SerializationUtil.serialize(value.coloredRouteTable));
            writeByteArray(out, SerializationUtil.serialize(value.coloredStationTable));

            out.writeInt(value.favoriteGroups.size());
            for (FavoriteGroup favoriteGroup : value.favoriteGroups) {
                out.writeUTF(favoriteGroup.getName());
                out.writeInt(favoriteGroup.size());
                for (FavoriteGroup.FavoriteItem favoriteItem : favoriteGroup.getItems()) {
                    writeByteArray(out, SerializationUtil.serialize(favoriteItem));
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Favorite deserialize(DataInput in, int available) throws IOException {
            Favorite favorite = new Favorite(in.readUTF());

            byte[] routeBytes = readByteArray(in);
            if(routeBytes != null) favorite.coloredRouteTable = SerializationUtil.deserialize(routeBytes, HashMap.class);

            byte[] stationBytes = readByteArray(in);
            if(stationBytes != null) favorite.coloredRouteTable = SerializationUtil.deserialize(stationBytes, HashMap.class);

            int favoriteGroupSize = in.readInt();
            for(int i=0; i<favoriteGroupSize; i++) {
                String name = in.readUTF();
                int favoriteItemSize = in.readInt();

                FavoriteGroup favoriteGroup = new FavoriteGroup(name);
                for(int j=0; j<favoriteItemSize; j++) {
                    byte[] favoriteItemBytes = readByteArray(in);
                    if(favoriteItemBytes != null) {
                        FavoriteGroup.FavoriteItem favoriteItem = SerializationUtil.deserialize(favoriteItemBytes, FavoriteGroup.FavoriteItem.class);
                        favoriteGroup.add(j, favoriteItem);
                    }
                }
                favorite.addFavoriteGroup(i, favoriteGroup);
            }

            return favorite;
        }
    };
}
