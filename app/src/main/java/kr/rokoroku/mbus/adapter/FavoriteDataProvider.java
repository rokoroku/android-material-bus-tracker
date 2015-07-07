package kr.rokoroku.mbus.adapter;

import java.util.List;

import kr.rokoroku.mbus.model.Favorite;
import kr.rokoroku.mbus.model.FavoriteGroup;

/**
 * Created by rok on 2015. 6. 2..
 */
public class FavoriteDataProvider {

    private Favorite favorite;
    private List<FavoriteGroup> favoriteGroups;

    private FavoriteGroup mLastRemovedGroup;
    private FavoriteGroup.FavoriteItem mLastRemovedChild;
    private int mLastRemovedGroupPosition = -1;
    private int mLastRemovedChildPosition = -1;
    private long mLastRemovedChildParentGroupId = -1;

    public FavoriteDataProvider(Favorite favorite) {
        setFavorite(favorite);
    }

    public Favorite getFavorite() {
        return favorite;
    }

    public void setFavorite(Favorite favorite) {
        this.favorite = favorite;
        this.favoriteGroups = favorite.getFavoriteGroups();
    }

    public int getGroupCount() {
        return favoriteGroups.size();
    }

    public int getChildCount(int groupIndex) {
        FavoriteGroup groupItem = getGroupItem(groupIndex);
        if (groupItem != null) {
            return groupItem.size();
        } else {
            return 0;
        }
    }

    public FavoriteGroup getGroupItem(int index) {
        if (index < favoriteGroups.size()) {
            return favoriteGroups.get(index);
        } else {
            return null;
        }
    }

    public FavoriteGroup.FavoriteItem getChildItem(int groupIndex, int childIndex) {
        FavoriteGroup groupItem = getGroupItem(groupIndex);
        if (groupItem != null) return groupItem.get(childIndex);
        return null;
    }

    public void moveGroupItem(int from, int to) {
        FavoriteGroup group = favoriteGroups.remove(from);
        if (group != null) {
            favoriteGroups.add(to, group);
        }
    }

    public void moveChildItem(int fromGroup, int fromChild, int toGroup, int toChild) {

        if (fromGroup < 0 || fromGroup >= favoriteGroups.size()) return;
        if (toGroup < 0 || toGroup >= favoriteGroups.size()) return;

        FavoriteGroup sourceGroup = favoriteGroups.get(fromGroup);
        if (fromGroup == toGroup) {
            sourceGroup.move(fromChild, toChild);

        } else {
            FavoriteGroup targetGroup = favoriteGroups.get(toGroup);
            FavoriteGroup.FavoriteItem sourceChild = sourceGroup.remove(fromChild);
            if (sourceChild != null && targetGroup != null) {
                targetGroup.add(toChild, sourceChild);
            }
        }
    }

    public void addGroupItem(int position, FavoriteGroup favoriteGroup) {
        if (position > favoriteGroups.size()) position = favoriteGroups.size();
        favoriteGroups.add(position, favoriteGroup);
    }

    public void addChildItem(int groupPosition, FavoriteGroup.FavoriteItem favoriteItem) {
        FavoriteGroup groupItem = getGroupItem(groupPosition);
        if (groupItem != null) groupItem.add(favoriteItem);
    }

    public void removeGroupItem(int groupIndex) {
        FavoriteGroup groupItem = favoriteGroups.remove(groupIndex);
        if (groupItem != null) {
            this.mLastRemovedChild = null;
            this.mLastRemovedGroup = groupItem;
            this.mLastRemovedChildPosition = -1;
            this.mLastRemovedChildParentGroupId = -1;
        }
    }

    public void removeChildItem(int groupIndex, int childIndex) {
        FavoriteGroup groupItem = getGroupItem(groupIndex);
        if (groupItem != null) {
            this.mLastRemovedChild = groupItem.remove(childIndex);

            if(groupItem.isEmpty()) {
                this.mLastRemovedGroup = favoriteGroups.remove(groupIndex);
                this.mLastRemovedGroupPosition = groupIndex;
            } else {
                this.mLastRemovedGroup = null;
                this.mLastRemovedGroupPosition = -1;
            }
            this.mLastRemovedChildPosition = childIndex;
            this.mLastRemovedChildParentGroupId = groupItem.getId();
        }
    }

    public int[] undoLastRemoval() {
        if (mLastRemovedGroup != null) {
            int position;
            if (mLastRemovedGroupPosition >= 0 && mLastRemovedGroupPosition < favoriteGroups.size()) {
                position = mLastRemovedGroupPosition;
            } else {
                position = favoriteGroups.size();
            }

            if(mLastRemovedChild != null) {
                mLastRemovedGroup.add(mLastRemovedChild);
            }
            addGroupItem(position, mLastRemovedGroup);

            mLastRemovedGroup = null;
            mLastRemovedChild = null;
            return new int[]{position, -1};

        } else if (mLastRemovedChild != null) {
            int groupPosition = -1;
            int childPosition = -1;

            // find the group
            FavoriteGroup group = null;
            for (int i=0; i < favoriteGroups.size(); i++) {
                FavoriteGroup favoriteGroup = favoriteGroups.get(i);
                if(mLastRemovedChildParentGroupId == favoriteGroup.getId()) {
                    groupPosition = i;
                    group = favoriteGroup;
                    break;
                }
            }

            if (group != null) {
                if (mLastRemovedChildPosition >= 0 && mLastRemovedChildPosition < group.size()) {
                    childPosition = mLastRemovedChildPosition;
                } else {
                    childPosition = group.size();
                }
                group.add(childPosition, mLastRemovedChild);

                mLastRemovedChild = null;
                mLastRemovedGroup = null;
                return new int[]{groupPosition, childPosition};
            }
        }
        return new int[]{-1, -1};
    }

}
