package kr.rokoroku.mbus.data.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by rok on 2015. 6. 24..
 */
public class SearchHistory implements Serializable, Comparable<SearchHistory> {

    static final long serialVersionUID = 1L;

    private String title;
    private Long timestamp;

    public SearchHistory(@NonNull String title) {
        this.title = title;
        this.timestamp = System.currentTimeMillis();
    }

    public SearchHistory(@NonNull String title, long timestamp) {
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
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
        return (int)(another.timestamp - timestamp);
    }

    public static final Serializer<SearchHistory> SERIALIZER = new Serializer<SearchHistory>() {
        @Override
        public void serialize(DataOutput out, SearchHistory value) throws IOException {
            out.writeUTF(value.title);
            out.writeLong(value.timestamp);
        }

        @Override
        public SearchHistory deserialize(DataInput in, int available) throws IOException {
            String title = in.readUTF();
            long timestamp = in.readLong();
            return new SearchHistory(title, timestamp);
        }
    };
}
