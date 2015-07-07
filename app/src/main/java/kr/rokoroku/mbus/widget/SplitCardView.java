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

package kr.rokoroku.mbus.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.cardview.R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.LinkedList;

import io.codetail.animation.SupportAnimator;
import io.codetail.widget.RevealFrameLayout;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ViewUtils;

/**
 * A FrameLayout with a splittable rounded corner background and shadow.
 */
public class SplitCardView extends FrameLayout {

    private int color;
    private boolean isRoundTop = true;
    private boolean isRoundBottom = true;
    private boolean[] radiiFlag = {true, true, true, true};
    private RevealFrameLayout mRevealFrameLayout;
    private LinkedList<ImageView> mInternalViewQueue;

    public SplitCardView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public SplitCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public SplitCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        // NO OP
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // NO OP
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardView, defStyleAttr, R.style.CardView_Light);
        int backgroundColor = a.getColor(R.styleable.CardView_cardBackgroundColor, 0xffffff);
        float radius = a.getDimension(R.styleable.CardView_cardCornerRadius, 0);
        a.recycle();

        float shadowSize = ViewUtils.dpToPixel(1.5f, getResources());
        float maxShadowSize = ViewUtils.dpToPixel(2.5f, getResources());
        boolean[] radiiFlag = {true, true, true, true};
        mRevealFrameLayout = (RevealFrameLayout) View.inflate(context, kr.rokoroku.mbus.R.layout.widget_reveal_frame_layout, null);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mRevealFrameLayout, layoutParams);
        mInternalViewQueue = new LinkedList<>();
        mInternalViewQueue.add(new ImageView(context));
        mInternalViewQueue.add(new ImageView(context));
        mRevealFrameLayout.addView(mInternalViewQueue.getLast(), layoutParams);
        mRevealFrameLayout.addView(mInternalViewQueue.getFirst(), layoutParams);

        if (Build.VERSION.SDK_INT >= 16) {
            mInternalViewQueue.getFirst().setImageDrawable(new SelectableRoundRectDrawable(context.getResources(), backgroundColor,
                    radius, shadowSize, maxShadowSize, radiiFlag));
            mInternalViewQueue.getLast().setImageDrawable(new SelectableRoundRectDrawable(context.getResources(), backgroundColor,
                    radius, shadowSize, maxShadowSize, radiiFlag));
        } else {
            mInternalViewQueue.getFirst().setImageDrawable(new SelectableRoundRectDrawable(context.getResources(), backgroundColor,
                    radius, shadowSize, maxShadowSize, radiiFlag));
            mInternalViewQueue.getLast().setImageDrawable(new SelectableRoundRectDrawable(context.getResources(), backgroundColor,
                    radius, shadowSize, maxShadowSize, radiiFlag));
        }
    }

    public void setRoundTop(boolean roundTop) {
        isRoundTop = roundTop;
        boolean[] radiiFlag = ((SelectableRoundRectDrawable) ((ImageView) mRevealFrameLayout.getChildAt(0)).getDrawable()).getRadiiFlag();
        radiiFlag[0] = roundTop;
        radiiFlag[1] = roundTop;
        ((SelectableRoundRectDrawable) mInternalViewQueue.getFirst().getDrawable()).setRadiiFlag(radiiFlag);
        ((SelectableRoundRectDrawable) mInternalViewQueue.getLast().getDrawable()).setRadiiFlag(radiiFlag);
    }


    public void setRoundBottom(boolean roundBottom) {
        isRoundBottom = roundBottom;
        boolean[] radiiFlag = ((SelectableRoundRectDrawable) ((ImageView) mRevealFrameLayout.getChildAt(0)).getDrawable()).getRadiiFlag();
        radiiFlag[2] = roundBottom;
        radiiFlag[3] = roundBottom;
        ((SelectableRoundRectDrawable) mInternalViewQueue.getFirst().getDrawable()).setRadiiFlag(radiiFlag);
        ((SelectableRoundRectDrawable) mInternalViewQueue.getLast().getDrawable()).setRadiiFlag(radiiFlag);
    }

    /**
     * Updates the background color of the CardView
     *
     * @param color The new color to set for the card background
     * @attr ref android.support.v7.splittableCardView.R.styleable#SplittableCardView_cardBackgroundColor
     */
    public void setCardBackgroundColor(int color) {
        if (this.color != color) {
            this.color = color;
            ((SelectableRoundRectDrawable) mInternalViewQueue.getFirst().getDrawable()).setColor(color);
            ((SelectableRoundRectDrawable) mInternalViewQueue.getLast().getDrawable()).setColor(color);
        }
    }

    public void animateCardBackgroundColor(int color) {
        ((SelectableRoundRectDrawable) mInternalViewQueue.getLast().getDrawable()).setColor(color);
        mInternalViewQueue.add(mInternalViewQueue.pop());
        RevealUtils.revealView(mInternalViewQueue.getFirst(), RevealUtils.Position.CENTER, new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                setCardBackgroundColor(color);
            }
        });
    }

    public void animateCardBackgroundColor(int color, RevealUtils.Position position) {
        this.color = color;
        ((SelectableRoundRectDrawable) mInternalViewQueue.getLast().getDrawable()).setColor(color);
        mInternalViewQueue.add(mInternalViewQueue.pop());
        RevealUtils.revealView(mInternalViewQueue.getFirst(), position, new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                setCardBackgroundColor(color);
            }
        });
    }

}
