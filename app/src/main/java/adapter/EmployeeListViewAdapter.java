package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstractModel.Employee;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;

import static util.GlobalVariable.closeLoadingDialog;
import static util.GlobalVariable.showLoadingDialog;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public class EmployeeListViewAdapter extends BaseAdapter{

    private List<Employee> data;
    private LayoutInflater layoutInflater;
    private Context context;
    private FirebaseFirestore db;
    private final String TAG = "EmployeeListViewAdapter";

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

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final EmployeeViewHolder holder;

        if(convertView == null){

            convertView = layoutInflater.inflate(R.layout.employee_list_item,null);
            holder = new EmployeeViewHolder(convertView);
            convertView.setTag(holder);
        }
        else{
            holder = (EmployeeViewHolder)convertView.getTag();
        }

        final Employee employee = this.data.get(position);
        holder.txtName.setText(employee.getName());
        holder.txtTimeWork.setText(employee.getDayWork());
        if(employee.isOnWork()){
            holder.imgOnWork.setImageResource(R.drawable.ic_true);
        }
        else{
            holder.imgOnWork.setImageResource(R.drawable.ic_false);
        }
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
        holder.imgOnWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean setFlag = holder.imgOnWork.getDrawable().getConstantState()
                        != v.getContext().getResources().getDrawable(R.drawable.ic_true).getConstantState();
                if(!setFlag){
                    // meaning that employee is on work
                    String content = v.getContext().getResources().getString(R.string.employee_left_work,employee.getName());
                    new MaterialDialog.Builder(v.getContext())
                            .content(content)
                            .positiveText(v.getContext().getResources().getString(R.string.main_agree))
                            .negativeText(v.getContext().getResources().getString(R.string.main_disagree))
                            .positiveColor(v.getContext().getResources().getColor(R.color.primary))
                            .negativeColor(v.getContext().getResources().getColor(R.color.black))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    showLoadingDialog(dialog.getContext());
                                    updateOnWork(setFlag,employee.getEmID());
                                    holder.imgOnWork.setImageResource(R.drawable.ic_false);
                                }
                            })
                            .build()
                            .show();
                }
                else{
                    // meaning that employee is not on work
                    String content = v.getContext().getResources().getString(R.string.employee_go_to_work,employee.getName());
                    new MaterialDialog.Builder(v.getContext())
                            .content(content)
                            .positiveText(v.getContext().getResources().getString(R.string.main_agree))
                            .negativeText(v.getContext().getResources().getString(R.string.main_disagree))
                            .positiveColor(v.getContext().getResources().getColor(R.color.primary))
                            .negativeColor(v.getContext().getResources().getColor(R.color.black))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    showLoadingDialog(dialog.getContext());
                                    updateOnWork(setFlag,employee.getEmID());
                                    holder.imgOnWork.setImageResource(R.drawable.ic_true);
                                }
                            })
                            .build()
                            .show();
                }
            }
        });
        return convertView;
    }

    private void updateOnWork(final boolean flagOnWork, String emID) {
        db = FirebaseFirestore.getInstance();
        db.collection(QuanLyConstants.EMPLOYEE)
                .document(emID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> data = new HashMap<>();
                            data.put(QuanLyConstants.EMPLOYEE_ONWORK,flagOnWork);
                            task.getResult().getReference().update(data);
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }
}

class EmployeeViewHolder{
    @BindView(R.id.employee_list_txtName)
    TextView txtName;
    @BindView(R.id.employee_list_txtPosition)
    TextView txtPosition;
    @BindView(R.id.employee_list_txtTimeWork)
    TextView txtTimeWork;
    @BindView(R.id.employee_list_txtOnWork)
    ImageView imgOnWork;

    public EmployeeViewHolder(View view) {
        ButterKnife.bind(this,view);
    }
}