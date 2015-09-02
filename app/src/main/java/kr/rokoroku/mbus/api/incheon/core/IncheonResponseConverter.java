package kr.rokoroku.mbus.api.incheon.core;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.rokoroku.mbus.api.incheon.data.IncheonArrivalInfo;
import kr.rokoroku.mbus.api.incheon.data.IncheonBusPosition;
import kr.rokoroku.mbus.data.model.District;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

/**
 * Created by rok on 2015. 4. 22..
 */
public class IncheonResponseConverter implements Converter {

    GsonConverter gsonConverter;
    SimpleXmlConverter xmlConverter;

    public IncheonResponseConverter() {
        super();
        gsonConverter = new GsonConverter(new Gson(), "UTF-8");
        xmlConverter = new SimpleXmlConverter();
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            if (type.equals(IncheonArrivalInfo.class)) {
                return xmlConverter.fromBody(body, type);
            } else {
                //read string
                StringBuffer stringBuffer = new StringBuffer();
                InputStream inputStream = body.in();
                byte[] b = new byte[4096];
                for (int n; (n = inputStream.read(b)) != -1; ) {
                    stringBuffer.append(new String(b, 0, n));
                }

                if (type.equals(IncheonBusPosition.class)) {
                    String string = stringBuffer.toString();

                    // correct invalid json format
                    if (string.startsWith("[")) {
                        string = "{\"result\":" + string + "}";
                    }
                    return gsonConverter.fromBody(new TypedString(string), type);

                } else if (type.equals(List.class)) {
                    if (stringBuffer.subSequence(0, 10).equals("routeData=")) {
                        return parseRouteStation(stringBuffer.toString());

                    } else {
                        return parseStationRoute(stringBuffer.toString());
                    }
                }
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }
        return null;
    }

    @Override
    public TypedOutput toBody(Object object) {
        return null;
    }

    private List<RouteStation> parseRouteStation(String body) throws Exception {
        List<RouteStation> routeStations = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(body.substring(10), "|");
        while (tokenizer.hasMoreTokens()) {
            String nextToken = tokenizer.nextToken();
            Scanner scanner = new Scanner(nextToken);
            scanner.useDelimiter(";");

            int seq = scanner.nextInt();
            String stationId = scanner.next();
            String stationName = scanner.next();

            if (!TextUtils.isEmpty(stationName) && !stationName.equals("없음")) {
                Station station = IncheonDbHelper.getInstance().getStation(stationId);
                if (station != null) {
                    RouteStation routeStation = new RouteStation(station, null, seq, District.INCHEON);
                    routeStations.add(routeStation);
                }
            }
        }
        return routeStations;
    }

    private List<StationRoute> parseStationRoute(String body) throws Exception {
        List<StationRoute> stationRoutes = new ArrayList<>();
        Document document = Jsoup.parse(body);
        Element table = document.getElementById("idRowTemplate").parent();
        for (Element row : table.getAllElements()) {
            Elements a = row.getElementsByClass("a");
            if(a != null) {
                String href = a.attr("href");
                Pattern pattern = Pattern.compile("\\((\\d+),\\s*'(\\w)+'+\\)");
                Matcher matcher = pattern.matcher(href);

                String routeId = matcher.group(0);
                String routeName = matcher.group(1);
                String routeType = row.getElementsByIndexEquals(1).html();

                Route route = IncheonDbHelper.getInstance().getRoute(routeId);
                if(route != null) {
                    StationRoute stationRoute = new StationRoute(route, null);
                    stationRoutes.add(stationRoute);
                }
            }
        }
        return stationRoutes;
    }
}
