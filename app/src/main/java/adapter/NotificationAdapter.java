package adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import manhquan.khoaluan_quanly.R;
import model.CookFood;
import model.NotiContent;
import model.Notification;
import util.NotificationTouchHelper;

/**
 * Created by LieuDucManh on 4/13/2018.
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
    private TextView txtTableTime;
    private ImageView imgStatusTable;

    public TableViewHolder(View itemView) {
        super(itemView);
        txtTableNumber = itemView.findViewById(R.id.list_item_table_number);
        txtTableTime = itemView.findViewById(R.id.list_item_table_time);
        imgStatusTable = itemView.findViewById(R.id.list_item_table_status);
    }

    public void setTxtTableNumber(ExpandableGroup group){
        txtTableNumber.setText(group.getTitle());
    }

    @SuppressLint("SetTextI18n")
    public void setTxtTableTime(ExpandableGroup group){
        if(group instanceof CookFood){
            CookFood cf = (CookFood)group;
            if(cf.getTime().equals("99:99")){
                return;
            }
            txtTableTime.setText("Time: " + cf.getTime());
        }
        else if(group instanceof Notification){
            Notification noti = (Notification)group;
            if(noti.getTime().equals("99:99")){
                return;
            }
            txtTableTime.setText("Time: " + noti.getTime());
        }

    }

    public void setImgStatusTable(ExpandableGroup group){
        CookFood cf = (CookFood)group;
        switch (cf.getStatus()){
            case 0: imgStatusTable.setVisibility(View.INVISIBLE);
                    break;
            case 1: imgStatusTable.setVisibility(View.VISIBLE);
                    // meaning table had been create done all the food called
                    imgStatusTable.setImageResource(R.drawable.ic_done);
                    break;
            case 2: imgStatusTable.setVisibility(View.VISIBLE);
                    // meaing table still have some food need to cook
                    imgStatusTable.setImageResource(R.drawable.ic_not_done);
                    break;
        }
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