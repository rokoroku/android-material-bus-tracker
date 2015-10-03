package kr.rokoroku.mbus.api.seoulweb.core;

import com.google.gson.Gson;
import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import java.io.InputStream;
import java.lang.reflect.Type;

import kr.rokoroku.mbus.api.gbisweb.model.GbisSearchAllResult;
import kr.rokoroku.mbus.api.seoulweb.model.StationByPositionResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisMapLineResult;
import kr.rokoroku.mbus.api.seoulweb.model.TopisRealtimeResult;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

/**
 * Created by rok on 2015. 7. 10..
 */
public class SeoulWebResponseConverter implements Converter {

    private Converter gsonConverter;
    private Converter xmlConverter;

    public SeoulWebResponseConverter() {
        Gson gson = new Gson();
        gsonConverter = new GsonConverter(new Gson(), "UTF-8");
        xmlConverter = new SimpleXmlConverter();
    }

    @Override
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        if (type.equals(TopisMapLineResult.class) || type.equals(TopisRealtimeResult.class)) try {

            //read string
            StringBuffer stringBuffer = new StringBuffer();
            InputStream inputStream = body.in();
            byte[] b = new byte[4096];
            for (int n; (n = inputStream.read(b)) != -1; ) {
                stringBuffer.append(new String(b, 0, n));
            }

            // correct invalid json format
            String string = stringBuffer.toString();
            if (string.startsWith("[")) {
                string = "{\"result\":" + string + "}";
            }
            return gsonConverter.fromBody(new TypedString(string), type);

        } catch (Exception e) {
            throw new ConversionException(e);

        } else if(type.equals(StationByPositionResult.class)) try {
            StringBuffer stringBuffer = new StringBuffer();
            InputStream inputStream = body.in();
            byte[] b = new byte[4096];
            for (int n; (n = inputStream.read(b)) != -1; ) {
                String string = new String(b, 0, n).replace("&", "&amp;");
                stringBuffer.append(string);
            }
            return xmlConverter.fromBody(new TypedString(stringBuffer.toString()), type);

        } catch (Exception e) {
            throw new ConversionException(e);
        }
        return gsonConverter.fromBody(body, type);
    }

    @Override
    public TypedOutput toBody(Object object) {
        return gsonConverter.toBody(object);
    }
}
