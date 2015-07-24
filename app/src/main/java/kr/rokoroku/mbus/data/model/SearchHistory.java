package kr.rokoroku.mbus.data.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by rok on 2015. 6. 24..
 */
public class SearchHistory implements Serializable, Comparable<SearchHistory> {

    private String title;
    private Long timestamp;

    public SearchHistory(@NonNull String title) {
        this.title = title;
        this.timestamp = System.currentTimeMillis();
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

}
