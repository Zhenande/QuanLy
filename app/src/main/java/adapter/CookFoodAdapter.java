package adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;

import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition;

import java.util.ArrayList;
import java.util.List;

import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.FoodInside;
import util.NotificationTouchHelper;

/**
 * Created by LieuDucManh on 4/13/2018.
 */
public class CookFoodAdapter extends CheckableChildRecyclerViewAdapter<TableViewHolder,MultiCheckFoodViewHolder> {


    public CookFoodAdapter(List<? extends CheckedExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public MultiCheckFoodViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.checked_item_food_name, parent,false);
        return new MultiCheckFoodViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(MultiCheckFoodViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final FoodInside foodInside = (FoodInside)group.getItems().get(childIndex);
        holder.setFoodName(foodInside.getContent());
    }

    @Override
    public TableViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_table_number, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(TableViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setTxtTableNumber(group);
        holder.setTxtTableTime(group);
        holder.setImgStatusTable(group);
    }

    @Override
    public void onGroupExpanded(int positionStart, int itemCount) {
        if (itemCount > 0) {
            int groupIndex = expandableList.getUnflattenedPosition(positionStart).groupPos;
            notifyItemRangeInserted(positionStart, itemCount);
            for (ExpandableGroup grp : getGroups()) {
                if (grp != getGroups().get(groupIndex)) {
                    if (this.isGroupExpanded(grp)) {
                        this.toggleGroup(grp);
                        this.notifyDataSetChanged();
                    }
                }
            }
        }
    }

}

class MultiCheckFoodViewHolder extends CheckableChildViewHolder{

    private CheckedTextView childCheckTextView;

    MultiCheckFoodViewHolder(View itemView) {
        super(itemView);
        childCheckTextView = itemView.findViewById(R.id.checked_item_food_name);
    }

    @Override
    public Checkable getCheckable() {
        return childCheckTextView;
    }

    public void setFoodName(String foodName){
        childCheckTextView.setText(foodName);
    }
}


