package adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import manhquan.khoaluan_quanly.CartActivity;
import manhquan.khoaluan_quanly.R;
import model.FoodOnBill;

/**
 * Created by LieuDucManh on 4/10/2018.
 */
public class FoodChooseListAdapter extends BaseAdapter {

    private ArrayList<FoodOnBill> listData;
    private Context context;
    private LayoutInflater inflater;

    public FoodChooseListAdapter(Context context, ArrayList<FoodOnBill> listData) {
        this.listData = listData;
        this.context = context;
        inflater = LayoutInflater.from(context);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FoodChooseHolder holder;

        if(convertView!= null){
            holder = (FoodChooseHolder)convertView.getTag();
        }
        else{
            convertView = inflater.inflate(R.layout.food_choose_list_item,null);
            holder = new FoodChooseHolder(convertView);
            convertView.setTag(holder);

        }

        FoodOnBill fob = listData.get(position);
        holder.txtID.setText(position+1+"");
        holder.txtName.setText(fob.getFoodName());
        holder.txtPrice.setText(fob.getPrice()+"");
        holder.txtQuantity.setText(fob.getQuantity()+"");


        final FoodOnBill finalFob = fob;
        final FoodChooseHolder finalHolder = holder;
        final int finalPos = position;
        holder.buttonSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = finalFob.getQuantity();
                if(quantity == 1){
                    new MaterialDialog.Builder(v.getContext())
                            .positiveText(v.getContext().getResources().getString(R.string.main_agree))
                            .negativeText(v.getContext().getResources().getString(R.string.main_disagree))
                            .positiveColor(v.getContext().getResources().getColor(R.color.primary_dark))
                            .negativeColor(v.getContext().getResources().getColor(R.color.black))
                            .title(v.getContext().getResources().getString(R.string.detail_menu_delete))
                            .content(v.getContext().getResources().getString(R.string.food_detail_content_delete))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    CartActivity.removeFood(finalPos);
                                }
                            })
                            .build()
                            .show();
                }
                else{
                    finalHolder.txtQuantity.setText(quantity+"");
                }
            }
        });

        holder.buttonSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = finalFob.getQuantity();
                finalHolder.txtQuantity.setText(quantity+"");
            }
        });

        return convertView;
    }
}

class FoodChooseHolder{

    @BindView(R.id.food_choose_stt)
    TextView txtID;
    @BindView(R.id.food_choose_name)
    TextView txtName;
    @BindView(R.id.food_choose_price)
    TextView txtPrice;
    @BindView(R.id.food_choose_quantity)
    EditText txtQuantity;
    @BindView(R.id.food_choose_sub)
    ImageButton buttonSub;
    @BindView(R.id.food_choose_sum)
    ImageButton buttonSum;

    public FoodChooseHolder(View view) {
        ButterKnife.bind(this,view);
    }
}
