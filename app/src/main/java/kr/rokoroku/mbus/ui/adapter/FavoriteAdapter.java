package kr.rokoroku.mbus.ui.adapter;

/**
 * Created by rok on 2015. 5. 29..
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fsn.cauly.CaulyNativeAdView;
import com.fsn.cauly.CaulyNativeAdViewListener;
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
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.util.CaulyAdUtil;
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
        extends AbstractExpandableItemAdapter<FavoriteAdapter.GroupViewHolder, FavoriteAdapter.ItemViewHolder>
        implements ExpandableDraggableItemAdapter<FavoriteAdapter.GroupViewHolder, FavoriteAdapter.ItemViewHolder>,
        ExpandableSwipeableItemAdapter<FavoriteAdapter.GroupViewHolder, FavoriteAdapter.ItemViewHolder> {

    private static final String TAG = "FavoriteAdapter";

    private static final int ITEM_SECTION = 1;
    private static final int ITEM_BUS = 2;
    private static final int ITEM_AD = 3;
    private static final int ITEM_FOOTER = 4;

    private final RecyclerViewExpandableItemManager mExpandableItemManager;
    private final FavoriteDataProvider mProvider;
    private EventListener mEventListener;
    private int mAdPosition = -1;

    private int mSwipedGroupPosition = -1;
    private int mSwipedChildPosition = -1;
    private int mTemporariliyCollapsedGroupPosition = -1;
    private int mInPlaceDroppedGroupPosition = -1;
    private int mInPlaceDroppedChildPosition = -1;

    private Set<Long> mGeneratedIdSet = new HashSet<>();
    private String mAdTag;
    private boolean isAdRemoveTriggered = false;

    public FavoriteAdapter(RecyclerViewExpandableItemManager recyclerViewExpandableItemManager,
                           FavoriteDataProvider dataProvider) {
        mExpandableItemManager = recyclerViewExpandableItemManager;
        mProvider = dataProvider;

        // ExpandableItemAdapter, ExpandableDraggableItemAdapter and ExpandableSwipeableItemAdapter
        // require stable ID, and also have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public RecyclerViewExpandableItemManager getExpandableItemManager() {
        return mExpandableItemManager;
    }

    public int getAdPosition() {
        int groupCount = mProvider.getGroupCount();
        if (groupCount == 0) return -1;
        else return mAdPosition;
    }

    public void setAdPosition(int position) {
        this.mAdPosition = position;
    }

    public String getAdTag() {
        return mAdTag;
    }

    public void setAdTag(String tag) {
        this.mAdTag = tag;
    }

    @Override
    public int getGroupCount() {
        int groupCount = mProvider.getGroupCount();
        int result = groupCount > 0 ? groupCount + 1 : 0;
        if (getAdPosition() != -1) result++;
        return result;
    }

    @Override
    public int getChildCount(int groupPosition) {
        int adPosition = getAdPosition();
        if (groupPosition == adPosition) {
            return 0;
        } else {
            int realGroupPosition = groupPosition;
            if (adPosition != -1 && groupPosition > adPosition) {
                realGroupPosition--;
            }
            return mProvider.getChildCount(realGroupPosition);
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        int adPosition = getAdPosition();
        if (groupPosition == adPosition) {
            return mAdTag.hashCode();
        } else {
            int realGroupPosition = groupPosition;
            if (adPosition != -1 && groupPosition > adPosition) {
                realGroupPosition--;
            }
            FavoriteGroup groupItem = mProvider.getGroupItem(realGroupPosition);
            if (groupItem != null) {
                return groupItem.getId();
            }
        }
        return generateRandomId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        int adPosition = getAdPosition();
        int realGroupPosition = groupPosition;
        if (adPosition != -1 && groupPosition > adPosition) {
            realGroupPosition--;
        }
        FavoriteItem childItem = mProvider.getChildItem(realGroupPosition, childPosition);
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
    public int getGroupItemViewType(int groupPosition) {
        int adPosition = getAdPosition();
        if (groupPosition == adPosition) {
            return ITEM_AD;
        } else {
            int realGroupPosition = groupPosition;
            if (adPosition != -1 && groupPosition > adPosition) {
                realGroupPosition--;
            }
            if (realGroupPosition < mProvider.getGroupCount()) {
                return ITEM_SECTION;
            } else {
                return ITEM_FOOTER;
            }
        }
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, int viewType) {
        int adPosition = getAdPosition();
        if (adPosition != -1 && groupPosition > adPosition) groupPosition--;
        if (viewType == ITEM_AD) {
            String adTag = getAdTag();
            if (adTag != null) {
                AdViewHolder adViewHolder = (AdViewHolder) holder;
                adViewHolder.requestAd(adTag);
            }
            final int dragState = holder.getDragStateFlags();
            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                CardView cardView = (CardView) holder.itemView.findViewById(R.id.card_view);
                if (cardView != null) {
                    ViewUtils.clearDrawableState(cardView.getForeground());
                }
            }
        } else if (viewType == ITEM_SECTION) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.setItem(mProvider.getGroupItem(groupPosition));

            sectionViewHolder.itemView.setEnabled(true);
            sectionViewHolder.itemView.setClickable(true);

            final int dragState = sectionViewHolder.getDragStateFlags();
            final int expandState = sectionViewHolder.getExpandStateFlags();
            final int swipeState = sectionViewHolder.getSwipeStateFlags();

            if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                    ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) ||
                    ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {

                if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {

                } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                    ViewUtils.runOnUiThread(() -> ViewCompat.animate(sectionViewHolder.mContainer)
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(200));

                } else {
                    ViewUtils.runOnUiThread(() -> ViewCompat.animate(sectionViewHolder.mContainer)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200));
                }

                MorphButton.MorphState state;
                if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
                    sectionViewHolder.mOverflowButton.setVisibility(View.VISIBLE);
                    state = MorphButton.MorphState.END;
                } else {
                    sectionViewHolder.mOverflowButton.setVisibility(View.GONE);
                    state = MorphButton.MorphState.START;
                }
                if (sectionViewHolder.mIndicator.getState() != state) {
                    sectionViewHolder.mIndicator.setState(state, true);
                }
            }
        }
    }

    @Override
    public void onBindChildViewHolder(ItemViewHolder holder, int groupPosition,
                                      int childPosition, int viewType) {
        // group item
        int adPosition = getAdPosition();
        if (adPosition != -1 && groupPosition > adPosition) groupPosition--;
        final FavoriteItem item = mProvider.getChildItem(groupPosition, childPosition);
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
                ViewUtils.runOnUiThread(() -> ViewCompat.animate(holder.mCardView)
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(200));
            } else {
                ViewUtils.runOnUiThread(() -> ViewCompat.animate(holder.mCardView)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200));
            }
        }
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_SECTION) {
            return new SectionViewHolder(inflater.inflate(R.layout.row_favorite_group, parent, false));
        } else if (viewType == ITEM_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
        } else if (viewType == ITEM_AD) {
            return new AdViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
        }
        return null;
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.row_favorite_item, parent, false));
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder,
                                                   int groupPosition, int x, int y, boolean expand) {
        if (holder instanceof SectionViewHolder) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            if (getAdPosition() != -1 && groupPosition > getAdPosition()) {
                groupPosition--;
            }

            if (groupPosition == getGroupCount()) {
                return false;
            }

            if (mSwipedGroupPosition == groupPosition) {
                return false;
            }

            // check is enabled
            if (!(sectionViewHolder.itemView.isEnabled() && sectionViewHolder.itemView.isClickable())) {
                return false;
            }

            final View containerView = sectionViewHolder.mContainer;
            final View dragHandleView = sectionViewHolder.itemView;

            final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
            final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

            return !ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);

        } else {
            return false;
        }
    }

    /**
     * Draggable Interface
     */
    private int preX, preY;

    @Override
    public boolean onCheckGroupCanStartDrag(GroupViewHolder holder,
                                            int groupPosition, int x, int y) {
        Log.d("FavoriteAdapter", "onCheckCanDrag");

        if (mSwipedGroupPosition == groupPosition) {
            mSwipedGroupPosition = -1;
            return false;
        }

        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.getDragHandleView();
        final View swipeableContainerView = holder.getSwipeableContainerView();

        if (swipeableContainerView != null && swipeableContainerView.getTranslationX() != 0) {
            return false;
        }

        if (containerView != null && dragHandleView != null) {
            if (dragHandleView.equals(containerView)) {
                return true;
            }

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

        } else {
            return false;
        }
    }

    @Override
    public boolean onCheckChildCanStartDrag(ItemViewHolder holder, int groupPosition,
                                            int childPosition, int x, int y) {
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
    public ItemDraggableRange onGetGroupItemDraggableRange(GroupViewHolder sectionViewHolder,
                                                           int groupPosition) {
        if (mExpandableItemManager.isGroupExpanded(groupPosition)) {
            mExpandableItemManager.collapseGroup(groupPosition);
            mTemporariliyCollapsedGroupPosition = groupPosition;
        } else {
            mTemporariliyCollapsedGroupPosition = -1;
        }
        int draggableStartRange = 0;
        int draggableEndRange = getGroupCount() - 1;
        if (getAdPosition() >= 0) {
            draggableEndRange--;
//            if(getAdPosition() == 0) {
//                draggableStartRange++;
//            } else if(getAdPosition() == draggableEndRange) {
//                draggableEndRange--;
//            }
        }
        return new GroupPositionItemDraggableRange(draggableStartRange, draggableEndRange);
    }

    @Override
    public ItemDraggableRange onGetChildItemDraggableRange(ItemViewHolder sectionViewHolder,
                                                           int groupPosition, int childPosition) {
        int draggableStartRange = 0;
        int draggableEndRange = getGroupCount() - 1;
        if (getAdPosition() >= 0) {
            draggableEndRange--;
            if (getAdPosition() == 0) {
                draggableStartRange++;
            } else if (getAdPosition() == draggableEndRange) {
                draggableEndRange--;
            }
        }
        return new GroupPositionItemDraggableRange(draggableStartRange, draggableEndRange);
    }


    @Override
    public void onMoveGroupItem(int fromGroupPosition, int toGroupPosition) {
        Log.d("onMoveGroupItem", fromGroupPosition + "->" + toGroupPosition + " (prev:" + mTemporariliyCollapsedGroupPosition + ")");
        if (fromGroupPosition == getAdPosition()) {
            mAdPosition = toGroupPosition;
            ViewUtils.runOnUiThread(this::notifyDataSetChanged, 100);

        } else {
            if (mEventListener != null) {
                mEventListener.onGroupItemMoved(fromGroupPosition, toGroupPosition);
            }
            if (fromGroupPosition != toGroupPosition) {
                int realFromGroupPosition = fromGroupPosition;
                int realToGroupPosition = toGroupPosition;
                int adPosition = getAdPosition();
                if (adPosition != -1) {
                    if (fromGroupPosition > adPosition) {
                        realFromGroupPosition--;
                    }
                    if (toGroupPosition > adPosition) {
                        realToGroupPosition--;
                    }

                    if (fromGroupPosition < adPosition && adPosition <= toGroupPosition) {
                        if (mAdPosition > 0) {
                            mAdPosition--;
                        }
                    } else if (fromGroupPosition > adPosition && adPosition >= toGroupPosition) {
                        if (mAdPosition <= mProvider.getGroupCount()) {
                            mAdPosition++;
                        }
                    }

                }
                if (realFromGroupPosition != realToGroupPosition) {
                    mProvider.moveGroupItem(realFromGroupPosition, realToGroupPosition);
                } else {
                    ViewUtils.runOnUiThread(this::notifyDataSetChanged, 100);
                }
            }
        }
        if (mTemporariliyCollapsedGroupPosition == fromGroupPosition) {
            mInPlaceDroppedGroupPosition = fromGroupPosition;
            mInPlaceDroppedChildPosition = -1;
            ViewUtils.runOnUiThread(() -> {
                mExpandableItemManager.notifyGroupAndChildrenItemsChanged(toGroupPosition);
                mExpandableItemManager.expandGroup(toGroupPosition);
                mTemporariliyCollapsedGroupPosition = -1;
            }, 100);
        } else {
            mInPlaceDroppedGroupPosition = -1;
            mInPlaceDroppedChildPosition = -1;
        }
    }

    @Override
    public void onMoveChildItem(int fromGroupPosition, int fromChildPosition,
                                int toGroupPosition, int toChildPosition) {
        Log.d("FavoriteAdapter", String.format("onMoveChildItem, (%d, %d) -> (%d, %d)", fromGroupPosition, fromChildPosition, toGroupPosition, toChildPosition));
        int realFromGroupPosition = fromGroupPosition;
        int realToGroupPosition = toGroupPosition;
        int adPosition = getAdPosition();
        if (adPosition != -1) {
            if (toGroupPosition > adPosition && toGroupPosition > 0) realToGroupPosition--;
            if (fromGroupPosition > adPosition && fromGroupPosition > 0) realFromGroupPosition--;
        }
        if (mEventListener != null) {
            mEventListener.onChildItemMoved(realFromGroupPosition, fromChildPosition, realToGroupPosition, toChildPosition);
        }
        if (fromGroupPosition == toGroupPosition && fromChildPosition == toChildPosition) {
            mInPlaceDroppedGroupPosition = fromGroupPosition;
            mInPlaceDroppedChildPosition = fromChildPosition;
        } else {
            mProvider.moveChildItem(realFromGroupPosition, fromChildPosition, realToGroupPosition, toChildPosition);
            mExpandableItemManager.expandGroup(toGroupPosition);
            mInPlaceDroppedGroupPosition = -1;
            mInPlaceDroppedChildPosition = -1;
        }
    }

    /**
     * Swipeable Interface
     */

    @Override
    public int onGetGroupItemSwipeReactionType(GroupViewHolder holder,
                                               int groupPosition, int x, int y) {
        int adPosition = getAdPosition();
        int realGroupPosition = groupPosition;
        if (adPosition == groupPosition) {
            // ad
            if(isAdRemoveTriggered) {
                return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
            } else {
                isAdRemoveTriggered = true;
                return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH_WITH_RUBBER_BAND_EFFECT;
            }
        } else {
            // normal group
            isAdRemoveTriggered = false;
            if (adPosition != -1 && groupPosition > adPosition) {
                realGroupPosition--;
            }
            if (realGroupPosition == mProvider.getGroupCount() || onCheckGroupCanStartDrag(holder, groupPosition, x, y)) {
                return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;

            } else if (mProvider.getChildCount(realGroupPosition) != 0) {
                if(mExpandableItemManager.isGroupExpanded(groupPosition)) {
                    return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
                } else {
                    return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH_WITH_RUBBER_BAND_EFFECT;
                }
            } else {
                return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
            }
        }
    }

    @Override
    public int onGetChildItemSwipeReactionType(ItemViewHolder sectionViewHolder,
                                               int groupPosition, int childPosition, int x, int y) {
        if (onCheckChildCanStartDrag(sectionViewHolder, groupPosition, childPosition, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        }

        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
    }

    @Override
    public void onSetGroupItemSwipeBackground(GroupViewHolder holder,
                                              int groupPosition, int type) {

    }

    @Override
    public void onSetChildItemSwipeBackground(ItemViewHolder sectionViewHolder,
                                              int groupPosition, int childPosition, int type) {

    }

    @Override
    public int onSwipeGroupItem(GroupViewHolder holder, int groupPosition,
                                int result) {
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
    public int onSwipeChildItem(ItemViewHolder sectionViewHolder, int groupPosition,
                                int childPosition, int result) {
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
    public void onPerformAfterSwipeGroupReaction(GroupViewHolder holder,
                                                 int groupPosition, int result, int reaction) {
        Log.d(TAG, "onPerformAfterSwipeGroupReaction(groupPosition = " + groupPosition + ", result = " + result + ", reaction = " + reaction + ")");
        final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mExpandableItemManager.getFlatPosition(expandablePosition);

        if (flatPosition == -1) return;
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            int adPosition = getAdPosition();
            int realGroupPosition = groupPosition;
            if (adPosition != -1) {
                if (groupPosition > adPosition) {
                    realGroupPosition--;
                } else if (groupPosition < adPosition) {
                    setAdPosition(adPosition - 1);
                }
            }

            if(adPosition != groupPosition) {
                mProvider.removeGroupItem(realGroupPosition);
                notifyDataSetChanged();

                if (mEventListener != null) {
                    mEventListener.onGroupItemRemoved(realGroupPosition);
                }

            } else {
                mAdPosition = -1;
                notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onPerformAfterSwipeChildReaction(ItemViewHolder sectionViewHolder,
                                                 int groupPosition, int childPosition, int result, int reaction) {
        Log.d(TAG, "onPerformAfterSwipeGroupReaction(groupPosition = " + groupPosition + ", childPosition = " + childPosition +
                ", result = " + result + ", reaction = " + reaction + ")");
        final long expandablePosition = RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mExpandableItemManager.getFlatPosition(expandablePosition);

        if (flatPosition == -1) return;
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            int adPosition = getAdPosition();
            int realGroupPosition = groupPosition;
            if (adPosition != -1) {
                if (groupPosition > adPosition) {
                    realGroupPosition--;
                }
            }
            boolean groupRemoved = mProvider.getChildCount(realGroupPosition) == 1;
            if(groupRemoved && adPosition != -1) {
                if (groupPosition < adPosition) {
                    setAdPosition(adPosition - 1);
                }
            }

            mProvider.removeChildItem(realGroupPosition, childPosition);
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
        protected View mSeparator;
        protected FavoriteItem mItem;
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
            mSeparator = v.findViewById(R.id.separator);

            mCardView.setOnClickListener(this);
            //mOverflowButton.setOnClickListener(this);
        }

        public void setItem(FavoriteItem item) {
            this.mItem = item;
            Context context = itemView.getContext();
            FavoriteItem.Type itemType = item.getType();
            if (itemType == FavoriteItem.Type.ROUTE) {
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

                Provider routeProvider = route.getProvider();
                if (!Provider.SEOUL.equals(routeProvider)) {
                    if (RouteType.checkIncheonRoute(route.getType())) {
                        mItemLabel.setText(Provider.INCHEON.getCityName(context));
                    } else {
                        mItemLabel.setText(routeProvider.getCityName(context));
                    }
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

            } else if (itemType == FavoriteItem.Type.STATION) {
                Station station = item.getData(Station.class);
                int color = Color.BLACK;

                if (station == null) return;
                String stationName = station.getName();
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
                    mItemTitle.setText(stationName);
                    mLinkItemLayout.setVisibility(View.GONE);
                } else {
                    String[] split = stationName.split("\\.");
                    mItemTitle.setText(split.length >= 2 ? split[0] + "." + split[1] : stationName);
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
                    FavoriteItem.Type type = mItem.getType();

                    if (type == FavoriteItem.Type.ROUTE) {
                        Route route = mItem.getData(Route.class);
                        RouteStation routeStation = mItem.getExtraData(RouteStation.class);

                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(RouteActivity.EXTRA_KEY_ROUTE, (Parcelable) route);
                        intent.putExtra(RouteActivity.EXTRA_KEY_REDIRECT_STATION_ID, routeStation != null ? routeStation.getLocalId() : null);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    } else if (type == FavoriteItem.Type.STATION) {
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

    public abstract class GroupViewHolder extends BaseViewHolder {
        public GroupViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.container);
        }

        public View mContainer;

        @Override
        public View getSwipeableContainerView() {
            return itemView;
        }

        public abstract View getDragHandleView();
    }

    public class SectionViewHolder extends GroupViewHolder implements View.OnClickListener, View.OnTouchListener {

        public TextView mTitle;
        public MorphButton mIndicator;
        public ImageButton mOverflowButton;
        public FavoriteGroup mItem;

        public SectionViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.section_title);
            mIndicator = (MorphButton) v.findViewById(R.id.indicator);
            mOverflowButton = (ImageButton) v.findViewById(R.id.overflow_button);
            mOverflowButton.setOnClickListener(this);
            mOverflowButton.setOnTouchListener(this);

            int color = ThemeUtils.getThemeColor(v.getContext(), android.R.attr.textColorSecondary);
            mOverflowButton.setColorFilter(color);
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
        public View getDragHandleView() {
            return mTitle;
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

    public class FooterViewHolder extends GroupViewHolder {

        public TextView mFooterText;

        public FooterViewHolder(View v) {
            super(v);
            mFooterText = (TextView) v.findViewById(R.id.footer_text);
        }

        @Override
        public View getDragHandleView() {
            return null;
        }
    }

    public class AdViewHolder extends GroupViewHolder implements CaulyNativeAdViewListener {

        private String mTag;
        private CaulyNativeAdView mAdView;

        public AdViewHolder(View v) {
            super(v);
            mContainer.setVisibility(View.VISIBLE);
        }

        public void requestAd(String tag) {
            if (mAdView == null || mTag != tag) {
                mTag = tag;
                Context context = mContainer.getContext();
                CaulyAdUtil.requestAd(context, tag, this);
            }
        }

        @Override
        public void onReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, boolean b) {
            if (caulyNativeAdView != null) {
                mAdView = caulyNativeAdView;
                ViewUtils.runOnUiThread(() -> {
                    if (caulyNativeAdView.getParent() != null) {
                        ((FrameLayout) caulyNativeAdView.getParent()).removeView(caulyNativeAdView);
                    }
                    mAdView.attachToView((ViewGroup) mContainer);
                    mContainer.requestLayout();
                });
            } else {
                onFailedToReceiveNativeAd(null, -1, null);
            }
        }

        @Override
        public void onFailedToReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, int i, String s) {
            CaulyAdUtil.removeAd(mTag);
            mAdPosition = -1;
            mAdView = null;
            mTag = null;
            ViewUtils.runOnUiThread(FavoriteAdapter.this::notifyDataSetChanged, 100);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

        @Override
        public View getDragHandleView() {
            return mContainer;
        }

    }

    public interface EventListener {
        void onGroupItemMoved(int fromGroupPosition, int toGroupPosition);

        void onChildItemMoved(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

        void onGroupItemRemoved(int groupPosition);

        void onChildItemRemoved(int groupPosition, int childPosition);
    }

}
