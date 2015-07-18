package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.github.clans.fab.FloatingActionButton;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.utils.BaseWrapperAdapter;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.codetail.animation.SupportAnimator;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.ui.adapter.SearchAdapter;
import kr.rokoroku.mbus.data.SearchDataProvider;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.data.model.SearchHistory;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;


public class SearchActivity extends AbstractBaseActivity
        implements SearchBox.SearchListener, SearchBox.MenuListener, MapFragment.OnEventListener {

    private final String MAP_FRAGMENT_TAG = "MAP";
    public static final String EXTRA_SEARCH_QUERY = "query";
    public static final String EXTRA_SEARCH_BY_LOCATION = "location";

    private SearchBox mSearchBox;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorLayout;

    private FloatingActionButton mMapButton;
    private FloatingActionButton mLocationButton;

    private FrameLayout mFragmentFrameLayout;
    private MapFragment mMapFragment;

    private SearchDataProvider mSearchDataProvider;
    private SearchAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean isMapVisible = false;
    private String previousSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawerEnable(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mFragmentFrameLayout = (FrameLayout) findViewById(R.id.fragment_frame);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        initSearchBox();
        initRecyclerView();
        initFloatingActionButton();

        int actionbarHeight = ThemeUtils.getDimension(this, R.attr.actionBarSize);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);

        final String query = getIntent().getStringExtra(EXTRA_SEARCH_QUERY);
        final boolean byLocation = getIntent().getBooleanExtra(EXTRA_SEARCH_BY_LOCATION, false);

        if (query != null) {
            mSearchBox.postDelayed(() -> mSearchBox.triggerSearch(query), 100);

        } else if (byLocation) {
            mMapButton.postDelayed(() -> showMap(), 100);
        }
    }

    private void initFloatingActionButton() {
        mMapButton = (FloatingActionButton) findViewById(R.id.fab_map);
        mMapButton.show(false);
        mMapButton.setOnClickListener(v -> {
            if (!isMapVisible) {
                showMap();
                mMapFragment.clearExtras();

            } else {
                hideMap();
            }
        });
        mLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mLocationButton.hide(false);
        mLocationButton.setShowProgressBackground(false);
        mLocationButton.setOnClickListener(v -> {
            if (mMapFragment != null) {
                mLocationButton.setIndeterminate(true);
                mLocationButton.setDrawableTint(ThemeUtils.getThemeColor(this, R.attr.colorAccent));
                mMapFragment.updateLocation(true);
            }
        });
    }

    private void showMap() {
        if (mMapFragment == null) {
            initMapFragment();
        }
        isMapVisible = true;
        RevealUtils.revealView(mFragmentFrameLayout, RevealUtils.Position.CENTER, 1000, null);
        previousSearchQuery = mSearchBox.getSearchText();
        if (TextUtils.isEmpty(previousSearchQuery)) previousSearchQuery = null;
        mSearchBox.setLogoText(getString(R.string.nearby_location));
        mMapButton.hide(true);
        mLocationButton.show(true);
        showToolbarLayer();
    }

    private void hideMap() {
        isMapVisible = false;
        mMapButton.show(true);
        mLocationButton.hide(true);
        mSearchBox.setLogoText(previousSearchQuery != null ? previousSearchQuery : getString(R.string.search_hint));
        RevealUtils.revealView(mSwipeRefreshLayout, RevealUtils.Position.CENTER, 1000, new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                mFragmentFrameLayout.setVisibility(View.GONE);
            }
        });
    }

    private void initSearchBox() {
        mSearchBox = new SearchBox(this);
        getToolbarLayer().removeAllViewsInLayout();
        getToolbarLayer().addView(mSearchBox,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        mSearchBox.enableVoiceRecognition(this);
        mSearchBox.disableMaterialIconAnimation(true);
        mSearchBox.setMaterialIconState(MaterialMenuDrawable.IconState.ARROW);
        mSearchBox.setHint(getString(R.string.search_hint));
        mSearchBox.setMenuListener(this);
        mSearchBox.setSearchListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchBox.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        }
        reloadSearchQuery();
    }

    private void initRecyclerView() {

        mSearchDataProvider = new SearchDataProvider();
        mAdapter = new SearchAdapter(mSearchDataProvider);
        mAdapter.setOnChildMenuItemClickListener((menuItem, object) -> {
            if (object != null) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_to_favorite:
                        if (object instanceof Route) {
                            FavoriteFacade.getInstance().addToFavorite((Route) object, null);
                            Snackbar.make(mCoordinatorLayout, R.string.alert_added_to_favorite, Snackbar.LENGTH_LONG).show();

                        } else if (object instanceof Station) {
                            FavoriteFacade.getInstance().addToFavorite((Station) object, null);
                            Snackbar.make(mCoordinatorLayout, R.string.alert_added_to_favorite, Snackbar.LENGTH_LONG).show();

                        }
                        break;

                    case R.id.action_map:
                        showMap();
                        if (object instanceof Route) {
                            Route route = (Route) object;
                            previousSearchQuery = mSearchBox.getSearchText();
                            if (TextUtils.isEmpty(previousSearchQuery)) previousSearchQuery = null;
                            mSearchBox.setLogoText(route.getName());
                            mMapFragment.setRoute(route);

                        } else if (object instanceof Station) {
                            Station station = (Station) object;
                            previousSearchQuery = mSearchBox.getSearchText();
                            if (TextUtils.isEmpty(previousSearchQuery)) previousSearchQuery = null;
                            mSearchBox.setLogoText(station.getName());
                            mMapFragment.setStation(station);
                        }
                        break;
                }
            }
        });
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new BaseWrapperAdapter<>(mAdapter));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());
        mRecyclerView.addItemDecoration(getAppBarHeaderSpacingItemDecoration());
        mRecyclerView.addOnScrollListener(getScrollListener());
    }

    public void reloadSearchQuery() {
        if (mSearchBox != null) {
            List<SearchHistory> searchHistoryTable = new ArrayList<>(DatabaseFacade.getInstance().getSearchHistoryTable());
            Collections.sort(searchHistoryTable);

            ArrayList<SearchResult> arrayList = new ArrayList<>();

            if (!isMapVisible && LocationClient.isLocationEnabled(this)) {
                Drawable locationDrawable = ContextCompat.getDrawable(this, R.drawable.ic_my_location_black_24dp);
                arrayList.add(new SearchResult(getString(R.string.search_by_location), locationDrawable));
            }

            Drawable historyDrawable = ContextCompat.getDrawable(this, R.drawable.ic_history_grey_600_18dp);
            int historyColor = ThemeUtils.getResourceColor(this, R.color.md_grey_600);
            for (SearchHistory searchHistory : searchHistoryTable) {
                arrayList.add(new SearchResult(searchHistory.getTitle(), historyDrawable, historyColor));
            }
            mSearchBox.setSearchables(arrayList);
        }
    }

    private void initMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMapFragment = (MapFragment) fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mMapFragment == null) {
            Bundle args = new Bundle();
            args.putBoolean(MapFragment.EXTRA_ENABLE_SEARCH, true);
            mMapFragment = MapFragment.newInstance(args);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_frame, mMapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
        mMapFragment.setOnEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocationClient.isLocationEnabled(this)) {
            mMapButton.setEnabled(false);
        } else {
            mMapButton.setEnabled(true);
        }
    }

    @Override
    public void onSearchOpened() {
        reloadSearchQuery();
    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchClosed() {

    }

    @Override
    public void onSearchTermChanged() {

    }

    @Override
    public void onSearch(String keyword) {

        if (keyword.length() < 2) {
            if (!TextUtils.isEmpty(keyword.replaceAll("\\d", ""))) {
                Snackbar.make(mCoordinatorLayout, getString(R.string.error_search_keyword_too_short), Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        if (getString(R.string.search_by_location).equals(keyword)) {
            if (!isMapVisible) {
                showMap();
                if (mMapFragment != null) {
                    mMapFragment.updateLocation(true);
                }
            }
            return;

        }

        final String finalKeyword = keyword.toUpperCase();
        if (isMapVisible) {
            previousSearchQuery = finalKeyword ;
            hideMap();
        }

        Set<SearchHistory> searchHistoryTable = DatabaseFacade.getInstance().getSearchHistoryTable();
        SearchHistory searchHistory = new SearchHistory(finalKeyword);

        //noinspection StatementWithEmptyBody
        while (searchHistoryTable.remove(searchHistory)) ;
        searchHistoryTable.add(searchHistory);

        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(true);

        mSearchDataProvider.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        ApiFacade apiFacade = ApiFacade.getInstance();
        apiFacade.searchByKeyword(finalKeyword, mSearchDataProvider, new SimpleProgressCallback() {
            @Override
            public void onComplete(boolean success, Object value) {
                mAdapter.notifyDataSetChanged();
                mSearchDataProvider.sortByKeyword(finalKeyword);
                mSwipeRefreshLayout.postDelayed(() -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(false);
                }, 500);
            }

            @Override
            public void onError(int progress, Throwable t) {
                Snackbar.make(mCoordinatorLayout, getString(R.string.error_with_reason, t.getMessage()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSearchBox.isSearchOpen()) {
            mSearchBox.toggleSearch();

        } else if (isMapVisible && previousSearchQuery != null) {
            hideMap();

        } else {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onMenuClick() {
        onBackPressed();
    }

    @Override
    public void onLocationUpdate(Location location) {
        mLocationButton.postDelayed(() -> {
            mLocationButton.setClickable(true);
            mLocationButton.setDrawableTint(Color.BLACK);
            mLocationButton.setIndeterminate(false);
            mLocationButton.setProgress(0, false);
        }, 1000);
    }

    @Override
    public void onStationClick(Station station) {
        Intent intent = new Intent(this, StationActivity.class);
        intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
        startActivity(intent);
    }

    @Override
    public void onErrorMessage(String error) {
        Snackbar.make(mCoordinatorLayout, error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            mSearchBox.triggerSearch(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
