package kr.rokoroku.mbus;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;

/**
 * Created by rok on 2015. 5. 29..
 */
public abstract class AbstractBaseActivity extends AppCompatActivity implements ActionMode.Callback {

    private static final String STATE_SELECTED_POSITION = "drawer_selected";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationDrawerView;

    private ViewGroup mUpperSurface;
    private View mUpperSurfaceShadow;
    private Toolbar mToolbar;

    private ViewGroup mContentFrame;
    private View mContentView;
    private View mAdView;

    private RecyclerView.OnScrollListener mScrollListener;
    private RecyclerView.ItemDecoration mItemDecoration;
    private int mUpperSurfaceHeight = 0;
    private int mThemeId = 0;
    private boolean mIsActivityVisible;

    private Menu mMenu;

    private Handler mHandler;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.mThemeId = BaseApplication.getInstance().getCurrentTheme();
        this.setTheme(mThemeId);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        mAdView = findViewById(R.id.ad_view);
        if (mAdView != null) {
            if (!BaseApplication.showAd) {
                mAdView.setVisibility(View.GONE);
            }
        }
        initToolbar();
        initDrawer();

        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Log.e("UncaughtException", thread.getName());
            ex.printStackTrace();
            defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        if (refresh != null && refresh.getIcon() != null) {
            Drawable icon = refresh.getIcon();
            ViewUtils.setTint(icon, ThemeUtils.getThemeColor(this, android.R.attr.textColorPrimary));
            refresh.setIcon(icon);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        int themeId = BaseApplication.getInstance().getCurrentTheme();
        if (themeId != mThemeId) {
            mThemeId = themeId;
            setTheme(themeId);
            recreate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return true;

//            case R.id.nav_action_change_theme:
//                BaseApplication.getInstance().switchTheme();
//                recreate();
//                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        if (mContentFrame == null) {
            mContentFrame = (ViewGroup) findViewById(R.id.content_frame);
        } else if (mContentView != null) {
            mContentFrame.removeView(mContentView);
        }
        mContentView = View.inflate(this, layoutResId, mContentFrame);
    }

    public View getContentView() {
        return mContentView;
    }

    public void setDrawerEnable(boolean enable) {
        mDrawerLayout.setDrawerLockMode(enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showToolbarLayer();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.post(() -> mScrollListener.onScrolled(recyclerView, 0, -1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mIsActivityVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseFacade.getInstance().commitAsync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mIsActivityVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt(STATE_SELECTED_POSITION, mNavigationDrawerView.);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        this.mActionModeCallback = callback;
        this.mActionMode = getToolbar().startActionMode(this);
        return mActionMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        boolean result = true;
        if (mActionModeCallback != null) {
            result = mActionModeCallback.onCreateActionMode(mode, menu);
        }
        if (result) {
            if (mUpperSurface != null) {
                mUpperSurface.setY(0);
            }
        }
        return result;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        boolean result = false;
        if (mActionModeCallback != null) {
            result = mActionModeCallback.onPrepareActionMode(mode, menu);
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        boolean result = false;
        if (mActionModeCallback != null) {
            result = mActionModeCallback.onActionItemClicked(mode, item);
        }
        return result;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mActionModeCallback != null) {
            mActionModeCallback.onDestroyActionMode(mode);
        }
        mActionMode = null;
        mActionModeCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else if (mActionMode != null) {
            mActionMode.finish();
        } else {
            super.onBackPressed();
        }
    }

    public RecyclerView.ItemDecoration getAppBarHeaderSpacingItemDecoration() {
        if (mItemDecoration == null) {
            mItemDecoration = new RecyclerView.ItemDecoration() {
                int actionBarSize = (int) (ThemeUtils.getDimension(AbstractBaseActivity.this, R.attr.actionBarSize)
                                        + ViewUtils.dpToPixel(6f, getResources()));

                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = actionBarSize;
                    }
                }
            };
        }
        return mItemDecoration;
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(mToolbar);

            // init marquee animation to  toolbar title
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);

            TextView titleTextView = null;
            titleTextView = (TextView) f.get(mToolbar);
            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setGravity(Gravity.CENTER_VERTICAL);
            titleTextView.setMarqueeRepeatLimit(-1);

        } catch (Exception ignored) {

        }

        // set parallax effect to toolbar
        initParallaxEffect();
    }

    private void initParallaxEffect() {
        mHandler = new Handler();
        mUpperSurface = (ViewGroup) findViewById(R.id.upper_surface);
        mUpperSurfaceShadow = findViewById(R.id.upper_surface_shadow);
        mUpperSurfaceHeight = 0;
        mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mUpperSurfaceHeight == 0) {
                    mUpperSurfaceHeight = mUpperSurface.getHeight();
                }

                if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 5) {
                    final float futureShadowAlpha = (float) recyclerView.computeVerticalScrollOffset() / mUpperSurfaceHeight;
                    if (futureShadowAlpha > 1) {
                        mUpperSurfaceShadow.setAlpha(1f);
                    } else {
                        mUpperSurfaceShadow.setAlpha(futureShadowAlpha);
                    }
                } else {
                    mUpperSurfaceShadow.setAlpha(0f);
                }

                if (getActionMode() == null) {
                    final float offset = dy * .66f;
                    final float futurePosY = mUpperSurface.getY() - offset;
                    if (futurePosY <= -mUpperSurfaceHeight) {
                        mUpperSurface.setY(-mUpperSurfaceHeight);
                    } else if (futurePosY >= 0) {
                        mUpperSurface.setY(0);
                    } else {
                        mUpperSurface.animate().cancel();
                        mUpperSurface.setY(futurePosY);
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.postDelayed(() -> {
                            if (offset < 0) {
                                showToolbarLayer();
                            } else if (recyclerView.computeVerticalScrollOffset() >= mUpperSurfaceHeight) {
                                hideToolbarLayer();
                            }
                        }, 300);
                    }
                } else {
                    mUpperSurface.setY(0);
                }
            }
        };
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                checkLocationForDrawerMenu();
            }
        });
        mNavigationDrawerView = (NavigationView) findViewById(R.id.navigation_drawer_view);
        mNavigationDrawerView.setNavigationItemSelectedListener(this::onOptionsItemSelected);
        checkLocationForDrawerMenu();
    }

    private void checkLocationForDrawerMenu() {
        Menu menu = mNavigationDrawerView.getMenu();
        if(menu != null){
            MenuItem menuItem = menu.findItem(R.id.nav_action_map);
            if (menuItem != null) {
                menuItem.setEnabled(LocationClient.isLocationProviderAvailable(getApplicationContext()));
            }
        }
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void hideToolbarLayer() {
        if (mUpperSurface != null) {
            mUpperSurface.animate()
                    .y(-mUpperSurfaceHeight)
                    .setDuration(300)
                    .start();
        }
    }

    public void showToolbarLayer() {
        if (mUpperSurface != null) {
            mUpperSurface.animate()
                    .y(0)
                    .setDuration(300)
                    .start();
        }
    }

    public void toggleToolbarLayer() {
        if (mUpperSurface != null) {
            if (mUpperSurface.getY() == 0) {
                hideToolbarLayer();
            } else {
                showToolbarLayer();
            }
        }
    }

    public ViewGroup getToolbarLayer() {
        return mUpperSurface;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public RecyclerView.OnScrollListener getScrollListener() {
        return mScrollListener;
    }

    public boolean isActivityVisible() {
        return mIsActivityVisible;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

}
