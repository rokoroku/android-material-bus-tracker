package kr.rokoroku.mbus.util;

/**
 * Created by rok on 2015. 7. 17..
 */
public abstract class SimpleProgressCallback<T> implements ProgressCallback<T> {
    @Override
    public void onComplete(boolean success, T value) {

    }

    @Override
    public void onProgressUpdate(int current, int target) {

    }

    @Override
    public void onError(int progress, Throwable t) {

    }
}
