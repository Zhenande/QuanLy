package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.BillDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.TableModel;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class GridListViewAdapter extends ListGridAdapter<TableModel, ViewHolder>{

    private Context context;

    public GridListViewAdapter(Context context, int totalCardsInRow) {
        super(context, totalCardsInRow);
        this.context = context;
    }

    @Override
    protected Card<ViewHolder> getNewCard(int cardwidth) {
        @SuppressLint("InflateParams") View cardView = getLayoutInflater().inflate(R.layout.table_list_item,null);

        // Use for resize image to fit devices resolution
//        ImageView imageTable = cardView.findViewById(R.id.table_list_image);
//        WindowManager wm = (WindowManager) cardView.getContext().getSystemService(Context.WINDOW_SERVICE);
//        Point size = new Point();
//        int width = imageTable.getLayoutParams().width;
//        if(width == 200){
//            wm.getDefaultDisplay().getRealSize(size);
//            imageTable.getLayoutParams().width = size.x / 5;
//            imageTable.getLayoutParams().height = size.y / 3;
//            imageTable.requestLayout();
//            Log.i("TESTING IMAGE", "x= " + size.x + " y= " + size.y);
//        }

        cardView.setMinimumHeight(cardwidth);

        ViewHolder viewHolder = new ViewHolder(cardView);

        return new Card<>(cardView,viewHolder);
    }

    @Override
    protected void setCardView(CardDataHolder<TableModel> cardDataHolder, ViewHolder cardViewHolder) {
        TableModel tModel = cardDataHolder.getData();
        if(!tModel.isAvailable()){
            // table has customer
            cardViewHolder.imgTable.setImageResource(R.drawable.ic_table_close);
        }else{
            // table free
            cardViewHolder.imgTable.setImageResource(R.drawable.ic_table_open);
        }
        cardViewHolder.tableNumber.setText(getContext().getResources().getString(R.string.tableNumber , tModel.getTableNumber()));
    }

    @Override
    protected void onCardClicked(TableModel cardData) {
        if(!cardData.isAvailable()) {
            Intent i = new Intent(getContext(), BillDetailActivity.class);
            i.putExtra(QuanLyConstants.TABLE_NUMBER, cardData.getTableNumber());
            getContext().startActivity(i);
        }
        else{
            new MaterialDialog.Builder(context)
                    .title(context.getResources().getString(R.string.notification))
                    .content(context.getResources().getString(R.string.noti_content,cardData.getTableNumber()))
                    .build()
                    .show();
        }
    }

    @Override
    protected void registerChildrenViewClickEvents(ViewHolder cardViewHolder, ChildViewsClickHandler childViewsClickHandler) {
        childViewsClickHandler.registerChildViewForClickEvent(cardViewHolder.imgTable, QuanLyConstants.IMAGE_VIEW_CLICK_ID);
    }

    @Override
    protected void onChildViewClicked(View clickedChildView, TableModel cardData, int eventId) {
        if(eventId == QuanLyConstants.IMAGE_VIEW_CLICK_ID){
            onCardClicked(cardData);
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
    @BindView(R.id.table_list_image) ImageView imgTable;
    @BindView(R.id.table_list_number) TextView tableNumber;

    public ViewHolder(View view) {
        ButterKnife.bind(this,view);
    }
}