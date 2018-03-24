package adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.Food;

/**
 * Created by ABC on 3/23/2018.
 */

public class GridFoodAdapter extends ListGridAdapter<Food,FoodHolder>{

    public GridFoodAdapter(Context context, int totalCardsInRow) {
        super(context, totalCardsInRow);
    }

    @Override
    protected Card<FoodHolder> getNewCard(int cardwidth) {
        View cardView = getLayoutInflater().inflate(R.layout.food_grid_list_item,null);
        cardView.setMinimumHeight(cardwidth);

        FoodHolder viewHolder = new FoodHolder();
        viewHolder.foodImage = cardView.findViewById(R.id.food_item_image);
        viewHolder.foodPrice = cardView.findViewById(R.id.food_item_price);
        viewHolder.foodName = cardView.findViewById(R.id.food_item_name);

        return new Card<>(cardView,viewHolder);
    }

    @Override
    protected void setCardView(CardDataHolder<Food> cardDataHolder, FoodHolder cardViewHolder) {
        Food food = cardDataHolder.getData();
        cardViewHolder.foodImage.setImageResource(food.getImageResource());
        cardViewHolder.foodPrice.setText(food.getPrice()+"");
        cardViewHolder.foodName.setText(food.getFoodName());
    }

    @Override
    protected void onCardClicked(Food cardData) {
        Toast.makeText(getContext(),
                "Food click " + cardData.getFoodName(), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void registerChildrenViewClickEvents(FoodHolder cardViewHolder, ChildViewsClickHandler childViewsClickHandler) {
        childViewsClickHandler.registerChildViewForClickEvent(cardViewHolder.foodImage, QuanLyConstants.IMAGE_VIEW_CLICK_ID);
    }

    @Override
    protected void onChildViewClicked(View clickedChildView, Food cardData, int eventId) {
        if(eventId == QuanLyConstants.IMAGE_VIEW_CLICK_ID){
            Toast.makeText(getContext(),
                    "FoodImage click " + cardData.getFoodName(), Toast.LENGTH_LONG)
                    .show();
        }
    }
}

class FoodHolder{
    ImageView foodImage;
    TextView foodPrice;
    TextView foodName;
}