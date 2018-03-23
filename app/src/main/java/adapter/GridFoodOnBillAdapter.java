package adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.FoodOnBill;

/**
 * Created by ABC on 3/11/2018.
 */

public class GridFoodOnBillAdapter extends ListGridAdapter<FoodOnBill, FoodOnBillHolder> {

    public GridFoodOnBillAdapter(Context context, int totalCardsInRow) {
        super(context, totalCardsInRow);
    }

    @Override
    protected Card<FoodOnBillHolder> getNewCard(int cardwidth) {
        View cardView = getLayoutInflater().inflate(R.layout.food_on_bill_list_item,null);
        cardView.setMinimumWidth(cardwidth);

        FoodOnBillHolder holder = new FoodOnBillHolder();
        holder.txtFoodName = cardView.findViewById(R.id.food_on_bill_list_food_name);
        holder.txtPrice = cardView.findViewById(R.id.food_on_bill_list_food_price);
        holder.txtQuantity = cardView.findViewById(R.id.food_on_bill_list_quantity);
        holder.txtTotalPrice = cardView.findViewById(R.id.food_on_bill_list_total_price);

        return new Card<>(cardView,holder);
    }

    @Override
    protected void setCardView(CardDataHolder<FoodOnBill> cardDataHolder, FoodOnBillHolder cardViewHolder) {
        FoodOnBill foodOnBill = cardDataHolder.getData();
        cardViewHolder.txtFoodName.setText(foodOnBill.getFoodName());
        cardViewHolder.txtPrice.setText(foodOnBill.getPrice() + "");
        cardViewHolder.txtQuantity.setText(foodOnBill.getQuantity()+"");
        cardViewHolder.txtTotalPrice.setText(foodOnBill.getPrice()*foodOnBill.getQuantity() + "");
    }

    @Override
    protected void onCardClicked(FoodOnBill cardData) {
        Toast.makeText(getContext(),
                "Card click " + cardData.getFoodName(), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void registerChildrenViewClickEvents(FoodOnBillHolder cardViewHolder, ChildViewsClickHandler childViewsClickHandler) {
        childViewsClickHandler.registerChildViewForClickEvent(cardViewHolder.txtFoodName, QuanLyConstants.IMAGE_VIEW_CLICK_ID);
    }

    @Override
    protected void onChildViewClicked(View clickedChildView, FoodOnBill cardData, int eventId) {
        if(eventId == QuanLyConstants.IMAGE_VIEW_CLICK_ID){
            Toast.makeText(getContext(),
                    "FoodName click " + cardData.getFoodName(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getCardSpacing() {
        return (2 * super.getCardSpacing());
    }

    @Override
    protected void setRowView(View rowView, int arg1) {
        rowView.setBackgroundColor(getContext().getResources().getColor(
                R.color.colorPrimaryDark));
    }
}

class FoodOnBillHolder{
    TextView txtFoodName;
    TextView txtPrice;
    TextView txtQuantity;
    TextView txtTotalPrice;
}
