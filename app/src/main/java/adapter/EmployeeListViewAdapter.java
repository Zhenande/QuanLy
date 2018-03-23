package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import abstractModel.Employee;
import manhquan.khoaluan_quanly.R;

/**
 * Created by ABC on 3/17/2018.
 */

public class EmployeeListViewAdapter extends BaseAdapter{

    private List<Employee> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public EmployeeListViewAdapter( Context context, List<Employee> data) {
        this.data = data;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        EmployeeViewHolder holder;

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.employee_list_item,null);
            holder = new EmployeeViewHolder();
            holder.txtName = convertView.findViewById(R.id.employee_list_txtName);
            holder.txtPosition = convertView.findViewById(R.id.employee_list_txtPosition);
            convertView.setTag(holder);
        }
        else{
            holder = (EmployeeViewHolder)convertView.getTag();
        }

        Employee employee = this.data.get(position);
        holder.txtName.setText(employee.getName());
        String textPosition;
        switch (employee.getPosition()){
            case 2: textPosition = context.getResources().getString(R.string.employee_Cook);
                    break;
            case 3: textPosition = context.getResources().getString(R.string.employee_Waiter);
                    break;
            case 4: textPosition = context.getResources().getString(R.string.employee_Cashier);
                    break;
            default: textPosition = context.getResources().getString(R.string.employee_Cook);
                    break;
        }
        holder.txtPosition.setText(textPosition);
        return convertView;
    }
}

class EmployeeViewHolder{
    TextView txtName;
    TextView txtPosition;
}