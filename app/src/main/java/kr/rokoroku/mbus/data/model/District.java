package kr.rokoroku.mbus.data.model;

import android.content.Context;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import kr.rokoroku.mbus.R;

/**
 * Created by rok on 2015. 6. 3..
 */
public enum District {
    SEOUL(1),
    GYEONGGI(2),
    INCHEON(3),
    UNKNOWN(-1);

    private final int value;

    District(int value) {
        this.value = value;
    }

    public static District valueOf(int code) {
        for (District district : District.values()) {
            if (district.value == code) return district;
        }
        return null;
    }

    public static District valueOfGbis(String siflag) {
        int flag = -1;
        if (siflag != null) try {
            flag = Integer.valueOf(siflag);
        } catch (Exception ignored) {

        }
        return valueOf(flag);
    }


    public String getCityName(Context context) {
        int strRes;
        switch (this) {
            case SEOUL:
                strRes = R.string.city_seoul;
                break;

            case GYEONGGI:
                strRes = R.string.city_gyeonggi;
                break;

            case INCHEON:
                strRes = R.string.city_incheon;
                break;

            default:
                strRes = android.R.string.unknownName;
                break;
        }
        return context.getString(strRes);
    }

    public static final Serializer<District> SERIALIZER = new Serializer<District>() {
        @Override
        public void serialize(DataOutput out, District value) throws IOException {
            if(value == null) {
                out.writeByte(0);
            } else {
                out.writeByte(1);
                out.writeInt(value.value);
            }
        }

        @Override
        public District deserialize(DataInput in, int available) throws IOException {
            boolean isNull = in.readByte() == 0;
            if(!isNull) {
                int value = in.readInt();
                return District.valueOf(value);
            } else {
                return null;
            }
        }
    };
}
