package kr.rokoroku.mbus.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.util.Log;
import android.util.NoSuchPropertyException;
import android.util.TypedValue;

/**
 * Created by rok on 2015. 6. 1..
 */
public class ThemeUtils {

    public static int getThemeColor(Context context, @AttrRes int resId) {
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();

        if (theme.resolveAttribute(resId, typedValue, true)) {
            if(typedValue.type == TypedValue.TYPE_REFERENCE) {
                return getResourceColor(context, typedValue.data);
            } else {
                return typedValue.data;
            }
        } else {
            throw new NoSuchPropertyException("No such property: " + resId);
        }
    }

    public static int getResourceColor(Context context, @ColorRes int resId) {
        return context.getResources().getColor(resId);
    }

    public static int getDimension(Context context, @AttrRes int resId) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(resId, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            return 0;
        }
    }

    public static int dimColor(int color, float amount) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= amount;
        return Color.HSVToColor(hsv);
    }
}
