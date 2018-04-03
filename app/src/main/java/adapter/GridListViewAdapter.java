package adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.BillDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.TableModel;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class GridListViewAdapter extends ListGridAdapter<TableModel, ViewHolder>{



    public GridListViewAdapter(Context context, int totalCardsInRow) {
        super(context, totalCardsInRow);
    }

    @Override
    protected Card<ViewHolder> getNewCard(int cardwidth) {
        View cardView = getLayoutInflater().inflate(R.layout.table_list_item,null);
        cardView.setMinimumHeight(cardwidth);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imgTable = cardView.findViewById(R.id.table_list_image);
        viewHolder.tableNumber = cardView.findViewById(R.id.table_list_number);

        return new Card<>(cardView,viewHolder);
    }

    @Override
    protected void setCardView(CardDataHolder<TableModel> cardDataHolder, ViewHolder cardViewHolder) {
        TableModel tModel = cardDataHolder.getData();
        if(tModel.isAvailable()){
            cardViewHolder.imgTable.setImageResource(R.drawable.ic_table_close);
        }else{
            cardViewHolder.imgTable.setImageResource(R.drawable.ic_table_open);
        }
        cardViewHolder.tableNumber.setText(getContext().getResources().getString(R.string.tableNumber) + " " + tModel.getTableNumber());
    }

    @Override
    protected void onCardClicked(TableModel cardData) {
        Intent i = new Intent(getContext(), BillDetailActivity.class);
        i.putExtra("TableNumber", cardData.getTableNumber());
        getContext().startActivity(i);
    }

    @Override
    protected void registerChildrenViewClickEvents(ViewHolder cardViewHolder, ChildViewsClickHandler childViewsClickHandler) {
        childViewsClickHandler.registerChildViewForClickEvent(cardViewHolder.imgTable, QuanLyConstants.IMAGE_VIEW_CLICK_ID);
    }

    @Override
    protected void onChildViewClicked(View clickedChildView, TableModel cardData, int eventId) {
        if(eventId == QuanLyConstants.IMAGE_VIEW_CLICK_ID){
            Toast.makeText(getContext(),
                    "Image click " + cardData.getTableNumber(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getCardSpacing() {
        return (6 * super.getCardSpacing());
    }

    @Override
    protected void setRowView(View rowView, int arg1) {
        rowView.setBackgroundColor(getContext().getResources().getColor(
                R.color.table_color));
    }

}

class ViewHolder{
    ImageView imgTable;
    TextView tableNumber;
}