package kr.rokoroku.mbus.data.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;

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
}
