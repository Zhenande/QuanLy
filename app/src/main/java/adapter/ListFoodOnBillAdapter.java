package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.birin.gridlistviewadapters.Card;
import com.birin.gridlistviewadapters.ListGridAdapter;
import com.birin.gridlistviewadapters.dataholders.CardDataHolder;
import com.birin.gridlistviewadapters.utils.ChildViewsClickHandler;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.Bill;
import model.FoodOnBill;
import util.MoneyFormatter;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class ListFoodOnBillAdapter extends BaseAdapter {

    private ArrayList<FoodOnBill> listData = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;

    public ListFoodOnBillAdapter(Context context, ArrayList<FoodOnBill> listData){
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
        FoodOnBillHolder holder;
        if(convertView!= null){
            holder = (FoodOnBillHolder)convertView.getTag();
        }
        else{
            convertView = layoutInflater.inflate(R.layout.food_on_bill_list_item,null);
            holder = new FoodOnBillHolder(convertView);
            convertView.setTag(holder);
        }

        FoodOnBill foodOnBill = listData.get(position);
        holder.txtFoodName.setText(foodOnBill.getFoodName());
        holder.txtPrice.setText(MoneyFormatter.formatToMoney(foodOnBill.getPrice()+""));
        holder.txtQuantity.setText(context.getResources().getString(R.string.normal_string,foodOnBill.getQuantity()));
        holder.txtTotalPrice.setText(MoneyFormatter.formatToMoney(foodOnBill.getPrice()*foodOnBill.getQuantity()+""));

        return convertView;
    }

}

class FoodOnBillHolder{
    @BindView(R.id.food_on_bill_list_food_name) TextView txtFoodName;
    @BindView(R.id.food_on_bill_list_food_price) TextView txtPrice;
    @BindView(R.id.food_on_bill_list_quantity) TextView txtQuantity;
    @BindView(R.id.food_on_bill_list_total_price) TextView txtTotalPrice;

    public FoodOnBillHolder(View view) {
        ButterKnife.bind(this,view);
    }
}
