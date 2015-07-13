package kr.rokoroku.mbus.adapter;

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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.rokoroku.mbus.RouteActivity;
import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.core.ApiFacade;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.model.ArrivalInfo;
import kr.rokoroku.mbus.model.Provider;
import kr.rokoroku.mbus.model.Route;
import kr.rokoroku.mbus.model.RouteType;
import kr.rokoroku.mbus.model.Station;
import kr.rokoroku.mbus.model.StationRoute;
import kr.rokoroku.mbus.util.FormatUtils;
import kr.rokoroku.mbus.util.TimeUtils;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;
import kr.rokoroku.mbus.widget.QuickAction;
import kr.rokoroku.mbus.widget.SplitCardView;


public class StationAdapter extends AbstractExpandableItemAdapter<StationAdapter.BaseViewHolder, StationAdapter.BusArrivalViewHolder> {

    private static final String TAG = "StationAdapter";

    private static final int ITEM_SECTION = 1;
    private static final int ITEM_BUS = 2;
    private static final int ITEM_FOOTER = 3;

    private final StationDataProvider mDataProvider;
    private int mExpandedPosition;
    private WeakReference<BusArrivalViewHolder> mBusArrivalViewHolderReference;

    private Timer mTimer;
    private Set<WeakReference<BusArrivalItemViewHolder>> mArrivalViewReferenceSet;

    public StationAdapter(StationDataProvider dataProvider) {
        mDataProvider = dataProvider;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
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
        if (item != null && item.getType().equals(StationDataProvider.StationListItemData.Type.ROUTE)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        if (item != null) {
            return item.getId();
        } else {
            return 0;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        if (item != null) {
            return item.getId() * 10 + childPosition;
        } else {
            return 0;
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
        return 0;
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
                return new BaseViewHolder(inflater.inflate(R.layout.row_footer_layout, parent, false));
            default:
                return null;
        }
    }

    @Override
    public BusArrivalViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BusArrivalViewHolder(inflater.inflate(R.layout.row_station_route_more, parent, false));
    }

    @Override
    public void onBindGroupViewHolder(BaseViewHolder holder, int groupPosition, int viewType) {
        StationDataProvider.StationListItemData listItemData = mDataProvider.getItem(groupPosition);
        if (holder instanceof SectionViewHolder) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.setItem(listItemData.getRouteType());

        } else if (holder instanceof RouteViewHolder) {
            RouteViewHolder routeViewHolder = (RouteViewHolder) holder;
            StationRoute stationRoute = listItemData.getStationRoute();
            routeViewHolder.setItem(stationRoute);

            StationDataProvider.StationListItemData dataBefore = mDataProvider.getItem(groupPosition - 1);
            StationDataProvider.StationListItemData dataAfter = mDataProvider.getItem(groupPosition + 1);

            boolean roundTop = dataBefore == null || dataBefore.getType().equals(StationDataProvider.StationListItemData.Type.SECTION);
            boolean roundBottom = mExpandedPosition != groupPosition && (dataAfter == null || dataAfter.getType().equals(StationDataProvider.StationListItemData.Type.SECTION));
            routeViewHolder.mCardView.setRoundTop(roundTop);
            routeViewHolder.mCardView.setRoundBottom(roundBottom);
            routeViewHolder.mSeparator.setVisibility(roundBottom ? View.GONE : View.VISIBLE);

            if (mExpandedPosition == groupPosition && listItemData.getType().equals(StationDataProvider.StationListItemData.Type.ROUTE)) {
                routeViewHolder.mNavigateButton.setVisibility(View.VISIBLE);
                routeViewHolder.mPaintButton.setVisibility(View.VISIBLE);
                routeViewHolder.mRemainLayout.setVisibility(View.GONE);
                routeViewHolder.mSeparator.setVisibility(View.GONE);
                //routeViewHolder.mCardView.setRoundBottom(false);
            } else {
                routeViewHolder.mNavigateButton.setVisibility(View.GONE);
                routeViewHolder.mPaintButton.setVisibility(View.GONE);
                routeViewHolder.mRemainLayout.setVisibility(View.VISIBLE);
            }

            Context context = holder.itemView.getContext();
            FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                    stationRoute.getProvider(), stationRoute.getRouteId());
            routeViewHolder.mCardView.setCardBackgroundColor(cardColor.getColor(context));

        } else {
            //footer?
        }
    }

    @Override
    public void onBindChildViewHolder(BusArrivalViewHolder holder, int groupPosition, int childPosition, int viewType) {

        mExpandedPosition = groupPosition;
        mBusArrivalViewHolderReference = new WeakReference<>(holder);

        holder.clear();
        Station station = mDataProvider.getStation();
        StationDataProvider.StationListItemData item = mDataProvider.getItem(groupPosition);
        if (item == null) {
            holder.mRouteId = null;
            holder.setItem(null);
            return;
        }
        StationRoute stationRoute = item.getStationRoute();
        holder.mRouteId = stationRoute.getRouteId();

        if (stationRoute.getArrivalInfo() == null || TimeUtils.checkShouldUpdate(stationRoute.getArrivalInfo().getTimestamp())) {
            ApiFacade.getInstance().fillArrivalInfoData(station, stationRoute, new ApiFacade.SimpleProgressCallback() {
                @Override
                public void onComplete(boolean success) {
                    if (success) holder.setItem(stationRoute.getArrivalInfo());
                }

                @Override
                public void onError(int progress, Throwable t) {
                    Toast.makeText(holder.itemView.getContext(), t.getCause().getMessage(), Toast.LENGTH_LONG).show();
                    holder.setItem(null);
                }
            });
        } else {
            holder.setItem(stationRoute.getArrivalInfo());
        }

        FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                stationRoute.getProvider(), stationRoute.getRouteId());
        holder.mCardView.setCardBackgroundColor(ThemeUtils.dimColor(cardColor.getColor(holder.itemView.getContext()), 0.95f));

        StationDataProvider.StationListItemData dataAfter = getItem(groupPosition + 1);
        boolean roundBottom = dataAfter == null || dataAfter.getType().equals(StationDataProvider.StationListItemData.Type.SECTION);
        holder.mCardView.setRoundBottom(roundBottom);
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
        public ImageButton mPaintButton;
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
            mPaintButton = (ImageButton) v.findViewById(R.id.paint_button);
            mBusArrivalItemViewHolder = new BusArrivalItemViewHolder(v);
            mSeparator = v.findViewById(R.id.separator);

            mPaintButton.setOnTouchListener(this);
            mPaintButton.setOnClickListener(this);
            mNavigateButton.setOnTouchListener(this);
            mNavigateButton.setOnClickListener(this);
        }

        public void setItem(StationRoute stationRoute) {
            Context context = itemView.getContext();

            mItem = stationRoute;
            itemView.setClickable(true);
            mRouteName.setText(stationRoute.getRouteName());
            Route route = stationRoute.getRoute();
            if (route != null && route.getTurnStationSeq() != -1) {
                String description = FormatUtils.formatHeadingTo(context, route, stationRoute);
                if(description != null) {
                    mRouteDestination.setVisibility(View.VISIBLE);
                    mRouteDestination.setText(description);
                } else {
                    mRouteDestination.setVisibility(View.GONE);
                }

            } else {
                mRouteDestination.setVisibility(View.GONE);
            }
            RouteType type = stationRoute.getRouteType();
            if (type != null && !RouteType.UNKNOWN.equals(type)) {
                int color = type.getColor(context);
                mRouteName.setTextColor(color);
                mRouteType.setTextColor(color);

                if (mDataProvider.hasLinkedStation() && !Provider.SEOUL.equals(stationRoute.getProvider())) {
                    mRouteType.setVisibility(View.VISIBLE);
                    mRouteType.setText(stationRoute.getProvider().getCityName(context));
                } else {
                    mRouteType.setVisibility(View.GONE);
                }

            } else {
                int color = ThemeUtils.getThemeColor(context, R.attr.cardPrimaryTextColor);
                mRouteName.setTextColor(color);
                mRouteType.setTextColor(color);
            }

            ArrivalInfo arrivalInfo = stationRoute.getArrivalInfo();
            if (arrivalInfo != null) {
                mBusArrivalItemViewHolder.setItem(arrivalInfo.getBusArrivalItem1());
            } else {
                mBusArrivalItemViewHolder.setItem(null);
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

        public void setItem(RouteType routeType) {
            Context context = itemView.getContext();
            mTitle.setText(routeType.getDescription(context));
            mLabel.setVisibility(View.GONE);
        }

    }

    public class BusArrivalViewHolder extends BaseViewHolder {

        public SplitCardView mCardView;
        public BusArrivalItemViewHolder mBusArrivalItem1;
        public BusArrivalItemViewHolder mBusArrivalItem2;
        public ProgressBar mProgressBar;
        public ViewGroup mBusArrivalLayout;
        public ArrivalInfo mItem;
        public String mRouteId;

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
            Context context = itemView.getContext();

            mProgressBar.setVisibility(View.GONE);
            if (mItem != null && !mItem.equals(arrivalInfo)) {
                mBusArrivalLayout.animate().alpha(1f).start();
                mBusArrivalLayout.setAlpha(0f);
            }
            mBusArrivalLayout.setVisibility(View.VISIBLE);

            mItem = arrivalInfo;
            if (arrivalInfo != null) {
                mRouteId = arrivalInfo.getRouteId();
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

                StationRoute stationRoute = mDataProvider.getStation().getStationRoute(arrivalInfo.getRouteId());
                FavoriteFacade.Color cardColor = FavoriteFacade.getInstance().getFavoriteRouteColor(
                        stationRoute.getProvider(), arrivalInfo.getRouteId());
                mCardView.setCardBackgroundColor(ThemeUtils.dimColor(cardColor.getColor(context), 0.95f));

            } else {
                mBusArrivalItem1.setItem(null);
                mBusArrivalItem2.setItem(null);
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
                    if(drawable instanceof AnimationDrawable) {
                        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                        if(!animationDrawable.isRunning()) {
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
