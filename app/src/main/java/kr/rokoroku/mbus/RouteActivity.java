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
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.data.RouteDataProvider;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.ui.adapter.RouteAdapter;
import kr.rokoroku.mbus.ui.widget.SplitCardView;
import kr.rokoroku.mbus.util.GeoUtils;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.ViewUtils;
import kr.rokoroku.widget.ConnectorView;


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
        if (LocationClient.isLocationProviderAvailable(this)) {
            mLocationButton.setEnabled(true);
            mLocationButton.show(true);
        } else {
            mLocationButton.setEnabled(false);
            mLocationButton.hide(true);
        }
        showToolbarLayer();
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
            Route storedRoute = DatabaseFacade.getInstance().getRoute(route.getProvider(), route.getId());
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
        if (mRouteDataProvider != null) {
            final Route route = mRouteDataProvider.getRoute();
            if (route.isRouteBaseInfoAvailable() && route.isRouteStationInfoAvailable()) {
                DatabaseFacade.getInstance().putRoute(route);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized boolean refreshData(boolean force) {

        Route route = mRouteDataProvider.getRoute();
        setToolbarTitle(route);
        showToolbarLayer();

        mSwipeRefreshLayout.setRefreshing(true);

        if (TimeUtils.checkShouldUpdate(route.getLastUpdateTime()) || force) {
            if (isRefreshing) return true;
            else isRefreshing = true;

            final boolean needUpdateRouteInfo = !mRouteDataProvider.isRouteInfoAvailable() || route.getType() == null || route.getType().equals(RouteType.UNKNOWN);
            ApiFacade.getInstance().getRouteData(route, mRouteDataProvider, new SimpleProgressCallback() {
                @Override
                public void onComplete(boolean success, Object value) {
                    mBusRouteAdapter.notifyDataSetChanged();
                    if (success) {
                        if (needUpdateRouteInfo) {
                            setToolbarTitle(mRouteDataProvider.getRoute());
                        }
                        ApiFacade.getInstance().getRouteRealtimeData(route, mRouteDataProvider, new SimpleProgressCallback() {
                            @Override
                            public void onComplete(boolean success, Object value) {
                                if (success) {
                                    scheduleTimer(BaseApplication.REFRESH_INTERVAL);
                                    mBusRouteAdapter.clearCache();
                                    mBusRouteAdapter.notifyDataSetChanged();
                                }
                                ViewUtils.runOnUiThread(() -> {
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
                        ViewUtils.runOnUiThread(() -> {
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

                if (mRedirectStationId != null) {
                    redirectTo(mRedirectStationId);
                    mRedirectStationId = null;
                }
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
        mBusRouteAdapter.setExpandableItemManager(mRecyclerViewExpandableItemManager);
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
//            if (mLastExpandedPosition >= 0 && mLastExpandedPosition != i) {
//                mRecyclerViewExpandableItemManager.collapseGroup(mLastExpandedPosition);
//            }
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
                        mode.setTitle(R.string.hint_select_favorite_link);

                        // init marquee animation to toolbar title
                        try {
                            View modeCustomView = mode.getCustomView();
                            Field f = modeCustomView.getClass().getDeclaredField("mTitleTextView");
                            f.setAccessible(true);

                            TextView titleTextView;
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
                                RouteStation secondClosestStation = null;
                                for (RouteStation routeStation : sortedStationList) {
                                    if (routeStation.getLocalId() != null) {
                                        if (closestStation == null) {
                                            closestStation = routeStation;
                                        } else {
                                            secondClosestStation = routeStation;
                                            break;
                                        }
                                    }
                                }

                                if (closestStation != null) {
                                    int distance1 = GeoUtils.calculateDistanceInMeter(
                                            new LatLng(location.getLatitude(), location.getLongitude()),
                                            new LatLng(closestStation.getLatitude(), closestStation.getLongitude()));

                                    if (distance1 > 3000) {
                                        Snackbar.make(mCoordinatorLayout, R.string.error_failed_to_retrieve_near_station, Snackbar.LENGTH_LONG).show();

                                    } else if (secondClosestStation != null) {
                                        int distance2 = GeoUtils.calculateDistanceInMeter(
                                                new LatLng(location.getLatitude(), location.getLongitude()),
                                                new LatLng(secondClosestStation.getLatitude(), secondClosestStation.getLongitude()));

                                        if (Math.abs(distance1 - distance2) <= 30) {
                                            openChooserDialog(closestStation, secondClosestStation);

                                        } else {
                                            redirectTo(closestStation.getLocalId());
                                        }
                                    } else {
                                        redirectTo(closestStation.getLocalId());
                                    }

                                }
                            }

                            mLocationButton.postDelayed(() ->

                            {
                                mLocationButton.setDrawableTint(Color.BLACK);
                                mLocationButton.setIndeterminate(false);
                                mLocationButton.setProgress(0, false);
                            }

                                    , 600);
                        }

                        @Override
                        public void onError(String failReason, ConnectionResult connectionResult) {
                            if (failReason != null) {
                                Snackbar.make(mCoordinatorLayout, getString(R.string.error_with_reason, failReason), Snackbar.LENGTH_LONG).show();
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

    private void openChooserDialog(RouteStation station1, RouteStation station2) {
        View chooserView = View.inflate(RouteActivity.this, R.layout.popup_nearby_station_chooser, null);
        Route route = mRouteDataProvider.getRoute();

        SplitCardView stationView = (SplitCardView) chooserView.findViewById(R.id.station_1);
        TextView stationTitle = (TextView) stationView.findViewById(R.id.station_title);
        TextView stationDescription = (TextView) stationView.findViewById(R.id.station_description);
        ConnectorView connectorView = (ConnectorView) stationView.findViewById(R.id.connector);

        SplitCardView stationView2 = (SplitCardView) chooserView.findViewById(R.id.station_2);
        TextView stationTitle2 = (TextView) stationView2.findViewById(R.id.station_title);
        TextView stationDescription2 = (TextView) stationView2.findViewById(R.id.station_description);
        ConnectorView connectorView2 = (ConnectorView) stationView2.findViewById(R.id.connector);

        stationView.setRoundTop(true);
        stationView.setRoundBottom(true);
        stationView2.setRoundTop(true);
        stationView2.setRoundBottom(true);

        String destination = null;
        String destination2 = null;
        List<RouteStation> routeStationList = route.getRouteStationList();
        for (RouteStation routeStation : routeStationList) {
            if (destination != null && destination2 != null) {
                break;

            } else {
                if (destination == null) {
                    if (station1.getSequence() < routeStation.getSequence()) {
                        destination = routeStation.getName();
                    }
                }
                if (destination2 == null) {
                    if (station2.getSequence() < routeStation.getSequence()) {
                        destination2 = routeStation.getName();
                    }
                }
            }
        }

        int lastSeq = routeStationList.get(routeStationList.size() - 1).getSequence();
        connectorView.setConnectorType(station1.getSequence() == 1 ? ConnectorView.ConnectorType.START
                : station1.getSequence() == lastSeq ? ConnectorView.ConnectorType.END : ConnectorView.ConnectorType.NODE);
        connectorView2.setConnectorType(station2.getSequence() == 1 ? ConnectorView.ConnectorType.START
                : station2.getSequence() == lastSeq ? ConnectorView.ConnectorType.END : ConnectorView.ConnectorType.NODE);

        int margin = (int) ViewUtils.dpToPixel(3f, getResources());
        ((FrameLayout.LayoutParams) connectorView.getLayoutParams()).topMargin = margin;
        ((FrameLayout.LayoutParams) connectorView.getLayoutParams()).bottomMargin = margin;
        ((FrameLayout.LayoutParams) connectorView2.getLayoutParams()).topMargin = margin;
        ((FrameLayout.LayoutParams) connectorView2.getLayoutParams()).bottomMargin = margin;

        FavoriteFacade.Color stationColor1 = FavoriteFacade.getInstance().getFavoriteStationColor(station1);
        FavoriteFacade.Color stationColor2 = FavoriteFacade.getInstance().getFavoriteStationColor(station2);
        stationView.setCardBackgroundColor(stationColor1.getColor(this));
        stationView2.setCardBackgroundColor(stationColor2.getColor(this));

        stationTitle.setText(station1.getName());
        stationTitle2.setText(station2.getName());
        stationDescription.setText(getString(R.string.hint_route_heading_to, destination));
        stationDescription2.setText(getString(R.string.hint_route_heading_to, destination2));

        MaterialDialog materialDialog = new MaterialDialog.Builder(RouteActivity.this)
                .title(R.string.hint_choose_nearby_station)
                .customView(chooserView, false)
                .cancelable(true)
                .show();

        stationView.setOnClickListener(v -> {
            materialDialog.dismiss();
            redirectTo(station1.getLocalId());
        });
        stationView2.setOnClickListener(v -> {
            materialDialog.dismiss();
            redirectTo(station2.getLocalId());
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
        FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
        List<FavoriteGroup> favoriteGroups = favoriteFacade.getCurrentFavorite().getFavoriteGroups();
        if(favoriteGroups.size() < 2) {
            FavoriteGroup favoriteGroup = favoriteFacade.getDefaultFavoriteGroup();
            favoriteFacade.addToFavorite(favoriteGroup, mRouteDataProvider.getRoute(), routeStation);

            String alertString;
            if (routeStation != null) {
                alertString = getString(R.string.alert_favorite_added_with, routeStation.getName());
            } else {
                alertString = getString(R.string.alert_favorite_added);
            }
            Snackbar.make(mCoordinatorLayout, alertString, Snackbar.LENGTH_LONG).show();

        } else {

            String[] items = new String[favoriteGroups.size()];
            for (int i = 0; i < favoriteGroups.size(); i++) {
                items[i] = favoriteGroups.get(i).getName();
            }
            new MaterialDialog.Builder(this)
                    .title(R.string.hint_choose_favorite_group)
                    .items(items)
                    .itemsCallbackMultiChoice(null, (materialDialog, integers, charSequences) -> true)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Integer[] selectedIndices = dialog.getSelectedIndices();
                            if (selectedIndices != null && selectedIndices.length > 0) {
                                for (Integer index : selectedIndices) {
                                    FavoriteGroup favoriteGroup = favoriteGroups.get(index);
                                    favoriteFacade.addToFavorite(favoriteGroup, mRouteDataProvider.getRoute(), routeStation);
                                }

                                String alertString;
                                if (routeStation != null) {
                                    alertString = getString(R.string.alert_favorite_added_with, routeStation.getName());
                                } else {
                                    alertString = getString(R.string.alert_favorite_added);
                                }
                                Snackbar.make(mCoordinatorLayout, alertString, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    })
                    .cancelable(true)
                    .show();
        }
    }
}
