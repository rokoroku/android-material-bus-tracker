package kr.rokoroku.mbus;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.utils.BaseWrapperAdapter;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Set;

import kr.rokoroku.mbus.ui.adapter.SearchAdapter;
import kr.rokoroku.mbus.data.SearchDataProvider;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.Database;
import kr.rokoroku.mbus.data.model.SearchHistory;
import kr.rokoroku.mbus.util.ThemeUtils;


public class SearchActivity extends AbstractBaseActivity
        implements SearchBox.SearchListener, SearchBox.MenuListener {

    private SearchBox mSearchBox;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ContentLoadingProgressBar mProgressBar;

    private SearchDataProvider mSearchDataProvider;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter<SearchAdapter.SearchViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawerEnable(true);

        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initSearchBox();
        initRecyclerView();

        int actionbarHeight = ThemeUtils.getDimension(this, R.attr.actionBarSize);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);

        final String query = getIntent().getStringExtra("query");
        if(query != null) mSearchBox.postDelayed(() -> mSearchBox.triggerSearch(query), 100);
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
        mSearchBox.setHint("노선 혹은 정류소 이름을 입력하세요.");
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
        mAdapter.setHasStableIds(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new BaseWrapperAdapter<>(mAdapter));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());
        mRecyclerView.addItemDecoration(getAppBarHeaderSpacingItemDecoration());
        mRecyclerView.addOnScrollListener(getScrollListener());
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
                Toast.makeText(this, "2글자 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Set<SearchHistory> searchHistoryTable = Database.getInstance().getSearchHistoryTable();
        SearchHistory searchHistory = new SearchHistory(keyword);

        //noinspection StatementWithEmptyBody
        while(searchHistoryTable.remove(searchHistory));
        searchHistoryTable.add(searchHistory);

        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(true);

        mSearchDataProvider.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        ApiFacade apiFacade = ApiFacade.getInstance();
        apiFacade.searchByKeyword(keyword, mSearchDataProvider, new ApiFacade.SimpleProgressCallback() {
            @Override
            public void onComplete(boolean success) {
                mSearchDataProvider.sortByKeyword(keyword);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.postDelayed(() -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(false);
                }, 500);
            }


            @Override
            public void onError(int progress, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SearchActivity.this, "Failed retrieving data", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSearchBox.isSearchOpen()) {
            mSearchBox.toggleSearch();
        } else {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onMenuClick() {
        onBackPressed();
    }
}
