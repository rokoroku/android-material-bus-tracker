package kr.rokoroku.mbus.util;

import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.crashlytics.android.Crashlytics;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by rok on 2015. 6. 6..
 */
public class RevealUtils {

    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        LEFT, CENTER, RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public static SupportAnimator revealView(View view, Position position, int duration, SupportAnimator.AnimatorListener listener) {

        Point point = calculatePoint(view, position);

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        SupportAnimator animator = null;
        try {
            animator = ViewAnimationUtils.createCircularReveal(view, point.x, point.y, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(duration);
            if (listener != null) animator.addListener(listener);
            animator.addListener(new SupportAnimator.SimpleAnimatorListener() {
                @Override
                public void onAnimationStart() {
                    view.getParent().bringChildToFront(view);
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd() {
//                RevealFrameLayout revealFrameLayout = (RevealFrameLayout) view.getParent();
//                for (int i = 0; i < revealFrameLayout.getChildCount(); i++) {
//                    View childView = revealFrameLayout.getChildAt(i);
//                    if (!view.equals(childView)) {
//                        childView.setVisibility(View.INVISIBLE);
//                    }
//                }
                }
            });
            animator.start();

        } catch (Exception e) {
            Log.e("RevealUtils", "Exception in RevealUtils", e);
            Crashlytics.logException(e);
            if(listener != null) {
                listener.onAnimationStart();
                listener.onAnimationEnd();
            }
        }

        return animator;
    }

    public static SupportAnimator unrevealView(View view, Position position, int duration, SupportAnimator.AnimatorListener listener) {

        Point point = calculatePoint(view, position);

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, point.x, point.y, finalRadius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        if (listener != null) animator.addListener(listener);
        animator.addListener(new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationStart() {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
        return animator;
    }

    private static Point calculatePoint(View view, Position position) {
        Point point = new Point();
        switch (position) {
            case TOP_LEFT:
                point.x = view.getLeft();
                point.y = view.getTop();
                break;
            case TOP_CENTER:
                point.x = (view.getLeft() + view.getRight()) / 2;
                point.y = view.getTop();
                break;
            case TOP_RIGHT:
                point.x = view.getRight();
                point.y = view.getTop();
                break;
            case LEFT:
                point.x = view.getLeft();
                point.y = (view.getTop() + view.getBottom()) / 2;
                break;
            case CENTER:
                point.x = (view.getLeft() + view.getRight()) / 2;
                point.y = (view.getTop() + view.getBottom()) / 2;
                break;
            case RIGHT:
                point.x = view.getRight();
                point.y = (view.getTop() + view.getBottom()) / 2;
                break;
            case BOTTOM_LEFT:
                point.x = view.getLeft();
                point.y = view.getBottom() / 2;
                break;
            case BOTTOM_CENTER:
                point.x = (view.getLeft() + view.getRight()) / 2;
                point.y = view.getBottom() / 2;
                break;
            case BOTTOM_RIGHT:
                point.x = view.getRight();
                point.y = view.getBottom() / 2;
                break;
        }
        return point;
    }
}
