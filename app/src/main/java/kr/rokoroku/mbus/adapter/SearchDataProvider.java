package kr.rokoroku.mbus.adapter;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.Station;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rok on 2015. 6. 2..
 */
public class SearchDataProvider {

    private List<Route> mRouteData;
    private List<Station> mStationData;

    public SearchDataProvider() {
        mRouteData = new ArrayList<>();
        mStationData = new ArrayList<>();
    }

    public void addRouteData(Collection<Route> routes) {
        for (Route route : routes) {
            if (!mRouteData.contains(route)) {
                mRouteData.add(route);
            }
        }
        Collections.sort(mRouteData, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
    }

    public void addStationData(Collection<Station> stations) {
        for (Station station : stations) {
            if (!mStationData.contains(station)) {
                mStationData.add(station);
            }
        }
        Collections.sort(mStationData, (lhs, rhs) -> {
            int result = lhs.getProvider().compareTo(rhs.getProvider());
            result = lhs.getName().compareTo(rhs.getName());
            if(result == 0) {
                result = lhs.getLocalId().compareTo(rhs.getLocalId());
            }
            return result;
        });
    }

    public int getCount() {
        int count = mRouteData.size() + mStationData.size();
        if (hasRouteData()) count++;
        if (hasStationData()) count++;
        return count;
    }

    public Object getItem(int index) {
        if (hasRouteData()) {
            if (index-- == 0) {
                return new SectionInfo(BaseApplication.getInstance().getString(R.string.bus_route), mRouteData.size());
            } else if (index < mRouteData.size()) {
                return mRouteData.get(index);
            }
            index -= mRouteData.size();
        }

        if (hasStationData()) {
            if (index-- == 0) {
                return new SectionInfo(BaseApplication.getInstance().getString(R.string.bus_stop), mStationData.size());
            } else if (index < mStationData.size()) {
                return mStationData.get(index);
            }
        }

        return null;
    }

    public void clear() {
        mRouteData.clear();
        mStationData.clear();
    }

    public boolean hasRouteData() {
        return !mRouteData.isEmpty();
    }

    public boolean hasStationData() {
        return !mStationData.isEmpty();
    }

    public static class SectionInfo {
        public String title;
        public int count;

        public SectionInfo(String title, int count) {
            this.title = title;
            this.count = count;
        }
    }

}
