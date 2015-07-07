package kr.rokoroku.mbus.adapter;

/**
 * Created by rok on 2015. 5. 29..
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
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

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.StationActivity;
import kr.rokoroku.mbus.core.ApiCaller;
import kr.rokoroku.mbus.model.ArrivalInfo;
import kr.rokoroku.mbus.model.BusLocation;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteStation;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;
import kr.rokoroku.mbus.model.StationRoute;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.widget.QuickAction;
import kr.rokoroku.mbus.widget.SplitCardView;
import kr.rokoroku.widget.ConnectorView;


public class RouteAdapter extends AbstractExpandableItemAdapter<RouteAdapter.BaseViewHolder, RouteAdapter.BusArrivalViewHolder> {

    private static final int ITEM_BUS = 0;
    private static final int ITEM_STATION = 1;
    private static final int ITEM_HEADER = 3;
    private static final int ITEM_FOOTER = 4;
    private static final String TAG = "RouteAdapter";

    private final RouteDataProvider mDataProvider;
    private int mExpandedPosition;
    private WeakReference<RouteAdapter.BusArrivalViewHolder> mBusArrivalViewHolderReference;

    private Timer mTimer;
    private Set<WeakReference<BusArrivalItemViewHolder>> mArrivalViewReferenceSet;

    public RouteAdapter(RouteDataProvider dataProvider) {
        mDataProvider = dataProvider;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
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
        if (groupPosition >= 0 && groupPosition < mDataProvider.getCount()) {
            return mDataProvider.isExpandable(groupPosition) ? 1 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        RouteDataProvider.RouteListItemData item = getItem(groupPosition);
        if (item != null) {
            return item.getId();
        } else {
            return 0;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        groupPosition--;
        if (groupPosition >= 0 && groupPosition < mDataProvider.getCount()) {
            return mDataProvider.getItem(groupPosition).getId() * 10 + childPosition;
        } else {
            return 0;
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
                if (itemData.getType().equals(RouteDataProvider.RouteListItemData.Type.BUS)) return ITEM_BUS;
                else return ITEM_STATION;
            }
        } else {
            return mDataProvider.getItem(groupPosition).getType()
                    .equals(RouteDataProvider.RouteListItemData.Type.STATION) ? ITEM_STATION : ITEM_BUS;
        }
    }

    public int getExpandedPosition() {
        return mExpandedPosition;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public BaseViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return createInheritViewHolder(parent, viewType);
    }

    @Override
    public BusArrivalViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.row_route_station_more, parent, false);
        return new BusArrivalViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(BaseViewHolder holder, int groupPosition, int viewType) {

        if (viewType == ITEM_HEADER) {
            Route route = mDataProvider.getRoute();
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.setItem(route);
            return;

        } else if (viewType == ITEM_FOOTER) {
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

            if (mExpandedPosition == groupPosition && routeStation.isBusStop()) {
                stationViewHolder.mNavigateButton.setVisibility(View.VISIBLE);
                stationViewHolder.mPaintButton.setVisibility(View.VISIBLE);
                stationViewHolder.mContainer.setRoundBottom(false);
            } else {
                stationViewHolder.mNavigateButton.setVisibility(View.GONE);
                stationViewHolder.mPaintButton.setVisibility(View.GONE);
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
        mExpandedPosition = groupPosition;
        return super.onHookGroupExpand(groupPosition, fromUser);
    }

    @Override
    public boolean onHookGroupCollapse(int groupPosition, boolean fromUser) {
        if (mExpandedPosition == groupPosition) mExpandedPosition = Integer.MIN_VALUE;
        return super.onHookGroupCollapse(groupPosition, fromUser);
    }

    @Override
    public void onBindChildViewHolder(BusArrivalViewHolder holder, int groupPosition, int childPosition, int viewType) {

        mExpandedPosition = groupPosition;
        mBusArrivalViewHolderReference = new WeakReference<BusArrivalViewHolder>(holder);

        holder.clear();
        Context context = holder.itemView.getContext();
        RouteStation routeStation = getItem(groupPosition).getRouteStation();

        FavoriteFacade.Color favoriteStationColor = FavoriteFacade.Color.WHITE;
        if (routeStation != null) {
            favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(
                    routeStation.getProvider(), routeStation.getId());
            holder.mStationId = routeStation.getId();
        }
        holder.mContainer.setCardBackgroundColor(ThemeUtils.dimColor(favoriteStationColor.getColor(context), 0.95f));

        if (routeStation != null &&
                (routeStation.getArrivalInfo() == null || TimeUtils.checkShouldUpdate(routeStation.getLastUpdateTime()))) {
            ApiCaller.getInstance().fillArrivalInfoData(routeStation, new StationRoute(mDataProvider.getRoute()), new ApiCaller.SimpleProgressCallback() {
                @Override
                public void onComplete(boolean success) {
                    if (success) {
                        holder.setItem(routeStation.getArrivalInfo());
                    }
                }

                @Override
                public void onError(int progress, Throwable t) {
                    holder.setItem(null);
                    Toast.makeText(holder.itemView.getContext(), t.getCause().getMessage(), Toast.LENGTH_LONG).show();
                    holder.mStationId = routeStation.getId();
                }
            });
        } else {
            holder.setItem(routeStation != null ? routeStation.getArrivalInfo() : null);
        }
        if (groupPosition == mDataProvider.getCount()) {
            holder.mConnector.setVisibility(View.INVISIBLE);
        } else {
            holder.mConnector.setVisibility(View.VISIBLE);
        }
        RouteDataProvider.RouteListItemData dataAfter = getItem(groupPosition + 1);
        holder.mContainer.setRoundBottom(dataAfter == null || RouteDataProvider.RouteListItemData.Type.BUS.equals(dataAfter.getType()));

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
                return new BaseViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
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
            if (route.getCompanyTel() != null) {
                companyName = companyName + " " + route.getCompanyTel();
            }

            String allocation = route.getAllocNormal() + "분";
            if (route.getAllocWeekend() != null) {
                allocation = context.getString(R.string.weekday) + allocation
                        + " / " + context.getString(R.string.weekend) + " " + route.getAllocWeekend() + "분";
            }

            String operationTime = route.getFirstUpTime() + " ~ " + route.getLastUpTime();

            mOperationTime.setText(operationTime);
            mOperatingCompany.setText(companyName);
            mAllocationInterval.setText(allocation);
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
            mBusTitle.setText(busLocation.getPlateNumber());
            int remainSeat = busLocation.getRemainSeat();
            if (remainSeat >= 0) {
                mBusDescription.setVisibility(View.VISIBLE);
                mBusDescription.setText("남은 좌석 : " + remainSeat + "석");
            } else {
                mBusDescription.setVisibility(View.GONE);
            }

            RouteType type = mDataProvider.getRoute().getType();
            if (type != null) {
                mBusIcon.setColorFilter(type.getColor(itemView.getContext()));
            }
            itemView.setClickable(false);
        }
    }

    public class StationViewHolder extends BaseViewHolder implements View.OnTouchListener, View.OnClickListener {

        public RouteStation mItem;
        public TextView mStationTitle;
        public TextView mStationId;
        public ImageButton mPaintButton;
        public ImageButton mNavigateButton;

        public StationViewHolder(View v) {
            super(v);
            mStationTitle = (TextView) v.findViewById(R.id.station_title);
            mStationId = (TextView) v.findViewById(R.id.station_id);
            mPaintButton = (ImageButton) v.findViewById(R.id.paint_button);
            mNavigateButton = (ImageButton) v.findViewById(R.id.navigate_button);
            mPaintButton.setOnTouchListener(this);
            mPaintButton.setOnClickListener(this);
            mNavigateButton.setOnTouchListener(this);
            mNavigateButton.setOnClickListener(this);
        }

        public void setItem(RouteStation routeStation) {
            mItem = routeStation;
            mStationTitle.setText(routeStation.getName());
            mStationId.setText(routeStation.getLocalId());
            itemView.setClickable(true);

            Context context = itemView.getContext();
            FavoriteFacade.Color favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(
                    routeStation.getProvider(), routeStation.getId());
            mContainer.setCardBackgroundColor(favoriteStationColor.getColor(context));

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
            if (v.equals(mPaintButton)) {
                showPalette(v, mItem);

            } else if (v.equals(mNavigateButton)) {
                // Navigate to StationActivity
                Station station = mItem;
                Intent intent = new Intent(context, StationActivity.class);
                intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) station);
                intent.putExtra(StationActivity.EXTRA_KEY_REDIRECT_ROUTE_ID, mDataProvider.getRoute().getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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

            if(mBusArrivalViewHolderReference != null) {
                BusArrivalViewHolder busArrivalViewHolder = mBusArrivalViewHolderReference.get();
                if (busArrivalViewHolder != null && station.getId().equals(busArrivalViewHolder.mStationId)) {
                    busArrivalViewHolder.mContainer.postDelayed(() ->
                            busArrivalViewHolder.mContainer.animateCardBackgroundColor(
                                    ThemeUtils.dimColor(color.getColor(context), 0.95f),
                                    RevealUtils.Position.TOP_CENTER), 60);
                }
                FavoriteFacade favoriteFacade = FavoriteFacade.getInstance();
                favoriteFacade.setFavoriteStationColor(station.getProvider(), station.getId(), color);
            }
        }
    }


    public class BusArrivalViewHolder extends BaseViewHolder {

        public SplitCardView mCardView;
        public BusArrivalItemViewHolder mBusArrivalItem1;
        public BusArrivalItemViewHolder mBusArrivalItem2;
        public ProgressBar mProgressBar;
        public ViewGroup mBusArrivalLayout;
        public ArrivalInfo mItem;
        public String mStationId;

        public BusArrivalViewHolder(View v) {
            super(v);
            mCardView = (SplitCardView) v.findViewById(R.id.card_view);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
            mBusArrivalLayout = (ViewGroup) v.findViewById(R.id.bus_arrival_layout);
            mBusArrivalItem1 = new BusArrivalItemViewHolder(v.findViewById(R.id.bus_arrival_item_1));
            mBusArrivalItem2 = new BusArrivalItemViewHolder(v.findViewById(R.id.bus_arrival_item_2));
            mCardView.setRoundTop(false);
            mCardView.setRoundBottom(false);
            mCardView.post(() -> {
                mCardView.getLayoutParams().height = mBusArrivalLayout.getHeight();
                mCardView.requestLayout();
            });
            mProgressBar.setVisibility(View.VISIBLE);
            mBusArrivalLayout.setVisibility(View.INVISIBLE);
        }

        public void setItem(ArrivalInfo arrivalInfo) {
            Context context = mContainer.getContext();
            mProgressBar.setVisibility(View.GONE);

            FavoriteFacade.Color favoriteStationColor = FavoriteFacade.Color.WHITE;
            if (mStationId != null) {
                favoriteStationColor = FavoriteFacade.getInstance().getFavoriteStationColor(
                        mDataProvider.getRoute().getProvider(), mStationId);
            }
            mContainer.setCardBackgroundColor(ThemeUtils.dimColor(favoriteStationColor.getColor(context), 0.95f));

            if(mItem != null && !mItem.equals(arrivalInfo)) {
                mBusArrivalLayout.setAlpha(0f);
                mBusArrivalLayout.animate().alpha(1f).start();
            }
            mBusArrivalLayout.setVisibility(View.VISIBLE);
            mItem = arrivalInfo;

            if (arrivalInfo != null) {
                mStationId = arrivalInfo.getStationId();
                mBusArrivalItem1.setItem(arrivalInfo.getBusArrivalItem1());
                mBusArrivalItem2.setItem(arrivalInfo.getBusArrivalItem2());
                if (mTimer == null) {
                    mTimer = new Timer(true);
                    mTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            mBusArrivalItem1.countdown();
                            mBusArrivalItem2.countdown();
                        }
                    }, 0, 1000);
                }
            } else {
                mStationId = null;
                mBusArrivalItem1.setItem(null);
                mBusArrivalItem2.setItem(null);
            }
        }

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
                if (mBusIcon != null) mBusIcon.setVisibility(View.VISIBLE);
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
                    mBusDescription.setText(mItemView.getContext().getString(R.string.information_unavailable));
                }
            }
        }

        public void countdown() {
            if (mTimer == null) {
                initTimer();
            }

            mItemView.post(() -> {
                if (mItem != null) {
                    long timeDiff = mItem.getPredictTime().getTime() - System.currentTimeMillis();
                    if (timeDiff >= 0) {

                        String timeString = DateUtils.formatElapsedTime(timeDiff / 1000);

                        mRemainTime.setText(timeString);
                        if (mRemainTime.getVisibility() == View.GONE) {
                            mRemainTime.setVisibility(View.VISIBLE);
                        }

                        if (mItem.getBehind() > 0) {
                            String remainString = mItem.getBehind() + " 정류장 전";
                            mRemainStation.setText(remainString);
                        } else {
                            mRemainStation.setText("");
                        }

                        if (mBusTitle != null) {
                            mBusTitle.setVisibility(View.VISIBLE);
                            mBusTitle.setText(mItem.getPlateNumber());
                        }

                        if (mBusIcon != null) {
                            mBusIcon.setVisibility(View.VISIBLE);
                        }

                        if (mBusDescription != null) {
                            if (mItem.getRemainSeat() >= 0) {
                                String remainSeatString;
                                if (mItem.getRemainSeat() == 0) remainSeatString = "빈 자리 없음";
                                else remainSeatString = "남은 좌석 : " + mItem.getRemainSeat();
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
                        String passString = "버스가 도착했거나 지나갔습니다.";
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
        }, 0, 1000);
    }

}
