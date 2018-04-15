package util;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import java.util.List;

import adapter.NotificationAdapter;
import model.Notification;

/**
 * Created by LieuDucManh on 4/13/2018.
 */

public class NotificationTouchHelper extends ItemTouchHelper.SimpleCallback {

    private NotificationAdapter adapter;
    private CallBackRemoveItem cbri;

    public interface CallBackRemoveItem{
        void onRemoveItem(int position, int parentPos);
    }

    public NotificationTouchHelper(NotificationAdapter adapter, CallBackRemoveItem cbri) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.cbri = cbri;
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        adapter.removeItem(position);
    }


}

