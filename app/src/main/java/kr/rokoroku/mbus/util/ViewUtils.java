package kr.rokoroku.mbus.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.NoSuchPropertyException;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by rok on 2015. 5. 29..
 */

public class ViewUtils {

    private static Handler sHandler;
    private static final int[] DRAWABLE_EMPTY_STATE = new int[]{};

    public static void runOnUiThread(Runnable runnable) {
        Looper mainLooper = Looper.getMainLooper();
        if(mainLooper.getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            if(sHandler == null) sHandler = new Handler(mainLooper);
            sHandler.post(runnable);
        }
    }

    public static void runOnUiThread(Runnable runnable, long delay) {
        if(sHandler == null) sHandler = new Handler(Looper.getMainLooper());
        sHandler.postDelayed(runnable, delay);
    }

    public static float dpToPixel(float dp, Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return metrics.density * dp;
    }

    public static void clearDrawableState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(DRAWABLE_EMPTY_STATE);
        }
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        //Log.d("hitTest", String.format("(%d, %d, %d, %d), (%d, %d)", left, top, right, bottom, x, y));
        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static Point getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(point);
        } else {
            Display display = windowManager.getDefaultDisplay();
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }
}