package kr.rokoroku.mbus.ui.adapter;

/**
 * Created by rok on 2015. 6. 2..
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import kr.rokoroku.mbus.R;
import kr.rokoroku.mbus.RouteActivity;
import kr.rokoroku.mbus.StationActivity;
import kr.rokoroku.mbus.data.SearchDataProvider;
import kr.rokoroku.mbus.data.model.Provider;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.util.FormatUtils;
import kr.rokoroku.mbus.util.ThemeUtils;
import kr.rokoroku.mbus.util.ViewUtils;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements OnClickListener {

    private static final String TAG = "SearchAdapter";

    private static final int ITEM_VIEW_TYPE_SECTION_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_SECTION_ITEM = 1;

    private SearchDataProvider mProvider;
    private OnChildMenuItemClickListener mOnChildMenuItemClickListener;

    public SearchAdapter(SearchDataProvider dataProvider) {
        mProvider = dataProvider;
        setHasStableIds(true);
    }

    public void setOnChildMenuItemClickListener(OnChildMenuItemClickListener onChildMenuItemClickListener) {
        this.mOnChildMenuItemClickListener = onChildMenuItemClickListener;
    }

    @Override
    public long getItemId(int position) {
        Object item = getItem(position);
        if (item != null) {
            if (item instanceof Route) {
                return ((Route) item).getId().hashCode();
            } else if (item instanceof Station) {
                return ((Station) item).getId().hashCode();
            } else if (item instanceof SearchDataProvider.SectionInfo) {
                return ((SearchDataProvider.SectionInfo) item).title.hashCode();
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mProvider.getItem(position);

        if (item instanceof SearchDataProvider.SectionInfo) {
            return ITEM_VIEW_TYPE_SECTION_HEADER;
        } else {
            return ITEM_VIEW_TYPE_SECTION_ITEM;
        }
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View v;
        switch (viewType) {
            case ITEM_VIEW_TYPE_SECTION_HEADER:
                v = inflater.inflate(R.layout.row_common_section, parent, false);
                break;
            case ITEM_VIEW_TYPE_SECTION_ITEM:
                v = inflater.inflate(R.layout.row_common_item, parent, false);
                break;
            default:
                v = inflater.inflate(R.layout.row_footer_layout, parent, false);
        }

        return new SearchViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_SECTION_HEADER:
                onBindSectionHeaderViewHolder(holder, position);
                break;
            case ITEM_VIEW_TYPE_SECTION_ITEM:
                onBindSectionItemViewHolder(holder, position);
                break;
        }
    }

    private void onBindSectionHeaderViewHolder(SearchViewHolder holder, int position) {
        Object item = getItem(position);
        if (item instanceof SearchDataProvider.SectionInfo) {
            SearchDataProvider.SectionInfo sectionInfo = (SearchDataProvider.SectionInfo) item;

            // set text
            holder.mSectionTitle.setText(sectionInfo.title);
            holder.mSectionLabel.setText(sectionInfo.count + " 검색결과");
            holder.mSectionLabel.setVisibility(View.VISIBLE);
        }
    }

    private void onBindSectionItemViewHolder(SearchViewHolder holder, int position) {
        Object item = getItem(position);
        Context context = holder.itemView.getContext();

        if (item instanceof Route) {
            Route route = (Route) item;

            int color = route.getType().getColor(context);
            String description = route.getType().getDescription(context);
            String regionName = route.getRegionName();
            if (regionName == null) {
                regionName = FormatUtils.formatRegionName(context, route);
                route.setRegionName(regionName);
            }
            if (regionName != null) {
                description = description + " / " + regionName;
            }

            holder.mItemTitle.setText(route.getName());
            holder.mItemTitle.setTextColor(color);
            holder.mItemTitle.setTypeface(Typeface.DEFAULT_BOLD);
            holder.mItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            holder.mItemIcon.setImageResource(R.drawable.ic_bus);
            holder.mItemIcon.setColorFilter(color);
            holder.mItemDescription.setText(description);
            holder.mContainer.setOnClickListener(this);
            holder.mContainer.setTag(route);

            if (!Provider.SEOUL.equals(route.getProvider())) {
                holder.mItemLabel.setText(route.getProvider().getCityName(context));
                holder.mItemLabel.setTextColor(color);
                holder.mItemLabel.setVisibility(View.VISIBLE);
            } else {
                holder.mItemLabel.setVisibility(View.GONE);
            }

        } else if (item instanceof Station) {
            Station station = (Station) item;
            int color = ThemeUtils.getResourceColor(context, R.color.primary_text_default_material_light);
            holder.mItemTitle.setText(station.getName());
            holder.mItemTitle.setTextColor(color);
            holder.mItemTitle.setTypeface(Typeface.DEFAULT);
            holder.mItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            holder.mItemIcon.setImageResource(R.drawable.ic_pin_drop);
            holder.mItemIcon.setColorFilter(color);
            holder.mItemLabel.setVisibility(View.GONE);

            //build local id string set
            String description = FormatUtils.formatStationIds(context, station);
            holder.mItemDescription.setText(description);
            holder.mContainer.setOnClickListener(this);
            holder.mContainer.setTag(station);
        }

    }

    public Object getItem(int position) {
        return mProvider.getItem(position);
    }

    @Override
    public int getItemCount() {
        return mProvider.getCount();
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        Object tag = v.getTag();
        if (tag == null) {
            return;

        } else if (tag instanceof Route) {
            Intent intent = new Intent(context, RouteActivity.class);
            intent.putExtra(RouteActivity.EXTRA_KEY_ROUTE, (Parcelable) tag);
            context.startActivity(intent);

        } else if (tag instanceof Station) {
            Intent intent = new Intent(context, StationActivity.class);
            intent.putExtra(StationActivity.EXTRA_KEY_STATION, (Parcelable) tag);
            context.startActivity(intent);
        }
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        //section item
        protected View mContainer;
        protected ImageView mItemIcon;
        protected TextView mItemTitle;
        protected TextView mItemLabel;
        protected TextView mItemDescription;
        protected ImageView mOverflowButton;

        //section header
        protected TextView mSectionTitle;
        protected TextView mSectionLabel;

        public SearchViewHolder(View v, int itemType) {
            super(v);
            switch (itemType) {
                case ITEM_VIEW_TYPE_SECTION_HEADER:
                    mSectionTitle = (TextView) v.findViewById(R.id.section_title);
                    mSectionLabel = (TextView) v.findViewById(R.id.section_label);
                    break;

                case ITEM_VIEW_TYPE_SECTION_ITEM:
                    mContainer = v.findViewById(R.id.card_view);
                    mItemIcon = (ImageView) v.findViewById(R.id.item_icon);
                    mItemTitle = (TextView) v.findViewById(R.id.item_title);
                    mItemLabel = (TextView) v.findViewById(R.id.item_label);
                    mItemDescription = (TextView) v.findViewById(R.id.item_description);
                    mOverflowButton = (ImageView) v.findViewById(R.id.overflow_button);
                    mOverflowButton.setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            ViewUtils.attachPopupMenu(view, R.menu.menu_popup_search_item, new MenuBuilder.Callback() {
                @Override
                public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                    if (mOnChildMenuItemClickListener != null) {
                        ViewUtils.runOnUiThread(() -> mOnChildMenuItemClickListener.onMenuItemClick(item, mContainer.getTag()));
                    }
                    return true;
                }

                @Override
                public void onMenuModeChange(MenuBuilder menu) {

                }
            });
        }

    }

    public interface OnChildMenuItemClickListener {
        void onMenuItemClick(MenuItem menuItem, Object object);
    }
}
