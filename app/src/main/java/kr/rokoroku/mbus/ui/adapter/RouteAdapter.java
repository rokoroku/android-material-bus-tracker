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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsn.cauly.CaulyNativeAdView;
import com.fsn.cauly.CaulyNativeAdViewListener;
import com.google.android.gms.ads.AdView;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import kr.rokoroku.mbus.BaseApplication;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.StationActivity;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.data.RouteDataProvider;
import kr.rokoroku.mbus.data.model.ArrivalInfo;
import kr.rokoroku.mbus.data.model.BusLocation;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.RouteType;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.CaulyAdUtil;
import kr.rokoroku.mbus.util.SimpleProgressCallback;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.ui.widget.QuickAction;
import kr.rokoroku.mbus.ui.widget.SplitCardView;
import kr.rokoroku.widget.ConnectorView;


public class RouteAdapter extends AbstractExpandableItemAdapter<RouteAdapter.BaseViewHolder, RouteAdapter.BaseChildViewHolder> {

    private static final int ITEM_BUS = 0;
    private static final int ITEM_STATION = 1;
    private static final int ITEM_HEADER = 3;
    private static final int ITEM_FOOTER = 4;
    private static final int ITEM_CHILD_STATION = 10;
    private static final int ITEM_CHILD_AD = 11;

    private static final String TAG = "RouteAdapter";

    private final RouteDataProvider mDataProvider;
    private long mExpandedGroupId = -1;
    private RecyclerViewExpandableItemManager mExpandableItemManager;
    private WeakReference<RouteAdapter.BusArrivalViewHolder> mBusArrivalViewHolderReference;
    private WeakReference<RouteAdapter.BusArrivalViewHolder> mBusArrivalViewHolderReference2;
    private WeakReference<RouteAdapter.AdViewHolder> mAdViewHolderReference;
    private WeakReference<RouteStation> mRouteStationReference;
    private Map<String, WeakReference<ArrivalInfo>> mArrivalInfoCache;
    private final Set<String> mReloadingArrivalInfoSet = new HashSet<>();
    boolean isFavoriteColorEnabled = BaseApplication.getSharedPreferences().getBoolean(BaseApplication.PREFERENCE_FAVORITE_COLOR, false);

    private Timer mTimer;
    private Set<WeakReference<BusArrivalItemViewHolder>> mArrivalViewReferenceSet;

    private OnItemInteractionListener mItemInteractionListener;
    private String mAdTag;

    public RouteAdapter(RouteDataProvider dataProvider) {
        mDataProvider = dataProvider;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public void setExpandableItemManager(RecyclerViewExpandableItemManager mExpandableItemManager) {
        this.mExpandableItemManager = mExpandableItemManager;
    }

    public void clearCache() {
        synchronized (mReloadingArrivalInfoSet) {
            mArrivalInfoCache = null;
            mReloadingArrivalInfoSet.clear();
        }
    }

    public void setAdTag(String adTag) {
        if (mAdTag != adTag) {
            this.mAdTag = adTag;
        }
    }

    @Override
    public int getGroupCount() {
        int count = mDataProvider.getCount();
        if (mDataProvider.isRouteInfoAvailable()) count += 2;
        return count;
    }

    @Override
    public int getChildCount(int groupPosition) {
        groupPosition--;
        int count = 0;
        if (groupPosition >= 0 && groupPosition < mDataProvider.getCount()) {
            RouteDataProvider.RouteListItemData item = mDataProvider.getItem(groupPosition);
            RouteStation routeStation = item.getRouteStation();

            if(routeStation != null && routeStation.isBusStop()) {
                ArrivalInfo arrivalInfo = getArrivalInfoCache(routeStation.getLocalId());
                if(arrivalInfo != null && arrivalInfo.getBusArrivalItem2() != null) {
                    count = 2;
                } else {
                    count = 1;
                }
                if (mAdTag != null) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public long getGroupId(int groupPosition) {
        RouteDataProvider.RouteListItemData item = getItem(groupPosition);
        if (item != null) {
            return item.getId();
        } else {
            return -1;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        groupPosition--;
        if (groupPosition >= 0 && groupPosition < mDataProvider.getCount()) {
            return mDataProvider.getItem(groupPosition).getId() * 10 + childPosition;
        } else {
            return -1;
        }
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        if (mDataProvider.isRouteInfoAvailable()) {
            if (groupPosition == 0) {
                return ITEM_HEADER;
            } else if (groupPosition == getGroupCount() - 1) {
                return ITEM_FOOTER;
            } else {
                RouteDataProvider.RouteListItemData itemData = mDataProvider.getItem(groupPosition - 1);
                if (itemData.getType().equals(RouteDataProvider.RouteListItemData.Type.BUS)) {
                    return ITEM_BUS;
                } else {
                    return ITEM_STATION;
                }
            }
        } else {
            return mDataProvider.getItem(groupPosition).getType()
                    .equals(RouteDataProvider.RouteListItemData.Type.STATION) ? ITEM_STATION : ITEM_BUS;
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
    public int getChildItemViewType(int groupPosition, int childPosition) {
        int childCount = getChildCount(groupPosition);
        if (mAdTag != null && childCount - 1 == childPosition) {
            return ITEM_CHILD_AD;
        }
        return ITEM_CHILD_STATION;
    }

    @Override
    public BaseViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return createInheritViewHolder(parent, viewType);
    }

    @Override
    public BaseChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_CHILD_STATION:
                return new BusArrivalViewHolder(inflater.inflate(R.layout.row_route_station_more, parent, false));

            case ITEM_CHILD_AD:
                return new AdViewHolder(inflater.inflate(R.layout.row_route_station_more_ad, parent, false));
        }
        return null;
    }

    @Override
    public void onBindGroupViewHolder(BaseViewHolder holder, int groupPosition, int viewType) {

        if (viewType == ITEM_HEADER) {
            Route route = mDataProvider.getRoute();
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.setItem(route);
            return;

        } else if (viewType == ITEM_FOOTER) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.setItem(mDataProvider.getRoute().getProvider());
            return;
        }

        int itemPosition = groupPosition;
        if (mDataProvider.isRouteInfoAvailable()) itemPosition--;

        if (holder.mConnector != null) {
            if (itemPosition == 0) {
                holder.mConnector.setConnectorType(ConnectorView.ConnectorType.START);
            } else if (itemPosition == mDataProvider.getCount() - 1) {
                holder.mConnector.setConnectorType(ConnectorView.ConnectorType.END);
            } else {
                holder.mConnector.setConnectorType(ConnectorView.ConnectorType.NODE);
            }
        }

        Context context = holder.itemView.getContext();
        RouteDataProvider.RouteListItemData routeListItemData = mDataProvider.getItem(itemPosition);
        if (routeListItemData.getType() == RouteDataProvider.RouteListItemData.Type.STATION) {
            RouteStation routeStation = routeListItemData.getRouteStation();
            StationViewHolder stationViewHolder = (StationViewHolder) holder;
            stationViewHolder.setItem(routeStation);

            FavoriteFacade.Color favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(routeStation.getProvider(), routeStation.getId());
            stationViewHolder.mContainer.setCardBackgroundColor(favoriteStationColor.getColor(context));
            RouteDataProvider.RouteListItemData dataBefore = mDataProvider.getItem(itemPosition - 1);
            stationViewHolder.mContainer.setRoundTop(dataBefore == null || RouteDataProvider.RouteListItemData.Type.BUS.equals(dataBefore.getType()));

            if (getExpandedPosition() == groupPosition && routeStation.isBusStop()) {
                stationViewHolder.mNavigateButton.setVisibility(View.VISIBLE);
                stationViewHolder.mFavoriteButton.setVisibility(View.VISIBLE);
                if (isFavoriteColorEnabled) {
                    stationViewHolder.mPalleteButton.setVisibility(View.VISIBLE);
                }
                stationViewHolder.mContainer.setRoundBottom(false);
            } else {
                stationViewHolder.mNavigateButton.setVisibility(View.GONE);
                stationViewHolder.mFavoriteButton.setVisibility(View.GONE);
                stationViewHolder.mPalleteButton.setVisibility(View.GONE);
                RouteDataProvider.RouteListItemData dataAfter = mDataProvider.getItem(itemPosition + 1);
                stationViewHolder.mContainer.setRoundBottom(dataAfter == null || RouteDataProvider.RouteListItemData.Type.BUS.equals(dataAfter.getType()));
            }


        } else if (routeListItemData.getType() == RouteDataProvider.RouteListItemData.Type.BUS) {
            BusViewHolder busViewHolder = (BusViewHolder) holder;
            busViewHolder.setItem(routeListItemData.getBusLocation());
        }
    }

    @Override
    public boolean onHookGroupExpand(int groupPosition, boolean fromUser) {

        int expandedPosition = getExpandedPosition();
        if (expandedPosition > 0) {
            mExpandableItemManager.collapseGroup(expandedPosition);
        }

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
    public void onBindChildViewHolder(BaseChildViewHolder holder, int groupPosition, int childPosition, int viewType) {

        holder.clear();
        Context context = holder.itemView.getContext();

        if (holder.mConnector != null) {
            if (groupPosition == mDataProvider.getCount()) {
                holder.mConnector.setVisibility(View.INVISIBLE);
            } else {
                holder.mConnector.setVisibility(View.VISIBLE);
            }
        }

        mExpandedGroupId = getGroupId(groupPosition);
        RouteStation routeStation = getItem(groupPosition).getRouteStation();
        FavoriteFacade.Color favoriteStationColor = FavoriteFacade.Color.WHITE;
        if (routeStation != null) {
            String id = routeStation.getId();
            if (id != null) {
                favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(mDataProvider.getRoute().getProvider(), id);
            }
        }
        holder.mCardView.setCardBackgroundColor(ThemeUtils.dimColor(favoriteStationColor.getColor(context), 0.95f));


        RouteDataProvider.RouteListItemData dataAfter = getItem(groupPosition + 1);

        int childCount = getChildCount(groupPosition);
        if (childPosition == childCount - 1) {
            holder.mCardView.setRoundBottom(dataAfter == null || RouteDataProvider.RouteListItemData.Type.BUS.equals(dataAfter.getType()));
        } else if (childPosition < childCount - 1) {
            holder.mCardView.setRoundBottom(false);
        }

        if (holder instanceof AdViewHolder) {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            if (mAdTag != null) {
                adViewHolder.requestAd(mAdTag);
                mAdViewHolderReference = new WeakReference<>(adViewHolder);
            } else {
                mAdViewHolderReference = null;
            }

            if (routeStation != null) {
                adViewHolder.mStationId = routeStation.getId();
            }
        }

        else if (holder instanceof BusArrivalViewHolder) {
            BusArrivalViewHolder arrivalViewHolder = (BusArrivalViewHolder) holder;
            if (childPosition == 0) {
                mBusArrivalViewHolderReference = new WeakReference<>(arrivalViewHolder);
            } else {
                mBusArrivalViewHolderReference2 = new WeakReference<>(arrivalViewHolder);
            }

            if (routeStation != null) {
                arrivalViewHolder.setColorByStationId(routeStation.getId());

                final String localStationId = routeStation.getLocalId();
                final String routeId = routeStation.getRouteId();

                ArrivalInfo arrivalInfo = getArrivalInfoCache(localStationId);
                if (arrivalInfo == null) arrivalInfo = routeStation.getArrivalInfo();
                if (arrivalInfo == null || TimeUtils.checkShouldUpdate(arrivalInfo.getTimestamp())) {
                    StationRoute stationRoute = routeStation.getStationRoute(routeId);
                    if (stationRoute == null) {
                        stationRoute = new StationRoute(mDataProvider.getRoute(), localStationId);
                    }

                    if (childPosition == 0) synchronized (mReloadingArrivalInfoSet) {
                        if (!mReloadingArrivalInfoSet.contains(localStationId)) {
                            mReloadingArrivalInfoSet.add(localStationId);

                            final StationRoute finalStationRoute = stationRoute;
                            ApiFacade.getInstance().getArrivalInfo(routeStation, stationRoute, new SimpleProgressCallback<List<ArrivalInfo>>() {
                                @Override
                                public void onComplete(boolean success, List<ArrivalInfo> value) {
                                    ArrivalInfo resultArrivalInfo = finalStationRoute.getArrivalInfo();
                                    arrivalViewHolder.setItem(resultArrivalInfo, childPosition);
                                    arrivalViewHolder.setColorByStationId(routeStation.getId());
                                    if (resultArrivalInfo == null) {
                                        resultArrivalInfo = new ArrivalInfo(routeId, routeStation.getId());
                                    }
                                    routeStation.putArrivalInfo(resultArrivalInfo);
                                    arrivalViewHolder.setItem(resultArrivalInfo, childPosition);
                                    putArrivalInfoCache(finalStationRoute.getLocalStationId(), resultArrivalInfo);

                                    ViewUtils.runOnUiThread(() -> {
                                        if (childCount != getChildCount(groupPosition)) notifyDataSetChanged();
                                    }, 100);
                                    ViewUtils.runOnUiThread(() -> mReloadingArrivalInfoSet.remove(localStationId), 1000);
                                }

                                @Override
                                public void onError(int progress, Throwable t) {
                                    Toast.makeText(arrivalViewHolder.itemView.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                                    arrivalViewHolder.setItem(null, childPosition);
                                    arrivalViewHolder.setColorByStationId(routeStation.getId());
                                    ViewUtils.runOnUiThread(() -> {
                                        if (childCount != getChildCount(groupPosition)) notifyDataSetChanged();
                                    }, 100);
                                    ViewUtils.runOnUiThread(() -> mReloadingArrivalInfoSet.remove(localStationId), 1000);
                                }
                            });
                            return;
                        }
                    }
                    arrivalViewHolder.setItem(arrivalInfo, childPosition);

                } else {
                    arrivalViewHolder.setItem(arrivalInfo, childPosition);
                    arrivalViewHolder.setColorByStationId(arrivalInfo.getStationId());
                    if (childPosition == 0 && getArrivalInfoCache(localStationId) == null) {
                        putArrivalInfoCache(localStationId, arrivalInfo);
                        ViewUtils.runOnUiThread(() -> {
                            if (childCount != getChildCount(groupPosition)) notifyDataSetChanged();
                        }, 250);
                    }
                }
            }
        }
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

    public RouteDataProvider.RouteListItemData getItem(int groupPosition) {
        if (mDataProvider.isRouteInfoAvailable()) groupPosition--;
        return mDataProvider.getItem(groupPosition);
    }

    public BaseViewHolder createInheritViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_BUS:
                return new BusViewHolder(inflater.inflate(R.layout.row_route_bus, parent, false));
            case ITEM_STATION:
                return new StationViewHolder(inflater.inflate(R.layout.row_route_station, parent, false));
            case ITEM_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.row_route_header, parent, false));
            case ITEM_FOOTER:
                return new FooterViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
        }
        return null;
    }

    public class BaseViewHolder extends AbstractExpandableItemViewHolder {

        public SplitCardView mContainer;
        public ConnectorView mConnector;

        public BaseViewHolder(View v) {
            super(v);
            mContainer = (SplitCardView) v.findViewById(R.id.card_view);
            mConnector = (ConnectorView) v.findViewById(R.id.connector);
        }


    }

    public class HeaderViewHolder extends BaseViewHolder {
        public TextView mOperationTime;
        public TextView mAllocationInterval;
        public TextView mOperatingCompany;

        public HeaderViewHolder(View v) {
            super(v);
            mOperationTime = (TextView) v.findViewById(R.id.operation_time);
            mOperatingCompany = (TextView) v.findViewById(R.id.operating_company);
            mAllocationInterval = (TextView) v.findViewById(R.id.allocation_interval);
        }

        public void setItem(Route route) {
            Context context = itemView.getContext();
            String companyName = route.getCompanyName();
            if (!TextUtils.isEmpty(route.getCompanyTel())) {
                companyName = companyName + " " + route.getCompanyTel();
            }

            String allocation = route.getAllocNormal() + "분";
            if (!TextUtils.isEmpty(route.getAllocWeekend())) {
                allocation = context.getString(R.string.weekday) + " " + allocation.trim()
                        + " / " + context.getString(R.string.weekend) + " " + route.getAllocWeekend().trim() + "분";
            }

            String operationTime = route.getFirstUpTime();
            if (route.getLastUpTime() != null)
                operationTime = operationTime + " ~ " + route.getLastUpTime();

            mOperationTime.setText(operationTime);
            mOperatingCompany.setText(companyName);
            mAllocationInterval.setText(allocation);
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

    public class BusViewHolder extends BaseViewHolder {

        public ImageView mBusIcon;
        public TextView mBusTitle;
        public TextView mBusDescription;

        public BusViewHolder(View v) {
            super(v);
            mBusIcon = (ImageView) v.findViewById(R.id.bus_icon);
            mBusTitle = (TextView) v.findViewById(R.id.bus_title);
            mBusDescription = (TextView) v.findViewById(R.id.bus_description);
        }

        public void setItem(BusLocation busLocation) {
            Context context = itemView.getContext();
            mBusTitle.setText(busLocation.getPlateNumber());

            int remainSeat = busLocation.getRemainSeat();
            if (remainSeat >= 0) {
                mBusDescription.setVisibility(View.VISIBLE);
                String remainSeatString;
                if (remainSeat == 0)
                    remainSeatString = context.getString(R.string.bus_arrival_no_remain_seat);
                else
                    remainSeatString = context.getString(R.string.bus_arrival_remain_seat, remainSeat);
                mBusDescription.setText(remainSeatString);
            } else {
                mBusDescription.setVisibility(View.GONE);
            }

            RouteType type = mDataProvider.getRoute().getType();
            if (type != null) {
                mBusIcon.setColorFilter(type.getColor(itemView.getContext()));
            }
            itemView.setClickable(false);

            Drawable drawable = mBusIcon.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                if (!animationDrawable.isRunning()) {
                    animationDrawable.start();
                }
            }
        }
    }

    public class StationViewHolder extends BaseViewHolder implements View.OnTouchListener, View.OnClickListener {

        public RouteStation mItem;
        public TextView mStationTitle;
        public TextView mStationDescription;
        public ImageButton mFavoriteButton;
        public ImageButton mNavigateButton;
        public ImageButton mPalleteButton;

        public StationViewHolder(View v) {
            super(v);
            mStationTitle = (TextView) v.findViewById(R.id.station_title);
            mStationDescription = (TextView) v.findViewById(R.id.station_description);
            mFavoriteButton = (ImageButton) v.findViewById(R.id.favorite_button);
            mNavigateButton = (ImageButton) v.findViewById(R.id.navigate_button);
            mPalleteButton = (ImageButton) v.findViewById(R.id.pallete_button);
            mFavoriteButton.setOnTouchListener(this);
            mFavoriteButton.setOnClickListener(this);
            mNavigateButton.setOnTouchListener(this);
            mNavigateButton.setOnClickListener(this);
            mPalleteButton.setOnTouchListener(this);
            mPalleteButton.setOnClickListener(this);

            if (isFavoriteColorEnabled) {
                mPalleteButton.setVisibility(View.VISIBLE);
            } else {
                mPalleteButton.setVisibility(View.GONE);
            }
        }

        public void setItem(RouteStation routeStation) {
            mItem = routeStation;
            mStationTitle.setText(routeStation.getName());
            String localId = routeStation.getLocalId();
            String description = routeStation.getCity();
            if (!TextUtils.isEmpty(localId)) {
                description = description + " - " + localId;
            }

            mStationDescription.setText(description);
            itemView.setClickable(true);

            Context context = itemView.getContext();
            FavoriteFacade.Color favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(
                    routeStation.getProvider(), routeStation.getId());
            mContainer.setCardBackgroundColor(favoriteStationColor.getColor(context));

            FavoriteItem favoriteItem = FavoriteFacade.getInstance().getItem(mDataProvider.getRoute(), routeStation);
            if (favoriteItem != null) {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_outline_24dp);
            }

            if (TextUtils.isEmpty(routeStation.getLocalId())) {
                mConnector.setIconType(ConnectorView.IconType.NONE);
            } else {
                mConnector.setIconType(ConnectorView.IconType.CIRCLE);
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

            } else if (v.equals(mNavigateButton)) {
                // Navigate to StationActivity
                Station station = mItem;
                Intent intent = new Intent(context, StationActivity.class);
                intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
                intent.putExtra(StationActivity.EXTRA_KEY_REDIRECT_ROUTE_ID, mDataProvider.getRoute().getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            } else if (v.equals(mPalleteButton)) {
                showPalette(v, mItem);
            }
        }

        public void showPalette(View view, Station station) {

            Context context = view.getContext();
            RelativeLayout contentView = (RelativeLayout) View.inflate(context, R.layout.popup_pallete, null);
            QuickAction quickAction = new QuickAction(context, R.style.Animation_AppCompat_DropDownUp, contentView, contentView);
            quickAction.show(view);

            contentView.findViewById(R.id.button1).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(1);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button2).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(2);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button3).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(3);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button4).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(4);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button5).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(5);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
            contentView.findViewById(R.id.button6).setOnClickListener(v1 -> {
                FavoriteFacade.Color color = FavoriteFacade.Color.valueOf(0);
                changeFavoriteColor(station, color);
                quickAction.dismiss();
            });
        }

        public void changeFavoriteColor(Station station, FavoriteFacade.Color color) {
            Context context = itemView.getContext();
            mContainer.animateCardBackgroundColor(color.getColor(context));

            int delay = 60;
            if (mBusArrivalViewHolderReference != null) {
                BusArrivalViewHolder busArrivalViewHolder = mBusArrivalViewHolderReference.get();
                if (busArrivalViewHolder != null && station.getId().equals(busArrivalViewHolder.mStationId)) {
                    busArrivalViewHolder.mContainer.postDelayed(() ->
                            busArrivalViewHolder.mContainer.animateCardBackgroundColor(
                                    ThemeUtils.dimColor(color.getColor(context), 0.95f),
                                    RevealUtils.Position.TOP_CENTER), delay);
                    delay += 60;
                }
            }

            if (mBusArrivalViewHolderReference2 != null) {
                BusArrivalViewHolder busArrivalViewHolder = mBusArrivalViewHolderReference2.get();
                if (busArrivalViewHolder != null && station.getId().equals(busArrivalViewHolder.mStationId)) {
                    busArrivalViewHolder.mContainer.postDelayed(() ->
                            busArrivalViewHolder.mContainer.animateCardBackgroundColor(
                                    ThemeUtils.dimColor(color.getColor(context), 0.95f),
                                    RevealUtils.Position.TOP_CENTER), delay);
                    delay += 60;
                }
            }

            if (mAdViewHolderReference != null) {
                AdViewHolder adViewHolder = mAdViewHolderReference.get();
                if (adViewHolder != null && station.getId().equals(adViewHolder.mStationId)) {
                    adViewHolder.mContainer.postDelayed(() ->
                            adViewHolder.mContainer.animateCardBackgroundColor(
                                    ThemeUtils.dimColor(color.getColor(context), 0.95f),
                                    RevealUtils.Position.TOP_CENTER), delay);
                }
            }

            FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
            favoriteFacade.setFavoriteStationColor(station.getProvider(), station.getId(), color);

        }
    }

    public class BaseChildViewHolder extends BaseViewHolder {

        public SplitCardView mCardView;

        public BaseChildViewHolder(View v) {
            super(v);
            mCardView = (SplitCardView) v.findViewById(R.id.card_view);
            mCardView.setRoundTop(false);
        }

        public void clear() {
            mContainer.setCardBackgroundColor(
                    ThemeUtils.dimColor(FavoriteFacade.Color.WHITE.getColor(itemView.getContext()), 0.95f));
        }
    }

    public class BusArrivalViewHolder extends BaseChildViewHolder {

        public BusArrivalItemViewHolder mBusArrivalItem;
        public TextView mBusArrivalTitle;
        public ProgressBar mProgressBar;
        public View mBusArrivalLayout;
        public View mBusOperationEndLayout;
        public ArrivalInfo mItem;
        public String mStationId;

        public BusArrivalViewHolder(View v) {
            super(v);
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
            mProgressBar.setVisibility(View.GONE);

            if (mItem != null && !mItem.equals(arrivalInfo)) {
                mBusArrivalLayout.setAlpha(0f);
                mBusArrivalLayout.animate().alpha(1f).start();
            }
            mItem = arrivalInfo;

            if (arrivalInfo != null) {
                if (arrivalInfo.getBusArrivalItem1() == null && arrivalInfo.isDriveEnd()) {
                    mBusOperationEndLayout.setVisibility(View.VISIBLE);
                    mBusArrivalLayout.setVisibility(View.INVISIBLE);
                } else {
                    mBusOperationEndLayout.setVisibility(View.INVISIBLE);
                    mBusArrivalLayout.setVisibility(View.VISIBLE);
                    if(index == 0) {
                        mBusArrivalTitle.setText(R.string.first_arriving_bus);
                        mBusArrivalItem.setItem(arrivalInfo.getBusArrivalItem1());
                    } else {
                        mBusArrivalTitle.setText(R.string.second_arriving_bus);
                        mBusArrivalItem.setItem(arrivalInfo.getBusArrivalItem2());
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
            } else {
                mBusArrivalLayout.setVisibility(View.VISIBLE);
                mBusOperationEndLayout.setVisibility(View.GONE);
                mBusArrivalItem.setItem(null);
            }
        }

        public void setColorByStationId(String stationId) {
            mStationId = stationId;
            Context context = mContainer.getContext();
            FavoriteFacade.Color favoriteStationColor = FavoriteFacade.Color.WHITE;
            if (mStationId != null) {
                favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(
                        mDataProvider.getRoute().getProvider(), mStationId);
            }
            mContainer.setCardBackgroundColor(ThemeUtils.dimColor(favoriteStationColor.getColor(context), 0.95f));
        }

        @Override
        public void clear() {
            mProgressBar.setVisibility(View.VISIBLE);
            mBusArrivalLayout.setVisibility(View.INVISIBLE);
            mContainer.setCardBackgroundColor(
                    ThemeUtils.dimColor(FavoriteFacade.Color.WHITE.getColor(itemView.getContext()), 0.95f));
        }

    }

    public class BusArrivalItemViewHolder {

        public View mItemView;
        public ImageView mBusIcon;
        public TextView mBusTitle;
        public TextView mBusDescription;
        public TextView mRemainTime;
        public TextView mRemainStation;
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

        public void setItem(ArrivalInfo.BusArrivalItem arrivalItem) {
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
                                if (mItem.getRemainSeat() == 0)
                                    remainSeatString = context.getString(R.string.bus_arrival_no_remain_seat);
                                else
                                    remainSeatString = context.getString(R.string.bus_arrival_remain_seat, mItem.getRemainSeat());
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

                    } else {
                        String passString = context.getString(R.string.bus_arrival_passed);
                        mRemainTime.setVisibility(View.GONE);
                        mRemainStation.setText(passString);
                        if (mBusIcon != null) mBusIcon.setVisibility(View.GONE);
                        if (mBusTitle != null) mBusTitle.setVisibility(View.GONE);
                        if (mBusDescription != null) mBusDescription.setText("");
                    }
                } else {
                    setItem(null);
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

    private synchronized void putArrivalInfoCache(String stationId, ArrivalInfo arrivalInfo) {
        if (mArrivalInfoCache == null) {
            mArrivalInfoCache = new HashMap<>();
        }
        if (!mArrivalInfoCache.containsKey(stationId)) {
            mArrivalInfoCache.put(stationId, new WeakReference<>(arrivalInfo));
        }
    }

    private ArrivalInfo getArrivalInfoCache(String stationId) {
        if (mArrivalInfoCache != null) {
            WeakReference<ArrivalInfo> reference = mArrivalInfoCache.get(stationId);
            if (reference != null) {
                ArrivalInfo arrivalInfo = reference.get();
                if (arrivalInfo != null) {
                    return arrivalInfo;
                } else synchronized (this) {
                    mArrivalInfoCache.remove(stationId);
                }
            }
        }
        return null;
    }

    public class AdViewHolder extends BaseChildViewHolder implements CaulyNativeAdViewListener {

        private String mTag;
        private String mStationId;
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
                    mCardView.post(() -> {
                        mCardView.getLayoutParams().height = mAdLayout.getHeight();
                        mCardView.requestLayout();
                    });
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
            ViewUtils.runOnUiThread(RouteAdapter.this::notifyDataSetChanged, 100);
        }
    }

    public OnItemInteractionListener getItemInteractionListener() {
        return mItemInteractionListener;
    }

    public void setItemInteractionListener(OnItemInteractionListener listener) {
        this.mItemInteractionListener = listener;
    }

    public interface OnItemInteractionListener {
        void onClickFavoriteButton(ImageButton button, RouteStation routeStation);
    }
}
