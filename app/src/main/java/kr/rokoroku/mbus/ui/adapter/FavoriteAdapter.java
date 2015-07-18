package kr.rokoroku.mbus.ui.adapter;

/**
 * Created by rok on 2015. 5. 29..
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableSwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.expandable.GroupPositionItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.wnafee.vector.MorphButton;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.RouteActivity;
import kr.rokoroku.mbus.StationActivity;
import kr.rokoroku.mbus.data.FavoriteDataProvider;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.FormatUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;


public class FavoriteAdapter
        extends AbstractExpandableItemAdapter<FavoriteAdapter.SectionViewHolder, FavoriteAdapter.ItemViewHolder>
        implements ExpandableDraggableItemAdapter<FavoriteAdapter.SectionViewHolder, FavoriteAdapter.ItemViewHolder>,
        ExpandableSwipeableItemAdapter<FavoriteAdapter.SectionViewHolder, FavoriteAdapter.ItemViewHolder> {

    private static final String TAG = "FavoriteAdapter";

    private static final int ITEM_SECTION = 1;
    private static final int ITEM_BUS = 2;
    private static final int ITEM_FOOTER = 3;

    private final RecyclerViewExpandableItemManager mExpandableItemManager;
    private final FavoriteDataProvider mProvider;
    private EventListener mEventListener;
    private Handler mHandler;

    private int mSwipedGroupPosition = -1;
    private int mSwipedChildPosition = -1;
    private int mDraggedGroupPosition = -1;
    private int mInPlaceDroppedGroupPosition = -1;
    private int mInPlaceDroppedChildPosition = -1;

    private Set<Long> mGeneratedIdSet = new HashSet<>();

    public FavoriteAdapter(RecyclerViewExpandableItemManager recyclerViewExpandableItemManager,
                           FavoriteDataProvider dataProvider) {
        mExpandableItemManager = recyclerViewExpandableItemManager;
        mProvider = dataProvider;
        mHandler = new Handler(Looper.getMainLooper());

        // ExpandableItemAdapter, ExpandableDraggableItemAdapter and ExpandableSwipeableItemAdapter
        // require stable ID, and also have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        int groupCount = mProvider.getGroupCount();
        return groupCount > 0 ? groupCount + 1 : 0;
    }

    @Override
    public int getChildCount(int groupPosition) {
        if (groupPosition < getGroupCount()) {
            return mProvider.getChildCount(groupPosition);
        } else {
            return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        FavoriteGroup groupItem = mProvider.getGroupItem(groupPosition);
        if (groupItem != null) {
            return groupItem.getId();
        } else {
            return generateRandomId();
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        FavoriteGroup.FavoriteItem childItem = mProvider.getChildItem(groupPosition, childPosition);
        if (childItem != null) {
            return childItem.getId();
        } else {
            return generateRandomId();
        }
    }

    private long generateRandomId() {
        long id;
        do {
            id = new Random().nextLong();
        } while (mGeneratedIdSet.contains(id));
        return id;
    }

    @Override
    public int getGroupItemViewType(int groupPoisition) {
        if (groupPoisition < mProvider.getGroupCount()) {
            return ITEM_SECTION;
        } else {
            return ITEM_FOOTER;
        }
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public void onBindGroupViewHolder(SectionViewHolder holder, int groupPosition, int viewType) {
        if (viewType == ITEM_SECTION) {
            holder.setItem(mProvider.getGroupItem(groupPosition));
        } else {
            holder.setItem(null);
            return;
        }

        holder.itemView.setEnabled(true);
        holder.itemView.setClickable(true);

        final int dragState = holder.getDragStateFlags();
        final int expandState = holder.getExpandStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {

            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                mHandler.post(() -> ViewCompat.animate(holder.mContainer).scaleX(0.95f).scaleY(0.95f).setDuration(200));

            } else {
                mHandler.post(() -> ViewCompat.animate(holder.mContainer).scaleX(1f).scaleY(1f).setDuration(200));
            }

            MorphButton.MorphState state;
            if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
                holder.mOverflowButton.setVisibility(View.VISIBLE);
                state = MorphButton.MorphState.END;
            } else {
                holder.mOverflowButton.setVisibility(View.GONE);
                state = MorphButton.MorphState.START;
            }
            if (holder.mIndicator.getState() != state) {
                holder.mIndicator.setState(state, true);
            }
        }
    }

    @Override
    public void onBindChildViewHolder(ItemViewHolder holder, int groupPosition, int childPosition, int viewType) {
        // group item
        final FavoriteGroup.FavoriteItem item = mProvider.getChildItem(groupPosition, childPosition);
        holder.itemView.setEnabled(true);
        holder.itemView.setClickable(true);

        // set listeners
//        holder.itemView.setOnClickListener(mItemViewOnClickListener);

        // set text
        if (item != null) {
            holder.setItem(item);
        }

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int expandState = holder.getExpandStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                ViewUtils.clearDrawableState(holder.mCardView.getForeground());

            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                mHandler.post(() -> ViewCompat.animate(holder.mCardView).scaleX(0.95f).scaleY(0.95f).setDuration(200));
            } else {
                mHandler.post(() -> ViewCompat.animate(holder.mCardView).scaleX(1f).scaleY(1f).setDuration(200));
            }
        }

        if (mInPlaceDroppedGroupPosition == groupPosition && mInPlaceDroppedChildPosition == childPosition) {
            holder.mOverflowButton.setVisibility(View.VISIBLE);
            mInPlaceDroppedGroupPosition = -1;
            mInPlaceDroppedChildPosition = -1;
        } else {
            holder.mOverflowButton.setVisibility(View.GONE);
        }
    }

    @Override
    public SectionViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.row_favorite_group, parent, false));
        } else {
            return new SectionViewHolder(inflater.inflate(R.layout.row_favorite_group, parent, false));
        }
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.row_favorite_item, parent, false));
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(SectionViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        if (groupPosition == getGroupCount()) {
            return false;
        }

        if (mSwipedGroupPosition == groupPosition) {
            return false;
        }

        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        final View containerView = holder.mContainer;
        final View dragHandleView = holder.itemView;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return !ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    /**
     * Draggable Interface
     */

    @Override
    public boolean onCheckGroupCanStartDrag(SectionViewHolder holder, int groupPosition, int x, int y) {
        Log.d("FavoriteAdapter", "onCheckCanDrag");

        if (groupPosition == mProvider.getGroupCount()) {
            return false;
        }

        if (holder.mTitle.getTranslationX() != 0) {
            return false;
        }

        if (mSwipedGroupPosition == groupPosition) {
            mSwipedGroupPosition = -1;
            return false;
        }

        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mTitle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        if (preX != x && preY != y) {
            preX = x;
            preY = y;
            return false;
        }
        preX = x;
        preY = y;

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    private int preX, preY;

    @Override
    public boolean onCheckChildCanStartDrag(ItemViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        Log.d("FavoriteAdapter", "onCheckCanDragChild");
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mCardView;
        final View dragHandleView = holder.mCardView;

        if (holder.mCardView.getTranslationX() != 0) {
            return false;
        }

        if (mSwipedChildPosition == childPosition) {
            // return false to raise View.OnClickListener#onClick() event
            mSwipedChildPosition = -1;
            return false;
        }

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);
        Log.d("FavoriteAdapter", containerView.getTop() + ", " + ViewCompat.getTranslationY(containerView));

        if (preX != x && preY != y) {
            preX = x;
            preY = y;
            return false;
        }
        preX = x;
        preY = y;

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }


    @Override
    public ItemDraggableRange onGetGroupItemDraggableRange(SectionViewHolder holder, int groupPosition) {
        if (mExpandableItemManager.isGroupExpanded(groupPosition)) {
            mExpandableItemManager.collapseGroup(groupPosition);
            mDraggedGroupPosition = groupPosition;
        } else {
            mDraggedGroupPosition = -1;
        }
        return new GroupPositionItemDraggableRange(0, mProvider.getGroupCount() - 1);
    }

    @Override
    public ItemDraggableRange onGetChildItemDraggableRange(ItemViewHolder holder, int groupPosition, int childPosition) {
        return new GroupPositionItemDraggableRange(0, mProvider.getGroupCount() - 1);
    }


    @Override
    public void onMoveGroupItem(int fromGroupPosition, int toGroupPosition) {
        Log.d("onMoveGroupItem", fromGroupPosition + "->" + toGroupPosition + " (prev:" + mDraggedGroupPosition + ")");
        if (mEventListener != null) {
            mEventListener.onGroupItemMoved(fromGroupPosition, toGroupPosition);
        }
        if (fromGroupPosition != toGroupPosition) {
            mProvider.moveGroupItem(fromGroupPosition, toGroupPosition);
        }
        if (mDraggedGroupPosition == fromGroupPosition) {
            mInPlaceDroppedGroupPosition = fromGroupPosition;
            mInPlaceDroppedChildPosition = -1;
            mHandler.postDelayed(() -> {
                mExpandableItemManager.notifyGroupAndChildrenItemsChanged(toGroupPosition);
                mExpandableItemManager.expandGroup(toGroupPosition);
                mDraggedGroupPosition = -1;
            }, 100);
        } else {
            mInPlaceDroppedGroupPosition = -1;
            mInPlaceDroppedChildPosition = -1;
        }
    }

    @Override
    public void onMoveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        Log.d("FavoriteAdapter", String.format("onMoveChildItem, (%d, %d) -> (%d, %d)", fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition));
        if (mEventListener != null) {
            mEventListener.onChildItemMoved(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
        }
        if (fromGroupPosition == toGroupPosition && fromChildPosition == toChildPosition) {
            mInPlaceDroppedGroupPosition = fromGroupPosition;
            mInPlaceDroppedChildPosition = fromChildPosition;
        } else {
            mProvider.moveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition);
            mExpandableItemManager.expandGroup(toGroupPosition);
            mInPlaceDroppedGroupPosition = -1;
            mInPlaceDroppedChildPosition = -1;
        }
    }

    /**
     * Swipeable Interface
     */

    @Override
    public int onGetGroupItemSwipeReactionType(SectionViewHolder holder, int groupPosition, int x, int y) {
        if (groupPosition == mProvider.getGroupCount() || onCheckGroupCanStartDrag(holder, groupPosition, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;

        } else if (mProvider.getChildCount(groupPosition) != 0) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH_WITH_RUBBER_BAND_EFFECT;

        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }
    }

    @Override
    public int onGetChildItemSwipeReactionType(ItemViewHolder holder, int groupPosition, int childPosition, int x, int y) {
        if (onCheckChildCanStartDrag(holder, groupPosition, childPosition, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        }

        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
    }

    @Override
    public void onSetGroupItemSwipeBackground(SectionViewHolder holder, int groupPosition, int type) {

    }

    @Override
    public void onSetChildItemSwipeBackground(ItemViewHolder holder, int groupPosition, int childPosition, int type) {

    }

    @Override
    public int onSwipeGroupItem(SectionViewHolder holder, int groupPosition, int result) {
        Log.d(TAG, "onSwipeGroupItem(groupPosition = " + groupPosition + ", result = " + result + ")");

        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                mSwipedGroupPosition = -1;
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;

            default:
            case RecyclerViewSwipeManager.RESULT_CANCELED:
                mSwipedGroupPosition = groupPosition;
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public int onSwipeChildItem(ItemViewHolder holder, int groupPosition, int childPosition, int result) {
        Log.d(TAG, "onSwipeChildItem(groupPosition = " + groupPosition + ", childPosition = " + childPosition + ", result = " + result + ")");

        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                mSwipedChildPosition = -1;
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;

            default:
            case RecyclerViewSwipeManager.RESULT_CANCELED:
                mSwipedChildPosition = childPosition;
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeGroupReaction(SectionViewHolder holder, int groupPosition, int result, int reaction) {
        Log.d(TAG, "onPerformAfterSwipeGroupReaction(groupPosition = " + groupPosition + ", result = " + result + ", reaction = " + reaction + ")");
        final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mExpandableItemManager.getFlatPosition(expandablePosition);

        if (flatPosition == -1) return;
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            mProvider.removeGroupItem(groupPosition);
            notifyDataSetChanged();

            if (mEventListener != null) {
                mEventListener.onGroupItemRemoved(groupPosition);
            }
        }
    }

    @Override
    public void onPerformAfterSwipeChildReaction(ItemViewHolder holder, int groupPosition, int childPosition, int result, int reaction) {
        Log.d(TAG, "onPerformAfterSwipeGroupReaction(groupPosition = " + groupPosition + ", childPosition = " + childPosition +
                ", result = " + result + ", reaction = " + reaction + ")");
        final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mExpandableItemManager.getFlatPosition(expandablePosition);

        if (flatPosition == -1) return;
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {

            mProvider.removeChildItem(groupPosition, childPosition);
            notifyDataSetChanged();

            if (mEventListener != null) {
                mEventListener.onChildItemRemoved(groupPosition, childPosition);
            }
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    /**
     * ViewHolder classes
     */

    public abstract class BaseViewHolder extends AbstractDraggableSwipeableItemViewHolder implements ExpandableItemViewHolder {

        private int mExpandStateFlags;

        public BaseViewHolder(View v) {
            super(v);
        }

        @Override
        public int getExpandStateFlags() {
            return mExpandStateFlags;
        }

        @Override
        public void setExpandStateFlags(int flag) {
            mExpandStateFlags = flag;
        }
    }

    public class ItemViewHolder extends BaseViewHolder implements View.OnClickListener {

        protected CardView mCardView;
        protected ImageView mItemIcon;
        protected TextView mItemTitle;
        protected TextView mItemLabel;
        protected TextView mItemDescription;
        protected ImageButton mOverflowButton;
        protected ImageButton mPaintButton;
        protected View mSeparator;
        protected FavoriteGroup.FavoriteItem mItem;
        protected View mLinkItemLayout;
        protected FavoriteLinkItemViewHolder mLinkItemViewHolder;

        public ItemViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.card_view);
            mItemIcon = (ImageView) v.findViewById(R.id.item_icon);
            mItemTitle = (TextView) v.findViewById(R.id.item_title);
            mItemLabel = (TextView) v.findViewById(R.id.item_label);
            mItemDescription = (TextView) v.findViewById(R.id.item_description);


            mLinkItemLayout = v.findViewById(R.id.link_layout);
            mLinkItemViewHolder = new FavoriteLinkItemViewHolder(mLinkItemLayout);
            mOverflowButton = (ImageButton) v.findViewById(R.id.overflow_button);
            mPaintButton = (ImageButton) v.findViewById(R.id.paint_button);
            mSeparator = v.findViewById(R.id.separator);

            mCardView.setOnClickListener(this);
            mOverflowButton.setOnClickListener(this);
        }

        public void setItem(FavoriteGroup.FavoriteItem item) {
            this.mItem = item;
            Context context = itemView.getContext();
            FavoriteGroup.FavoriteItem.Type itemType = item.getType();
            if (itemType == FavoriteGroup.FavoriteItem.Type.ROUTE) {
                Route route = item.getData(Route.class);
                if (route == null) return;

                int color = route.getType().getColor(context);

                mItemTitle.setText(route.getName());
                mItemTitle.setTextColor(color);
                mItemTitle.setTypeface(Typeface.DEFAULT_BOLD);
                mItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                mItemIcon.setImageResource(R.drawable.ic_bus);
                mItemIcon.setColorFilter(color);
                String description = route.getType().getDescription(context);
                String regionName = route.getRegionName();
                if (regionName == null) {
                    regionName = FormatUtils.formatRegionName(context, route);
                    route.setRegionName(regionName);
                }
                if (regionName != null) {
                    description = description + " / " + regionName;
                }
                mItemDescription.setText(description);

                if (!Provider.SEOUL.equals(route.getProvider())) {
                    mItemLabel.setText(route.getProvider().getCityName(context));
                    mItemLabel.setTextColor(color);
                    mItemLabel.setVisibility(View.VISIBLE);
                } else {
                    mItemLabel.setVisibility(View.GONE);
                }

                RouteStation routeStation = item.getExtraData(RouteStation.class);
                if (routeStation == null) {
                    mLinkItemLayout.setVisibility(View.GONE);
                } else {
                    mLinkItemLayout.setVisibility(View.VISIBLE);
                    mLinkItemViewHolder.setItem(route, routeStation);
                }

            } else if (itemType == FavoriteGroup.FavoriteItem.Type.STATION) {
                Station station = item.getData(Station.class);
                int color = Color.BLACK;

                if (station == null) return;
                mItemTitle.setText(station.getName());
                mItemTitle.setTextColor(color);
                mItemTitle.setTypeface(Typeface.DEFAULT);
                mItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                mItemIcon.setImageResource(R.drawable.ic_pin_drop);
                mItemIcon.setColorFilter(color);
                mItemLabel.setVisibility(View.GONE);

                String description = FormatUtils.formatStationIds(context, station);
                mItemDescription.setText(description);

                StationRoute stationRoute = item.getExtraData(StationRoute.class);
                if (stationRoute == null) {
                    mLinkItemLayout.setVisibility(View.GONE);
                } else {
                    mLinkItemLayout.setVisibility(View.VISIBLE);
                    mLinkItemViewHolder.setItem(station, stationRoute);
                }
            }
        }

        @Override
        public View getSwipeableContainerView() {
            return mCardView;
        }

        @Override
        public void onClick(View v) {
            if (v.equals(mCardView)) {
                if (mItem != null) {
                    Context context = v.getContext();
                    FavoriteGroup.FavoriteItem.Type type = mItem.getType();
                    Log.d("startActivity", mItem.toString());

                    if (type == FavoriteGroup.FavoriteItem.Type.ROUTE) {
                        Route route = mItem.getData(Route.class);
                        RouteStation routeStation = mItem.getExtraData(RouteStation.class);

                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(RouteActivity.EXTRA_KEY_ROUTE, (Parcelable) route);
                        intent.putExtra(RouteActivity.EXTRA_KEY_REDIRECT_STATION_ID, routeStation != null ? routeStation.getLocalId() : null);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    } else if (type == FavoriteGroup.FavoriteItem.Type.STATION) {
                        Station station = mItem.getData(Station.class);
                        StationRoute stationRoute = mItem.getExtraData(StationRoute.class);

                        Intent intent = new Intent(context, StationActivity.class);
                        intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
                        intent.putExtra(StationActivity.EXTRA_KEY_REDIRECT_ROUTE_ID, stationRoute != null ? stationRoute.getRouteId() : null);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            } else if (v.equals(mOverflowButton)) {

            }
        }
    }

    public class FavoriteLinkItemViewHolder {

        public View mLinkLayout;
        public ImageView mLinkItemIcon;
        public TextView mLinkItemTitle;
        public TextView mLinkItemLabel;
        public TextView mLinkItemDescription;

        public FavoriteLinkItemViewHolder(View view) {
            this.mLinkLayout = view;
            this.mLinkItemIcon = (ImageView) view.findViewById(R.id.link_item_icon);
            this.mLinkItemTitle = (TextView) view.findViewById(R.id.link_item_title);
            this.mLinkItemLabel = (TextView) view.findViewById(R.id.link_item_label);
            this.mLinkItemDescription = (TextView) view.findViewById(R.id.link_item_description);
        }

        public void setItem(Station station, StationRoute stationRoute) {
            Context context = mLinkLayout.getContext();
            RouteType routeType = stationRoute.getRouteType();
            int color = routeType.getColor(context);

            this.mLinkItemIcon.setImageResource(R.drawable.ic_bus);
            this.mLinkItemIcon.setColorFilter(color);
            this.mLinkItemTitle.setText(stationRoute.getRouteName());
            this.mLinkItemTitle.setTextColor(color);

            Provider provider = stationRoute.getProvider();
            if (!Provider.SEOUL.equals(provider)) {
                this.mLinkItemLabel.setVisibility(View.VISIBLE);
                this.mLinkItemLabel.setText(provider.getCityName(context));
                this.mLinkItemLabel.setTextColor(color);
            } else {
                this.mLinkItemLabel.setVisibility(View.GONE);
            }

            String description = FormatUtils.formatHeadingTo(context, stationRoute);
            if (description != null) {
                mLinkItemDescription.setText(description);
                mLinkItemDescription.setVisibility(View.VISIBLE);
            } else {
                mLinkItemDescription.setVisibility(View.GONE);
            }
        }

        public void setItem(Route route, RouteStation routeStation) {
            Context context = mLinkLayout.getContext();

            int color = ThemeUtils.getThemeColor(context, R.attr.cardPrimaryTextColor);
            this.mLinkItemTitle.setText(routeStation.getName());
            this.mLinkItemTitle.setTextColor(color);
            this.mLinkItemIcon.setImageResource(R.drawable.ic_pin_drop);
            this.mLinkItemIcon.setColorFilter(color);
            this.mLinkItemLabel.setVisibility(View.GONE);

            String description = FormatUtils.formatStationIds(context, routeStation);
            mLinkItemDescription.setText(description);
            mLinkItemDescription.setVisibility(View.VISIBLE);
        }
    }

    public class SectionViewHolder extends BaseViewHolder implements View.OnClickListener, View.OnTouchListener {

        public View mContainer;
        public TextView mTitle;
        public MorphButton mIndicator;
        public ImageButton mOverflowButton;
        public FavoriteGroup mItem;

        public SectionViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.container);
            mTitle = (TextView) v.findViewById(R.id.section_title);
            mIndicator = (MorphButton) v.findViewById(R.id.indicator);
            mOverflowButton = (ImageButton) v.findViewById(R.id.overflow_button);
            mOverflowButton.setOnClickListener(this);
            mOverflowButton.setOnTouchListener(this);
        }

        public void setItem(FavoriteGroup group) {
            mItem = group;
            if (group != null) mTitle.setText(group.getName());
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

        @Override
        public void onClick(View view) {
            ViewUtils.attachPopupMenu(view,
                    R.menu.menu_popup_favorite_group,
                    new MenuBuilder.Callback() {
                        @Override
                        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                            if (item.getItemId() == R.id.action_modify) {
                                Context context = view.getContext();
                                new MaterialDialog.Builder(context)
                                        .title(R.string.action_rename_favorite_group)
                                        .inputMaxLength(20)
                                        .input(context.getString(R.string.hint_input_group_name),
                                                mTitle.getText(), false, (dialog, input) -> {
                                                    if (!TextUtils.isEmpty(input)) {
                                                        String name = input.toString().trim();
                                                        mItem.setName(name);
                                                        mTitle.setText(name);
                                                        if (mInPlaceDroppedGroupPosition != -1) {
                                                            mExpandableItemManager.expandGroup(mInPlaceDroppedGroupPosition);
                                                        }
                                                    }
                                                }).show();

                            } else if (item.getItemId() == R.id.action_remove) {
                                int groupPosition = -1;
                                for (int i = 0; i < mProvider.getGroupCount(); i++) {
                                    FavoriteGroup groupItem = mProvider.getGroupItem(i);
                                    if (groupItem != null && groupItem.equals(mItem)) {
                                        groupPosition = i;
                                        break;
                                    }
                                }

                                if (groupPosition != -1) {
                                    mProvider.removeGroupItem(groupPosition);
                                    notifyDataSetChanged();

                                    if (mEventListener != null) {
                                        mEventListener.onGroupItemRemoved(groupPosition);
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        public void onMenuModeChange(MenuBuilder menu) {

                        }
                    });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    itemView.setClickable(false);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_HOVER_EXIT:
                    itemView.setClickable(true);
                    break;

            }
            return v.onTouchEvent(event);
        }

    }

    public class FooterViewHolder extends SectionViewHolder {

        public FooterViewHolder(View v) {
            super(v);
            mTitle.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);
            mOverflowButton.setVisibility(View.GONE);
        }
    }

    public interface EventListener {
        void onGroupItemMoved(int fromGroupPosition, int toGroupPosition);

        void onChildItemMoved(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

        void onGroupItemRemoved(int groupPosition);

        void onChildItemRemoved(int groupPosition, int childPosition);
    }

}
