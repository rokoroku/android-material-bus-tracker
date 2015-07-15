/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.rokoroku.mbus.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class CardedTextView extends TextView {

    private float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
            getResources().getDisplayMetrics());
    private RectF rectF;
    private Paint paint;
    private int color;

    public CardedTextView(Context context) {
        super(context);
        setTextColor(getCurrentTextColor());
    }

    public CardedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextColor(getCurrentTextColor());
    }

    public CardedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextColor(getCurrentTextColor());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTextColor(getCurrentTextColor());
    }

    @Override
    public void setTextColor(int color) {
        if (this.color != color) {
            super.setTextColor(Color.WHITE);
            if (paint == null) paint = new Paint();
            paint.setColor(color);
            invalidate();
        }
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (rectF == null)  rectF = new RectF(0, 0, getWidth(), getHeight());
        else rectF.set(0, 0, getWidth(), getHeight());

        canvas.drawRoundRect(rectF, radius, radius, paint);
        super.onDraw(canvas);
    }
}
