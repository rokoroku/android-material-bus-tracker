package kr.rokoroku.mbus.util;

/**
 * Created by rok on 2015. 7. 16..
 */
public interface ValueCallback<T> {
    void onSuccess(T value);
    void onFailure(Throwable t);
}
