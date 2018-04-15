package adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import manhquan.khoaluan_quanly.R;
import model.NotiContent;
import util.NotificationTouchHelper;

/**
 * Created by vpmn-os-quocnb on 4/13/2018.
 */

public class NotificationAdapter extends ExpandableRecyclerViewAdapter<TableViewHolder, FoodViewHolder> {

    private NotificationTouchHelper.CallBackRemoveItem cbri;

    public NotificationAdapter(Activity activity, List<? extends ExpandableGroup> groups) {
        super(groups);
        cbri = (NotificationTouchHelper.CallBackRemoveItem)activity;
    }

    @Override
    public TableViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_table_number, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public FoodViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_food_name, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(FoodViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final NotiContent noti = (NotiContent) group.getItems().get(childIndex);
        holder.onBind(noti);
    }

    @Override
    public void onBindGroupViewHolder(TableViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setTxtTableNumber(group);
    }

    public void removeItem(int position) {
        ExpandableListPosition listPos = expandableList.getUnflattenedPosition(position);
//        ExpandableGroup group = expandableList.getExpandableGroup(listPos);

        int parentPosition = listPos.groupPos;
        switch (listPos.type) {
            case ExpandableListPosition.CHILD:
//                if (group.getItemCount() == 1) {
//                    notifyItemRangeRemoved(position - 1, 2);
//                } else {
//                    notifyItemRemoved(position);
//                }
                cbri.onRemoveItem(position, parentPosition);
                break;
            case ExpandableListPosition.GROUP:
//                while(group.getItemCount()!=0){
//                    group.getItems().remove(0);
//                    notifyItemRemoved(position+1);
//                }
//                notifyItemRemoved(position);
                cbri.onRemoveItem(position, -1);
                break;
        }
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

class TableViewHolder extends GroupViewHolder{
    private TextView txtTableNumber;

    public TableViewHolder(View itemView) {
        super(itemView);
        txtTableNumber = itemView.findViewById(R.id.list_item_table_number);
    }

    public void setTxtTableNumber(ExpandableGroup group){
        txtTableNumber.setText(group.getTitle());
    }
}

class FoodViewHolder extends ChildViewHolder{
    private TextView txtFoodName;

    public FoodViewHolder(View itemView) {
        super(itemView);
        txtFoodName = itemView.findViewById(R.id.list_item_food_name);
    }

    public void onBind(NotiContent noti){
        txtFoodName.setText(noti.getContent());
    }
}