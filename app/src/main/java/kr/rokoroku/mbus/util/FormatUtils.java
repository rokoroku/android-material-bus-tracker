package kr.rokoroku.mbus.util;

import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.model.Provider;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;
import kr.rokoroku.mbus.model.StationRoute;

/**
 * Created by rok on 2015. 7. 8..
 */
public class FormatUtils {

    public static String formatHeadingTo(Context context, Route route, StationRoute stationRoute) {

        int sequence = stationRoute.getSequence();
        if(sequence == -1) {
            List<RouteStation> routeStationList = route.getRouteStationList();
            if(routeStationList != null) {
                Provider provider = stationRoute.getProvider();
                for (RouteStation routeStation : routeStationList) {
                    String localIdByProvider = routeStation.getLocalIdByProvider(provider);
                    if(localIdByProvider != null && localIdByProvider.equals(stationRoute.getStationLocalId())) {
                        sequence = routeStation.getSequence();
                        stationRoute.setSequence(sequence);
                        break;
                    }
                }
            }
        }

        if(sequence > 0) {
            String headingStation;
            switch (route.getProvider()) {
                case SEOUL:
                    if (sequence < route.getTurnStationSeq()) {
                        headingStation = route.getEndStationName();
                    } else {
                        headingStation = route.getStartStationName();
                    }
                    break;

                case GYEONGGI:
                default:
                    if (sequence < route.getTurnStationSeq()) {
                        String turnStationName = route.getTurnStationName();
                        if (turnStationName == null) turnStationName = route.getEndStationName();
                        headingStation = turnStationName;

                    } else if (route.getEndStationName() != null
                            && route.getEndStationName().equals(route.getTurnStationName())) {
                        headingStation = route.getEndStationName();

                    } else {
                        headingStation = route.getStartStationName();
                    }
                    break;

            }
            Log.d("formatHeadingTo", String.format("%s: seq=%d, ->%s (%s <-> %s)", route.getName(), sequence, headingStation, route.getStartStationName(), route.getEndStationName()));
            return context.getString(R.string.heading_to, headingStation);

        } else {
            return null;
        }
    }

    public static String formatStationIds(Context context, Station station) {
        StringBuilder stringBuilder = new StringBuilder();
        if (station.getId() != null) {
            String entryString = String.format("%s(%s)", station.getLocalId(), station.getProvider().getCityName(context));
            stringBuilder.append(entryString);
        }

        List<Station.ExternalEntry> stationLinkedEntries = station.getExternalEntries();
        if (stationLinkedEntries != null) {
            for (Station.ExternalEntry externalEntry : stationLinkedEntries) {
                String entryString = String.format("%s(%s)", externalEntry.getKey(), externalEntry.getProvider().getCityName(context));
                stringBuilder.append(", ").append(entryString);
            }
        }

        return stringBuilder.toString();
    }

    public static String formatRegionName(Context context, Route route) {
        String regionName = null;
        List<RouteStation> routeStationList = route.getRouteStationList();
        if (routeStationList != null && !routeStationList.isEmpty()) {
            Set<String> stringSet = new LinkedHashSet<>();
            for (RouteStation routeStation : routeStationList) {
                stringSet.add(routeStation.getCity());
            }

            StringBuilder builder = null;

            String token = "시";
            if(route.getType().equals(RouteType.GREEN_SUBURB)) {
                token = "군";
            }
            Iterator<String> iterator = stringSet.iterator();
            if (iterator.hasNext()) do {
                String str = iterator.next();
                if (str.endsWith(token)) {
                    str = str.substring(0, str.lastIndexOf(token));
                    if (builder == null) {
                        builder = new StringBuilder(str);
                    } else {
                        builder.append(", ").append(str);
                    }
                }
            } while (iterator.hasNext());

            regionName = builder != null ? builder.toString() : null;

        }
        return regionName;
    }
}


