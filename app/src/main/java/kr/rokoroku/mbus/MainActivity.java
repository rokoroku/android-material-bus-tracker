package kr.rokoroku.mbus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.fsn.cauly.CaulyNativeAdView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import co.naughtyspirit.showcaseview.ShowcaseView;
import co.naughtyspirit.showcaseview.targets.TargetView;
import co.naughtyspirit.showcaseview.utils.PositionsUtil;
import io.codetail.animation.SupportAnimator;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.DatabaseFacade;
import kr.rokoroku.mbus.core.LocationClient;
import kr.rokoroku.mbus.data.SearchDataProvider;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.ui.adapter.FavoriteAdapter;
import kr.rokoroku.mbus.data.FavoriteDataProvider;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.SearchHistory;
import kr.rokoroku.mbus.ui.adapter.SearchAdapter;
import kr.rokoroku.mbus.util.CaulyAdUtil;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;


public class MainActivity extends AbstractBaseActivity implements RecyclerViewFragment.RecyclerViewInterface,
        FavoriteAdapter.EventListener, SearchBox.SearchListener, SearchBox.MenuListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "expandable_state";
    private static final String TAG_FRAGMENT_MAP = "MAP";
    private static final String TAG_FRAGMENT_SEARCH = "SEARCH";
    private static final String TAG_FRAGMENT_FAVORITE = "FAVORITE";
    private static final int RESULT_GPS_SETTINGS = 55;

    private SearchBox mSearchBox;
    private CoordinatorLayout mCoordinatorLayout;

    private FrameLayout mMapFrame;
    private FrameLayout mSearchFrame;
    private FrameLayout mFavoriteFrame;

    private MapFragment mMapFragment;
    private RecyclerViewFragment mSearchFragment;
    private RecyclerViewFragment mFavoriteFragment;

    private FloatingActionMenu mPlusButton;
    private FloatingActionButton mAddNewGroupButton;
    private FloatingActionButton mAddNewFavoriteButton;
    private FloatingActionButton mLocationButton;
    private FloatingActionButton mSearchButton;

    private FavoriteAdapter mFavoriteAdapter;
    private FavoriteDataProvider mFavoriteDataProvider;
    private RecyclerViewExpandableItemManager mFavoriteItemManager;

    private SearchAdapter mSearchAdapter;
    private SearchDataProvider mSearchDataProvider;
    private String previousSearchQuery;
    private boolean wasLocationEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDrawerEnable(true);

        mMapFrame = (FrameLayout) findViewById(R.id.map_frame);
        mSearchFrame = (FrameLayout) findViewById(R.id.search_frame);
        mFavoriteFrame = (FrameLayout) findViewById(R.id.favorite_frame);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        initSearchBox();
        initFloatingActionButtons();

        initFavoriteFragment(savedInstanceState);
        initSearchFragment();
        initMapFragment();

        alertGpsEnable();
        SharedPreferences sharedPreferences = getSharedPreferences(BaseApplication.SHARED_PREFERENCE_KEY, MODE_PRIVATE);
        String homeScreen = sharedPreferences.getString(BaseApplication.PREFERENCE_HOME_SCREEN, "1");
        boolean locationEnabled = LocationClient.isLocationProviderAvailable(this);
        switch (homeScreen) {
            case "0":
                showNewSearch(false);
                break;
            default:
            case "1":
                showFavorite(false);
                break;

            case "2":
                showFavorite(false);
                if (locationEnabled) {
                    showMap();
                }
                break;
        }
    }

    private void alertGpsEnable() {
        if (!LocationClient.isLocationProviderAvailable(this)) {
            boolean doNotAskGps = BaseApplication.getSharedPreferences()
                    .getBoolean(BaseApplication.PREFERENCE_DO_NOT_ASK_GPS_AGAIN, false);
            if (!doNotAskGps) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.action_enable_gps)
                        .content(R.string.hint_enable_gps)
                        .items(new String[]{getString(R.string.hint_do_not_ask_again)})
                        .itemsCallbackMultiChoice(null, (dialog1, which, text) -> true)
                        .positiveText(R.string.action_ok)
                        .negativeText(R.string.action_no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, RESULT_GPS_SETTINGS);
                            }

                            @Override
                            public void onAny(MaterialDialog dialog) {
                                Integer[] selectedIndices = dialog.getSelectedIndices();
                                if (selectedIndices != null && selectedIndices.length > 0) {
                                    BaseApplication.getSharedPreferences().edit()
                                            .putBoolean(BaseApplication.PREFERENCE_DO_NOT_ASK_GPS_AGAIN, true)
                                            .apply();
                                }
                            }
                        }).build();
                dialog.show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mFavoriteAdapter != null) {
            mFavoriteAdapter.notifyDataSetChanged();
            if (mFavoriteDataProvider.getGroupCount() == 1) {
                mFavoriteAdapter.getExpandableItemManager().expandGroup(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean locationEnabled = LocationClient.isLocationProviderAvailable(this);
        if (wasLocationEnabled != locationEnabled) {
            mLocationButton.setEnabled(locationEnabled);
            enableSearchBoxLocationButton(locationEnabled);
        }
        if (mFavoriteAdapter != null && mFavoriteAdapter.getAdTag() != null) {
            if (mFavoriteAdapter.getAdPosition() == -1) {
                String adTag = mFavoriteAdapter.getAdTag();
                CaulyAdUtil.removeAd(adTag);
                String newTag = String.valueOf(adTag.hashCode());
                CaulyAdUtil.requestAd(MainActivity.this, newTag, new CaulyAdUtil.SimpleCaulyNativeAdListener() {
                    @Override
                    public void onReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, boolean b) {
                        mFavoriteAdapter.setAdTag(newTag);
                        mFavoriteAdapter.setAdPosition(0);
                        ViewUtils.runOnUiThread(mFavoriteAdapter::notifyDataSetChanged);
                    }
                });
            }
        }
        wasLocationEnabled = locationEnabled;
    }

    private void enableSearchBoxLocationButton(boolean locationEnabled) {
        mSearchBox.setUserSideButtonFunctionEnabled(locationEnabled);
    }

    private void initSearchBox() {
        mSearchBox = new SearchBox(this);
        getToolbarLayer().removeAllViewsInLayout();
        getToolbarLayer().addView(mSearchBox,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        mSearchBox.enableVoiceRecognition(this);
        mSearchBox.setHint(getString(R.string.hint_search));
        mSearchBox.setMenuListener(this);
        mSearchBox.setSearchListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchBox.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        }

        Drawable locationDrawable = ContextCompat.getDrawable(this, R.drawable.ic_my_location_black_24dp);
        ViewUtils.setTint(locationDrawable, ThemeUtils.getResourceColor(this, R.color.md_grey_600));
        mSearchBox.setSideButtonDrawable(locationDrawable);
        mSearchBox.setSideButtonOnClickListener(v -> {
            if (mMapFragment != null) {
                if (mMapFrame.getVisibility() != View.VISIBLE) {
                    mMapFragment.clearExtras();

                } else {
                    mLocationButton.setIndeterminate(true);
                    mLocationButton.setDrawableTint(ThemeUtils.getThemeColor(MainActivity.this, R.attr.colorAccent));
                    mMapFragment.updateLocation(true);
                }
            }
            showMap();
            enableSearchBoxLocationButton(false);
        });
        mSearchBox.setUserSideButtonFunctionEnabled(LocationClient.isLocationProviderAvailable(this));

        reloadSearchSuggestion();
    }

    private void initFloatingActionButtons() {
        mPlusButton = (FloatingActionMenu) findViewById(R.id.fab_plus);
        mAddNewGroupButton = (FloatingActionButton) findViewById(R.id.fab_new_group);
        mAddNewFavoriteButton = (FloatingActionButton) findViewById(R.id.fab_new_favorite);
        mLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mSearchButton = (FloatingActionButton) findViewById(R.id.fab_search);

        mAddNewFavoriteButton.setOnClickListener(v -> {
            showNewSearch(true);
            if (!mSearchBox.isSearchOpen()) {
                mSearchBox.toggleSearch();
            }
        });
        mAddNewGroupButton.setOnClickListener(v -> {
            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.action_new_favorite_group)
                    .input(R.string.hint_input_group_name, 0, (materialDialog, charSequence) -> {
                        if (!TextUtils.isEmpty(charSequence)) {
                            String name = charSequence.toString().trim();
                            for (FavoriteGroup group : mFavoriteDataProvider.getFavorite().getFavoriteGroups()) {
                                if (group.getName().equals(name)) return;
                            }
                            FavoriteGroup favoriteGroup = new FavoriteGroup(name);
                            int groupPosition = 0;

                            mFavoriteDataProvider.addGroupItem(groupPosition, favoriteGroup);
                            mFavoriteAdapter.notifyDataSetChanged();

                            int adPosition = mFavoriteAdapter.getAdPosition();
                            if (adPosition != -1 && adPosition >= groupPosition) {
                                if (adPosition != 0) {
                                    mFavoriteAdapter.setAdPosition(adPosition + 1);
                                    ViewUtils.runOnUiThread(mFavoriteAdapter::notifyDataSetChanged, 50);
                                }
                            }

                            ViewUtils.runOnUiThread(() -> {
                                if (mFavoriteFragment != null) {
                                    mFavoriteFragment.getRecyclerView().smoothScrollToPosition(groupPosition);
                                }
                                mFavoriteItemManager.expandGroup(groupPosition);
                                Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_group_added, Snackbar.LENGTH_LONG).show();
                                showToolbarLayer();
                            }, 100);

                            View view = materialDialog.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    })
                    .show();
            mPlusButton.close(true);
        });

        mLocationButton = (FloatingActionButton) findViewById(R.id.fab_location);
        mLocationButton.hide(false);
        mLocationButton.setShowProgressBackground(false);
        mLocationButton.setOnClickListener(v -> {
            if (mMapFragment != null) {
                if (mMapFrame.getVisibility() != View.VISIBLE) {
                    mMapFragment.clearExtras();

                } else {
                    mLocationButton.setIndeterminate(true);
                    mLocationButton.setDrawableTint(ThemeUtils.getThemeColor(this, R.attr.colorAccent));
                    mMapFragment.updateLocation(true);
                }
            }
            showMap();
        });

        mSearchButton.hide(false);
        mSearchButton.setOnClickListener(v -> {
            String searchText = mSearchBox.getSearchText();
            if (!TextUtils.isEmpty(searchText)) {
                mSearchBox.triggerSearch(new SearchResult(searchText, null));
                View focus = mSearchBox.findFocus();
                if (focus != null) focus.clearFocus();
                if (mSearchBox.isSearchOpen()) {
                    mSearchBox.toggleSearch();
                }
                mSearchButton.hide(true);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mFavoriteItemManager != null) {
            outState.putParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER, mFavoriteItemManager.getSavedState());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_action_search:
                hideMap();
                showPreviousSearch(true);
                closeDrawer();
                return true;

            case R.id.nav_action_favorite:
                hideMap();
                showFavorite(true);
                closeDrawer();
                return true;

            case R.id.nav_action_map:
                showMap();
                mMapFragment.clearExtras();
                closeDrawer();
                return true;

            case R.id.nav_action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                closeDrawer();
                return true;

            case R.id.nav_action_about:
                MaterialDialog aboutDialog = new MaterialDialog.Builder(this)
                        .customView(R.layout.popup_about, true)
                        .cancelable(true)
                        .build();
                TextView titleTextView = (TextView) aboutDialog.getView().findViewById(R.id.app_title);
                TextView versionTextView = (TextView) aboutDialog.getView().findViewById(R.id.app_version);
                try {
                    titleTextView.setText(getString(R.string.app_name).replace("(베타)", ""));
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionTextView.setText("v" + pInfo.versionName + " (beta)");
                } catch (PackageManager.NameNotFoundException e) {
                    versionTextView.setText("");
                    e.printStackTrace();
                }
                aboutDialog.show();
                closeDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSearchOpened() {
        reloadSearchSuggestion();
        mPlusButton.close(true);
    }

    @Override
    public void onSearchCleared() {
        mSearchButton.hide(true);
    }

    @Override
    public void onSearchClosed() {
        mSearchButton.hide(true);
    }

    @Override
    public void onSearchTermChanged() {
        if (TextUtils.isEmpty(mSearchBox.getSearchText())) {
            mSearchButton.hide(true);
        } else if (mSearchButton.isHidden()) {
            mSearchButton.show(true);
        }
    }

    @Override
    public void onSearch(SearchResult result) {
        String keyword = result.title;
        if (keyword.length() < 2) {
            if (!TextUtils.isEmpty(keyword.replaceAll("\\d", ""))) {
                Snackbar.make(mCoordinatorLayout, getString(R.string.error_search_keyword_too_short), Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        boolean isMapVisible = mMapFrame.getVisibility() == View.VISIBLE;
        if (getString(R.string.hint_search_by_location).equals(keyword)) {
            if (!isMapVisible) {
                showMap();
            }
            mMapFragment.updateLocation(true);
            mSearchBox.setSearchString("", false);
            return;
        }

        Object resultExtra = result.getExtra();
        if (resultExtra != null) {
            if (resultExtra instanceof Route) {
                Route route = (Route) resultExtra;
                Intent intent = new Intent(this, RouteActivity.class);
                intent.putExtra(RouteActivity.EXTRA_KEY_ROUTE, (Parcelable) route);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (resultExtra instanceof Station) {
                Station station = (Station) resultExtra;

                Intent intent = new Intent(this, StationActivity.class);
                intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            mSearchBox.setSearchString("", false);
            mSearchBox.setLogoText(getString(R.string.hint_search));
            return;
        }

        final String finalKeyword = keyword.toUpperCase().trim();
        if (isMapVisible) {
            previousSearchQuery = finalKeyword;
            hideMap();
        }

        Set<SearchHistory> searchHistoryTable = DatabaseFacade.getInstance().getSearchHistoryTable();
        SearchHistory searchHistory = new SearchHistory(finalKeyword);

        //noinspection StatementWithEmptyBody
        while (searchHistoryTable.remove(searchHistory)) ;
        searchHistoryTable.add(searchHistory);

        showNewSearch(true);
        mSearchFragment.setRefreshEnabled(true);
        mSearchFragment.setRefreshing(true);
        mSearchFragment.setOverlayView(null, true);

        mSearchDataProvider.clear();
        mSearchAdapter.notifyDataSetChanged();
        ApiFacade apiFacade = ApiFacade.getInstance();
        apiFacade.searchByKeyword(finalKeyword, mSearchDataProvider, new SimpleProgressCallback() {
            @Override
            public void onComplete(boolean success, Object value) {
                ViewUtils.runOnUiThread(() -> {
                    mSearchDataProvider.sortByKeyword(finalKeyword);
                    mSearchAdapter.notifyDataSetChanged();
                    mSearchBox.closeSearch();
                    mSearchFragment.setRefreshing(false);
                    mSearchFragment.setRefreshEnabled(false);
                    if (mSearchDataProvider.getCount() == 0) {
                        View placeholder = View.inflate(MainActivity.this, R.layout.empty_placeholer, null);
                        TextView text = (TextView) placeholder.findViewById(R.id.text);
                        text.setText(R.string.placeholder_search_empty);
                        ImageView image = (ImageView) placeholder.findViewById(R.id.image);
                        image.setImageResource(R.drawable.ic_empty_black_48dp);
                        image.setVisibility(View.VISIBLE);
                        image.setColorFilter(ThemeUtils.getThemeColor(MainActivity.this, android.R.attr.textColorSecondary), PorterDuff.Mode.MULTIPLY);
                        mSearchFragment.setOverlayView(placeholder, true);
                    }
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
        if (isDrawerOpen()) {
            closeDrawer();

        } else if (mSearchBox.isSearchOpen()) {
            mSearchBox.toggleSearch();
            View focus = mSearchBox.findFocus();
            if (focus != null) focus.clearFocus();

        } else if (mMapFrame.getVisibility() == View.VISIBLE) {
            hideMap();

        } else if (mFavoriteFrame.getVisibility() != View.VISIBLE) {
            showFavorite(true);

        } else if (mPlusButton.isOpened()) {
            mPlusButton.close(true);

        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.action_quit)
                    .content(R.string.hint_quit)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            runOnUiThread(() -> {
                                moveTaskToBack(true);
                                finish();
                            });
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Favorite currentFavorite = FavoriteFacade.getInstance().getCurrentFavorite();
        DatabaseFacade.getInstance().putBookmark(currentFavorite.getName(), currentFavorite);
    }

    @Override
    public void onMenuClick() {
        if (mSearchBox.getMaterialIconState() == MaterialMenuDrawable.IconState.ARROW) {
            onBackPressed();
        } else {
            openDrawer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFavoriteAdapter != null) {
            mFavoriteAdapter.setAdPosition(-1);
            CaulyAdUtil.removeAd(mFavoriteAdapter.getAdTag());
        }
    }

    @Override
    public void onGroupItemRemoved(int groupPosition) {
        Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_group_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> {
                    undoLastRemoval();
                }).show();
        showToolbarLayer();
    }

    @Override
    public void onChildItemRemoved(int groupPosition, int childPosition) {
        Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> {
                    undoLastRemoval();
                }).show();
        showToolbarLayer();
    }

    @Override
    public void onChildItemMoved(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        showToolbarLayer();
    }

    @Override
    public void onGroupItemMoved(int fromGroupPosition, int toGroupPosition) {
        showToolbarLayer();
    }

    public void undoLastRemoval() {
        int[] position = mFavoriteDataProvider.undoLastRemoval();
        RecyclerView recyclerView = mFavoriteFragment.getRecyclerView();
        if (position[0] >= 0 && position[1] == -1) {
            //undo group removal
            int adPosition = mFavoriteAdapter.getAdPosition();
            if (adPosition != -1) {
                if (adPosition >= position[0]) {
                    if (adPosition != 0) adPosition++;
                    else position[0]++;
                    mFavoriteAdapter.setAdPosition(adPosition);
                } else {
                    position[0]++;
                }
            }

            long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForGroup(position[0]);
            int flatPosition = mFavoriteItemManager.getFlatPosition(expandablePosition);

            mFavoriteAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(flatPosition);
            mFavoriteItemManager.expandGroup(position[0]);

        } else if (position[0] >= 0 && position[1] >= 0) {
            //undo child removal
            long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(position[0], position[1]);
            int flatPosition = mFavoriteItemManager.getFlatPosition(expandablePosition);

            mFavoriteAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(flatPosition);
            mFavoriteItemManager.expandGroup(position[0]);
        }
    }

    public void reloadSearchSuggestion() {
        if (mSearchBox != null) {
            ArrayList<SearchResult> searchResults = new ArrayList<>();

            // add historic items to searchResults
            List<SearchHistory> searchHistoryTable = new ArrayList<>();
            try {
                Set<SearchHistory> searchHistories = DatabaseFacade.getInstance().getSearchHistoryTable();
                searchHistoryTable.addAll(searchHistories);
                Collections.sort(searchHistoryTable);
            } catch (Exception e) {
                Log.e(TAG_FRAGMENT_SEARCH, "Exception in reloadSearchSuggestion", e);
                try {
                    DatabaseFacade.getInstance().getSearchHistoryTable().clear();
                } catch (Exception e1) {
                    //ignore
                }
            }

            if (!searchHistoryTable.isEmpty()) {
                Drawable historyDrawable = ContextCompat.getDrawable(this, R.drawable.ic_history_grey_600_18dp);
                int historyColor = ThemeUtils.getResourceColor(this, R.color.md_grey_600);
                for (SearchHistory searchHistory : searchHistoryTable) {
                    searchResults.add(new SearchResult(searchHistory.getTitle(), historyDrawable, historyColor));
                }
            }

            // add favorite items to searchResults
            if (mFavoriteDataProvider != null) {
                int groupCount = mFavoriteDataProvider.getGroupCount();
                for (int i = 0; i < groupCount; i++) {
                    FavoriteGroup groupItem = mFavoriteDataProvider.getGroupItem(i);
                    int size = groupItem.size();
                    for (int j = 0; j < size; j++) {
                        FavoriteItem item = groupItem.get(j);
                        FavoriteItem.Type itemType = item.getType();
                        if (itemType == FavoriteItem.Type.ROUTE) {
                            Route route = item.getData(Route.class);
                            if (route != null) {
                                int color = route.getType().getColor(this);
                                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_bus);
                                ViewUtils.setTint(drawable, color);
                                SearchResult searchResult = new SearchResult(route.getName(), drawable, color);
                                searchResult.setExtra(route);
                                if (!searchResults.contains(searchResult)) {
                                    searchResults.add(searchResult);
                                }
                            }
                        } else if (itemType == FavoriteItem.Type.STATION) {
                            Station station = item.getData(Station.class);
                            if (station != null) {
                                int color = ThemeUtils.getThemeColor(this, R.attr.cardPrimaryTextColor);
                                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_pin_drop);
                                ViewUtils.setTint(drawable, color);
                                SearchResult searchResult = new SearchResult(station.getName(), drawable, color);
                                searchResult.setExtra(station);
                                if (!searchResults.contains(searchResult)) {
                                    searchResults.add(searchResult);
                                }
                            }
                        }
                    }
                }
            }

            mSearchBox.setSearchables(searchResults);
        }
    }

    private void showFirstFavoriteHintIfNotShown() {
        String preferenceKey = "Hint_Favorite";
        if (mFavoriteFragment != null && !getSharedPreferences(ShowcaseView.PREFERENCE_NAME, MODE_PRIVATE).contains(preferenceKey)) {
            RecyclerView recyclerView = mFavoriteFragment.getRecyclerView();
            View childView = null;
            for (int i = 1; i < recyclerView.getChildCount() - 1; i++) {
                View view = recyclerView.getChildAt(i);
                if (recyclerView.getChildViewHolder(view) instanceof FavoriteAdapter.ItemViewHolder) {
                    childView = view;
                    break;
                }
            }
            if (childView != null) {
                new ShowcaseView.Builder(this, preferenceKey)
                        .setDescription(getString(R.string.hint_favorite_tutorial), PositionsUtil.ItemPosition.CENTER)
                        .setTarget(new TargetView(childView, TargetView.ShowcaseType.RECTANGLE))
                        .setHideOnAction(true)
                        .setOneShot(true)
                        .build();
            }
        }
    }

    private void showMap() {
        if (mMapFragment == null) {
            initMapFragment();
        }

        attachOrShowFragment(mMapFrame, mMapFragment, TAG_FRAGMENT_MAP);
        if (mMapFragment.getMap() == null) {
            mMapFrame.setVisibility(View.INVISIBLE);

        } else if (mMapFrame.getVisibility() != View.VISIBLE) {
            RevealUtils.revealView(mMapFrame, RevealUtils.Position.CENTER, 1000, null);
            previousSearchQuery = mSearchBox.getSearchText();
            if (TextUtils.isEmpty(previousSearchQuery)) previousSearchQuery = null;
            mSearchBox.setLogoText(getString(R.string.hint_nearby_location));
            mLocationButton.show(true);
            enableSearchBoxLocationButton(false);
        }
        showToolbarLayer();
    }

    private void hideMap() {
        if (mMapFrame.getVisibility() == View.VISIBLE) {
            if (mSearchFrame.getVisibility() != View.VISIBLE) {
                mLocationButton.hide(true);
            }
            mSearchBox.setLogoText(previousSearchQuery != null ? previousSearchQuery : getString(R.string.hint_search));
            RevealUtils.unrevealView(mMapFrame, RevealUtils.Position.CENTER, 600, null);
            enableSearchBoxLocationButton(LocationClient.isLocationProviderAvailable(this));
        }
    }

    private void showNewSearch(boolean animate) {
        showSearch(animate, true);
    }

    private void showPreviousSearch(boolean animate) {
        showSearch(animate, false);
    }

    private void showSearch(boolean animate, boolean clear) {
        if (mSearchFrame.getVisibility() != View.VISIBLE) {
            attachOrShowFragment(mSearchFrame, mSearchFragment, TAG_FRAGMENT_SEARCH);
            if (clear) {
                mSearchDataProvider.clear();
                mSearchAdapter.notifyDataSetChanged();
            }

            if (animate) {
                RevealUtils.revealView(mSearchFrame, RevealUtils.Position.CENTER, 600, new SupportAnimator.SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mMapFrame.setVisibility(View.GONE);
                        mFavoriteFrame.setVisibility(View.GONE);
                        if (mMapFragment != null) detachFragment(mMapFragment);
                        if (mFavoriteFragment != null) detachFragment(mFavoriteFragment);
                    }
                });
            } else {
                mSearchFrame.getParent().bringChildToFront(mSearchFrame);
                mMapFrame.setVisibility(View.GONE);
                mSearchFrame.setVisibility(View.VISIBLE);
                mFavoriteFrame.setVisibility(View.GONE);
                if (mFavoriteFragment != null) detachFragment(mFavoriteFragment);
                if (mMapFragment != null) detachFragment(mMapFragment);
            }

            if (clear || mSearchDataProvider.getCount() == 0) {
                View placeholder = View.inflate(MainActivity.this, R.layout.empty_placeholer, null);
                TextView text = (TextView) placeholder.findViewById(R.id.text);
                text.setText(R.string.placeholder_search_start);
                ImageView image = (ImageView) placeholder.findViewById(R.id.image);
                image.setImageResource(R.drawable.ic_search_black_48dp);
                image.setVisibility(View.VISIBLE);
                image.setColorFilter(ThemeUtils.getThemeColor(MainActivity.this, android.R.attr.textColorSecondary));
                mSearchFragment.setOverlayView(placeholder, false);
            }

            mSearchBox.setMaterialIconState(MaterialMenuDrawable.IconState.ARROW);
            mSearchBox.setMaterialIconMorphAnimationEnable(false);
            mPlusButton.hideMenuButton(animate);
            mLocationButton.show(animate);
        }

        showToolbarLayer();
    }

    private void showFavorite(boolean animate) {
        if (mFavoriteFrame.getVisibility() != View.VISIBLE) {
            attachOrShowFragment(mFavoriteFrame, mFavoriteFragment, TAG_FRAGMENT_FAVORITE);
            if (animate) {
                RevealUtils.revealView(mFavoriteFrame, RevealUtils.Position.CENTER, 600, new SupportAnimator.SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mMapFrame.setVisibility(View.GONE);
                        mSearchFrame.setVisibility(View.GONE);
                        if (mSearchFragment != null) detachFragment(mSearchFragment);
                        if (mMapFragment != null) detachFragment(mMapFragment);
                    }
                });
            } else {
                mFavoriteFrame.getParent().bringChildToFront(mFavoriteFrame);
                mMapFrame.setVisibility(View.GONE);
                mSearchFrame.setVisibility(View.GONE);
                mFavoriteFrame.setVisibility(View.VISIBLE);
                if (mSearchFragment != null) detachFragment(mSearchFragment);
                if (mMapFragment != null) detachFragment(mMapFragment);
            }
            mFavoriteAdapter.notifyDataSetChanged();
            mPlusButton.showMenuButton(animate);
            mLocationButton.hide(animate);
            mSearchBox.setMaterialIconState(MaterialMenuDrawable.IconState.BURGER);
            mSearchBox.setMaterialIconMorphAnimationEnable(true);
            mSearchBox.setSearchString("", false);
            mSearchBox.setLogoText(getString(R.string.hint_search));
            mSearchBox.closeSearch();
        }
        ViewUtils.runOnUiThread(this::showFirstFavoriteHintIfNotShown, 500);
        showToolbarLayer();
    }

    private void initMapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMapFragment = (MapFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_MAP);
        if (mMapFragment == null) {
            Bundle args = new Bundle();
            args.putBoolean(MapFragment.EXTRA_ENABLE_SEARCH, true);
            mMapFragment = MapFragment.newInstance(args);
        }
        if (mMapFragment != null) {
            mMapFragment.setOnEventListener(new MapFragment.OnEventListener() {
                @Override
                public void onMapLoaded(GoogleMap map) {
                    ViewUtils.runOnUiThread(MainActivity.this::showMap);
                }

                @Override
                public void onLocationUpdate(Location location) {
                    ViewUtils.runOnUiThread(() -> {
                        mLocationButton.setClickable(true);
                        mLocationButton.setDrawableTint(Color.BLACK);
                        mLocationButton.setIndeterminate(false);
                        mLocationButton.setProgress(0, false);
                    }, 1000);
                }

                @Override
                public void onStationClick(Station station) {
                    Intent intent = new Intent(MainActivity.this, StationActivity.class);
                    intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
                    startActivity(intent);
                }

                @Override
                public void onErrorMessage(String error) {
                    Snackbar.make(mCoordinatorLayout, error, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void initSearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mSearchFragment = (RecyclerViewFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_SEARCH);
        if (mSearchFragment == null) {
            mSearchFragment = RecyclerViewFragment.createInstance(TAG_FRAGMENT_SEARCH);
        }
        if (mSearchAdapter == null) {
            mSearchDataProvider = new SearchDataProvider();
            mSearchAdapter = new SearchAdapter(mSearchDataProvider);
            mSearchAdapter.setOnChildMenuItemClickListener((menuItem, object) -> {
                if (object != null) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_add_to_favorite:
                            FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
                            List<FavoriteGroup> favoriteGroups = favoriteFacade.getCurrentFavorite().getFavoriteGroups();
                            if (favoriteGroups.size() < 2) {
                                FavoriteGroup favoriteGroup = favoriteFacade.getDefaultFavoriteGroup();
                                addFavoriteItem(favoriteGroup, object);

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
                                                        if (object instanceof Route) {
                                                            favoriteFacade.addToFavorite(favoriteGroup, (Route) object, null);
                                                        } else if (object instanceof Station) {
                                                            favoriteFacade.addToFavorite(favoriteGroup, (Station) object, null);
                                                        }
                                                    }

                                                    Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_added, Snackbar.LENGTH_LONG).show();
                                                    mFavoriteAdapter.notifyDataSetChanged();

                                                    for (Integer index : selectedIndices) {
                                                        int adPosition = mFavoriteAdapter.getAdPosition();
                                                        if (adPosition != -1 && index >= adPosition)
                                                            index++;
                                                        mFavoriteAdapter.getExpandableItemManager().expandGroup(index);
                                                    }
                                                }
                                            }
                                        })
                                        .cancelable(true)
                                        .show();
                            }
                            break;

                        case R.id.action_map:
                            showMap();
                            if (object instanceof Route) {
                                Route route = (Route) object;
                                previousSearchQuery = mSearchBox.getSearchText();
                                if (TextUtils.isEmpty(previousSearchQuery))
                                    previousSearchQuery = null;
                                mSearchBox.setLogoText(route.getName());
                                mMapFragment.setRoute(route);

                            } else if (object instanceof Station) {
                                Station station = (Station) object;
                                previousSearchQuery = mSearchBox.getSearchText();
                                if (TextUtils.isEmpty(previousSearchQuery))
                                    previousSearchQuery = null;
                                mSearchBox.setLogoText(station.getName());
                                mMapFragment.setStation(station);
                            }
                            break;
                    }
                }
            });
        }
        if (mSearchFragment != null && mSearchAdapter != null) {
            mSearchFragment.setAdapter(mSearchAdapter);
        }
    }

    private void initFavoriteFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFavoriteFragment = (RecyclerViewFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_FAVORITE);
        if (mFavoriteFragment == null) {
            mFavoriteFragment = RecyclerViewFragment.createInstance(TAG_FRAGMENT_FAVORITE);
        }
        if (mFavoriteAdapter == null) {
            Favorite favorite = FavoriteFacade.getInstance().getCurrentFavorite();
            Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
            mFavoriteItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
            mFavoriteDataProvider = new FavoriteDataProvider(favorite);
            mFavoriteAdapter = new FavoriteAdapter(mFavoriteItemManager, mFavoriteDataProvider);
            mFavoriteAdapter.setEventListener(new FavoriteAdapter.EventListener() {
                @Override
                public void onGroupItemRemoved(int groupPosition) {
                    Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_group_removed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, v -> {
                                undoLastRemoval();
                            }).show();
                    showToolbarLayer();
                }

                @Override
                public void onChildItemRemoved(int groupPosition, int childPosition) {
                    Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_removed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, v -> {
                                undoLastRemoval();
                            }).show();
                    showToolbarLayer();
                }

                @Override
                public void onChildItemMoved(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
                    showToolbarLayer();
                }

                @Override
                public void onGroupItemMoved(int fromGroupPosition, int toGroupPosition) {
                    showToolbarLayer();
                }
            });
            mFavoriteAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    if (mFavoriteDataProvider.getGroupCount() == 0) {
                        View placeholder = View.inflate(MainActivity.this, R.layout.empty_placeholer, null);
                        TextView text = (TextView) placeholder.findViewById(R.id.text);
                        text.setText(R.string.placeholder_favorite_empty);
                        ImageView image = (ImageView) placeholder.findViewById(R.id.image);
                        image.setImageResource(R.drawable.ic_empty);
                        image.setVisibility(View.VISIBLE);
                        image.setColorFilter(ThemeUtils.getThemeColor(MainActivity.this, android.R.attr.textColorSecondary), PorterDuff.Mode.MULTIPLY);
                        mFavoriteFragment.setOverlayView(placeholder, true);
                    } else {
                        mFavoriteFragment.setOverlayView(null, true);
                    }
                }
            });
            if (mFavoriteDataProvider.getGroupCount() > 0) {
                mFavoriteFrame.setAlpha(0);
                CaulyAdUtil.requestAd(this, TAG_FRAGMENT_SEARCH, new CaulyAdUtil.SimpleCaulyNativeAdListener() {
                    @Override
                    public void onReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, boolean b) {
                        mFavoriteAdapter.setAdPosition(0);
                        mFavoriteAdapter.setAdTag(TAG_FRAGMENT_SEARCH);
                        showFavoriteFrame();
                    }

                    @Override
                    public void onFailedToReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, int i, String s) {
                        Log.e("CaulyAd", s);
                        mFavoriteAdapter.setAdPosition(-1);
                        showFavoriteFrame();
                    }

                    private void showFavoriteFrame() {
                        ViewUtils.runOnUiThread(() -> {
                            mFavoriteAdapter.notifyDataSetChanged();
                            mFavoriteFrame.animate()
                                    .alpha(1.0f)
                                    .setStartDelay(100)
                                    .setDuration(300)
                                    .start();
                        });
                    }
                });
            }
        }
        if (mFavoriteFragment != null && mFavoriteAdapter != null) {
            mFavoriteFragment.setAdapter(mFavoriteAdapter);
        }
    }

    private void attachOrShowFragment(ViewGroup container, Fragment fragment, String tag) {
        if (fragment.isHidden()) {
            getSupportFragmentManager().beginTransaction()
                    .show(fragment)
                    .commit();

        } else if (!fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(container.getId(), fragment, tag)
                    .commit();
        }
    }

    private void detachFragment(Fragment fragment) {
        if (fragment.isAdded() && !fragment.isDetached() && !fragment.isHidden()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(fragment)
                    .commit();
        }
    }

    private void addFavoriteItem(FavoriteGroup favoriteGroup, Object object) {

        FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
        if (object instanceof Route) {
            favoriteFacade.addToFavorite(favoriteGroup, (Route) object, null);
        } else if (object instanceof Station) {
            favoriteFacade.addToFavorite(favoriteGroup, (Station) object, null);
        }
        Snackbar.make(mCoordinatorLayout, R.string.alert_favorite_added, Snackbar.LENGTH_LONG).show();

        mFavoriteAdapter.notifyDataSetChanged();
        for (int i = 0; i < mFavoriteDataProvider.getGroupCount(); i++) {
            if (mFavoriteDataProvider.getGroupItem(i).equals(favoriteGroup)) {
                mFavoriteAdapter.getExpandableItemManager().expandGroup(i);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                mSearchBox.triggerSearch(new SearchResult(spokenText, null));
            }
            return;

        } else if (requestCode == RESULT_GPS_SETTINGS) {
            boolean locationEnabled = LocationClient.isLocationProviderAvailable(this);
            if (locationEnabled) {
                Snackbar.make(mCoordinatorLayout, "GPS 기능이 활성화되었습니다.", Snackbar.LENGTH_LONG).show();

            } else {
                Snackbar.make(mCoordinatorLayout, "GPS 기능이 비활성화되었습니다.", Snackbar.LENGTH_LONG).show();
            }
            if (wasLocationEnabled != locationEnabled) {
                mLocationButton.setEnabled(locationEnabled);
                enableSearchBoxLocationButton(locationEnabled);
            }
            wasLocationEnabled = locationEnabled;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public RecyclerView.Adapter getAdapter(String tag) {
        if (TAG_FRAGMENT_SEARCH.equals(tag)) {
            return mSearchAdapter;

        } else if (TAG_FRAGMENT_FAVORITE.equals(tag)) {
            return mFavoriteAdapter;
        }
        return null;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return getAppBarHeaderSpacingItemDecoration();
    }
}
