package kr.rokoroku.mbus.util;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.mapdb.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kr.rokoroku.mbus.data.model.RouteType;

/**
 * Created by rok on 2015. 7. 7..
 * <p>
 * http://stackoverflow.com/a/10221233
 */
public class SerializationUtil {

    /**
     * Reads a Map from a Parcel that was stored using a String array and a Bundle.
     *
     * @param in   the Parcel to retrieve the map from
     * @param type the class used for the value objects in the map, equivalent to V.class before type erasure
     * @return a map containing the items retrieved from the parcel
     */
    public static <V extends Parcelable> Map<String, V> readMap(Parcel in, Class<? extends V> type) {

        Map<String, V> map = new HashMap<>();
        if (in != null) {
            String[] keys = in.createStringArray();
            Bundle bundle = in.readBundle(type.getClassLoader());
            for (String key : keys)
                map.put(key, type.cast(bundle.getParcelable(key)));
        }
        return map;
    }


    /**
     * Reads into an existing Map from a Parcel that was stored using a String array and a Bundle.
     *
     * @param map  the Map<String,V> that will receive the items from the parcel
     * @param in   the Parcel to retrieve the map from
     * @param type the class used for the value objects in the map, equivalent to V.class before type erasure
     */
    public static <V extends Parcelable> void readMap(Map<String, V> map, Parcel in, Class<V> type) {

        if (map != null) {
            map.clear();
            if (in != null) {
                String[] keys = in.createStringArray();
                Bundle bundle = in.readBundle(type.getClassLoader());
                for (String key : keys)
                    map.put(key, type.cast(bundle.getParcelable(key)));
            }
        }
    }


    /**
     * Writes a Map to a Parcel using a String array and a Bundle.
     *
     * @param map the Map<String,V> to store in the parcel
     * @param out the Parcel to store the map in
     */
    public static void writeMap(Map<String, ? extends Parcelable> map, Parcel out) {

        if (map != null && map.size() > 0) {
            Set<String> keySet = map.keySet();
            Bundle b = new Bundle();
            for (String key : keySet)
                b.putParcelable(key, map.get(key));
            String[] array = keySet.toArray(new String[keySet.size()]);
            out.writeStringArray(array);
            out.writeBundle(b);
        } else {
            //String[] array = Collections.<String>emptySet().toArray(new String[0]);
            // you can use a static instance of String[0] here instead
            out.writeStringArray(new String[0]);
            out.writeBundle(Bundle.EMPTY);
        }
    }

    public static byte[] serialize(Object object) {
        if(object != null) try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            return bos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes != null) try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            Object object = in.readObject();
            return clazz.cast(object);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void serialize(DataOutput out, Object object) throws IOException {
        if (object == null) {
            out.writeByte(0);

        } else {
            try {
                out.writeByte(1);
                Field field = object.getClass().getDeclaredField("SERIALIZER");
                Serializer serializer = (Serializer) field.getDeclaringClass().cast(Serializer.class);
                serializer.serialize(out, object);

            } catch (Exception e) {
                e.printStackTrace();

                byte[] bytes = serialize(object);
                out.writeInt(bytes.length);
                out.write(bytes);
            }
        }
    }

    public static <T> T deserialize(DataInput in, Object object, Class<T> clazz) throws IOException {
        if (in.readByte() == 1) try {
            Field field = object.getClass().getDeclaredField("SERIALIZER");
            Serializer serializer = (Serializer) field.getDeclaringClass().cast(Serializer.class);
            Object deserialize = serializer.deserialize(in, 0);
            return clazz.cast(deserialize);

        } catch (Exception e) {
            e.printStackTrace();

            byte[] bytes = new byte[in.readInt()];
            in.readFully(bytes);
            return deserialize(bytes, clazz);

        } else {
            return null;
        }
    }

    public static void serializeString(DataOutput out, String string) throws IOException {
        if(string == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeUTF(string);
        }
    }

    public static String deserializeString(DataInput in) throws IOException {
        boolean isNull = in.readByte() == 0;
        if(!isNull) {
            return in.readUTF();
        } else {
            return null;
        }
    }


    public static void serializeDouble(DataOutput out, Double aDouble) throws IOException {
        if(aDouble == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeDouble(aDouble);
        }
    }

    public static Double deserializeDouble(DataInput in) throws IOException {
        boolean isNull = in.readByte() == 0;
        if(!isNull) {
            return in.readDouble();
        } else {
            return null;
        }
    }


    public static void writeByteArray(DataOutput out, byte[] bytes) throws IOException {
        int size = bytes != null ? bytes.length : 0;

        out.writeInt(size);
        if(size > 0) {
            out.write(bytes);
        }
    }

    public static byte[] readByteArray(DataInput in) throws IOException {
        int size = in.readInt();

        if(size > 0) {
            byte[] bytes = new byte[size];
            in.readFully(bytes);
            return bytes;
        } else {
            return null;
        }
    }

}
