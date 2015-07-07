package kr.rokoroku.mbus.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.quinny898.library.persistentsearch.SearchResult;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by rok on 2015. 6. 24..
 */
public class SearchHistory implements Serializable, Comparable<SearchHistory> {

    public enum Type {
        ROUTE, STATION
    }

    private Type type;
    private String title;
    private String key;
    private Provider provider;
    private Long timestamp;

    public SearchHistory(@NonNull String title) {
        this.title = title;
        this.timestamp = System.currentTimeMillis();
    }

    public SearchHistory(@NonNull String title, Type type, String key, Provider provider) {
        this.title = title;
        this.type = type;
        this.key = TextUtils.isEmpty(key) ? null : key;
        this.provider = provider;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getKey() {
        return key;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchHistory that = (SearchHistory) o;

        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public int compareTo(SearchHistory another) {
        return (int)(timestamp - another.timestamp);
    }

    @Override
    public String toString() {
        return "SearchHistory{" +
                "title='" + title + '\'' +
                ", key='" + key + '\'' +
                ", type=" + type +
                ", provider=" + provider +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class ValueSerializer extends Serializer<SearchHistory> implements Serializable {
        @Override
        public void serialize(DataOutput out, SearchHistory value) throws IOException {
            out.writeUTF(value.title);
            out.writeInt(value.type != null ? value.type.ordinal() : -1);
            out.writeUTF(value.key != null ? value.key : "");
            out.writeInt(value.provider != null ? value.provider.getCityCode() : -1);
            out.writeLong(value.timestamp);
        }

        @Override
        public SearchHistory deserialize(DataInput in, int available) throws IOException {
            String title = in.readUTF();
            int typeOrdinal = in.readInt();
            Type type = typeOrdinal != -1 ? Type.values()[typeOrdinal] : null;
            String key = in.readUTF();
            Provider provider = Provider.valueOf(in.readInt());
            SearchHistory history = new SearchHistory(title, type, key, provider);
            history.timestamp = in.readLong();
            return history;
        }

        @Override
        public int fixedSize() {
            return -1;
        }
    }

    public static ValueSerializer serializer = new ValueSerializer();

}
