package kr.rokoroku.mbus.api.seoul.model;

import org.simpleframework.xml.Element;

/**
 * Created by rok on 2015. 6. 9..
 */
public class SeoulBusArrival {
    /**
     * neus2 : 403
     * brdrde_Num1 : 0
     * neus1 : 391
     * mkTm : 2015-06-09 13:32:25.0
     * vehId2 : 0
     * brdrde_Num2 : 0
     * vehId1 : 4300
     * dir : 답십리
     * nmain3Sec2 : 2619
     * arsId : 12198
     * nmain3Sec1 : 2536
     * rtNm : 720
     * busType1 : 1
     * busType2 : 1
     * namin2Sec1 : 795
     * namin2Sec2 : 852
     * isArrive1 : 1
     * isArrive2 : 0
     * goal2 : 10641
     * sectOrd2 : 0
     * goal1 : 10592
     * nmainSec1 : 636
     * sectOrd1 : 6
     * firstTm : 20150609040900
     * nmainSec2 : 693
     * repTm1 : 2015-06-09 13:32:18.0
     * stNm : 은평경찰서
     * nstnId1 : 36948
     * nstnId2 : 36950
     * exps2 : 453
     * exps1 : 440
     * rerdie_Div2 : 1
     * rerdie_Div1 : 1
     * plainNo2 : 서울74사7891
     * nstnOrd2 : 6
     * plainNo1 : 서울74사7891
     * nmain3Stnid2 : 33824
     * traTime1 : 391
     * nmain3Stnid1 : 33824
     * traTime2 : 403
     * avgCf1 : .95
     * nmainStnid2 : 3067
     * avgCf2 : .95
     * nmainStnid1 : 3067
     * neuCf1 : .95
     * nstnSec1 : 86
     * neuCf2 : .95
     * reride_Num1 : 0
     * nmainOrd1 : 16
     * full1 : 0
     * full2 : 0
     * isLast1 : 0
     * isLast2 : -1
     * term : 7
     * nmainOrd2 : 16
     * nstnOrd1 : 7
     * nstnSpd1 : 18
     * routeType : 3
     * stationNm2 : 구파발역3번출구
     * nstnSpd2 : 15
     * stationNm1 : 진관동주민센터
     * kals2 : 414
     * kals1 : 405
     * kalCf2 : 1.01
     * nmain3Ord2 : 34
     * kalCf1 : 1.01
     * nmain3Ord1 : 34
     * brerde_Div2 : 1
     * stId : 3059
     * brerde_Div1 : 1
     * staOrd : 12
     * reride_Num2 : 0
     * nmain2Ord2 : 17
     * traSpd1 : 16
     * nstnSec2 : 57
     * nmain2Ord1 : 17
     * lastTm : 20150609232200
     * traSpd2 : 18
     * expCf1 : 1.01
     * expCf2 : 1.01
     * nmain2Stnid2 : 36043
     * busRouteId : 3072000
     * nmain2Stnid1 : 36043
     */

    @Element
    private String stId;        // 정류소 ID
    @Element
    private String stNm;        // 정류소명
    @Element
    private String arsId;        // 정류소 고유번호
    @Element
    private String busRouteId;        // 노선ID
    @Element
    private String rtNm;        // 노선명
    @Element(required = false)
    private String firstTm;        // 첫차시간
    @Element(required = false)
    private String lastTm;        // 막차시간
    @Element(required = false)
    private String term;        // 배차간격 (분)
    @Element(required = false)
    private String routeType;        // 노선유형 (1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
    @Element(required = false)
    private String nextBus;        // 막차운행여부 (N:막차아님, Y:막차)
    @Element(required = false)
    private String staOrd;        // 요청정류소순번
    @Element(required = false)
    private String dir;        // 노선명
    @Element(required = false)
    private String mkTm;        // 노선명
    @Element(required = false)
    private String vehId1;        // 첫번째도착예정버스ID
    @Element(required = false)
    private String plainNo1;        // 첫번째도착예정차량번호
    @Element(required = false)
    private String sectOrd1;        // 첫번째도착예정버스의 현재구간 순번
    @Element(required = false)
    private String stationNm1;        // 첫번째도착예정버스의 최종 정류소명
    @Element(required = false)
    private String traTime1;        // 첫번째도착예정버스의 여행시간 (분)
    @Element(required = false)
    private String traSpd1;        // 첫번째도착예정버스의 여행속도 (Km/h)
    @Element(required = false)
    private String isArrive1;        // 첫번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착)
    @Element(required = false)
    private String repTm1;        // 첫번째도착예정버스의 최종 보고 시간
    @Element(required = false)
    private String isLast1;        // 첫번째도착예정버스의 막차여부 (0:막차아님, 1:막차)
    @Element(required = false)
    private String busType1;        // 첫번째도착예정버스의 차량유형 (0:일반버스, 1:저상버스, 2:굴절버스)
    @Element(required = false)
    private String avgCf1;        // 첫번째 도착예정 버스의 이동평균 보정계수
    @Element(required = false)
    private String expCf1;        // 첫번째 도착예정 버스의 지수평활 보정계수
    @Element(required = false)
    private String kalCf1;        // 첫번째 도착예정 버스의 기타1평균 보정계수
    @Element(required = false)
    private String neuCf1;        // 첫번째 도착예정 버스의 기타2평균 보정계수
    @Element(required = false)
    private String exps1;        // 첫번째 도착예정 버스의 지수평활 도착예정시간(초)
    @Element(required = false)
    private String kals1;        // 첫번째 도착예정 버스의 기타1 도착예정시간(초)
    @Element(required = false)
    private String neus1;        // 첫번째 도착예정 버스의 기타2 도착예정시간(초)
    @Element(required = false)
    private String rerdie_Div1;        // 첫번째 도착예정 버스의 버스내부 제공용 현재 재차 구분
    @Element(required = false)
    private String reride_Num1;        // 첫번째 도착예정 버스의 버스내부 제공용 현재 재차 인원
    @Element(required = false)
    private String brerde_Div1;        // 첫번째 도착예정 버스의 버스내부 제공용 현재 뒷차 구분
    @Element(required = false)
    private String brdrde_Num1;        // 첫번째 도착예정 버스의 버스내부 제공용 현재 뒷차 인원
    @Element(required = false)
    private String full1;        // 첫번째 도착예정 버스의 만차여부
    @Element(required = false)
    private String nstnId1;        // 첫번째 도착예정 버스의 다음정류소 ID
    @Element(required = false)
    private String nstnOrd1;        // 첫번째 도착예정 버스의다음 정류소 순번
    @Element(required = false)
    private String nstnSpd1;        // 첫번째 도착예정 버스의 다음 정류소 예정여행시간
    @Element(required = false)
    private String nstnSec1;        // 첫번째 도착예정 버스의 다음 정류소 예정여행시간
    @Element(required = false)
    private String nmainStnid1;        // 첫번째 도착예정 버스의 1번째 주요정류소 ID
    @Element(required = false)
    private String nmainOrd1;        // 첫번째 도착예정 버스의 1번째 주요정류소 순번
    @Element(required = false)
    private String nmainSec1;        // 첫번째 도착예정 버스의 1번째 주요정류소 예정여행시간
    @Element(required = false)
    private String nmain2Stnid1;        // 첫번째 도착예정 버스의 2번째 주요정류소 ID
    @Element(required = false)
    private String nmain2Ord1;        // 첫번째 도착예정 버스의 2번째 주요정류소 순번
    @Element(required = false)
    private String namin2Sec1;        // 첫번째 도착예정 버스의 2번째 주요정류소 예정여행시간
    @Element(required = false)
    private String nmain3Stnid1;        // 첫번째 도착예정 버스의 3번째 주요정류소 ID
    @Element(required = false)
    private String nmain3Ord1;        // 첫번째 도착예정 버스의 3번째 주요정류소 순번
    @Element(required = false)
    private String nmain3Sec1;        // 첫번째 도착예정 버스의 3번째 주요정류소 예정여행시간
    @Element(required = false)
    private String goal1;        // 첫번째 도착예정 버스의 종점 도착예정시간(초)
    @Element(required = false)
    private String vehId2;        // 두번째 도착예정버스ID
    @Element(required = false)
    private String plainNo2;        // 두번째도착예정차량번호
    @Element(required = false)
    private String sectOrd2;        // 두번째도착예정버스의 현재구간 순번
    @Element(required = false)
    private String stationNm2;        // 두번째도착예정버스의 최종 정류소명
    @Element(required = false)
    private String traTime2;        // 두번째도착예정버스의 여행시간
    @Element(required = false)
    private String traSpd2;        // 두번째도착예정버스의 여행속도
    @Element(required = false)
    private String isArrive2;        // 두번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착)
    @Element(required = false)
    private String repTm2;        // 두번째도착예정버스의 최종 보고 시간
    @Element(required = false)
    private String isLast2;        // 두번째도착예정버스의 막차여부 (0:막차아님, 1:막차)
    @Element(required = false)
    private String busType2;        // 두번째도착예정버스의 차량유형 (0:일반버스, 1:저상버스, 2:굴절버스)
    @Element(required = false)
    private String avgCf2;        // 두번째 도착예정 버스의 이동평균 보정계수
    @Element(required = false)
    private String expCf2;        // 두번째 도착예정 버스의 지수평활 보정계수
    @Element(required = false)
    private String kalCf2;        // 두번째 도착예정 버스의 기타1평균 보정계수
    @Element(required = false)
    private String neuCf2;        // 두번째 도착예정 버스의 기타2평균 보정계수
    @Element(required = false)
    private String exps2;        // 두번째 도착예정 버스의 지수평활 도착예정시간(초)
    @Element(required = false)
    private String kals2;        // 두번째 도착예정 버스의 기타1 도착예정시간(초)
    @Element(required = false)
    private String neus2;        // 두번째 도착예정 버스의 기타2 도착예정시간(초)
    @Element(required = false)
    private String rerdie_Div2;        // 두번째 도착예정 버스의 버스내부 제공용 현재 재차 구분
    @Element(required = false)
    private String reride_Num2;        // 두번째 도착예정 버스의 버스내부 제공용 현재 재차 인원
    @Element(required = false)
    private String brerde_Div2;        // 두번째 도착예정 버스의 버스내부 제공용 현재 뒷차 구분
    @Element(required = false)
    private String brdrde_Num2;        // 두번째 도착예정 버스의 버스내부 제공용 현재 뒷차 인원
    @Element(required = false)
    private String full2;        // 두번째 도착예정 버스의 만차여부
    @Element(required = false)
    private String nstnId2;        // 두번째 도착예정 버스의 다음정류소 ID
    @Element(required = false)
    private String nstnOrd2;        // 두번째 도착예정 버스의다음 정류소 순번
    @Element(required = false)
    private String nstnSpd2;        // 두번째 도착예정 버스의 다음 정류소 예정여행시간
    @Element(required = false)
    private String nstnSec2;        // 두번째 도착예정 버스의 다음 정류소 예정여행시간
    @Element(required = false)
    private String nmainStnid2;        // 두번째 도착예정 버스의 1번째 주요정류소 ID
    @Element(required = false)
    private String nmainOrd2;        // 두번째 도착예정 버스의 1번째 주요정류소 순번
    @Element(required = false)
    private String nmainSec2;        // 두번째 도착예정 버스의 1번째 주요정류소 예정여행시간
    @Element(required = false)
    private String nmain2Stnid2;        // 두번째 도착예정 버스의 2번째 주요정류소 ID
    @Element(required = false)
    private String nmain2Ord2;        // 두번째 도착예정 버스의 2번째 주요정류소 순번
    @Element(required = false)
    private String namin2Sec2;        // 두번째 도착예정 버스의 2번째 주요정류소 예정여행시간
    @Element(required = false)
    private String nmain3Stnid2;        // 두번째 도착예정 버스의 3번째 주요정류소 ID
    @Element(required = false)
    private String nmain3Ord2;        // 두번째 도착예정 버스의 3번째 주요정류소 순번
    @Element(required = false)
    private String nmain3Sec2;        // 두번째 도착예정 버스의 3번째 주요정류소 예정여행시간
    @Element(required = false)
    private String goal2;        // 두번째 도착예정 버스의 종점 도착예정시간(초)

    public String getArsId() {
        return arsId;
    }

    public void setArsId(String arsId) {
        this.arsId = arsId;
    }

    public String getAvgCf1() {
        return avgCf1;
    }

    public void setAvgCf1(String avgCf1) {
        this.avgCf1 = avgCf1;
    }

    public String getAvgCf2() {
        return avgCf2;
    }

    public void setAvgCf2(String avgCf2) {
        this.avgCf2 = avgCf2;
    }

    public String getBrdrde_Num1() {
        return brdrde_Num1;
    }

    public void setBrdrde_Num1(String brdrde_Num1) {
        this.brdrde_Num1 = brdrde_Num1;
    }

    public String getBrdrde_Num2() {
        return brdrde_Num2;
    }

    public void setBrdrde_Num2(String brdrde_Num2) {
        this.brdrde_Num2 = brdrde_Num2;
    }

    public String getBrerde_Div1() {
        return brerde_Div1;
    }

    public void setBrerde_Div1(String brerde_Div1) {
        this.brerde_Div1 = brerde_Div1;
    }

    public String getBrerde_Div2() {
        return brerde_Div2;
    }

    public void setBrerde_Div2(String brerde_Div2) {
        this.brerde_Div2 = brerde_Div2;
    }

    public String getBusRouteId() {
        return busRouteId;
    }

    public void setBusRouteId(String busRouteId) {
        this.busRouteId = busRouteId;
    }

    public String getBusType1() {
        return busType1;
    }

    public void setBusType1(String busType1) {
        this.busType1 = busType1;
    }

    public String getBusType2() {
        return busType2;
    }

    public void setBusType2(String busType2) {
        this.busType2 = busType2;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getExpCf1() {
        return expCf1;
    }

    public void setExpCf1(String expCf1) {
        this.expCf1 = expCf1;
    }

    public String getExpCf2() {
        return expCf2;
    }

    public void setExpCf2(String expCf2) {
        this.expCf2 = expCf2;
    }

    public String getExps1() {
        return exps1;
    }

    public void setExps1(String exps1) {
        this.exps1 = exps1;
    }

    public String getExps2() {
        return exps2;
    }

    public void setExps2(String exps2) {
        this.exps2 = exps2;
    }

    public String getFirstTm() {
        return firstTm;
    }

    public void setFirstTm(String firstTm) {
        this.firstTm = firstTm;
    }

    public String getFull1() {
        return full1;
    }

    public void setFull1(String full1) {
        this.full1 = full1;
    }

    public String getFull2() {
        return full2;
    }

    public void setFull2(String full2) {
        this.full2 = full2;
    }

    public String getGoal1() {
        return goal1;
    }

    public void setGoal1(String goal1) {
        this.goal1 = goal1;
    }

    public String getGoal2() {
        return goal2;
    }

    public void setGoal2(String goal2) {
        this.goal2 = goal2;
    }

    public String getIsArrive1() {
        return isArrive1;
    }

    public void setIsArrive1(String isArrive1) {
        this.isArrive1 = isArrive1;
    }

    public String getIsArrive2() {
        return isArrive2;
    }

    public void setIsArrive2(String isArrive2) {
        this.isArrive2 = isArrive2;
    }

    public String getIsLast1() {
        return isLast1;
    }

    public void setIsLast1(String isLast1) {
        this.isLast1 = isLast1;
    }

    public String getIsLast2() {
        return isLast2;
    }

    public void setIsLast2(String isLast2) {
        this.isLast2 = isLast2;
    }

    public String getKalCf1() {
        return kalCf1;
    }

    public void setKalCf1(String kalCf1) {
        this.kalCf1 = kalCf1;
    }

    public String getKalCf2() {
        return kalCf2;
    }

    public void setKalCf2(String kalCf2) {
        this.kalCf2 = kalCf2;
    }

    public String getKals1() {
        return kals1;
    }

    public void setKals1(String kals1) {
        this.kals1 = kals1;
    }

    public String getKals2() {
        return kals2;
    }

    public void setKals2(String kals2) {
        this.kals2 = kals2;
    }

    public String getLastTm() {
        return lastTm;
    }

    public void setLastTm(String lastTm) {
        this.lastTm = lastTm;
    }

    public String getMkTm() {
        return mkTm;
    }

    public void setMkTm(String mkTm) {
        this.mkTm = mkTm;
    }

    public String getNamin2Sec1() {
        return namin2Sec1;
    }

    public void setNamin2Sec1(String namin2Sec1) {
        this.namin2Sec1 = namin2Sec1;
    }

    public String getNamin2Sec2() {
        return namin2Sec2;
    }

    public void setNamin2Sec2(String namin2Sec2) {
        this.namin2Sec2 = namin2Sec2;
    }

    public String getNeuCf1() {
        return neuCf1;
    }

    public void setNeuCf1(String neuCf1) {
        this.neuCf1 = neuCf1;
    }

    public String getNeuCf2() {
        return neuCf2;
    }

    public void setNeuCf2(String neuCf2) {
        this.neuCf2 = neuCf2;
    }

    public String getNeus1() {
        return neus1;
    }

    public void setNeus1(String neus1) {
        this.neus1 = neus1;
    }

    public String getNeus2() {
        return neus2;
    }

    public void setNeus2(String neus2) {
        this.neus2 = neus2;
    }

    public String getNextBus() {
        return nextBus;
    }

    public void setNextBus(String nextBus) {
        this.nextBus = nextBus;
    }

    public String getNmain2Ord1() {
        return nmain2Ord1;
    }

    public void setNmain2Ord1(String nmain2Ord1) {
        this.nmain2Ord1 = nmain2Ord1;
    }

    public String getNmain2Ord2() {
        return nmain2Ord2;
    }

    public void setNmain2Ord2(String nmain2Ord2) {
        this.nmain2Ord2 = nmain2Ord2;
    }

    public String getNmain2Stnid1() {
        return nmain2Stnid1;
    }

    public void setNmain2Stnid1(String nmain2Stnid1) {
        this.nmain2Stnid1 = nmain2Stnid1;
    }

    public String getNmain2Stnid2() {
        return nmain2Stnid2;
    }

    public void setNmain2Stnid2(String nmain2Stnid2) {
        this.nmain2Stnid2 = nmain2Stnid2;
    }

    public String getNmain3Ord1() {
        return nmain3Ord1;
    }

    public void setNmain3Ord1(String nmain3Ord1) {
        this.nmain3Ord1 = nmain3Ord1;
    }

    public String getNmain3Ord2() {
        return nmain3Ord2;
    }

    public void setNmain3Ord2(String nmain3Ord2) {
        this.nmain3Ord2 = nmain3Ord2;
    }

    public String getNmain3Sec1() {
        return nmain3Sec1;
    }

    public void setNmain3Sec1(String nmain3Sec1) {
        this.nmain3Sec1 = nmain3Sec1;
    }

    public String getNmain3Sec2() {
        return nmain3Sec2;
    }

    public void setNmain3Sec2(String nmain3Sec2) {
        this.nmain3Sec2 = nmain3Sec2;
    }

    public String getNmain3Stnid1() {
        return nmain3Stnid1;
    }

    public void setNmain3Stnid1(String nmain3Stnid1) {
        this.nmain3Stnid1 = nmain3Stnid1;
    }

    public String getNmain3Stnid2() {
        return nmain3Stnid2;
    }

    public void setNmain3Stnid2(String nmain3Stnid2) {
        this.nmain3Stnid2 = nmain3Stnid2;
    }

    public String getNmainOrd1() {
        return nmainOrd1;
    }

    public void setNmainOrd1(String nmainOrd1) {
        this.nmainOrd1 = nmainOrd1;
    }

    public String getNmainOrd2() {
        return nmainOrd2;
    }

    public void setNmainOrd2(String nmainOrd2) {
        this.nmainOrd2 = nmainOrd2;
    }

    public String getNmainSec1() {
        return nmainSec1;
    }

    public void setNmainSec1(String nmainSec1) {
        this.nmainSec1 = nmainSec1;
    }

    public String getNmainSec2() {
        return nmainSec2;
    }

    public void setNmainSec2(String nmainSec2) {
        this.nmainSec2 = nmainSec2;
    }

    public String getNmainStnid1() {
        return nmainStnid1;
    }

    public void setNmainStnid1(String nmainStnid1) {
        this.nmainStnid1 = nmainStnid1;
    }

    public String getNmainStnid2() {
        return nmainStnid2;
    }

    public void setNmainStnid2(String nmainStnid2) {
        this.nmainStnid2 = nmainStnid2;
    }

    public String getNstnId1() {
        return nstnId1;
    }

    public void setNstnId1(String nstnId1) {
        this.nstnId1 = nstnId1;
    }

    public String getNstnId2() {
        return nstnId2;
    }

    public void setNstnId2(String nstnId2) {
        this.nstnId2 = nstnId2;
    }

    public String getNstnOrd1() {
        return nstnOrd1;
    }

    public void setNstnOrd1(String nstnOrd1) {
        this.nstnOrd1 = nstnOrd1;
    }

    public String getNstnOrd2() {
        return nstnOrd2;
    }

    public void setNstnOrd2(String nstnOrd2) {
        this.nstnOrd2 = nstnOrd2;
    }

    public String getNstnSec1() {
        return nstnSec1;
    }

    public void setNstnSec1(String nstnSec1) {
        this.nstnSec1 = nstnSec1;
    }

    public String getNstnSec2() {
        return nstnSec2;
    }

    public void setNstnSec2(String nstnSec2) {
        this.nstnSec2 = nstnSec2;
    }

    public String getNstnSpd1() {
        return nstnSpd1;
    }

    public void setNstnSpd1(String nstnSpd1) {
        this.nstnSpd1 = nstnSpd1;
    }

    public String getNstnSpd2() {
        return nstnSpd2;
    }

    public void setNstnSpd2(String nstnSpd2) {
        this.nstnSpd2 = nstnSpd2;
    }

    public String getPlainNo1() {
        return plainNo1;
    }

    public void setPlainNo1(String plainNo1) {
        this.plainNo1 = plainNo1;
    }

    public String getPlainNo2() {
        return plainNo2;
    }

    public void setPlainNo2(String plainNo2) {
        this.plainNo2 = plainNo2;
    }

    public String getRepTm1() {
        return repTm1;
    }

    public void setRepTm1(String repTm1) {
        this.repTm1 = repTm1;
    }

    public String getRepTm2() {
        return repTm2;
    }

    public void setRepTm2(String repTm2) {
        this.repTm2 = repTm2;
    }

    public String getRerdie_Div1() {
        return rerdie_Div1;
    }

    public void setRerdie_Div1(String rerdie_Div1) {
        this.rerdie_Div1 = rerdie_Div1;
    }

    public String getRerdie_Div2() {
        return rerdie_Div2;
    }

    public void setRerdie_Div2(String rerdie_Div2) {
        this.rerdie_Div2 = rerdie_Div2;
    }

    public String getReride_Num1() {
        return reride_Num1;
    }

    public void setReride_Num1(String reride_Num1) {
        this.reride_Num1 = reride_Num1;
    }

    public String getReride_Num2() {
        return reride_Num2;
    }

    public void setReride_Num2(String reride_Num2) {
        this.reride_Num2 = reride_Num2;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getRtNm() {
        return rtNm;
    }

    public void setRtNm(String rtNm) {
        this.rtNm = rtNm;
    }

    public String getSectOrd1() {
        return sectOrd1;
    }

    public void setSectOrd1(String sectOrd1) {
        this.sectOrd1 = sectOrd1;
    }

    public String getSectOrd2() {
        return sectOrd2;
    }

    public void setSectOrd2(String sectOrd2) {
        this.sectOrd2 = sectOrd2;
    }

    public String getStaOrd() {
        return staOrd;
    }

    public void setStaOrd(String staOrd) {
        this.staOrd = staOrd;
    }

    public String getStationNm1() {
        return stationNm1;
    }

    public void setStationNm1(String stationNm1) {
        this.stationNm1 = stationNm1;
    }

    public String getStationNm2() {
        return stationNm2;
    }

    public void setStationNm2(String stationNm2) {
        this.stationNm2 = stationNm2;
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getStNm() {
        return stNm;
    }

    public void setStNm(String stNm) {
        this.stNm = stNm;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTraSpd1() {
        return traSpd1;
    }

    public void setTraSpd1(String traSpd1) {
        this.traSpd1 = traSpd1;
    }

    public String getTraSpd2() {
        return traSpd2;
    }

    public void setTraSpd2(String traSpd2) {
        this.traSpd2 = traSpd2;
    }

    public String getTraTime1() {
        return traTime1;
    }

    public void setTraTime1(String traTime1) {
        this.traTime1 = traTime1;
    }

    public String getTraTime2() {
        return traTime2;
    }

    public void setTraTime2(String traTime2) {
        this.traTime2 = traTime2;
    }

    public String getVehId1() {
        return vehId1;
    }

    public void setVehId1(String vehId1) {
        this.vehId1 = vehId1;
    }

    public String getVehId2() {
        return vehId2;
    }

    public void setVehId2(String vehId2) {
        this.vehId2 = vehId2;
    }

    @Override
    public String toString() {
        return "SeoulBusArrival{" +
                "arsId='" + arsId + '\'' +
                ", stId='" + stId + '\'' +
                ", stNm='" + stNm + '\'' +
                ", busRouteId='" + busRouteId + '\'' +
                ", rtNm='" + rtNm + '\'' +
                ", firstTm='" + firstTm + '\'' +
                ", lastTm='" + lastTm + '\'' +
                ", term='" + term + '\'' +
                ", routeType='" + routeType + '\'' +
                ", nextBus='" + nextBus + '\'' +
                ", staOrd='" + staOrd + '\'' +
                ", dir='" + dir + '\'' +
                ", mkTm='" + mkTm + '\'' +
                ", vehId1='" + vehId1 + '\'' +
                ", plainNo1='" + plainNo1 + '\'' +
                ", sectOrd1='" + sectOrd1 + '\'' +
                ", stationNm1='" + stationNm1 + '\'' +
                ", traTime1='" + traTime1 + '\'' +
                ", traSpd1='" + traSpd1 + '\'' +
                ", isArrive1='" + isArrive1 + '\'' +
                ", repTm1='" + repTm1 + '\'' +
                ", isLast1='" + isLast1 + '\'' +
                ", busType1='" + busType1 + '\'' +
                ", avgCf1='" + avgCf1 + '\'' +
                ", expCf1='" + expCf1 + '\'' +
                ", kalCf1='" + kalCf1 + '\'' +
                ", neuCf1='" + neuCf1 + '\'' +
                ", exps1='" + exps1 + '\'' +
                ", kals1='" + kals1 + '\'' +
                ", neus1='" + neus1 + '\'' +
                ", rerdie_Div1='" + rerdie_Div1 + '\'' +
                ", reride_Num1='" + reride_Num1 + '\'' +
                ", brerde_Div1='" + brerde_Div1 + '\'' +
                ", brdrde_Num1='" + brdrde_Num1 + '\'' +
                ", full1='" + full1 + '\'' +
                ", nstnId1='" + nstnId1 + '\'' +
                ", nstnOrd1='" + nstnOrd1 + '\'' +
                ", nstnSpd1='" + nstnSpd1 + '\'' +
                ", nstnSec1='" + nstnSec1 + '\'' +
                ", nmainStnid1='" + nmainStnid1 + '\'' +
                ", nmainOrd1='" + nmainOrd1 + '\'' +
                ", nmainSec1='" + nmainSec1 + '\'' +
                ", nmain2Stnid1='" + nmain2Stnid1 + '\'' +
                ", nmain2Ord1='" + nmain2Ord1 + '\'' +
                ", namin2Sec1='" + namin2Sec1 + '\'' +
                ", nmain3Stnid1='" + nmain3Stnid1 + '\'' +
                ", nmain3Ord1='" + nmain3Ord1 + '\'' +
                ", nmain3Sec1='" + nmain3Sec1 + '\'' +
                ", goal1='" + goal1 + '\'' +
                ", vehId2='" + vehId2 + '\'' +
                ", plainNo2='" + plainNo2 + '\'' +
                ", sectOrd2='" + sectOrd2 + '\'' +
                ", stationNm2='" + stationNm2 + '\'' +
                ", traTime2='" + traTime2 + '\'' +
                ", traSpd2='" + traSpd2 + '\'' +
                ", isArrive2='" + isArrive2 + '\'' +
                ", repTm2='" + repTm2 + '\'' +
                ", isLast2='" + isLast2 + '\'' +
                ", busType2='" + busType2 + '\'' +
                ", avgCf2='" + avgCf2 + '\'' +
                ", expCf2='" + expCf2 + '\'' +
                ", kalCf2='" + kalCf2 + '\'' +
                ", neuCf2='" + neuCf2 + '\'' +
                ", exps2='" + exps2 + '\'' +
                ", kals2='" + kals2 + '\'' +
                ", neus2='" + neus2 + '\'' +
                ", rerdie_Div2='" + rerdie_Div2 + '\'' +
                ", reride_Num2='" + reride_Num2 + '\'' +
                ", brerde_Div2='" + brerde_Div2 + '\'' +
                ", brdrde_Num2='" + brdrde_Num2 + '\'' +
                ", full2='" + full2 + '\'' +
                ", nstnId2='" + nstnId2 + '\'' +
                ", nstnOrd2='" + nstnOrd2 + '\'' +
                ", nstnSpd2='" + nstnSpd2 + '\'' +
                ", nstnSec2='" + nstnSec2 + '\'' +
                ", nmainStnid2='" + nmainStnid2 + '\'' +
                ", nmainOrd2='" + nmainOrd2 + '\'' +
                ", nmainSec2='" + nmainSec2 + '\'' +
                ", nmain2Stnid2='" + nmain2Stnid2 + '\'' +
                ", nmain2Ord2='" + nmain2Ord2 + '\'' +
                ", namin2Sec2='" + namin2Sec2 + '\'' +
                ", nmain3Stnid2='" + nmain3Stnid2 + '\'' +
                ", nmain3Ord2='" + nmain3Ord2 + '\'' +
                ", nmain3Sec2='" + nmain3Sec2 + '\'' +
                ", goal2='" + goal2 + '\'' +
                '}';
    }
}

