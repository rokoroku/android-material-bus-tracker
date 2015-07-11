package kr.rokoroku.mbus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchAllResult;
import kr.rokoroku.mbus.api.gbisweb.model.GbisWebSearchBusRouteResult;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusRouteInfo;
import kr.rokoroku.mbus.util.FormatUtils;
import kr.rokoroku.mbus.util.TimeUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Route implements Parcelable, Serializable {

    private String id;
    private String name;
    private RouteType type;
    private String startStationId;
    private String endStationId;
    private String startStationName;
    private String endStationName;
    private String turnStationName;
    private int turnStationSeq = -1;
    private String turnStationId;
    private String firstUpTime;
    private String lastUpTime;
    private String firstDownTime;
    private String lastDownTime;
    private String allocNormal;
    private String allocWeekend;
    private String regionName;
    private String companyName;
    private String companyTel;
    private String garageName;
    private String garageTel;
    private District district;
    private Provider provider;
    private List<RouteStation> routeStationList;

    private transient List<BusLocation> busLocationList;
    private transient Date lastUpdateTime;
    private transient List<MapLine> mapLineList;

    public Route() {

    }

    public Route(String id, String name, Provider provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;

        if (provider != null) {
            switch (provider) {
                case SEOUL:
                    this.companyName = "서울대중교통";
                    this.companyTel = "1577-0287";
                    this.regionName = "서울";
                    break;

                case GYEONGGI:
                    break;
            }
        }
    }

    public Route(SeoulBusRouteInfo seoulBusRouteInfo) {
        setSeoulBusInfo(seoulBusRouteInfo);
    }

    public Route(GbisWebSearchAllResult.ResultEntity.BusRouteEntity.ListEntity entity) {
        this.id = entity.getRouteId();
        this.name = entity.getRouteNm();
        this.type = RouteType.valueOfGbis(entity.getRouteTp());
        this.regionName = entity.getRouteRegion();
        this.district = District.valueOfGbis(entity.getSiFlag());
        this.provider = Provider.GYEONGGI;
    }

    public Route(GbisWebSearchBusRouteResult.ResultEntity result) {
        setGbisRouteEntity(result);
        setGbisStationEntity(result);
        setGbisRealtimeBusEntity(result);
    }

    public void setSeoulBusInfo(SeoulBusRouteInfo seoulBusRouteInfo) {
        this.id = seoulBusRouteInfo.getBusRouteId();
        this.name = seoulBusRouteInfo.getBusRouteNm();
        this.type = RouteType.valueOfTopis(seoulBusRouteInfo.getRouteType());
        this.allocNormal = seoulBusRouteInfo.getTerm();
        try {
            this.firstUpTime = TimeUtils.getGbisDateFormat().format(TimeUtils.getSeoulBusDateFormat().parse(seoulBusRouteInfo.getFirstBusTm()));
            this.lastUpTime = TimeUtils.getGbisDateFormat().format(TimeUtils.getSeoulBusDateFormat().parse(seoulBusRouteInfo.getLastBusTm()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.startStationName = seoulBusRouteInfo.getStStationNm();
        this.endStationName = seoulBusRouteInfo.getEdStationNm();
        this.companyName = "서울대중교통";
        this.companyTel = "1577-0287";
        this.regionName = "서울";
        this.district = District.SEOUL;
        this.provider = Provider.SEOUL;
    }

    public void setGbisRouteEntity(GbisWebSearchBusRouteResult.ResultEntity resultEntity) {
        GbisWebSearchBusRouteResult.ResultEntity.GgEntity ggEntity = resultEntity.getGg();
        GbisWebSearchBusRouteResult.ResultEntity.GgEntity.RouteEntity routeEntity = ggEntity.getRoute();

        this.id = routeEntity.getRouteId();
        this.name = routeEntity.getRouteNm();
        this.type = RouteType.valueOfGbis(ggEntity.getRouteTypeCd());
        this.allocNormal = routeEntity.getNpeekAlloc();
        if (!"0".equals(routeEntity.getNpeekAlloc2()) && !routeEntity.getNpeekAlloc().equals(routeEntity.getNpeekAlloc2())) {
            this.allocNormal += "~" + routeEntity.getNpeekAlloc2();
        }
        this.allocWeekend = routeEntity.getPeekAlloc();
        if (!"0".equals(routeEntity.getPeekAlloc2()) && !routeEntity.getPeekAlloc().equals(routeEntity.getPeekAlloc2())) {
            this.allocWeekend += "~" + routeEntity.getPeekAlloc2();
        }
        this.firstUpTime = routeEntity.getUpFirstTime();
        this.lastUpTime = routeEntity.getUpLastTime();
        this.firstDownTime = routeEntity.getDownFirstTime();
        this.lastDownTime = routeEntity.getDownLastTime();
        this.companyName = routeEntity.getCompanyNm();
        this.companyTel = routeEntity.getCompanyTel();
        this.garageName = routeEntity.getGarageNm();
        this.garageTel = routeEntity.getGarageTel();
        this.startStationName = routeEntity.getStStaNm();
        this.endStationName = routeEntity.getEdStaNm();
        this.turnStationSeq = Integer.parseInt(routeEntity.getTurnSeq());
        this.district = District.GYEONGGI;
        this.provider = Provider.GYEONGGI;
    }

    public void setGbisStationEntity(GbisWebSearchBusRouteResult.ResultEntity resultEntity) {
        GbisWebSearchBusRouteResult.ResultEntity.GgEntity ggEntity = resultEntity.getGg();

        this.routeStationList = new ArrayList<>();
        int stationSequence = 1;
        for (GbisWebSearchBusRouteResult.ResultEntity.GgEntity.StationEntity stationEntity : ggEntity.getUp().getList()) {
            RouteStation routeStation = new RouteStation(stationEntity, id, stationSequence++);
            routeStation.setDirection(Direction.UP);
            routeStationList.add(routeStation);
            if (turnStationSeq == routeStation.getSequence()) {
                    turnStationId = routeStation.getId();
            }
        }
        if (turnStationSeq == -1) {
            turnStationSeq = stationSequence;
        }

        for (GbisWebSearchBusRouteResult.ResultEntity.GgEntity.StationEntity stationEntity : ggEntity.getDown().getList()) {
            RouteStation routeStation = new RouteStation(stationEntity, id, stationSequence++);
            routeStation.setDirection(Direction.DOWN);
            routeStationList.add(routeStation);
            if (turnStationSeq == routeStation.getSequence()) {
                turnStationId = routeStation.getId();
            }
        }
    }

    public void setGbisRealtimeBusEntity(GbisWebSearchBusRouteResult.ResultEntity resultEntity) {
        int index = 0;
        for (GbisWebSearchBusRouteResult.ResultEntity.RealTimeEntity.BusEntity busEntity : resultEntity.getRealTime().getList()) {
            BusLocation busLocation = new BusLocation(busEntity);
            busLocation.setRouteId(this.id);
            busLocation.setType(this.type);
            for (index = 0; index < routeStationList.size(); index++) {
                RouteStation routeStation = routeStationList.get(index);
                if (routeStation.getId().equals(busLocation.getCurrentStationId())) {
                    if (busLocationList == null) busLocationList = new ArrayList<>();
                    busLocation.setStationSeq(routeStation.getSequence());
                    busLocationList.add(busLocation);
                    break;
                }
            }
        }
    }

    public boolean isRouteBaseInfoAvailable() {
        return companyName != null && allocNormal != null;
    }

    public boolean isRouteStationInfoAvailable() {
        return routeStationList != null;
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

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public String getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(String startStationId) {
        this.startStationId = startStationId;
    }

    public String getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(String endStationId) {
        this.endStationId = endStationId;
    }

    public String getStartStationName() {
        if (startStationName == null && routeStationList != null) {
            startStationName = routeStationList.get(0).getName();
        }
        return startStationName;
    }

    public void setStartStationName(String startStationName) {
        this.startStationName = startStationName;
    }

    public String getEndStationName() {
        if (endStationName == null && routeStationList != null) {
            endStationName = routeStationList.get(routeStationList.size() - 1).getName();
        }
        return endStationName;
    }

    public String getTurnStationName() {
        if (turnStationName == null && routeStationList != null && getTurnStationSeq() < routeStationList.size()) {
            RouteStation routeStation = routeStationList.get(getTurnStationSeq());
            if (routeStation != null) turnStationName = routeStation.getName();
        }
        return turnStationName;
    }

    public void setTurnStationName(String turnStationName) {
        this.turnStationName = turnStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
    }

    public String getGarageName() {
        return garageName;
    }

    public void setGarageName(String garageName) {
        this.garageName = garageName;
    }

    public String getGarageTel() {
        return garageTel;
    }

    public void setGarageTel(String garageTel) {
        this.garageTel = garageTel;
    }

    public String getFirstUpTime() {
        return firstUpTime;
    }

    public void setFirstUpTime(String firstUpTime) {
        this.firstUpTime = firstUpTime;
    }

    public String getLastUpTime() {
        return lastUpTime;
    }

    public void setLastUpTime(String lastUpTime) {
        this.lastUpTime = lastUpTime;
    }

    public String getFirstDownTime() {
        return firstDownTime;
    }

    public void setFirstDownTime(String firstDownTime) {
        this.firstDownTime = firstDownTime;
    }

    public String getLastDownTime() {
        return lastDownTime;
    }

    public void setLastDownTime(String lastDownTime) {
        this.lastDownTime = lastDownTime;
    }

    public String getAllocNormal() {
        return allocNormal;
    }

    public void setAllocNormal(String allocNormal) {
        this.allocNormal = allocNormal;
    }

    public String getAllocWeekend() {
        return allocWeekend;
    }

    public void setAllocWeekend(String allocWeekend) {
        this.allocWeekend = allocWeekend;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyTel() {
        return companyTel;
    }

    public void setCompanyTel(String companyTel) {
        this.companyTel = companyTel;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<RouteStation> getRouteStationList() {
        return routeStationList;
    }

    public void setRouteStationList(List<RouteStation> routeStationList) {
        this.routeStationList = routeStationList;
        this.lastUpdateTime = new Date();
    }

    public List<BusLocation> getBusLocationList() {
        return busLocationList;
    }

    public void setBusLocationList(List<BusLocation> busLocationList) {
        this.busLocationList = busLocationList;
        this.lastUpdateTime = new Date();
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getTurnStationId() {
        return turnStationId;
    }

    public void setTurnStationId(String turnStationId) {
        this.turnStationId = turnStationId;
    }

    public int getTurnStationSeq() {
        return turnStationSeq;
    }

    public void setTurnStationSeq(int turnStationSeq) {
        this.turnStationSeq = turnStationSeq;
    }

    public List<MapLine> getMapLineList() {
        return mapLineList;
    }

    public void setMapLineList(List<MapLine> mapLineList) {
        this.mapLineList = mapLineList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return id.equals(route.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Route{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", startStationId='" + startStationId + '\'' +
                ", endStationId='" + endStationId + '\'' +
                ", startStationName='" + startStationName + '\'' +
                ", endStationName='" + endStationName + '\'' +
                ", firstUpTime='" + firstUpTime + '\'' +
                ", lastUpTime='" + lastUpTime + '\'' +
                ", firstDownTime='" + firstDownTime + '\'' +
                ", lastDownTime='" + lastDownTime + '\'' +
                ", allocNormal='" + allocNormal + '\'' +
                ", allocWeekend='" + allocWeekend + '\'' +
                ", regionName='" + regionName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyTel='" + companyTel + '\'' +
                ", garageName='" + garageName + '\'' +
                ", garageTel='" + garageTel + '\'' +
                ", district=" + district +
                ", provider=" + provider +
                ", routeStationList=" + routeStationList +
                ", busLocationList=" + busLocationList +
                ", lastUpdateTime=" + lastUpdateTime +
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
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.startStationId);
        dest.writeString(this.endStationId);
        dest.writeString(this.startStationName);
        dest.writeString(this.turnStationName);
        dest.writeString(this.endStationName);
        dest.writeString(this.firstUpTime);
        dest.writeString(this.lastUpTime);
        dest.writeString(this.firstDownTime);
        dest.writeString(this.lastDownTime);
        dest.writeString(this.allocNormal);
        dest.writeString(this.allocWeekend);
        dest.writeString(this.regionName);
        dest.writeString(this.companyName);
        dest.writeString(this.companyTel);
        dest.writeString(this.garageName);
        dest.writeString(this.garageTel);
        dest.writeInt(this.district == null ? -1 : this.district.ordinal());
        dest.writeInt(this.provider == null ? -1 : this.provider.ordinal());
        dest.writeLong(lastUpdateTime != null ? lastUpdateTime.getTime() : -1);
        dest.writeByte(routeStationList != null ? (byte) 1 : (byte) 0);
        dest.writeByte(busLocationList != null ? (byte) 1 : (byte) 0);
        if (routeStationList != null) {
            dest.writeTypedList(routeStationList);
        }
        if (busLocationList != null) {
            dest.writeTypedList(busLocationList);
        }
    }

    protected Route(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : RouteType.values()[tmpType];
        this.startStationId = in.readString();
        this.endStationId = in.readString();
        this.startStationName = in.readString();
        this.turnStationName = in.readString();
        this.endStationName = in.readString();
        this.firstUpTime = in.readString();
        this.lastUpTime = in.readString();
        this.firstDownTime = in.readString();
        this.lastDownTime = in.readString();
        this.allocNormal = in.readString();
        this.allocWeekend = in.readString();
        this.regionName = in.readString();
        this.companyName = in.readString();
        this.companyTel = in.readString();
        this.garageName = in.readString();
        this.garageTel = in.readString();
        int tmpDistrict = in.readInt();
        this.district = tmpDistrict == -1 ? null : District.values()[tmpDistrict];
        int tmpProvider = in.readInt();
        this.provider = tmpProvider == -1 ? null : Provider.values()[tmpProvider];
        long tmpLastUpdateAt = in.readLong();
        this.lastUpdateTime = tmpLastUpdateAt == -1 ? null : new Date(tmpLastUpdateAt);
        boolean readRouteStationList = in.readByte() == 1;
        boolean readBusPositionList = in.readByte() == 1;
        if (readRouteStationList) {
            routeStationList = new ArrayList<>();
            in.readTypedList(routeStationList, RouteStation.CREATOR);
        }
        if (readBusPositionList) {
            busLocationList = new ArrayList<>();
            in.readTypedList(busLocationList, BusLocation.CREATOR);
        }
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };


}
