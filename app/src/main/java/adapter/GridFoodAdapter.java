package adapter;

import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.FoodDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Food;
import util.GlideApp;



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
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("images/" + food.getImageResource());
        Log.i("ImageLink","images/" + food.getImageResource());
        GlideApp.with(getContext())
                .load(imageRef)
                .into(cardViewHolder.foodImage);
        cardViewHolder.foodPrice.setText(food.getPrice()+"");
        cardViewHolder.foodName.setText(food.getFoodName());
    }

    @Override
    protected void onCardClicked(Food cardData) {
        Intent i = new Intent(getContext(), FoodDetailActivity.class);
        i.putExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME,cardData.getFoodName());
        getContext().startActivity(i);
    }

    @Override
    protected void registerChildrenViewClickEvents(FoodHolder cardViewHolder, ChildViewsClickHandler childViewsClickHandler) {
        childViewsClickHandler.registerChildViewForClickEvent(cardViewHolder.foodImage, QuanLyConstants.IMAGE_VIEW_CLICK_ID);
    }

    @Override
    protected void onChildViewClicked(View clickedChildView, Food cardData, int eventId) {
        if(eventId == QuanLyConstants.IMAGE_VIEW_CLICK_ID){
            Intent i = new Intent(getContext(), FoodDetailActivity.class);
            i.putExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME,cardData.getFoodName());
            getContext().startActivity(i);
        }
    }

}

class FoodHolder{
    ImageView foodImage;
    TextView foodPrice;
    TextView foodName;
}