package kr.rokoroku.mbus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.adapter.RouteAdapter;
import kr.rokoroku.mbus.adapter.RouteDataProvider;
import kr.rokoroku.mbus.core.ApiCaller;
import kr.rokoroku.mbus.core.DatabaseHelper;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.model.FavoriteGroup;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.ViewUtils;
import kr.rokoroku.mbus.widget.FloatingActionLayout;


public class RouteActivity extends AbstractBaseActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "12345";
    public static final String EXTRA_KEY_ROUTE = "route";
    public static final String EXTRA_KEY_REDIRECT_STATION_ID = "station_id";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionLayout mFloatingActionLayout;
    private FloatingActionMenu mPlusButton;
    private FloatingActionButton mLocationButton;
    private FloatingActionButton mAddFavoriteButton;
    private FloatingActionButton mAddFavoriteSelectedButton;

    private RouteDataProvider mRouteDataProvider;
    private RouteAdapter mBusRouteAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewExpandableItemManager.OnGroupExpandListener mGroupExpandListener;
    private MenuItem mRefreshActionItem;

    private Timer mTimer;
    private String mRedirectStationId;
    private int mLastExpandedPosition = -1;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        setDrawerEnable(false);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //inject view reference
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mFloatingActionLayout = (FloatingActionLayout) findViewById(R.id.fab_layout);
        mPlusButton = (FloatingActionMenu) findViewById(R.id.fab_plus);
        mLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mAddFavoriteButton = (FloatingActionButton) findViewById(R.id.fab_bookmark);
        mAddFavoriteSelectedButton = (FloatingActionButton) findViewById(R.id.fab_bookmark_selection);

        //setup refresh layout
        int actionbarHeight = ThemeUtils.getDimension(this, R.attr.actionBarSize);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //get intent extras
        onNewIntent(getIntent());

        //setup recycler view
        initRecyclerView(savedInstanceState);

        //init floating action button
        initFloatingActionButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TimeUtils.checkShouldUpdate(mRouteDataProvider.getLastUpdateTime())) {
            refreshData(mRouteDataProvider);
        } else {
            scheduleTimer(5000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //get extra
        Route route = intent.getParcelableExtra(EXTRA_KEY_ROUTE);
        if (route == null) {
            finish();

        } else {
            Route storedRoute = DatabaseHelper.getInstance().getRoute(route.getProvider(), route.getId());
            if (storedRoute != null) route = storedRoute;

            if (mRouteDataProvider == null) mRouteDataProvider = new RouteDataProvider(route);
            else mRouteDataProvider.setRoute(route);

            mRedirectStationId = intent.getStringExtra(EXTRA_KEY_REDIRECT_STATION_ID);

            runOnUiThread(() -> refreshData(mRouteDataProvider));
        }

        setIntent(intent);
    }

    private synchronized void refreshData(RouteDataProvider routeDataProvider) {

        mSwipeRefreshLayout.setRefreshing(true);

        Route route = routeDataProvider.getRoute();
        setToolbarTitle(route);

        if(isRefreshing) return;
        else isRefreshing = true;

        final boolean needUpdateRouteInfo = !mRouteDataProvider.isRouteInfoAvailable() || route.getType() == null || route.getType().equals(RouteType.UNKNOWN);
        ApiCaller.getInstance().fillRouteData(route.getId(), routeDataProvider, new ApiCaller.SimpleProgressCallback() {
            @Override
            public void onComplete(boolean success) {
                mBusRouteAdapter.notifyDataSetChanged();
                if (success) {
                    if (needUpdateRouteInfo) {
                        setToolbarTitle(routeDataProvider.getRoute());
                    }
                    ApiCaller.getInstance().fillRouteRealtimeLocation(routeDataProvider, new ApiCaller.SimpleProgressCallback() {
                        @Override
                        public void onComplete(boolean success) {
                            if(success) {
                                scheduleTimer(30000);
                            }
                            mSwipeRefreshLayout.postDelayed(() -> {
                                isRefreshing = false;
                                mSwipeRefreshLayout.setRefreshing(false);
                                if (mRedirectStationId != null) {
                                    final String redirectId = mRedirectStationId;
                                    mRedirectStationId = null;

                                    int count = routeDataProvider.getCount();
                                    for (int i = 0; i < count; i++) {
                                        RouteStation routeStation = routeDataProvider.getItem(i).getRouteStation();
                                        if (routeStation != null && redirectId.equals(routeStation.getLocalId())) {
                                            int fourthHeight = ViewUtils.getScreenSize(RouteActivity.this).y / 4;
                                            ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(i, fourthHeight);
                                            mRecyclerViewExpandableItemManager.expandGroup(i + 1);
                                            break;
                                        }
                                    }
                                }
                            }, 500);
                            if (mRefreshActionItem != null) mRefreshActionItem.setActionView(null);
                        }

                        @Override
                        public void onProgressUpdate(int current, int target) {
                            mBusRouteAdapter.notifyDataSetChanged();
                            //mGroupExpandListener.onGroupExpand(0, false);
                        }

                        @Override
                        public void onError(int progress, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(RouteActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mSwipeRefreshLayout.postDelayed(() -> {
                        isRefreshing = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                    }, 500);
                }
            }

            @Override
            public void onProgressUpdate(int current, int target) {
                mBusRouteAdapter.notifyDataSetChanged();
                //mGroupExpandListener.onGroupExpand(0, false);
                Log.d("RouteActivity", String.format("onProgressUpdate (%d/%d)", current, target));
            }

            @Override
            public void onError(int progress, Throwable t) {
                t.printStackTrace();
                Toast.makeText(RouteActivity.this, "error", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setToolbarTitle(Route route) {
        String routeName = route.getName();
        if (routeName != null) {
            setTitle(routeName);
        } else {
            setTitle("");
        }

        RouteType routeType = route.getType();
        if (routeType != null && !RouteType.UNKNOWN.equals(routeType)) {
            getToolbar().setTitleTextColor(routeType.getColor(this));
        }
    }

    private void initRecyclerView(Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(this);

        //item manager
        Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //set data provider to adapter
        mBusRouteAdapter = new RouteAdapter(mRouteDataProvider);
        RecyclerView.Adapter wrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mBusRouteAdapter);       // wrap for expanding

        //set item layout
        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        //set adapter
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addItemDecoration(getAppBarHeaderSpacingItemDecoration());
        mRecyclerView.addOnScrollListener(getScrollListener());

        //set listener
        mGroupExpandListener = (i, b) -> {
            if (mLastExpandedPosition >= 0 && mLastExpandedPosition != i) {
                mRecyclerViewExpandableItemManager.collapseGroup(mLastExpandedPosition);
            }
            mLastExpandedPosition = i;
        };
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(mGroupExpandListener);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
    }

    private void initFloatingActionButtons() {
        mAddFavoriteButton.setOnClickListener(v -> {
            List<FavoriteGroup> favoriteGroups = FavoriteFacade.getInstance().getCurrentFavorite().getFavoriteGroups();
            if (favoriteGroups.isEmpty()) {
                favoriteGroups.add(new FavoriteGroup(getString(R.string.default_favorite_group)));
            }
            FavoriteGroup favoriteGroup = favoriteGroups.get(0);
            FavoriteGroup.FavoriteItem item = new FavoriteGroup.FavoriteItem(mRouteDataProvider.getRoute());

            favoriteGroup.add(item);
            Snackbar.make(mCoordinatorLayout, R.string.added_to_favorite, Snackbar.LENGTH_LONG).show();
            mPlusButton.close(true);
        });
        mAddFavoriteSelectedButton.setOnClickListener(v -> {
            List<FavoriteGroup> favoriteGroups = FavoriteFacade.getInstance().getCurrentFavorite().getFavoriteGroups();
            if (favoriteGroups.isEmpty()) {
                favoriteGroups.add(new FavoriteGroup(getString(R.string.default_favorite_group)));
            }
            FavoriteGroup favoriteGroup = favoriteGroups.get(0);
            FavoriteGroup.FavoriteItem item = new FavoriteGroup.FavoriteItem(mRouteDataProvider.getRoute());

            if (mLastExpandedPosition >= 0 && mRecyclerViewExpandableItemManager.isGroupExpanded(mLastExpandedPosition)) {
                RouteStation routeStation = mBusRouteAdapter.getItem(mLastExpandedPosition).getRouteStation();
                item.setExtraData(routeStation);
            }

            favoriteGroup.add(item);
            Snackbar.make(mCoordinatorLayout, R.string.added_to_favorite, Snackbar.LENGTH_LONG).show();
            mPlusButton.close(true);
        });
        mAddFavoriteSelectedButton.setTag(EXTRA_KEY_REDIRECT_STATION_ID);

        mPlusButton.setOnMenuToggleListener(b -> {
            if (b) {
                mLocationButton.hide(true);
            } else {
                mLocationButton.postDelayed(() -> mLocationButton.show(true), 200);
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshData(mRouteDataProvider);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_refresh:
                mRefreshActionItem = item;
                item.setActionView(R.layout.widget_refresh_action_view);
                onRefresh();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void scheduleTimer(long delay) {

        //cancel all scheduled tasks
        if (mTimer != null) mTimer.cancel();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (isActivityVisible()) {
                        refreshData(mRouteDataProvider);
                    }
                });
            }
        }, delay);
    }
}
