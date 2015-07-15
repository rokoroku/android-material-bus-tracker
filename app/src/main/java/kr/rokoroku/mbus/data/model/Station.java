package kr.rokoroku.mbus.data.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
import kr.rokoroku.mbus.api.seoulweb.model.StationRouteResult;
import kr.rokoroku.mbus.util.GeoUtils;

public class Station implements Parcelable, Serializable {

    private String id;
    private String name;
    private String city;
    private String localId;
    private Double longitude;
    private Double latitude;
    private Provider provider;

    private List<ExternalEntry> externalEntryList;
    private List<StationRoute> stationRouteList;
    private transient Date lastUpdateTime;
    private transient Map<String, ArrivalInfo> temporalArrivalInfos ;

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
        this.externalEntryList = other.externalEntryList;
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
        if(latLng != null) {
            this.latitude = latLng.latitude;
            this.longitude = latLng.longitude;
        }
        this.provider = Provider.GYEONGGI;

        String mobileNoSi = stationEntity.getMobileNoSi();
        ExternalEntry externalEntry = ExternalEntry.createFromCityString(city, mobileNoSi);
        if (externalEntry != null) addExternalEntry(externalEntry);
    }

    public Station(GbisSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity) {
        this.id = listEntity.getStationId();
        this.name = listEntity.getStationNm();
        this.city = listEntity.getDistrictGnm();
        this.localId = listEntity.getStationNo();
        Double latitude = listEntity.getLat();
        Double longitude = listEntity.getLon();
        LatLng latLng = GeoUtils.convertEPSG3857(latitude, longitude);
        if(latLng != null) {
            this.latitude = latLng.latitude;
            this.longitude = latLng.longitude;
        }
        this.provider = Provider.GYEONGGI;

        String stationNoSi = listEntity.getStationNoSi();
        ExternalEntry externalEntry = ExternalEntry.createFromCityString(city, stationNoSi);
        if (externalEntry != null) addExternalEntry(externalEntry);

    }

    public Station(SeoulStationInfo seoulStationInfo) {
        this.id = seoulStationInfo.getStId();
        this.name = seoulStationInfo.getStNm();
        this.city = "서울시";
        String arsId = seoulStationInfo.getArsId();
        if ("0".equals(arsId)) arsId = null;
        this.localId = arsId;
        Double x = Double.valueOf(seoulStationInfo.getTmX());
        Double y = Double.valueOf(seoulStationInfo.getTmY());
        LatLng latLng = GeoUtils.convertTm(x, y);
        if(latLng != null) {
            this.latitude = latLng.latitude;
            this.longitude = latLng.longitude;
        }
        this.provider = Provider.SEOUL;
    }

    public Station(SeoulBusRouteStation seoulBusRouteStation) {
        this.id = seoulBusRouteStation.getStationId();
        this.name = seoulBusRouteStation.getStationNm();
        this.city = "서울시";
        String arsId = seoulBusRouteStation.getStationNo();
        if ("0".equals(arsId)) arsId = null;
        this.localId = arsId;
        this.latitude = Double.valueOf(seoulBusRouteStation.getGpsY());
        this.longitude = Double.valueOf(seoulBusRouteStation.getGpsX());
        this.provider = Provider.SEOUL;
    }

    public Station(StationRouteResult stationRouteResult) {
        if (stationRouteResult.result != null) {
            List<StationRoute> stationRouteList = new ArrayList<>();
            List<ArrivalInfo> arrivalInfoList = new ArrayList<>();

            for (StationRouteResult.RouteEntity routeEntity : stationRouteResult.result) {
                if (this.getId() == null) {
                    this.setId(routeEntity.stId);
                }
                if (this.getLocalId() == null && !routeEntity.arsId.equals("0")) {
                    this.setLocalId(routeEntity.arsId);
                }
                if (this.getName() == null) {
                    this.setName(routeEntity.stNm);
                }

                StationRoute stationRoute = new StationRoute(routeEntity);
                ArrivalInfo arrivalInfo = new ArrivalInfo(routeEntity);

                // exclude non-seoul entry
                if(RouteType.checkSeoulRoute(stationRoute.getRouteType())) {
                    stationRouteList.add(stationRoute);
                    arrivalInfoList.add(arrivalInfo);
                }
            }

            this.setCity("서울시");
            this.setStationRouteList(stationRouteList);
            this.setArrivalInfos(arrivalInfoList);
            this.setProvider(Provider.SEOUL);
        }
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
        this.localId = localId;
    }

    public String getLocalIdByProvider(Provider provider) {
        if (this.provider.equals(provider)) {
            return localId;
        }
        if (externalEntryList != null) {
            for (ExternalEntry externalEntry : externalEntryList) {
                if (externalEntry.getProvider().equals(provider)) return externalEntry.getKey();
            }
        }
        return null;
    }

    public void addExternalEntry(ExternalEntry externalEntry) {
        if (this.provider.equals(externalEntry.getProvider())) {
            return;
        }
        if (externalEntryList == null) externalEntryList = new ArrayList<>();
        if (!externalEntryList.contains(externalEntry)) {
            externalEntryList.add(externalEntry);
        }
    }

    public List<ExternalEntry> getExternalEntries() {
        return externalEntryList;
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

    public void putArrivalInfo(ArrivalInfo arrivalInfo) {
        StationRoute stationRoute = getStationRoute(arrivalInfo.getRouteId());
        if (stationRoute != null) {
            stationRoute.setArrivalInfo(arrivalInfo);
        } else {
            if (temporalArrivalInfos == null) temporalArrivalInfos = new TreeMap<>();
            temporalArrivalInfos.put(arrivalInfo.getRouteId(), arrivalInfo);
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
        if(stationRouteList != null) {
            this.stationRouteList = new ArrayList<>(stationRouteList);
        } else {
            this.stationRouteList = null;
        }
    }

    public void putStationRouteList(Collection<StationRoute> stationRoutes) {
        if(stationRouteList == null) {
            setStationRouteList(stationRoutes);
        } else {
            for (StationRoute stationRoute : stationRoutes) {
                if(!stationRouteList.contains(stationRoute)) {
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

    public boolean isEveryRouteInfosAvailable() {
        if(!isLocalRouteInfoAvailable()) return false;
        if(externalEntryList != null) {
            for(ExternalEntry externalEntry : externalEntryList) {
                Provider linkEntryProvider = externalEntry.getProvider();
                if(!linkEntryProvider.equals(provider)) {
                    if(!isExternalRouteInfoAvailable(linkEntryProvider)) return false;
                }
            }
        }
        return true;
    }

    public boolean isExternalRouteInfoAvailable(Provider provider) {
        if(isLocalRouteInfoAvailable()) {
            for (StationRoute stationRoute : stationRouteList) {
                if(stationRoute.getProvider().equals(provider)) return true;
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
                ", externalEntryList=" + externalEntryList +
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
        dest.writeByte(externalEntryList == null ? (byte) 0 : (byte) 1);
        if (stationRouteList != null) dest.writeTypedList(stationRouteList);
        if (externalEntryList != null) dest.writeTypedList(externalEntryList);
        dest.writeLong(lastUpdateTime != null ? lastUpdateTime.getTime() : -1);
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
            this.externalEntryList = new ArrayList<>();
            this.externalEntryList = in.createTypedArrayList(ExternalEntry.CREATOR);
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

    public static class ExternalEntry implements Parcelable, Serializable {
        private Provider provider;
        private String key;

        public ExternalEntry(Provider provider, String key) {
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

            ExternalEntry externalEntry = (ExternalEntry) o;

            if (provider != null && provider != externalEntry.provider) return false;
            if (key != null) return key.equals(externalEntry.key);
            return false;
        }

        protected ExternalEntry(Parcel in) {
            this.key = in.readString();
            int tmpProvider = in.readInt();
            this.provider = tmpProvider == -1 ? null : Provider.values()[tmpProvider];
        }

        public static final Creator<ExternalEntry> CREATOR = new Creator<ExternalEntry>() {
            public ExternalEntry createFromParcel(Parcel source) {
                return new ExternalEntry(source);
            }

            public ExternalEntry[] newArray(int size) {
                return new ExternalEntry[size];
            }
        };

        public static ExternalEntry createFromCityString(String city, String key) {
            ExternalEntry externalEntry = null;
            if (!TextUtils.isEmpty(key)) {
                key = key.trim();
                if (city != null) switch (city) {
                    case "부평구":
                    case "남동구":
                    case "계양구":
                    case "서구":
                        externalEntry = new ExternalEntry(Provider.INCHEON, key);
                        break;

                    default:
                        externalEntry = new ExternalEntry(Provider.SEOUL, key);
                        break;
                }
            }
            return externalEntry;
        }


        @Override
        public String toString() {
            return "ExternalEntry{" +
                    "provider=" + provider +
                    ", key='" + key + '\'' +
                    '}';
        }
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
}
