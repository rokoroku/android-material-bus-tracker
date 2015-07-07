package kr.rokoroku.mbus.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by rok on 2015. 5. 29..
 */

public class ViewUtils {

    private static final int[] EMPTY_STATE = new int[]{};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        Log.d("hitTest", String.format("(%d, %d, %d, %d), (%d, %d)", left, top, right, bottom, x, y));
        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    /**
     * Converts from device independent pixels (dp or dip) to
     * device dependent pixels. This method returns the input
     * multiplied by the display's density. The result is not
     * rounded nor clamped.
     * <p>
     * The value returned by this method is well suited for
     * drawing with the Canvas API but should not be used to
     * set layout dimensions.
     *
     * @param dp        The value in dp to convert to pixels
     * @param resources An instances of Resources
     */
    public static float dpToPixel(float dp, Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return metrics.density * dp;
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
}