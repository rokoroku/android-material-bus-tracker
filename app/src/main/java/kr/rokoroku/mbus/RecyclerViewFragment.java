package kr.rokoroku.mbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.BaseWrapperAdapter;

import java.util.ArrayDeque;

import kr.rokoroku.mbus.ui.adapter.FavoriteAdapter;
import kr.rokoroku.mbus.ui.adapter.SearchAdapter;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;

public class RecyclerViewFragment extends Fragment {

    public static final String TAG_ADAPTER = "type";

    private String mAdapterType;
    private ViewGroup mContentView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mOverlayView;
    private RecyclerViewInterface recyclerViewInterface;
    private ArrayDeque<Runnable> pendingTasks = new ArrayDeque<>();

    public static RecyclerViewFragment createInstance(String type) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TAG_ADAPTER, type);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mAdapterType = args.getString(TAG_ADAPTER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContentView = (ViewGroup) view;

        // run pending tasks
        int tasks = pendingTasks.size();
        while (tasks-- > 0) {
            pendingTasks.poll().run();
        }
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RecyclerViewInterface) {
            recyclerViewInterface = (RecyclerViewInterface) activity;
        } else {
            throw new RuntimeException("Activity must implement RecyclerViewInterface");
        }

        // get adapter type from interface
        if (mAdapterType == null) mAdapterType = getArguments().getString(TAG_ADAPTER);
        RecyclerView.Adapter adapter = recyclerViewInterface.getAdapter(mAdapterType);
        if (adapter instanceof FavoriteAdapter) {
            setAdapter((FavoriteAdapter) adapter);

        } else if (adapter instanceof SearchAdapter) {
            setAdapter((SearchAdapter) adapter);
        }

        // run pending tasks
        int tasks = pendingTasks.size();
        while (tasks-- > 0) {
            pendingTasks.poll().run();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recyclerViewInterface = null;
    }

    public void setAdapter(SearchAdapter searchAdapter) {

        if (mRecyclerView == null || getActivity() == null) {
            pendingTasks.addFirst(() -> setAdapter(searchAdapter));

        } else if (mAdapter != searchAdapter) {
            mAdapter = searchAdapter;

            // disable swipe refresh
            mSwipeRefreshLayout.setEnabled(false);

            //noinspection unchecked
            mRecyclerView.setAdapter(new BaseWrapperAdapter<>(searchAdapter));
            mRecyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());

            FragmentActivity activity = getActivity();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.addItemDecoration(recyclerViewInterface.getItemDecoration());
            mRecyclerView.addOnScrollListener(recyclerViewInterface.getScrollListener());

            if (activity instanceof AbstractBaseActivity) {
                int actionbarHeight = ThemeUtils.getDimension(activity, R.attr.actionBarSize);
                mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);
            }
        }
    }

    public void setAdapter(FavoriteAdapter favoriteAdapter) {

        if (mRecyclerView == null || getActivity() == null) {
            pendingTasks.addFirst(() -> setAdapter(favoriteAdapter));

        } else if (mAdapter != favoriteAdapter) {
            mAdapter = favoriteAdapter;

            // disable swipe refresh
            mSwipeRefreshLayout.setEnabled(false);

            // expandable item manager
            RecyclerViewExpandableItemManager expandableItemManager = favoriteAdapter.getExpandableItemManager();

            // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
            RecyclerViewTouchActionGuardManager touchActionGuardManager = new RecyclerViewTouchActionGuardManager();
            touchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
            touchActionGuardManager.setEnabled(true);

            // drag & drop manager
            RecyclerViewDragDropManager dragDropManager = new RecyclerViewDragDropManager();
            dragDropManager.setInitiateOnLongPress(true);
            dragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));

            // swipe manager
            RecyclerViewSwipeManager swipeManager = new RecyclerViewSwipeManager();

            // wrap adapter
            RecyclerView.Adapter wrappedAdapter = expandableItemManager.createWrappedAdapter(favoriteAdapter);    // wrap for expanding
            wrappedAdapter = dragDropManager.createWrappedAdapter(wrappedAdapter);   // wrap for dragging
            wrappedAdapter = swipeManager.createWrappedAdapter(wrappedAdapter);      // wrap for swiping

            // Change animations are enabled by default since support-v7-recyclerview v22.
            // Disable the change animation in order to make turning back animation of swiped item works properly.
            // Also need to disable them when using animation indicator.
            GeneralItemAnimator animator = new SwipeDismissItemAnimator();
            animator.setSupportsChangeAnimations(false);

            mRecyclerView.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
            mRecyclerView.setItemAnimator(animator);

            FragmentActivity activity = getActivity();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.addItemDecoration(recyclerViewInterface.getItemDecoration());
            mRecyclerView.addOnScrollListener(recyclerViewInterface.getScrollListener());

            if (activity instanceof AbstractBaseActivity) {
                int actionbarHeight = ThemeUtils.getDimension(activity, R.attr.actionBarSize);
                mSwipeRefreshLayout.setProgressViewOffset(true, 0, actionbarHeight * 2);
            }

            // NOTE:
            // The initialization order is very important! This order determines the priority of touch event handling.
            //
            // priority: TouchActionGuard > Swipe > DragAndDrop > ExpandableItem
            touchActionGuardManager.attachRecyclerView(mRecyclerView);
            swipeManager.attachRecyclerView(mRecyclerView);
            dragDropManager.attachRecyclerView(mRecyclerView);
            expandableItemManager.attachRecyclerView(mRecyclerView);

            for (int i = 0; i < favoriteAdapter.getGroupCount(); i++) {
                expandableItemManager.expandGroup(i);
            }
        }
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (mSwipeRefreshLayout == null) {
            pendingTasks.add(() -> mSwipeRefreshLayout.setOnRefreshListener(listener));
        } else {
            mSwipeRefreshLayout.setOnRefreshListener(listener);
        }
    }

    public void setRefreshEnabled(boolean enable) {
        if (mSwipeRefreshLayout == null) {
            pendingTasks.add(() -> mSwipeRefreshLayout.setEnabled(enable));
        } else {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    public void setRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout == null) {
            pendingTasks.add(() -> setRefreshing(refreshing));
        } else {
            ViewUtils.runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setOverlayView(View view, boolean animate) {
        if (mContentView == null) {
            pendingTasks.add(() -> setOverlayView(view, true));

        } else if (mOverlayView != view) {
            ViewUtils.runOnUiThread(() -> {
                if (mOverlayView != null) {
                    final View overlaidView = mOverlayView;
                    if(animate) {
                        overlaidView.animate().alpha(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mContentView.removeView(overlaidView);
                            }
                        });
                    } else {
                        mContentView.removeView(overlaidView);
                    }
                }
                if (view != null) {
                    mContentView.addView(view);
                    if(animate) {
                        view.setAlpha(0);
                        view.animate().alpha(1).setDuration(250);
                    }
                }
                mOverlayView = view;
            });
        }
    }

    public interface RecyclerViewInterface {
        RecyclerView.Adapter getAdapter(String tag);

        RecyclerView.ItemDecoration getItemDecoration();

        RecyclerView.OnScrollListener getScrollListener();
    }
}
