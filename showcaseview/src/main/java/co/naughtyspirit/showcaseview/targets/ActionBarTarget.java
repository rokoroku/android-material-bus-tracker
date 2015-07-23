/*
 *
 *  * Copyright 2015 Atanas Dimitrov <atanas@naughtyspirit.co>
 *  *                 NaughtySpirit 2014
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package co.naughtyspirit.showcaseview.targets;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Seishin <atanas@naughtyspirit.co>
 * on 2/11/15.
 * NaughtySpirit 2015
 */
public class ActionBarTarget implements Target {

    private final TargetView.ShowcaseType type;

    private float[] location;
    private int margin = 10;

    @SuppressWarnings("deprecation")
    public ActionBarTarget(Context context, TargetView.ShowcaseType showCaseType) {
        this.type = showCaseType;

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }

        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }


        int[] location = {0, statusBarHeight};

        if (type.equals(TargetView.ShowcaseType.CIRCLE)) {
            float x = location[0];
            float y = location[1] + (actionBarHeight / 2);
            float radius = actionBarHeight;
            setCircleLocation(x, y, radius + margin);

        } else if (type.equals(TargetView.ShowcaseType.RECTANGLE)) {
            float left = location[0] - margin;
            float top = location[1] - margin;
            float right = location[0] + actionBarHeight + margin;
            float bottom = location[1] + actionBarHeight + margin;

            setRectLocation(left, top, right, bottom);
        }

    }

    @Override
    public TargetView.ShowcaseType getType() {
        return type;
    }

    @Override
    public void setCircleLocation(float x, float y, float radius) {
        location = new float[3];

        location[0] = x;
        location[1] = y;
        location[2] = radius;
    }

    @Override
    public void setRectLocation(float left, float top, float right, float bottom) {
        location = new float[4];

        location[0] = left;
        location[1] = top;
        location[2] = right;
        location[3] = bottom;
    }

    @Override
    public float[] getLocation() {
        return location;
    }

    @Override
    public void setTargetMargin(int margin) {
        this.margin = margin;
    }
}
