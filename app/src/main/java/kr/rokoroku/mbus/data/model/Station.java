package kr.rokoroku.mbus.data.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchStationByPosResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
import kr.rokoroku.mbus.api.seoulweb.model.RouteStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.SearchStationResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationByPositionResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.SerializeUtil;

public class Station implements Parcelable, Serializable {

    static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String city;
    private String localId;
    private Double longitude;
    private Double latitude;
    private Provider provider;

    private List<RemoteEntry> remoteEntryList;
    private List<StationRoute> stationRouteList;
    private transient Date lastUpdateTime;
    private transient Map<String, ArrivalInfo> temporalArrivalInfos;

    protected Station() {

    }

    public Station(String id, Provider provider) {
        this.id = id;
        this.provider = provider;
    }

    public Station(Station other) {
        this.id = other.id;
        this.name = other.name;
        this.city = other.city;
        this.localId = other.localId;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.provider = other.provider;
        this.remoteEntryList = other.remoteEntryList;
        this.stationRouteList = other.stationRouteList;
    }

    public Station(GbisSearchRouteResult.ResultEntity.GgEntity.StationEntity stationEntity) {
        this.id = stationEntity.getStationId();
        this.name = stationEntity.getStationNm();
        this.city = stationEntity.getGnm();
        this.localId = stationEntity.getStationNo();
        Double latitude = stationEntity.getLat();
        Double longitude = stationEntity.getLon();
        LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.provider = Provider.GYEONGGI;

        RemoteEntry localEntry = RemoteEntry.createFromCityString(city, localId);
        if (localEntry != null && !localEntry.getProvider().equals(provider)) {
            this.provider = localEntry.getProvider();
        }

        String mobileNoSi = stationEntity.getMobileNoSi();
        RemoteEntry remoteEntry = RemoteEntry.createFromCityString(city, mobileNoSi);
        if (remoteEntry != null) addRemoteEntry(remoteEntry);
    }

    public Station(GbisSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity) {
        this.id = listEntity.getStationId();
        this.name = listEntity.getStationNm();
        this.city = listEntity.getDistrictGnm();
        this.localId = listEntity.getStationNo();
        Double latitude = listEntity.getLat();
        Double longitude = listEntity.getLon();
        LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.provider = Provider.GYEONGGI;

        RemoteEntry localEntry = RemoteEntry.createFromCityString(city, localId);
        if (localEntry != null && !localEntry.getProvider().equals(provider)) {
            this.provider = localEntry.getProvider();
        }

        String stationNoSi = listEntity.getStationNoSi();
        RemoteEntry remoteEntry = RemoteEntry.createFromCityString(city, stationNoSi);
        if (remoteEntry != null) addRemoteEntry(remoteEntry);

    }

    public Station(SeoulStationInfo seoulStationInfo) {
        this.id = seoulStationInfo.getStId();
        this.name = seoulStationInfo.getStNm();
        this.city = "서울시";
        this.setLocalId(seoulStationInfo.getArsId());
        Double x = Double.valueOf(seoulStationInfo.getTmX());
        Double y = Double.valueOf(seoulStationInfo.getTmY());
        LatLng latLng = GeoUtils.convertTm(x, y);
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.provider = Provider.SEOUL;
    }

    public Station(SeoulBusRouteStation seoulBusRouteStation) {
        this.id = seoulBusRouteStation.getStationId();
        this.name = seoulBusRouteStation.getStationNm();
        this.city = "서울시";
        this.setLocalId(seoulBusRouteStation.getStationNo());
        this.latitude = Double.valueOf(seoulBusRouteStation.getGpsY());
        this.longitude = Double.valueOf(seoulBusRouteStation.getGpsX());
        this.provider = Provider.SEOUL;
    }

    public Station(StationRouteResult stationRouteResult) {
        if (stationRouteResult.result != null) {
            List<StationRoute> stationRouteList = new ArrayList<>();

            for (StationRouteResult.RouteEntity routeEntity : stationRouteResult.result) {
                if (this.getId() == null) {
                    this.setId(routeEntity.stId);
                }
                if (this.getLocalId() == null) {
                    this.setLocalId(routeEntity.arsId);
                }
                if (this.getName() == null) {
                    this.setName(routeEntity.stNm);
                }

                StationRoute stationRoute = new StationRoute(routeEntity);
                ArrivalInfo arrivalInfo = new ArrivalInfo(routeEntity);
                stationRoute.setArrivalInfo(arrivalInfo);

                // exclude non-seoul entry
                if (RouteType.checkSeoulRoute(stationRoute.getRouteType())) {
                    stationRouteList.add(stationRoute);
                }
            }

            this.setCity("서울시");
            this.setStationRouteList(stationRouteList);
            this.setProvider(Provider.SEOUL);
        }
    }

    public Station(RouteStationResult.StationEntity stationEntity) {
        this.id = stationEntity.stationId;
        this.name = stationEntity.stationNm;
        this.setLocalId(stationEntity.stationNo);
        this.city = "서울시";
        this.latitude = stationEntity.gpsY;
        this.longitude = stationEntity.gpsX;
        this.provider = Provider.SEOUL;
    }

    /**
     * Important! this constructor doesn't contain stationId.
     * Rather, it uses localId as stationId.
     *
     * @param resultEntity result entity from SeoulWebRestClient
     */
    public Station(StationByPositionResult.ResultEntity resultEntity) {
        this.id = "ars" + resultEntity.arsId;
        this.name = resultEntity.stationName;
        this.setLocalId(resultEntity.arsId);
        this.latitude = resultEntity.gpsY;
        this.longitude = resultEntity.gpsX;
        this.provider = Provider.SEOUL;
    }

    public Station(GbisSearchStationByPosResult.ResultEntity.ResultMapEntity.ListEntity listEntity) {
        this.id = listEntity.getStationId();
        this.name = listEntity.getStationNm();
        this.provider = Provider.GYEONGGI;
        this.setLocalId(listEntity.getStaNo());

        double latitude = Double.parseDouble(listEntity.getLat());
        double longitude = Double.parseDouble(listEntity.getLon());
        LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public Station(SearchStationResult.StationEntity stationEntity) {
        this.id = stationEntity.stId;
        this.name = stationEntity.stNm;
        this.setLocalId(stationEntity.arsId);
        this.provider = Provider.SEOUL;
        this.latitude = stationEntity.gpsY;
        this.longitude = stationEntity.gpsX;
        this.city = "서울시";

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        if (localId == null || "0".equals(localId) || "미정차".equals(localId)) {
            localId = null;
        }
        if (localId != null) {
            localId = localId.trim();
            if (!TextUtils.isEmpty(localId)) {
                this.localId = localId;
            }
        }
    }

    public String getLocalIdByProvider(Provider provider) {
        if (this.provider.equals(provider)) {
            return localId;
        }
        if (remoteEntryList != null) {
            for (RemoteEntry remoteEntry : remoteEntryList) {
                if (remoteEntry.getProvider().equals(provider)) return remoteEntry.getKey();
            }
        }
        return null;
    }

    public void addRemoteEntry(RemoteEntry remoteEntry) {
        if (this.provider.equals(remoteEntry.getProvider())) {
            return;
        }
        if (remoteEntryList == null) remoteEntryList = new ArrayList<>();
        if (!remoteEntryList.contains(remoteEntry)) {
            remoteEntryList.add(remoteEntry);
        }
    }

    public List<RemoteEntry> getRemoteEntries() {
        return remoteEntryList;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public ArrivalInfo getArrivalInfo(String routeId) {
        StationRoute stationRoute = getStationRoute(routeId);
        if (stationRoute != null) {
            return stationRoute.getArrivalInfo();
        } else {
            return temporalArrivalInfos != null ? temporalArrivalInfos.get(routeId) : null;
        }
    }

    public List<ArrivalInfo> getArrivalInfoList() {
        if (getStationRouteList() != null) {
            List<ArrivalInfo> arrivalInfoList = new ArrayList<>();
            for (StationRoute stationRoute : getStationRouteList()) {
                ArrivalInfo arrivalInfo = getArrivalInfo(stationRoute.getRouteId());
                arrivalInfoList.add(arrivalInfo);
            }
            return arrivalInfoList;
        }
        return null;
    }

    public void putArrivalInfo(ArrivalInfo arrivalInfo) {
        if (arrivalInfo == null || arrivalInfo.getRouteId() == null) return;

        String routeId = arrivalInfo.getRouteId();
        StationRoute stationRoute = getStationRoute(routeId);
        if (stationRoute != null) {
            stationRoute.setArrivalInfo(arrivalInfo);
        } else {
            if (temporalArrivalInfos == null) temporalArrivalInfos = new HashMap<>();
            temporalArrivalInfos.put(routeId, arrivalInfo);
        }
    }

    public void putArrivalInfos(Collection<ArrivalInfo> arrivalInfos) {
        for (ArrivalInfo arrivalInfo : arrivalInfos) {
            putArrivalInfo(arrivalInfo);
        }
    }

    public void setArrivalInfos(Collection<ArrivalInfo> arrivalInfoList) {
        this.lastUpdateTime = new Date();
        for (ArrivalInfo arrivalInfo : arrivalInfoList) {
            putArrivalInfo(arrivalInfo);
        }
    }

    public List<StationRoute> getStationRouteList() {
        return stationRouteList;
    }

    public void setStationRouteList(Collection<StationRoute> stationRouteList) {
        if (stationRouteList != null) {
            this.stationRouteList = new ArrayList<>(stationRouteList);
        } else {
            this.stationRouteList = null;
        }
    }

    public void putStationRouteList(Collection<StationRoute> stationRoutes) {
        if (stationRouteList == null) {
            setStationRouteList(stationRoutes);
        } else {
            for (StationRoute stationRoute : stationRoutes) {
                if (!stationRouteList.contains(stationRoute)) {
                    stationRouteList.add(stationRoute);
                }
            }
        }
    }

    public StationRoute getStationRoute(String routeId) {
        if (routeId != null && stationRouteList != null) {
            for (StationRoute stationRoute : stationRouteList) {
                if (routeId.equals(stationRoute.getRouteId())) {
                    return stationRoute;
                }
            }
        }
        return null;
    }

    public boolean isEveryArrivalInfoAvailable() {
        if (stationRouteList != null) {
            for (StationRoute stationRoute : stationRouteList) {
                if (stationRoute.getArrivalInfo() == null) return false;
            }
        }
        return true;
    }

    public boolean isLocalRouteInfoAvailable() {
        return stationRouteList != null;
    }

    public boolean isEveryRouteInfoAvailable() {
        if (!isLocalRouteInfoAvailable()) return false;
        if (remoteEntryList != null) {
            for (RemoteEntry remoteEntry : remoteEntryList) {
                Provider linkEntryProvider = remoteEntry.getProvider();
                if (!linkEntryProvider.equals(provider)) {
                    if (!isRemoteRouteInfoAvailable(linkEntryProvider)) return false;
                }
            }
        }
        return true;
    }

    public boolean isRemoteRouteInfoAvailable(Provider provider) {
        if (isLocalRouteInfoAvailable()) {
            for (StationRoute stationRoute : stationRouteList) {
                if (stationRoute.getProvider().equals(provider)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Station{" +
                "city='" + city + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", localId='" + localId + '\'' +
                ", longitude=" + String.format("%.6f", longitude) +
                ", latitude=" + String.format("%.6f", latitude) +
                ", provider=" + provider +
                ", remoteEntryList=" + remoteEntryList +
                ", stationRouteList=" + stationRouteList +
                ", lastUpdateTime=" + lastUpdateTime +
                ", temporalArrivalInfos=" + temporalArrivalInfos +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.city);
        dest.writeString(this.localId);
        dest.writeValue(this.longitude);
        dest.writeValue(this.latitude);
        dest.writeInt(this.provider == null ? -1 : this.provider.ordinal());
        dest.writeByte(stationRouteList == null ? (byte) 0 : (byte) 1);
        dest.writeByte(remoteEntryList == null ? (byte) 0 : (byte) 1);
        if (stationRouteList != null) dest.writeTypedList(stationRouteList);
        if (remoteEntryList != null) dest.writeTypedList(remoteEntryList);
        dest.writeLong(lastUpdateTime != null ? lastUpdateTime.getTime() : -1);
    }

    public static class RemoteEntry implements Parcelable, Serializable {
        private Provider provider;
        private String key;

        public RemoteEntry(Provider provider, String key) {
            this.provider = provider;
            this.key = key;
        }

        public Provider getProvider() {
            return provider;
        }

        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.key);
            dest.writeInt(this.provider == null ? -1 : this.provider.ordinal());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RemoteEntry remoteEntry = (RemoteEntry) o;

            if (provider != null && provider != remoteEntry.provider) return false;
            if (key != null) return key.equals(remoteEntry.key);
            return false;
        }

        protected RemoteEntry(Parcel in) {
            this.key = in.readString();
            int tmpProvider = in.readInt();
            this.provider = tmpProvider == -1 ? null : Provider.values()[tmpProvider];
        }

        public static final Creator<RemoteEntry> CREATOR = new Creator<RemoteEntry>() {
            public RemoteEntry createFromParcel(Parcel source) {
                return new RemoteEntry(source);
            }

            public RemoteEntry[] newArray(int size) {
                return new RemoteEntry[size];
            }
        };

        public static RemoteEntry createFromCityString(String city, String localId) {
            RemoteEntry remoteEntry = null;
            if (!TextUtils.isEmpty(localId)) {
                localId = localId.trim();
                if (localId.startsWith("6") || localId.startsWith("9")) {
                    if (city != null) switch (city) {
                        case "중구":
                        case "동구":
                        case "남구":
                        case "연수구":
                        case "남동구":
                        case "부평구":
                        case "계양구":
                        case "서구":
                        case "강화군":
                        case "웅진군":
                            remoteEntry = new RemoteEntry(Provider.INCHEON, localId);
                            break;

                        default:
                            remoteEntry = new RemoteEntry(Provider.GYEONGGI, localId);
                            break;
                    }
                }
            }
            return remoteEntry;
        }

        @Override
        public String toString() {
            return "RemoteEntry{" +
                    "provider=" + provider +
                    ", key='" + key + '\'' +
                    '}';
        }

        public static Serializer<RemoteEntry> SERIALIZER = new Serializer<RemoteEntry>() {
            @Override
            public void serialize(DataOutput out, RemoteEntry value) throws IOException {
                out.writeUTF(value.key);
                Provider.SERIALIZER.serialize(out, value.provider);
            }

            @Override
            public RemoteEntry deserialize(DataInput in, int available) throws IOException {
                String key = in.readUTF();
                Provider provider = Provider.SERIALIZER.deserialize(in, available);

                return new RemoteEntry(provider, key);
            }
        };
    }

    public static class DistanceComparator implements Comparator<Station> {

        private double latitude;
        private double longitude;

        public DistanceComparator(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public int compare(Station lhs, Station rhs) {
            double distLhs = getAbsDistance(lhs.getLatitude(), lhs.getLongitude());
            double distRhs = getAbsDistance(rhs.getLatitude(), rhs.getLongitude());
            return Double.compare(distLhs, distRhs);
        }

        public double getAbsDistance(double latitude, double longitude) {
            double diffLhsLat = Math.abs(this.latitude - latitude);
            double diffLhsLng = Math.abs(this.longitude - longitude);
            return diffLhsLat + diffLhsLng;
        }
    }

    protected Station(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.city = in.readString();
        this.localId = in.readString();
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        int tempProvider = in.readInt();
        this.provider = tempProvider == -1 ? null : Provider.values()[tempProvider];
        boolean isRouteListExist = in.readByte() == 1;
        boolean isLinkedEntryListExist = in.readByte() == 1;
        if (isRouteListExist) {
            this.stationRouteList = new ArrayList<>();
            this.stationRouteList = in.createTypedArrayList(StationRoute.CREATOR);
        }
        if (isLinkedEntryListExist) {
            this.remoteEntryList = new ArrayList<>();
            this.remoteEntryList = in.createTypedArrayList(RemoteEntry.CREATOR);
        }
        long tmpLastUpdateTime = in.readLong();
        this.lastUpdateTime = tmpLastUpdateTime == -1 ? null : new Date(tmpLastUpdateTime);
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    public static final Serializer<Station> SERIALIZER = new Serializer<Station>() {
        @Override
        public void serialize(DataOutput out, Station value) throws IOException {
            SerializeUtil.writeString(out, value.id);
            SerializeUtil.writeString(out, value.name);
            SerializeUtil.writeString(out, value.city);
            SerializeUtil.writeString(out, value.localId);
            SerializeUtil.writeDouble(out, value.latitude);
            SerializeUtil.writeDouble(out, value.longitude);
            Provider.SERIALIZER.serialize(out, value.provider);

            SerializeUtil.writeList(out, value.remoteEntryList, RemoteEntry.SERIALIZER);
            SerializeUtil.writeList(out, value.stationRouteList, StationRoute.SERIALIZER);
        }

        @Override
        public Station deserialize(DataInput in, int available) throws IOException {
            Station station = new Station();
            station.id = SerializeUtil.readString(in);
            station.name = SerializeUtil.readString(in);
            station.city = SerializeUtil.readString(in);
            station.localId = SerializeUtil.readString(in);
            station.latitude = SerializeUtil.readDouble(in);
            station.longitude = SerializeUtil.readDouble(in);
            station.provider = Provider.SERIALIZER.deserialize(in, available);

            station.remoteEntryList = SerializeUtil.readList(in, RemoteEntry.SERIALIZER);
            station.stationRouteList = SerializeUtil.readList(in, StationRoute.SERIALIZER);
            return station;
        }
    };
}
