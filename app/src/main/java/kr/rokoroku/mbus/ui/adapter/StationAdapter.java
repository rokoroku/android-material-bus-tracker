package kr.rokoroku.mbus.ui.adapter;

/**
 * Created by rok on 2015. 5. 29..
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsn.cauly.CaulyNativeAdView;
import com.fsn.cauly.CaulyNativeAdViewListener;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.RouteActivity;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.StationDataProvider;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.CaulyAdUtil;
import kr.rokoroku.mbus.util.FormatUtils;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ViewUtils;
import kr.rokoroku.mbus.ui.widget.QuickAction;
import kr.rokoroku.mbus.ui.widget.SplitCardView;


public class StationAdapter extends AbstractExpandableItemAdapter<StationAdapter.BaseViewHolder, StationAdapter.BaseChildViewHolder> {

    private static final String TAG = "StationAdapter";

    private static final int ITEM_SECTION = 1;
    private static final int ITEM_BUS = 2;
    private static final int ITEM_FOOTER = 3;

    private static final int ITEM_CHILD_ROUTE = 10;
    private static final int ITEM_CHILD_AD = 11;

    private final StationDataProvider mDataProvider;
    private long mExpandedGroupId = -1;
    private WeakReference<BusArrivalViewHolder> mBusArrivalViewHolderReference;

    private Timer mTimer;
    private Set<WeakReference<BusArrivalItemViewHolder>> mArrivalViewReferenceSet;
    private final Set<String> mReloadingArrivalInfoSet = new HashSet<>();

    private OnItemInteractionListener mItemInteractionListener;
    private String mAdTag;

    public StationAdapter(StationDataProvider dataProvider) {
        mDataProvider = dataProvider;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public void clearCache() {
        mReloadingArrivalInfoSet.clear();
    }

    @Override
    public int getGroupCount() {
        int count = mDataProvider.getCount();
        if (count > 0) count++;     //for footer
        return count;
    }

    @Override
    public int getChildCount(int groupPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        int count = 0;
        if (item != null && item.getType().equals(StationDataProvider.StationListItemData.Type.ROUTE)) {
            StationRoute stationRoute = item.getStationRoute();
            ArrivalInfo arrivalInfo = stationRoute.getArrivalInfo();
            if(arrivalInfo != null && arrivalInfo.getBusArrivalItem2() != null) {
                count = 2;
            } else {
                count = 1;
            }
            if (mAdTag != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public long getGroupId(int groupPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        if (item != null) {
            return item.getId();
        } else {
            return -1;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        if (item != null) {
            return item.getId() * 10 + childPosition;
        } else {
            return -1;
        }
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);

        if (item == null) {
            return ITEM_FOOTER;

        } else {
            switch (item.getType()) {
                case ROUTE:
                    return ITEM_BUS;

                case SECTION:
                    return ITEM_SECTION;

                default:
                    return ITEM_SECTION;
            }
        }
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        int childCount = getChildCount(groupPosition);
        if (childCount > 0 && mAdTag != null && childCount - 1 == childPosition) {
            return ITEM_CHILD_AD;
        }
        return ITEM_CHILD_ROUTE;
    }

    @Override
    public BaseViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_SECTION:
                return new SectionViewHolder(inflater.inflate(R.layout.row_common_section, parent, false));
            case ITEM_BUS:
                return new RouteViewHolder(inflater.inflate(R.layout.row_station_route, parent, false));
            case ITEM_FOOTER:
                return new FooterViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
            default:
                return null;
        }
    }

    @Override
    public BaseChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_CHILD_ROUTE:
                return new BusArrivalViewHolder(inflater.inflate(R.layout.row_station_route_more, parent, false));
            case ITEM_CHILD_AD:
                return new AdViewHolder(inflater.inflate(R.layout.row_station_route_more_ad, parent, false));
        }
        return null;
    }

    @Override
    public void onBindGroupViewHolder(BaseViewHolder holder, int groupPosition, int viewType) {
        Context context = holder.itemView.getContext();
        StationDataProvider.StationListItemData listItemData = mDataProvider.getItem(groupPosition);
        if (holder instanceof SectionViewHolder) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.setItem(listItemData.getTitle(context));

        } else if (holder instanceof RouteViewHolder) {
            RouteViewHolder routeViewHolder = (RouteViewHolder) holder;
            StationRoute stationRoute = listItemData.getStationRoute();
            routeViewHolder.setItem(stationRoute);

            StationDataProvider.StationListItemData dataBefore = mDataProvider.getItem(groupPosition - 1);
            StationDataProvider.StationListItemData dataAfter = mDataProvider.getItem(groupPosition + 1);

            boolean roundTop = dataBefore == null || dataBefore.getType().equals(StationDataProvider.StationListItemData.Type.SECTION);
            boolean roundBottom = getExpandedPosition() != groupPosition && (dataAfter == null || dataAfter.getType().equals(StationDataProvider.StationListItemData.Type.SECTION));
            routeViewHolder.mCardView.setRoundTop(roundTop);
            routeViewHolder.mCardView.setRoundBottom(roundBottom);
            routeViewHolder.mSeparator.setVisibility(roundBottom ? View.GONE : View.VISIBLE);

            if (getExpandedPosition() == groupPosition && listItemData.getType().equals(StationDataProvider.StationListItemData.Type.ROUTE)) {
                routeViewHolder.mNavigateButton.setVisibility(View.VISIBLE);
                routeViewHolder.mFavoriteButton.setVisibility(View.VISIBLE);
                routeViewHolder.mRemainLayout.setVisibility(View.GONE);
                routeViewHolder.mSeparator.setVisibility(View.GONE);
                //routeViewHolder.mCardView.setRoundBottom(false);
            } else {
                routeViewHolder.mNavigateButton.setVisibility(View.GONE);
                routeViewHolder.mFavoriteButton.setVisibility(View.GONE);
                routeViewHolder.mRemainLayout.setVisibility(View.VISIBLE);
            }

            FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                    stationRoute.getProvider(), stationRoute.getRouteId());
            routeViewHolder.mCardView.setCardBackgroundColor(cardColor.getColor(context));

        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).setItem(mDataProvider.getProvider());
        }
    }

    @Override
    public void onBindChildViewHolder(BaseChildViewHolder holder, int groupPosition, int childPosition, int viewType) {

        Station station = mDataProvider.getStation();
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        Context context = holder.itemView.getContext();

        if (item == null) {
            if (holder instanceof BusArrivalViewHolder) {
                ((BusArrivalViewHolder)holder).clear();
                ((BusArrivalViewHolder)holder).mRouteId = null;
                ((BusArrivalViewHolder)holder).setItem(null, childPosition);
            }
            return;
        }

        StationRoute stationRoute = item.getStationRoute();
        final String routeId = stationRoute.getRouteId();
        final int childCount = getChildCount(groupPosition);

        FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                stationRoute.getProvider(), routeId);

        holder.mCardView.setCardBackgroundColor(ThemeUtils.dimColor(cardColor.getColor(context), 0.95f));
        holder.mCardView.setRoundTop(false);

        StationDataProvider.StationListItemData dataAfter = getItem(groupPosition + 1);

        if (childPosition == childCount - 1) {
            holder.mCardView.setRoundBottom(dataAfter == null || dataAfter.getType().equals(StationDataProvider.StationListItemData.Type.SECTION));
        } else if (childPosition < childCount - 1) {
            holder.mCardView.setRoundBottom(false);
        }


        if (holder instanceof AdViewHolder) {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            if (mAdTag != null) {
                adViewHolder.requestAd(mAdTag);
            }
        }

        else if (holder instanceof BusArrivalViewHolder) {
            BusArrivalViewHolder arrivalViewHolder = (BusArrivalViewHolder) holder;

            mExpandedGroupId = getGroupId(groupPosition);
            mBusArrivalViewHolderReference = new WeakReference<>(arrivalViewHolder);

            arrivalViewHolder.clear();
            arrivalViewHolder.mRouteId = routeId;

            if (stationRoute.getArrivalInfo() == null || TimeUtils.checkShouldUpdate(stationRoute.getArrivalInfo().getTimestamp())) {
                synchronized (mReloadingArrivalInfoSet) {
                    if (!mReloadingArrivalInfoSet.contains(routeId)) {
                        mReloadingArrivalInfoSet.add(routeId);
                        ApiFacade.getInstance().getArrivalInfo(station, stationRoute, new SimpleProgressCallback<List<ArrivalInfo>>() {
                            @Override
                            public void onComplete(boolean success, List<ArrivalInfo> value) {
                                ArrivalInfo resultArrivalInfo = stationRoute.getArrivalInfo();
                                if (resultArrivalInfo == null) {
                                    resultArrivalInfo = new ArrivalInfo(routeId, station.getId());
                                }
                                stationRoute.setArrivalInfo(resultArrivalInfo);
                                ViewUtils.runOnUiThread(StationAdapter.this::notifyDataSetChanged);
                                ViewUtils.runOnUiThread(() -> mReloadingArrivalInfoSet.remove(routeId), 1000);
                            }

                            @Override
                            public void onError(int progress, Throwable t) {
                                Toast.makeText(arrivalViewHolder.itemView.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                                arrivalViewHolder.setItem(null, childPosition);
                                ViewUtils.runOnUiThread(() -> mReloadingArrivalInfoSet.remove(routeId), 1000);
                            }
                        });
                        return;
                    }
                }
                arrivalViewHolder.setItem(stationRoute.getArrivalInfo(), childPosition);

            } else {
                arrivalViewHolder.setItem(stationRoute.getArrivalInfo(), childPosition);
            }
        }
    }

    public int getExpandedPosition() {
        if(mExpandedGroupId != -1) {
            for (int i = 0; i < getGroupCount(); i++) {
                if (getGroupId(i) == mExpandedGroupId) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onHookGroupExpand(int groupPosition, boolean fromUser) {
        mExpandedGroupId = getGroupId(groupPosition);
        return super.onHookGroupExpand(groupPosition, fromUser);
    }

    @Override
    public boolean onHookGroupCollapse(int groupPosition, boolean fromUser) {
        if(getExpandedPosition() == groupPosition) {
            mExpandedGroupId = -1;
        }
        return super.onHookGroupCollapse(groupPosition, fromUser);
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(BaseViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        final View containerView = holder.itemView;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return !ViewUtils.hitTest(containerView, x - offsetX, y - offsetY);
    }

    public StationDataProvider.StationListItemData getItem(int groupPosition) {
        return mDataProvider.getItem(groupPosition);
    }

    public void setAdTag(String adTag) {
        if (mAdTag != adTag) {
            this.mAdTag = adTag;
        }
    }

    public class BaseViewHolder extends AbstractExpandableItemViewHolder {
        public BaseViewHolder(View v) {
            super(v);
        }
    }

    public class RouteViewHolder extends BaseViewHolder implements View.OnTouchListener, View.OnClickListener {

        public SplitCardView mCardView;
        public TextView mRouteName;
        public TextView mRouteType;
        public TextView mRouteDestination;
        public ViewGroup mRemainLayout;
        public ImageButton mNavigateButton;
        public ImageButton mFavoriteButton;
        public BusArrivalItemViewHolder mBusArrivalItemViewHolder;
        public View mSeparator;
        private StationRoute mItem;

        public RouteViewHolder(View v) {
            super(v);
            mCardView = (SplitCardView) v.findViewById(R.id.card_view);
            mRouteName = (TextView) v.findViewById(R.id.route_name);
            mRouteType = (TextView) v.findViewById(R.id.route_type);
            mRouteDestination = (TextView) v.findViewById(R.id.route_destination);
            mRemainLayout = (ViewGroup) v.findViewById(R.id.remain_layout);
            mNavigateButton = (ImageButton) v.findViewById(R.id.navigate_button);
            mFavoriteButton = (ImageButton) v.findViewById(R.id.favorite_button);
            mBusArrivalItemViewHolder = new BusArrivalItemViewHolder(v);
            mSeparator = v.findViewById(R.id.separator);

            mFavoriteButton.setOnTouchListener(this);
            mFavoriteButton.setOnClickListener(this);
            mNavigateButton.setOnTouchListener(this);
            mNavigateButton.setOnClickListener(this);
        }

        public void setItem(StationRoute stationRoute) {
            Context context = itemView.getContext();

            mItem = stationRoute;
            itemView.setClickable(true);
            mRouteName.setText(stationRoute.getRouteName());
            String description = FormatUtils.formatHeadingTo(context, stationRoute);
            if (description != null) {
                mRouteDestination.setVisibility(View.VISIBLE);
                mRouteDestination.setText(description);
            } else {
                mRouteDestination.setVisibility(View.GONE);
            }
            RouteType type = stationRoute.getRouteType();
            if (type != null && !RouteType.UNKNOWN.equals(type)) {
                int color = type.getColor(context);
                mRouteName.setTextColor(color);
                mRouteType.setTextColor(color);

                Provider provider = stationRoute.getProvider();
                if (!mDataProvider.getProvider().equals(provider)) {
                    if (RouteType.checkIncheonRoute(type)) {
                        mRouteType.setText(Provider.INCHEON.getCityName(context));
                    } else {
                        mRouteType.setText(provider.getCityName(context));
                    }
                    mRouteType.setVisibility(View.VISIBLE);
                } else {
                    mRouteType.setVisibility(View.GONE);
                }

            } else {
                int color = ThemeUtils.getThemeColor(context, R.attr.cardPrimaryTextColor);
                mRouteName.setTextColor(color);
                mRouteType.setTextColor(color);
            }

            if (mDataProvider.checkFavoritedRoute(mItem)) {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_star_filled);
            } else {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_star);
            }

            ArrivalInfo arrivalInfo = stationRoute.getArrivalInfo();
            if (arrivalInfo != null) {
                if (arrivalInfo.getBusArrivalItem1() == null && arrivalInfo.isDriveEnd()) {
                    mBusArrivalItemViewHolder.setItem(arrivalInfo, null);
                    mBusArrivalItemViewHolder.mRemainStation.setVisibility(View.VISIBLE);
                    mBusArrivalItemViewHolder.mRemainStation.setText(R.string.bus_arrival_operation_end);
                } else {
                    mBusArrivalItemViewHolder.setItem(arrivalInfo, arrivalInfo.getBusArrivalItem1());
                }
            } else {
                mBusArrivalItemViewHolder.setItem(arrivalInfo, null);
            }
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

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            if (v.equals(mFavoriteButton)) {
                if (mItemInteractionListener != null && mItem != null) {
                    mItemInteractionListener.onClickFavoriteButton(mFavoriteButton, mItem);
                }
                //showPalette(v, mItem);

            } else if (v.equals(mNavigateButton)) {
                // Navigate to RouteActivity
                Route route = mItem.getRoute();
                if (route == null) {
                    route = new Route(mItem.getRouteId(), mItem.getRouteName(), mItem.getProvider());
                }
                Intent intent = new Intent(context, RouteActivity.class);
                intent.putExtra(RouteActivity.EXTRA_KEY_ROUTE, (Parcelable) route);

                Provider routeProvider = route.getProvider();
                Station station = mDataProvider.getStation();
                String redirectStationId = station.getLocalId();
                if (!routeProvider.equals(station.getProvider())) {
                    String externalProviderLocalId = station.getLocalIdByProvider(routeProvider);
                    if (externalProviderLocalId != null)
                        redirectStationId = externalProviderLocalId;
                }
                intent.putExtra(RouteActivity.EXTRA_KEY_REDIRECT_STATION_ID, redirectStationId);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        public void showPalette(View view, StationRoute route) {

            Context context = view.getContext();
            RelativeLayout contentView = (RelativeLayout) View.inflate(context, R.layout.popup_pallete, null);
            QuickAction quickAction = new QuickAction(context, R.style.Animation_AppCompat_DropDownUp, contentView, contentView);
            quickAction.show(view);

            contentView.findViewById(R.id.button1).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(1);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button2).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(2);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button3).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(3);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button4).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(4);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button5).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(5);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button6).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(0);
                changeFavoriteColor(route, color);
                quickAction.dismiss();
            });
        }

        public void changeFavoriteColor(StationRoute route, FavoriteFacade.Color color) {
            Context context = itemView.getContext();
            mCardView.animateCardBackgroundColor(color.getColor(context));

            if (mBusArrivalViewHolderReference != null) {
                final BusArrivalViewHolder busArrivalViewHolder = mBusArrivalViewHolderReference.get();
                if (busArrivalViewHolder != null && route.getRouteId().equals(busArrivalViewHolder.mRouteId)) {
                    busArrivalViewHolder.mCardView.postDelayed(() ->
                            busArrivalViewHolder.mCardView.animateCardBackgroundColor(
                                    ThemeUtils.dimColor(color.getColor(context), 0.95f),
                                    RevealUtils.Position.TOP_CENTER), 70);
                }
            }
            FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
            favoriteFacade.setFavoriteRouteColor(route.getProvider(), route.getRouteId(), color);
        }
    }

    public class SectionViewHolder extends BaseViewHolder {

        public TextView mTitle;
        public TextView mLabel;

        public SectionViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.section_title);
            mLabel = (TextView) v.findViewById(R.id.section_label);
        }

        public void setItem(String title) {
            mTitle.setText(title);
            mLabel.setVisibility(View.GONE);
        }

    }

    public class FooterViewHolder extends BaseViewHolder {
        public TextView mFooterText;

        public FooterViewHolder(View v) {
            super(v);
            mFooterText = (TextView) v.findViewById(R.id.footer_text);
        }

        public void setItem(Provider provider) {
            if (provider != null) {
                Context context = itemView.getContext();
                mFooterText.setText(context.getString(R.string.hint_powered_by, provider.getProviderText()));
            }
        }
    }

    public class BaseChildViewHolder extends BaseViewHolder {
        public SplitCardView mCardView;

        public BaseChildViewHolder(View v) {
            super(v);
            mCardView = (SplitCardView) v.findViewById(R.id.card_view);
        }
    }

    public class BusArrivalViewHolder extends BaseChildViewHolder {

        public BusArrivalItemViewHolder mBusArrivalItem;
        public TextView mBusArrivalTitle;
        public ProgressBar mProgressBar;
        public View mBusArrivalLayout;
        public View mBusOperationEndLayout;
        public ArrivalInfo mItem;
        public String mRouteId;

        public BusArrivalViewHolder(View v) {
            super(v);
            mCardView = (SplitCardView) v.findViewById(R.id.card_view);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
            mBusArrivalLayout = v.findViewById(R.id.bus_arrival_layout);
            mBusOperationEndLayout = v.findViewById(R.id.bus_operation_end_layout);
            mBusArrivalTitle = (TextView) v.findViewById(R.id.bus_arrival_title);
            mBusArrivalItem = new BusArrivalItemViewHolder(v.findViewById(R.id.bus_arrival_item));
            mCardView.setRoundTop(false);
            mCardView.setRoundBottom(false);
            mCardView.post(() -> {
                mCardView.getLayoutParams().height = mBusArrivalLayout.getHeight();
                mCardView.requestLayout();
            });
            mProgressBar.setVisibility(View.VISIBLE);
            mBusArrivalLayout.setVisibility(View.INVISIBLE);
        }

        public void setItem(ArrivalInfo arrivalInfo, int index) {
            Context context = itemView.getContext();

            mProgressBar.setVisibility(View.GONE);
            if (mItem != null && !mItem.equals(arrivalInfo)) {
                mBusArrivalLayout.animate().alpha(1f).start();
                mBusArrivalLayout.setAlpha(0f);
            }

            mItem = arrivalInfo;
            if (arrivalInfo != null) {
                mRouteId = arrivalInfo.getRouteId();
                if (arrivalInfo.getBusArrivalItem1() == null && arrivalInfo.isDriveEnd()) {
                    mBusArrivalLayout.setVisibility(View.INVISIBLE);
                    mBusOperationEndLayout.setVisibility(View.VISIBLE);
                } else {
                    mBusArrivalLayout.setVisibility(View.VISIBLE);
                    mBusOperationEndLayout.setVisibility(View.INVISIBLE);

                    if(index == 0) {
                        mBusArrivalTitle.setText(R.string.first_arriving_bus);
                        mBusArrivalItem.setItem(arrivalInfo, arrivalInfo.getBusArrivalItem1());
                    } else {
                        mBusArrivalTitle.setText(R.string.second_arriving_bus);
                        mBusArrivalItem.setItem(arrivalInfo, arrivalInfo.getBusArrivalItem2());
                    }
                    if (mTimer == null) {
                        mTimer = new Timer(true);
                        mTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                mBusArrivalItem.countdown();
                            }
                        }, 0, 1000);
                    }
                }
                StationRoute stationRoute = mDataProvider.getStation().getStationRoute(arrivalInfo.getRouteId());
                FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                        stationRoute.getProvider(), arrivalInfo.getRouteId());
                mCardView.setCardBackgroundColor(ThemeUtils.dimColor(cardColor.getColor(context), 0.95f));

            } else {
                mBusArrivalLayout.setVisibility(View.VISIBLE);
                mBusOperationEndLayout.setVisibility(View.GONE);

                mBusArrivalItem.setItem(arrivalInfo, null);
                FavoriteFacade.Color cardColor = FavoriteFacade.Color.WHITE;
                if (mRouteId != null) {
                    cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                            mDataProvider.getStation().getStationRoute(mRouteId).getProvider(), mRouteId);
                }
                mCardView.setCardBackgroundColor(ThemeUtils.dimColor(cardColor.getColor(context), 0.95f));
            }
        }

        public void clear() {
            mProgressBar.setVisibility(View.VISIBLE);
            mBusArrivalLayout.setVisibility(View.INVISIBLE);
        }

    }

    public class BusArrivalItemViewHolder {

        public View mItemView;
        public ImageView mBusIcon;
        public TextView mBusTitle;
        public TextView mBusDescription;
        public TextView mRemainTime;
        public TextView mRemainStation;
        private ArrivalInfo mArrivalInfo;
        private ArrivalInfo.BusArrivalItem mItem;

        public BusArrivalItemViewHolder(View v) {
            mItemView = v;
            mBusIcon = (ImageView) v.findViewById(R.id.bus_icon);
            mBusTitle = (TextView) v.findViewById(R.id.bus_title);
            mBusDescription = (TextView) v.findViewById(R.id.bus_description);
            mRemainTime = (TextView) v.findViewById(R.id.remain_time);
            mRemainStation = (TextView) v.findViewById(R.id.remain_station);

            if (mArrivalViewReferenceSet == null) mArrivalViewReferenceSet = new HashSet<>();
            mArrivalViewReferenceSet.add(new WeakReference<>(this));
        }

        public void setItem(ArrivalInfo arrivalInfo, ArrivalInfo.BusArrivalItem arrivalItem) {
            mArrivalInfo = arrivalInfo;
            mItem = arrivalItem;
            if (mItem != null) {
                if (mBusIcon != null) {
                    mBusIcon.setVisibility(View.VISIBLE);
                    Drawable drawable = mBusIcon.getDrawable();
                    if (drawable instanceof AnimationDrawable) {
                        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                        if (!animationDrawable.isRunning()) {
                            animationDrawable.start();
                        }
                    }
                }
                if (mBusTitle != null) mBusTitle.setVisibility(View.VISIBLE);
                if (mRemainTime != null) mRemainTime.setVisibility(View.VISIBLE);
                if (mRemainStation != null) mRemainStation.setVisibility(View.VISIBLE);
                countdown();

            } else {
                if (mBusIcon != null) mBusIcon.setVisibility(View.GONE);
                if (mBusTitle != null) mBusTitle.setVisibility(View.GONE);
                if (mRemainTime != null) mRemainTime.setVisibility(View.GONE);
                if (mRemainStation != null) mRemainStation.setVisibility(View.GONE);
                if (mBusDescription != null) {
                    mBusDescription.setVisibility(View.VISIBLE);
                    mBusDescription.setText(mItemView.getContext().getString(R.string.hint_information_unavailable));
                }
            }
        }

        public void countdown() {
            if (mTimer == null) {
                initTimer();
            }

            ViewUtils.runOnUiThread(() -> {
                Context context = mItemView.getContext();
                if (mItem != null) {
                    long timeDiff = mItem.getPredictTime().getTime() - System.currentTimeMillis();
                    if (timeDiff >= 0) {

                        String timeString = context.getString(R.string.bus_arrival_behind_time_in_minute,
                                (int) Math.round(timeDiff / 1000 / 60 + 0.5));

                        mRemainTime.setText(timeString);
                        if (mRemainTime.getVisibility() == View.GONE) {
                            mRemainTime.setVisibility(View.VISIBLE);
                        }

                        if (mItem.getBehind() > 0) {
                            String remainString = context.getString(R.string.bus_arrival_behind_stations, mItem.getBehind());
                            mRemainStation.setText(remainString);
                        } else {
                            mRemainStation.setText("");
                        }

                        if (mBusTitle != null) {
                            mBusTitle.setVisibility(View.VISIBLE);
                            String plateNumber = mItem.getPlateNumber();
                            if(TextUtils.isEmpty(plateNumber)) {
                                plateNumber = context.getString(R.string.unknown_vehicle_number);
                            }
                            mBusTitle.setText(plateNumber);
                        }

                        if (mBusIcon != null) {
                            mBusIcon.setVisibility(View.VISIBLE);
                        }

                        if (mBusDescription != null) {
                            if (mItem.getRemainSeat() >= 0) {
                                String remainSeatString;
                                if (mItem.getRemainSeat() == 0) remainSeatString = context.getString(R.string.bus_arrival_no_remain_seat);
                                else remainSeatString = context.getString(R.string.bus_arrival_remain_seat, mItem.getRemainSeat());
                                mBusDescription.setVisibility(View.VISIBLE);
                                mBusDescription.setText(remainSeatString);
                            } else {
                                mBusDescription.setVisibility(View.GONE);
                            }
                        }

                        if (timeDiff < 60000) {
                            mRemainTime.setTypeface(null, Typeface.BOLD);
                            mRemainTime.setTextColor(Color.RED);
                        } else {
                            mRemainTime.setTypeface(null, Typeface.NORMAL);
                            mRemainTime.setTextColor(Color.BLACK);
                        }

                    } else if (!mArrivalInfo.isDriveEnd()) {
                        String passString = context.getString(R.string.bus_arrival_passed);
                        mRemainTime.setVisibility(View.GONE);
                        mRemainStation.setText(passString);
                        if (mBusIcon != null) mBusIcon.setVisibility(View.GONE);
                        if (mBusTitle != null) mBusTitle.setVisibility(View.GONE);
                        if (mBusDescription != null) mBusDescription.setText("");
                    }
                } else {
                    setItem(mArrivalInfo, null);
                }
            });
        }
    }

    private void initTimer() {
        mTimer = new Timer(true);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Set<WeakReference<BusArrivalItemViewHolder>> removedReferences = null;
                for (WeakReference<BusArrivalItemViewHolder> reference : mArrivalViewReferenceSet) {
                    BusArrivalItemViewHolder busArrivalItemViewHolder = reference.get();
                    if (busArrivalItemViewHolder != null) {
                        busArrivalItemViewHolder.countdown();
                    } else {
                        if (removedReferences == null) removedReferences = new HashSet<>();
                        removedReferences.add(reference);
                    }
                }
                if (removedReferences != null) {
                    mArrivalViewReferenceSet.removeAll(removedReferences);
                }
            }
        }, 0, 10000);
    }

    public class AdViewHolder extends BaseChildViewHolder implements CaulyNativeAdViewListener {

        private String mTag;
        private ViewGroup mAdLayout;
        private CaulyNativeAdView mAdView;

        public AdViewHolder(View v) {
            super(v);
            mAdLayout = (ViewGroup) v.findViewById(R.id.ad_layout);
        }

        public void requestAd(String tag) {
            if (mAdView == null || !TextUtils.equals(mTag, tag) || (mAdView != null && mAdView.getParent() != mAdLayout) ) {
                mTag = tag;
                Context context = mCardView.getContext();
                CaulyAdUtil.requestAd2(context, tag, this);
            }
        }

        @Override
        public void onReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, boolean b) {
            if (caulyNativeAdView != null) {
                mAdView = caulyNativeAdView;
                ViewUtils.runOnUiThread(() -> {
                    if (mAdView.getParent() != null) {
                        ((ViewGroup) caulyNativeAdView.getParent()).removeView(mAdView);
                    }
                    mAdLayout.removeAllViewsInLayout();
                    mAdLayout.addView(mAdView);
                });
            } else {
                onFailedToReceiveNativeAd(null, -1, null);
            }
        }

        @Override
        public void onFailedToReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, int i, String s) {
            CaulyAdUtil.removeAd(mTag);
            mAdView = null;
            mTag = null;
            mAdTag = null;
            ViewUtils.runOnUiThread(StationAdapter.this::notifyDataSetChanged, 100);
        }
    }

    public OnItemInteractionListener getItemInteractionListener() {
        return mItemInteractionListener;
    }

    public void setItemInteractionListener(OnItemInteractionListener listener) {
        this.mItemInteractionListener = listener;
    }

    public interface OnItemInteractionListener {
        void onClickFavoriteButton(ImageButton button, StationRoute stationRoute);
    }
}
