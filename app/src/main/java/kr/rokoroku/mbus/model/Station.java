package kr.rokoroku.mbus.model;

/**
 * Created by rok on 2015. 5. 31..
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteStation;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfo;
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
        this.externalEntryList = other.externalEntryList;
        this.stationRouteList = other.stationRouteList;
    }

    public Station(GbisWebSearchBusRouteResult.ResultEntity.GgEntity.StationEntity stationEntity) {
        this.id = stationEntity.getStationId();
        this.name = stationEntity.getStationNm();
        this.city = stationEntity.getGnm();
        this.localId = stationEntity.getStationNo();
        this.latitude = stationEntity.getLat();
        this.longitude = stationEntity.getLon();
        this.provider = Provider.GYEONGGI;

        String mobileNoSi = stationEntity.getMobileNoSi();
        ExternalEntry externalEntry = ExternalEntry.createFromCityString(city, mobileNoSi);
        if (externalEntry != null) addExternalEntry(externalEntry);
    }

    public Station(GbisWebSearchAllResult.ResultEntity.BusStationEntity.ListEntity listEntity) {
        this.id = listEntity.getStationId();
        this.name = listEntity.getStationNm();
        this.city = listEntity.getDistrictGnm();
        this.localId = listEntity.getStationNo();
        this.latitude = listEntity.getLat();
        this.longitude = listEntity.getLon();
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
        Double[] latlng = GeoUtils.inverseMercator(x, y);
        this.latitude = latlng[0];
        this.longitude = latlng[1];
        this.provider = Provider.SEOUL;
    }

    public Station(SeoulBusRouteStation seoulBusRouteStation) {
        this.id = seoulBusRouteStation.getStationId();
        this.name = seoulBusRouteStation.getStationNm();
        this.city = "서울시";
        String arsId = seoulBusRouteStation.getStationNo();
        if ("0".equals(arsId)) arsId = null;
        this.localId = arsId;
        Double x = Double.valueOf(seoulBusRouteStation.getGpsX());
        Double y = Double.valueOf(seoulBusRouteStation.getGpsY());
        Double[] latlng = GeoUtils.inverseMercator(x, y);
        this.latitude = latlng[0];
        this.longitude = latlng[1];
        this.provider = Provider.SEOUL;
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
            this.stationRouteList = new ArrayList<>();
            this.stationRouteList.addAll(stationRouteList);
        } else {
            this.stationRouteList = null;
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


    public boolean isEveryLinkedRouteInfoAvailable() {
        if(!isLocalRouteInfoAvailable()) return false;
        if(externalEntryList != null) {
            for(ExternalEntry externalEntry : externalEntryList) {
                Provider linkEntryProvider = externalEntry.getProvider();
                if(!linkEntryProvider.equals(provider)) {
                    if(!isLinkedRouteInfoAvailable(linkEntryProvider)) return false;
                }
            }
        }
        return true;
    }

    public boolean isLinkedRouteInfoAvailable(Provider provider) {
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
                ", longitude=" + longitude +
                ", latitude=" + latitude +
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

            if (provider != externalEntry.provider) return false;
            return key.equals(externalEntry.key);

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
}
