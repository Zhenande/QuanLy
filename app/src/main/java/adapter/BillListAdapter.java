package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
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
            convertView = layoutInflater.inflate(R.layout.bill_list_item,null);
            holder = new BillHolder();
            holder.txtBillNumber = convertView.findViewById(R.id.bill_list_bill_number);
            holder.txtTime = convertView.findViewById(R.id.bill_list_time);
            holder.txtCustomerName = convertView.findViewById(R.id.bill_list_customer_name);
            holder.txtCost = convertView.findViewById(R.id.bill_list_cost);
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
    TextView txtBillNumber;
    TextView txtTime;
    TextView txtCustomerName;
    TextView txtCost;
}
