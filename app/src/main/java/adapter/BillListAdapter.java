package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import manhquan.khoaluan_quanly.R;
import model.Bill;

/**
 * Created by LieuDucManh on 3/31/2018.
 */

public class BillListAdapter extends BaseAdapter {

    private ArrayList<Bill> listBill = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;

    public BillListAdapter(Context context, ArrayList<Bill> listBill){
        this.listBill = listBill;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listBill.size();
    }

    @Override
    public Object getItem(int position) {
        return listBill.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BillHolder holder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.food_on_bill_list_item,null);
            holder = new BillHolder(convertView);
            convertView.setTag(holder);
        }
        else{
            holder = (BillHolder)convertView.getTag();
        }

        Bill bill = listBill.get(position);
        holder.txtBillNumber.setText(bill.getId());
        holder.txtTime.setText(bill.getTime());
        holder.txtCustomerName.setText(bill.getCustomerName());
        holder.txtCost.setText(bill.getCostTotal());

        return convertView;
    }
}

class BillHolder{
    @BindView(R.id.bill_list_bill_number) TextView txtBillNumber;
    @BindView(R.id.bill_list_time) TextView txtTime;
    @BindView(R.id.bill_list_customer_name) TextView txtCustomerName;
    @BindView(R.id.bill_list_cost) TextView txtCost;

    public BillHolder(View view) {
        ButterKnife.bind(this,view);
    }
}