package adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.Food;
import util.GlideApp;
import util.MoneyFormatter;


public class MenuFoodListAdapter extends BaseAdapter{

    private ArrayList<Food> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public MenuFoodListAdapter(Context context, ArrayList<Food> listData) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FoodHolder holder;

        if(convertView != null){
            holder = (FoodHolder)convertView.getTag();
        }
        else{
            convertView = layoutInflater.inflate(R.layout.menu_food_list_item,null);
            holder = new FoodHolder(convertView);
            convertView.setTag(holder);
        }

        Food food = listData.get(position);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child(QuanLyConstants.FOOD_PATH_IMAGE + food.getImageResource());
        GlideApp.with(context)
                .load(imageRef)
                .into(holder.foodImage);
        holder.foodPrice.setText(context.getResources().getString(R.string.money_type,MoneyFormatter.formatToMoney(food.getPrice()+"")));
        holder.foodName.setText(food.getFoodName());

        return convertView;
    }
}

class FoodHolder{
    @BindView(R.id.food_item_image)
    ImageView foodImage;
    @BindView(R.id.food_item_price)
    TextView foodPrice;
    @BindView(R.id.food_item_name)
    TextView foodName;

    public FoodHolder(View view) {
        ButterKnife.bind(this,view);
    }
}