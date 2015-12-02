package kr.rokoroku.mbus.core;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.Nullable;

import java.util.List;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.ThemeUtils;

/**
 * Created by rok on 2015. 6. 23..
 */
public class FavoriteFacade {

    public enum Color {
        WHITE(0, R.attr.cardColorDefault),
        RED(1, R.attr.cardColorAccent1),
        BLUE(2, R.attr.cardColorAccent2),
        GREEN(3, R.attr.cardColorAccent3),
        YELLOW(4, R.attr.cardColorAccent4),
        PURPLE(5, R.attr.cardColorAccent5);

        private int index;

        @AttrRes
        private int colorAttr;

        Color(int index, int colorAttr) {
            this.index = index;
            this.colorAttr = colorAttr;
        }

        public int getColorAttr() {
            return colorAttr;
        }

        public int getIndex() {
            return index;
        }

        public int getColor(Context context) {
            return ThemeUtils.getThemeColor(context, colorAttr);
        }

        @Nullable
        public static Color valueOf(int key) {
            for (Color color : Color.values()) {
                if (color.index == key) return color;
            }
            return null;
        }
    }

    private static FavoriteFacade instance;

    public static FavoriteFacade getInstance() {
        if (instance == null) instance = new FavoriteFacade();
        return instance;
    }

    private FavoriteFacade() {

    }

    private Favorite currentFavorite;

    public void setFavoriteRouteColor(Provider provider, String routeId, Color color) {
        getCurrentFavorite().setRouteColor(provider, routeId, color.getIndex());
    }

    public void setFavoriteStationColor(Provider provider, String stationId, Color color) {
        getCurrentFavorite().setStationColor(provider, stationId, color.getIndex());
    }


    public Color getFavoriteRouteColor(Route route) {
        return getFavoriteRouteColor(route.getProvider(), route.getId());
    }

    public Color getFavoriteRouteColor(Provider provider, String routeId) {
        Integer colorIndex = getCurrentFavorite().getRouteColor(provider, routeId);
        if (colorIndex == null) colorIndex = 0;
        return Color.valueOf(colorIndex);
    }

    public Color getFavoriteStationColor(Station station) {
        return getFavoriteStationColor(station.getProvider(), station.getId());
    }

    public Color getFavoriteStationColor(Provider provider, String stationId) {
        Integer colorIndex = getCurrentFavorite().getStationColor(provider, stationId);
        if (colorIndex == null) colorIndex = 0;
        return Color.valueOf(colorIndex);
    }

    public Favorite getCurrentFavorite() {
        if (currentFavorite == null) {
            Favorite storedFavorite = DatabaseFacade.getInstance().getBookmark(DatabaseFacade.DEFAULT_FAVORITE_ID);
            currentFavorite = new Favorite(storedFavorite);
        }
        return currentFavorite;
    }

    public FavoriteGroup getDefaultFavoriteGroup() {
        FavoriteGroup defaultGroup = null;
        List<FavoriteGroup> favoriteGroups = getCurrentFavorite().getFavoriteGroups();
        String defaultGroupName = BaseApplication.getInstance().getString(R.string.favorite_default_group);
        for (FavoriteGroup favoriteGroup : favoriteGroups) {
            if (defaultGroupName.equals(favoriteGroup.getName())) {
                defaultGroup = favoriteGroup;
                break;
            }
        }
        if (defaultGroup == null) {
            defaultGroup = new FavoriteGroup(defaultGroupName);
            favoriteGroups.add(defaultGroup);
        }
        return defaultGroup;
    }

    public FavoriteGroup addToFavorite(FavoriteGroup favoriteGroup, Route route, RouteStation routeStation) {
        if (favoriteGroup == null) favoriteGroup = getDefaultFavoriteGroup();
        FavoriteItem item = new FavoriteItem(route);
        if (routeStation != null) item.setExtraData(routeStation);

        favoriteGroup.add(item);
        DatabaseFacade.getInstance().putBookmark(currentFavorite.getName(), currentFavorite);
        return favoriteGroup;
    }


    public FavoriteGroup addToFavorite(FavoriteGroup favoriteGroup, Station station, StationRoute stationRoute) {
        if (favoriteGroup == null) favoriteGroup = getDefaultFavoriteGroup();
        FavoriteItem item = new FavoriteItem(station);
        if (stationRoute != null) item.setExtraData(stationRoute);

        favoriteGroup.add(item);
        DatabaseFacade.getInstance().putBookmark(currentFavorite.getName(), currentFavorite);
        return favoriteGroup;
    }

    public boolean isAdded(Route route) {
        Favorite bookmark = DatabaseFacade.getInstance().getBookmark(currentFavorite.getName());
        if (bookmark != null && route != null) {
            for (FavoriteGroup favoriteGroup : bookmark.getFavoriteGroups()) {
                for (FavoriteItem favoriteItem : favoriteGroup.getItems()) {
                    if (favoriteItem.contains(route)) return true;
                }
            }
        }
        return false;
    }

    public boolean isAdded(Station station) {
        Favorite bookmark = DatabaseFacade.getInstance().getBookmark(currentFavorite.getName());
        if (bookmark != null && station != null) {
            for (FavoriteGroup favoriteGroup : bookmark.getFavoriteGroups()) {
                for (FavoriteItem favoriteItem : favoriteGroup.getItems()) {
                    if (favoriteItem.contains(station)) return true;
                }
            }
        }
        return false;
    }
}
