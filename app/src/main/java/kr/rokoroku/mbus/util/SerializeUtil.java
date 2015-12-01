package kr.rokoroku.mbus.util;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rok on 2015. 7. 7..
 * <p>
 * http://stackoverflow.com/a/10221233
 */
public class SerializeUtil {

    @SuppressWarnings("unchecked")
    public static void serialize(DataOutput out, Object object) throws IOException {
        if (object == null) {
            out.writeByte(0);

        } else try {
            out.writeByte(1);
            Field field = object.getClass().getDeclaredField("SERIALIZER");
            Serializer serializer = (Serializer) field.getDeclaringClass().cast(Serializer.class);
            serializer.serialize(out, object);

        } catch (Exception e) {
            throw new IOException("Cannot Find SERIALIZER", e);
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(DataInput in, Class<T> clazz) throws IOException {
        boolean isNull = in.readByte() == 0;
        if (!isNull) try {
            Field field = clazz.getDeclaredField("SERIALIZER");
            Serializer<T> serializer = (Serializer<T>) field.getDeclaringClass().cast(Serializer.class);
            Object deserialize = serializer.deserialize(in, 0);
            return clazz.cast(deserialize);

        } catch (Exception e) {
            throw new IOException("Cannot Find SERIALIZER", e);

        }
        else {
            return null;
        }
    }

    public static void writeString(DataOutput out, String string) throws IOException {
        if (string == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeUTF(string);
        }
    }

    public static String readString(DataInput in) throws IOException {
        boolean isNull = in.readByte() == 0;
        if (!isNull) {
            return in.readUTF();
        } else {
            return null;
        }
    }

    public static void writeInt(DataOutput out, int value) throws IOException {
        out.writeInt(value);
    }

    public static int readInt(DataInput in) throws IOException {
        return in.readInt();
    }

    public static void writeDouble(DataOutput out, Double aDouble) throws IOException {
        if (aDouble == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeDouble(aDouble);
        }
    }

    public static Double readDouble(DataInput in) throws IOException {
        boolean isNull = in.readByte() == 0;
        if (!isNull) {
            return in.readDouble();
        } else {
            return null;
        }
    }

    public static <V> void writeList(DataOutput out,
                                     List<V> list,
                                     Serializer<V> serializer) throws IOException {
        if (list == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);

            int size = list.size();
            out.writeInt(size);
            for (int i = 0; i < size; i++) {
                V object = list.get(i);
                serializer.serialize(out, object);
            }
        }
    }

    public static <V> List<V> readList(DataInput in,
                                       Serializer<V> serializer) throws IOException {
        boolean isNull = in.readByte() == 0;
        if (!isNull) {
            int size = in.readInt();
            List<V> list = new ArrayList<V>();
            for (int i = 0; i < size; i++) {
                V object = serializer.deserialize(in, 1);
                list.add(object);
            }
            return list;

        } else {
            return null;
        }
    }

    public static <K, V> void writeMap(DataOutput out,
                                       Map<K, V> map,
                                       Serializer<K> keySerializer,
                                       Serializer<V> valueSerializer) throws IOException {
        if (map != null) {
            out.writeByte(1);

            int size = map.size();
            out.writeInt(size);

            Set<Map.Entry<K, V>> entries = map.entrySet();
            for (Map.Entry<K, V> entry : entries) {
                keySerializer.serialize(out, entry.getKey());
                valueSerializer.serialize(out, entry.getValue());
            }

        } else {
            out.writeByte(0);
        }
    }

    public static <K, V> Map<K, V> readMap(DataInput in,
                                           Serializer<K> keySerializer,
                                           Serializer<V> valueSerializer) throws IOException {
        boolean isNull = in.readByte() == 0;
        if (!isNull) {
            Map<K, V> map = new HashMap<>();

            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                K key = keySerializer.deserialize(in, 0);
                V value = valueSerializer.deserialize(in, 0);
                map.put(key, value);
            }

            return map;
        } else {
            return null;
        }

    }

    public static void writeByteArray(DataOutput out, byte[] bytes) throws IOException {
        int size = bytes != null ? bytes.length : 0;

        out.writeInt(size);
        if (size > 0) {
            out.write(bytes);
        }
    }

    public static byte[] readByteArray(DataInput in) throws IOException {
        int size = in.readInt();

        if (size > 0) {
            byte[] bytes = new byte[size];
            in.readFully(bytes);
            return bytes;
        } else {
            return null;
        }
    }

}
