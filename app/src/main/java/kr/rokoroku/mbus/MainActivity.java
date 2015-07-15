package kr.rokoroku.mbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Set;

import kr.rokoroku.mbus.ui.adapter.FavoriteAdapter;
import kr.rokoroku.mbus.data.FavoriteDataProvider;
import kr.rokoroku.mbus.core.Database;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.SearchHistory;
import kr.rokoroku.mbus.util.ThemeUtils;


public class MainActivity extends AbstractBaseActivity
        implements SearchBox.SearchListener, SearchBox.MenuListener, FavoriteAdapter.EventListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "expandable_state";

    private SearchBox mSearchBox;
    private RecyclerView mRecyclerView;
    private CoordinatorLayout mCoordinatorLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FavoriteAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private FloatingActionMenu mPlusButton;
    private FloatingActionButton mAddNewGroupButton;
    private FloatingActionButton mAddNewEntryButton;

    private FavoriteDataProvider mFavoriteDataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDrawerEnable(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        mPlusButton = (FloatingActionMenu) findViewById(R.id.fab_plus);
        mAddNewGroupButton = (FloatingActionButton) findViewById(R.id.fab_new_group);
        mAddNewEntryButton = (FloatingActionButton) findViewById(R.id.fab_new_entry);

        Favorite favorite = FavoriteFacade.getInstance().getCurrentFavorite();
        mFavoriteDataProvider = new FavoriteDataProvider(favorite);

        initSearchBox();
        initRecyclerView(savedInstanceState);
        initFloatingActionButtons();

        int actionbarHeight = ThemeUtils.getDimension(this, R.attr.actionBarSize);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);
        mSwipeRefreshLayout.setEnabled(false);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        if (mFavoriteDataProvider != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initSearchBox() {
        mSearchBox = new SearchBox(this);
        getToolbarLayer().removeAllViewsInLayout();
        getToolbarLayer().addView(mSearchBox,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        mSearchBox.enableVoiceRecognition(this);
        mSearchBox.setHint("노선 혹은 정류소 이름을 입력하세요.");
        mSearchBox.setMenuListener(this);
        mSearchBox.setSearchListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchBox.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        }
        reloadSearchQuery();
    }

    private void initRecyclerView(Bundle savedInstanceState) {

        Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        mAdapter = new FavoriteAdapter(mRecyclerViewExpandableItemManager, mFavoriteDataProvider);
        mAdapter.setEventListener(this);
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);    // wrap for expanding
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mWrappedAdapter);   // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        // Also need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addItemDecoration(getAppBarHeaderSpacingItemDecoration());
        mRecyclerView.addOnScrollListener(getScrollListener());


        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop > ExpandableItem
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mRecyclerViewExpandableItemManager.expandGroup(i);
        }
    }

    private void initFloatingActionButtons() {

        mAddNewGroupButton.setOnClickListener(v -> {
            new MaterialDialog.Builder(MainActivity.this)
                    .title("새 그룹")
                    .input("그룹 이름을 입력하셈", null, (materialDialog, charSequence) -> {
                        if (!TextUtils.isEmpty(charSequence)) {
                            String name = charSequence.toString().trim();
                            for (FavoriteGroup group : mFavoriteDataProvider.getFavorite().getFavoriteGroups()) {
                                if (group.getName().equals(name)) return;
                            }
                            FavoriteGroup favoriteGroup = new FavoriteGroup(name);
                            int groupPosition = 0;

                            mFavoriteDataProvider.addGroupItem(groupPosition, favoriteGroup);
                            mAdapter.notifyDataSetChanged();

                            getHandler().postDelayed(() -> {
                                mRecyclerView.smoothScrollToPosition(groupPosition);
                                mRecyclerViewExpandableItemManager.expandGroup(groupPosition);
                                Snackbar.make(mCoordinatorLayout, "그룹이 생성되었습니다.", Snackbar.LENGTH_LONG).show();
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

        mAddNewEntryButton.setOnClickListener(v -> {

            Route randomRoute = Database.getInstance().getRandomRoute();
            if (randomRoute != null && randomRoute.getId() != null) {
                int groupPosition = 0;

                FavoriteGroup.FavoriteItem favoriteItem = new FavoriteGroup.FavoriteItem(randomRoute);
                FavoriteGroup groupItem = mFavoriteDataProvider.getGroupItem(groupPosition);
                if (groupItem == null) {
                    groupItem = new FavoriteGroup("기본 그룹");
                    mFavoriteDataProvider.addGroupItem(groupPosition, groupItem);
                }
                groupItem.add(favoriteItem);
                mAdapter.notifyDataSetChanged();
                getHandler().postDelayed(() -> {
                    mRecyclerView.smoothScrollToPosition(groupPosition);
                    mRecyclerViewExpandableItemManager.expandGroup(groupPosition);
                    Snackbar.make(mCoordinatorLayout, "즐겨찾기가 추가되었습니다.", Snackbar.LENGTH_LONG).show();
                    showToolbarLayer();
                }, 100);
            }
            mPlusButton.close(true);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
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
        if (keyword == null || keyword.length() < 2) {
            if (!TextUtils.isEmpty(keyword.replaceAll("\\d", ""))) {
                Toast.makeText(this, "2글자 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mSearchBox.setSearchString(null);
        mSearchBox.clearSearchable();

        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("query", keyword);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if (mSearchBox.isSearchOpen()) {
            mSearchBox.toggleSearch();
        } else {
            new MaterialDialog.Builder(this)
                    .title("종료")
                    .content("종료할꺼야?")
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Favorite currentFavorite = FavoriteFacade.getInstance().getCurrentFavorite();
        Database.getInstance().putBookmark(currentFavorite.getName(), currentFavorite);
    }

    @Override
    public void onMenuClick() {
        openDrawer();
    }

    @Override
    public void onGroupItemRemoved(int groupPosition) {
        Snackbar.make(mCoordinatorLayout, "그룹이 제거되었습니다.", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    undoLastRemoval();
                }).show();
        showToolbarLayer();
    }

    @Override
    public void onChildItemRemoved(int groupPosition, int childPosition) {
        Snackbar.make(mCoordinatorLayout, "즐겨찾기가 제거되었습니다.", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
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
        if (position[0] >= 0 && position[1] == -1) {
            long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForGroup(position[0]);
            int flatPosition = mRecyclerViewExpandableItemManager.getFlatPosition(expandablePosition);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(flatPosition);
            mRecyclerViewExpandableItemManager.expandGroup(position[0]);

        } else if (position[0] >= 0 && position[1] >= 0) {
            long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(position[0], position[1]);
            int flatPosition = mRecyclerViewExpandableItemManager.getFlatPosition(expandablePosition);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(flatPosition);
            mRecyclerViewExpandableItemManager.expandGroup(position[0]);
        }
    }

    public void reloadSearchQuery() {
        if(mSearchBox != null) {
            Set<SearchHistory> searchHistoryTable = Database.getInstance().getSearchHistoryTable();
            ArrayList<SearchResult> arrayList = new ArrayList<>();
            for (SearchHistory searchHistory : searchHistoryTable) {
                arrayList.add(new SearchResult(searchHistory.getTitle(), null));
            }
            mSearchBox.setSearchables(arrayList);
        }
    }

}
