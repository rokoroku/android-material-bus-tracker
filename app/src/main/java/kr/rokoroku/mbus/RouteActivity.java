package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.ui.adapter.RouteAdapter;
import kr.rokoroku.mbus.data.RouteDataProvider;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.Database;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.ViewUtils;


public class RouteActivity extends AbstractBaseActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "expandable_state";

    public static final String EXTRA_KEY_ROUTE = "route";
    public static final String EXTRA_KEY_REDIRECT_STATION_ID = "station_id";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CoordinatorLayout mCoordinatorLayout;
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

    private LocationClient mLocationClient;

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
        if (!refreshData(false)) {
            scheduleTimer(5000);
        }
        if (mBusRouteAdapter != null) {
            mBusRouteAdapter.notifyDataSetChanged();
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
        Route route = intent.getParcelableExtra(EXTRA_KEY_ROUTE);
        mRedirectStationId = intent.getStringExtra(EXTRA_KEY_REDIRECT_STATION_ID);
        if (route == null) {
            finish();

        } else {
            Route storedRoute = Database.getInstance().getRoute(route.getProvider(), route.getId());
            if (storedRoute != null) route = storedRoute;

            if (mRouteDataProvider == null) mRouteDataProvider = new RouteDataProvider(route);
            else mRouteDataProvider.setRoute(route);

            if (mBusRouteAdapter != null) {
                runOnUiThread(() -> refreshData(true));
            }
        }

        // set intent
        setIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("RouteActivity", "onStop");
        if (mRouteDataProvider != null) {
            final Route route = mRouteDataProvider.getRoute();
            if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                Database.getInstance().putRoute(route);
                Log.d("RouteActivity", "onStop222");
            }
        }
    }

    private synchronized boolean refreshData(boolean force) {

        Route route = mRouteDataProvider.getRoute();
        setToolbarTitle(route);

        mSwipeRefreshLayout.setRefreshing(true);

        if (TimeUtils.checkShouldUpdate(route.getLastUpdateTime()) || force) {
            if (isRefreshing) return true;
            else isRefreshing = true;

            final boolean needUpdateRouteInfo = !mRouteDataProvider.isRouteInfoAvailable() || route.getType() == null || route.getType().equals(RouteType.UNKNOWN);
            ApiFacade.getInstance().getRouteData(route, mRouteDataProvider, new ApiFacade.SimpleProgressCallback() {
                @Override
                public void onComplete(boolean success) {
                    mBusRouteAdapter.notifyDataSetChanged();
                    if (success) {
                        if (needUpdateRouteInfo) {
                            setToolbarTitle(mRouteDataProvider.getRoute());
                        }
                        ApiFacade.getInstance().getRouteRealtimeData(route, mRouteDataProvider, new ApiFacade.SimpleProgressCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                if (success) {
                                    scheduleTimer(BaseApplication.REFRESH_INTERVAL);
                                }
                                mSwipeRefreshLayout.postDelayed(() -> {
                                    isRefreshing = false;
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    if (mRedirectStationId != null) {
                                        redirectTo(mRedirectStationId);
                                        mRedirectStationId = null;
                                    }
                                }, 500);
                                if (mRefreshActionItem != null)
                                    mRefreshActionItem.setActionView(null);
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
            return true;

        } else if (!isRefreshing) {
            mSwipeRefreshLayout.postDelayed(() -> {
                mSwipeRefreshLayout.setRefreshing(false);
                mBusRouteAdapter.notifyDataSetChanged();
            }, 500);
        }
        return false;
    }

    private void redirectTo(String localStationId) {
        if (localStationId != null) {
            int count = mRouteDataProvider.getCount();
            for (int i = 0; i < count; i++) {
                RouteStation routeStation = mRouteDataProvider.getItem(i).getRouteStation();
                if (routeStation != null && localStationId.equals(routeStation.getLocalId())) {
                    int fourthHeight = ViewUtils.getScreenSize(RouteActivity.this).y / 4;
                    ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(i, fourthHeight);
                    mRecyclerViewExpandableItemManager.expandGroup(i + 1);
                    break;
                }
            }
        }
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
            ActionMode actionMode = getActionMode();
            if (actionMode != null) {
                if (mLastExpandedPosition >= 0 && mRecyclerViewExpandableItemManager.isGroupExpanded(mLastExpandedPosition)) {
                    RouteStation routeStation = mBusRouteAdapter.getItem(mLastExpandedPosition).getRouteStation();
                    addToFavorite(routeStation);
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
                RouteStation routeStation = mBusRouteAdapter.getItem(mLastExpandedPosition).getRouteStation();
                addToFavorite(routeStation);

            } else if (getActionMode() == null) {
                startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.setTitle(R.string.add_to_favorite_hint_select_one);
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
        mAddFavoriteSelectedButton.setTag(EXTRA_KEY_REDIRECT_STATION_ID);

        mLocationButton.setShowProgressBackground(false);
        mLocationButton.setOnClickListener(v -> {
            if (mLocationClient == null) {
                initLocationClient();
                mLocationClient.start(false);
            } else {
                mLocationClient.start(true);
            }
            mLocationButton.setIndeterminate(true);
            mLocationButton.setDrawableTint(ThemeUtils.getThemeColor(this, R.attr.colorAccent));
        });
        mPlusButton.setOnMenuToggleListener(b -> {
            if (b) {
                mLocationButton.hide(true);
            } else {
                mLocationButton.postDelayed(() -> mLocationButton.show(true), 200);
            }
        });
    }

    private void initLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = LocationClient.with(this)
                    .listener(new LocationClient.Listener() {
                        @Override
                        public void onLocationUpdate(Location location) {
                            List<RouteStation> routeStationList = mRouteDataProvider.getRouteStationList();
                            if (routeStationList != null && !routeStationList.isEmpty()) {
                                List<RouteStation> sortedStationList = new ArrayList<>(routeStationList);

                                Station.DistanceComparator distanceComparator
                                        = new Station.DistanceComparator(location.getLatitude(), location.getLongitude());
                                Collections.sort(sortedStationList, distanceComparator);

                                RouteStation closestStation = null;
                                for (RouteStation routeStation : sortedStationList) {
                                    if (routeStation.getLocalId() != null) {
                                        closestStation = routeStation;
                                        break;
                                    }
                                }

                                if (closestStation == null || 1000 < GeoUtils.calculateDistanceInMeter(
                                        new LatLng(location.getLatitude(), location.getLongitude()),
                                        new LatLng(closestStation.getLatitude(), closestStation.getLongitude()))) {
                                    Snackbar.make(mCoordinatorLayout, R.string.error_failed_to_retrieve_near_station, Snackbar.LENGTH_LONG).show();

                                } else {
                                    redirectTo(closestStation.getLocalId());
                                }
                            }

                            mLocationButton.postDelayed(() -> {
                                mLocationButton.setDrawableTint(Color.BLACK);
                                mLocationButton.setIndeterminate(false);
                                mLocationButton.setProgress(0, false);
                            }, 600);
                        }

                        @Override
                        public void onError(String failReason, ConnectionResult connectionResult) {
                            if (failReason != null) {
                                Snackbar.make(mCoordinatorLayout, "오류가 발생했습니다: " + failReason, Snackbar.LENGTH_LONG).show();
                            }
                            mLocationButton.postDelayed(() -> {
                                mLocationButton.setDrawableTint(Color.BLACK);
                                mLocationButton.setIndeterminate(false);
                                mLocationButton.setProgress(0, false);
                            }, 600);
                        }
                    }).build();
        }
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
                intent.putExtra(MapsActivity.EXTRA_KEY_ROUTE, (Parcelable) mRouteDataProvider.getRoute());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
                        refreshData(false);
                    }
                });
            }
        }, delay);
    }

    private void addToFavorite(RouteStation routeStation) {

        List<FavoriteGroup> favoriteGroups = FavoriteFacade.getInstance().getCurrentFavorite().getFavoriteGroups();
        if (favoriteGroups.isEmpty()) {
            favoriteGroups.add(new FavoriteGroup(getString(R.string.default_favorite_group)));
        }
        FavoriteGroup favoriteGroup = favoriteGroups.get(0);
        FavoriteGroup.FavoriteItem item = new FavoriteGroup.FavoriteItem(mRouteDataProvider.getRoute());
        if (routeStation != null) item.setExtraData(routeStation);

        favoriteGroup.add(item);
        Snackbar.make(mCoordinatorLayout, R.string.added_to_favorite, Snackbar.LENGTH_LONG).show();
    }

}
