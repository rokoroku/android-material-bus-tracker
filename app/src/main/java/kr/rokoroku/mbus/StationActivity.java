package kr.rokoroku.mbus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.api.ApiWrapperInterface;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.ui.adapter.StationAdapter;
import kr.rokoroku.mbus.data.StationDataProvider;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.ViewUtils;


public class StationActivity extends AbstractBaseActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "expandable_state";
    public static final String EXTRA_KEY_STATION = "station";
    public static final String EXTRA_KEY_REDIRECT_ROUTE_ID = "route_id";

    private RecyclerView mRecyclerView;
    private CoordinatorLayout mCoordinatorLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MenuItem mRefreshActionItem;

    private FloatingActionMenu mPlusButton;
    private FloatingActionButton mAddFavoriteButton;
    private FloatingActionButton mAddFavoriteSelectedButton;

    private StationDataProvider mStationDataProvider;
    private RecyclerView.Adapter mBusStationAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewExpandableItemManager.OnGroupExpandListener mGroupExpandListener;

    private Timer mTimer;
    private String mRedirectRouteId;
    private int mLastExpandedPosition = -1;
    private boolean isRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        setDrawerEnable(false);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //inject view reference
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mPlusButton = (FloatingActionMenu) findViewById(R.id.fab_plus);
        mAddFavoriteButton = (FloatingActionButton) findViewById(R.id.fab_bookmark);
        mAddFavoriteSelectedButton = (FloatingActionButton) findViewById(R.id.fab_bookmark_selection);

        //setup refresh layout
        int actionbarHeight = ThemeUtils.getDimension(this, R.attr.actionBarSize);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //get extra
        onNewIntent(getIntent());

        //setup recycler view
        initRecyclerView(savedInstanceState);

        //init floating action button
        initFloatingActionButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!refreshData(false)) {
            scheduleTimer(5000);
        }
        if (mBusStationAdapter != null) {
            mBusStationAdapter.notifyDataSetChanged();
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

        // get extras from new intent
        Station station = intent.getParcelableExtra(EXTRA_KEY_STATION);
        mRedirectRouteId = intent.getStringExtra(EXTRA_KEY_REDIRECT_ROUTE_ID);
        if (station == null) {
            finish();

        } else {
            if (station.getId() != null) {
                Station storedStation = DatabaseFacade.getInstance().getStation(station.getProvider(), station.getId());
                if (storedStation != null) station = storedStation;
            }

            if (mStationDataProvider == null)
                mStationDataProvider = new StationDataProvider(station);
            else mStationDataProvider.setStation(station);

            if (mBusStationAdapter != null) {
                runOnUiThread(() -> refreshData(true));
            }
        }

        // set intent
        setIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mStationDataProvider != null) {
            final Station station = mStationDataProvider.getStation();
            if (station.isEveryRouteInfoAvailable()) {
                DatabaseFacade.getInstance().putStationForEachProvider(station);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mStationDataProvider != null && mStationDataProvider.getStation() != null) {
            Station station = mStationDataProvider.getStation();
            MenuItem item = menu.findItem(R.id.action_map);
            if (item != null) {
                boolean isMapAvailable = station.getLongitude() != null && station.getLatitude() != null;
                item.setEnabled(isMapAvailable);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private synchronized boolean refreshData(boolean force) {

        Station station = mStationDataProvider.getStation();
        setTitle(station.getName());

        mSwipeRefreshLayout.setRefreshing(true);
        if (TimeUtils.checkShouldUpdate(station.getLastUpdateTime()) || force) {
            if (isRefreshing) return false;
            else isRefreshing = true;

            if (station.getId() == null && station.getLocalId() != null && station.getProvider().equals(Provider.SEOUL)) {
                ApiFacade.getInstance().getSeoulWebRestClient().getStationBaseInfo(station.getLocalId(), new ApiWrapperInterface.Callback<Station>() {
                    @Override
                    public void onSuccess(Station result) {
                        if (result != null) {
                            mStationDataProvider.setStation(result);
                            DatabaseFacade.getInstance().putStation(Provider.SEOUL, result);
                            refreshData(true);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        Snackbar.make(mCoordinatorLayout, getString(R.string.error_with_reason, t), Snackbar.LENGTH_LONG).show();
                    }
                });
            } else {
                ApiFacade.getInstance().getStationData(station, mStationDataProvider, new SimpleProgressCallback<Station>() {
                    @Override
                    public void onComplete(boolean success, Station value) {
                        mBusStationAdapter.notifyDataSetChanged();
                        Log.d("StationActivity", "upperOnComplete " + success);
                        if (success && mStationDataProvider.getRawStationRouteList() != null) {
                            if (mRedirectRouteId != null) {
                                final String redirectId = mRedirectRouteId;
                                mRedirectRouteId = null;
                                mRecyclerView.postDelayed(() -> {
                                    int count = mStationDataProvider.getCount();
                                    for (int i = 0; i < count; i++) {
                                        StationRoute stationRoute = mStationDataProvider.getItem(i).getStationRoute();
                                        if (stationRoute != null && redirectId.equals(stationRoute.getRouteId())) {
                                            int fifthHeight = ViewUtils.getScreenSize(StationActivity.this).y / 5;
                                            ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(i, fifthHeight);
                                            mRecyclerViewExpandableItemManager.expandGroup(i);
                                            break;
                                        }
                                    }
                                }, 500);
                            }
                            ApiFacade.getInstance().getArrivalInfo(mStationDataProvider.getStation(), null, new SimpleProgressCallback<List<ArrivalInfo>>() {
                                @Override
                                public void onComplete(boolean success, List<ArrivalInfo> value) {
                                    if (success && value != null) {
                                        mStationDataProvider.setArrivalInfos(value);
                                        mBusStationAdapter.notifyDataSetChanged();
                                        scheduleTimer(BaseApplication.REFRESH_INTERVAL);
                                    }
                                    if (mRefreshActionItem != null) {
                                        mRefreshActionItem.setActionView(null);
                                    }
                                    mSwipeRefreshLayout.postDelayed(() -> {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        isRefreshing = false;
                                    }, 500);
                                }

                                @Override
                                public void onProgressUpdate(int current, int target) {
                                    mBusStationAdapter.notifyDataSetChanged();
                                    Log.d("StationActivity", String.format("onProgressUpdate (%d/%d)", current, target));
                                }

                                @Override
                                public void onError(int progress, Throwable t) {
                                    Toast.makeText(StationActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                    t.printStackTrace();
                                }
                            });
                        } else {
                            Toast.makeText(StationActivity.this, "Complete but no stationRoutes", Toast.LENGTH_LONG).show();
                            if (mRefreshActionItem != null) {
                                mRefreshActionItem.setActionView(null);
                            }
                            mSwipeRefreshLayout.postDelayed(() -> {
                                mSwipeRefreshLayout.setRefreshing(false);
                                isRefreshing = false;
                                if (mRedirectRouteId != null) {
                                    final String redirectId = mRedirectRouteId;
                                    int count = mStationDataProvider.getCount();
                                    for (int i = 0; i < count; i++) {
                                        StationRoute stationRoute = mStationDataProvider.getItem(i).getStationRoute();
                                        if (stationRoute != null && redirectId.equals(stationRoute.getRouteId())) {
                                            int fifthHeight = ViewUtils.getScreenSize(StationActivity.this).y / 5;
                                            ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(i, fifthHeight);
                                            mRecyclerViewExpandableItemManager.expandGroup(i);
                                            mRedirectRouteId = null;
                                            break;
                                        }
                                    }
                                }
                            }, 500);
//                    scheduleTimer(5000);
                        }
                    }

                    @Override
                    public void onProgressUpdate(int current, int target) {
                        Log.d("StationActivity", String.format("upperOnProgressUpdate (%d/%d)", current, target));
                    }

                    @Override
                    public void onError(int progress, Throwable t) {
                        Snackbar.make(mCoordinatorLayout, getString(R.string.error_with_reason, t.getMessage()), Snackbar.LENGTH_LONG).show();
                        t.printStackTrace();
                    }
                });
            }
            return true;

        } else if (!isRefreshing) {
            mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 500);
        }
        return false;
    }

    private void initRecyclerView(Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(this);

        //item manager
        Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //set data provider to adapter
        mBusStationAdapter = new StationAdapter(mStationDataProvider);
        RecyclerView.Adapter wrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mBusStationAdapter);

        //set adapter
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addOnScrollListener(getScrollListener());
        mRecyclerView.addItemDecoration(getAppBarHeaderSpacingItemDecoration());

        //set listener
        mGroupExpandListener = (i, b) -> {
            if (mLastExpandedPosition >= 0 && mLastExpandedPosition != i) {
                mRecyclerViewExpandableItemManager.collapseGroup(mLastExpandedPosition);
            }
            mLastExpandedPosition = i;
            ActionMode actionMode = getActionMode();
            if (actionMode != null) {
                if (mLastExpandedPosition >= 0 && mRecyclerViewExpandableItemManager.isGroupExpanded(mLastExpandedPosition)) {
                    StationRoute stationRoute = mStationDataProvider.getItem(mLastExpandedPosition).getStationRoute();
                    addToFavorite(stationRoute);
                }
                actionMode.finish();
            }
        };
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(mGroupExpandListener);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
    }

    private void initFloatingActionButtons() {
        mAddFavoriteButton.setOnClickListener(v -> {
            addToFavorite(null);
            mPlusButton.close(true);
        });

        mAddFavoriteSelectedButton.setOnClickListener(v -> {
            if (mLastExpandedPosition >= 0 && mRecyclerViewExpandableItemManager.isGroupExpanded(mLastExpandedPosition)) {
                StationRoute stationRoute = mStationDataProvider.getItem(mLastExpandedPosition).getStationRoute();
                addToFavorite(stationRoute);

            } else if (getActionMode() == null) {
                startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.setTitle(R.string.hint_select_favorite_link);
                        // init marquee animation to toolbar title
                        try {
                            View modeCustomView = mode.getCustomView();
                            Field f = modeCustomView.getClass().getDeclaredField("mTitleTextView");
                            f.setAccessible(true);

                            TextView titleTextView = null;
                            titleTextView = (TextView) f.get(modeCustomView);
                            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            titleTextView.setFocusable(true);
                            titleTextView.setFocusableInTouchMode(true);
                            titleTextView.requestFocus();
                            titleTextView.setSingleLine(true);
                            titleTextView.setSelected(true);
                            titleTextView.setMarqueeRepeatLimit(-1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }
                });
            }
            mPlusButton.close(true);
        });
    }

    @Override
    public void onRefresh() {
        refreshData(true);
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

            case R.id.action_map:
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra(MapsActivity.EXTRA_KEY_STATION, (Parcelable) mStationDataProvider.getStation());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void scheduleTimer(int delay) {

        //cancel all scheduled tasks
        if (mTimer != null) mTimer.cancel();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (isActivityVisible()) {
                        refreshData(false);
                    }
                });
            }
        }, delay);
    }

    private void addToFavorite(StationRoute stationRoute) {
        FavoriteFacade.getInstance().addToFavorite(mStationDataProvider.getStation(), stationRoute);
        Snackbar.make(mCoordinatorLayout, R.string.alert_added_to_favorite, Snackbar.LENGTH_LONG).show();
    }
}
