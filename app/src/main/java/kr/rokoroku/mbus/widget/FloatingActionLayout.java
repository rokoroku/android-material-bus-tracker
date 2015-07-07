package kr.rokoroku.mbus.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import kr.rokoroku.mbus.util.support.AnimationUtils;
import kr.rokoroku.mbus.util.support.ViewGroupUtils;

/**
 * Created by rok on 2015. 7. 2..
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionLayout.Behavior.class)
public class FloatingActionLayout extends FrameLayout {

    public FloatingActionLayout(Context context) {
        super(context);
    }

    public FloatingActionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingActionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingActionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<FloatingActionLayout> {
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
        private Rect mTmpRect;
        private boolean mIsAnimatingOut;
        private float mTranslationY;

        public Behavior() {
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionLayout child, View dependency) {
            return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        }

        @SuppressWarnings("NullArgumentToVariableArgMethod")
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionLayout child, View dependency) {
            if(dependency instanceof Snackbar.SnackbarLayout) {
                this.updateFabTranslationForSnackbar(parent, child, dependency);
            } else if(dependency instanceof AppBarLayout) {
                AppBarLayout appBarLayout = (AppBarLayout)dependency;
                if(this.mTmpRect == null) {
                    this.mTmpRect = new Rect();
                }

                Rect rect = this.mTmpRect;
                ViewGroupUtils.getDescendantRect(parent, dependency, rect);

                int minimumHeightForVisibleOverlappingContent = -1;
                try {
                    Method minimumHeightForVisibleOverlappingContentMethod =
                            AppBarLayout.class.getDeclaredMethod("getMinimumHeightForVisibleOverlappingContent", null);
                    minimumHeightForVisibleOverlappingContentMethod.setAccessible(true);
                    minimumHeightForVisibleOverlappingContent =
                            (int) minimumHeightForVisibleOverlappingContentMethod.invoke(appBarLayout, null);

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if(rect.bottom <= minimumHeightForVisibleOverlappingContent) {
                    if(!this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                        this.animateOut(child);
                    }
                } else if(child.getVisibility() != View.VISIBLE) {
                    this.animateIn(child);
                }
            }

            return false;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FloatingActionLayout fab, View snackbar) {
            float translationY = this.getFabTranslationYForSnackbar(parent, fab);
            if(translationY != this.mTranslationY) {
                ViewCompat.animate(fab).cancel();
                if(Math.abs(translationY - this.mTranslationY) == (float)snackbar.getHeight()) {
                    ViewCompat.animate(fab).translationY(translationY).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener((ViewPropertyAnimatorListener)null);
                } else {
                    ViewCompat.setTranslationY(fab, translationY);
                }

                this.mTranslationY = translationY;
            }

        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionLayout fab) {
            float minOffset = 0.0F;
            List dependencies = parent.getDependencies(fab);
            int i = 0;

            for(int z = dependencies.size(); i < z; ++i) {
                View view = (View)dependencies.get(i);
                if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
                }
            }

            return minOffset;
        }

        private void animateIn(FloatingActionLayout layout) {
            layout.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= 14) {
                // removed the scale X & Y to avoid strange animation behavior with the FAB menu
                ViewCompat.animate(layout).alpha(1.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener((ViewPropertyAnimatorListener)null).start();
            } else {
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(layout.getContext(), android.support.design.R.anim.fab_in);
                anim.setDuration(200L);
                anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                layout.startAnimation(anim);
            }

        }

        private void animateOut(final FloatingActionLayout layout) {
            if(Build.VERSION.SDK_INT >= 14) {
                // removed the scale X & Y to avoid strange animation behavior with the FAB menu
                ViewCompat.animate(layout).alpha(0.0F).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer().setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        Behavior.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        Behavior.this.mIsAnimatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        Behavior.this.mIsAnimatingOut = false;
                        view.setVisibility(View.GONE);
                    }
                }).start();
            } else {
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(layout.getContext(), android.support.design.R.anim.fab_out);
                anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                anim.setDuration(200L);
                anim.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                    public void onAnimationStart(Animation animation) {
                        Behavior.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationEnd(Animation animation) {
                        Behavior.this.mIsAnimatingOut = false;
                        layout.setVisibility(View.GONE);
                    }
                });
                layout.startAnimation(anim);
            }

        }

        static {
            SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        }
    }
}
