package fragment;


import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;

import adapter.BillListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import model.Bill;


/**
 * A simple {@link Fragment} subclass.
 */
public class BillFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {


    private static final String TAG = "BillFragment";
    @BindView(R.id.bill_fragment_list_bill)
    public ListView listBill;
    @BindView(R.id.bill_fragment_button_date)
    public Button buttonDate;
    @BindView(R.id.bill_fragment_id)
    public EditText edBillNumber;
    @BindView(R.id.bill_fragment_search)
    public Button buttonSearch;

    private BillListAdapter listBillAdapter;
    private ArrayList<Bill> listData;
    private SpinnerDatePickerDialogBuilder dateSpinner;
    private View view;
    private FirebaseFirestore db;
    private MaterialDialog dialogLoading;
    private ActionBar actionBar;

    public BillFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bill, container, false);
        ButterKnife.bind(this,view);
        db = FirebaseFirestore.getInstance();

        dateSpinner = new SpinnerDatePickerDialogBuilder();

        buttonDate.setOnClickListener(BillFragment.this);
        buttonSearch.setOnClickListener(BillFragment.this);

        //Create header for list bill -- Start
        ViewGroup myHeader = (ViewGroup)inflater.inflate(R.layout.bill_list_header, listBill,false);
        listBill.addHeaderView(myHeader,null,false);

        //Create header for list bill -- End

        listData = new ArrayList<>();
        listBillAdapter = new BillListAdapter(view.getContext(),listData);
        listBill.setAdapter(listBillAdapter);

        Calendar cal = Calendar.getInstance();
        buttonDate.setText(view.getResources().getString(R.string.full_date,new Object[]{cal.get(Calendar.DAY_OF_MONTH),
                                                            cal.get(Calendar.MONTH)+1,
                                                            cal.get(Calendar.YEAR)}));
        renderData(buttonDate.getText().toString());

        return view;
    }


    private void renderData(String date) {
        showLoadingDialog();
        listData.clear();
        db.collection(QuanLyConstants.ORDER)
            .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
            .whereEqualTo(QuanLyConstants.ORDER_DATE, date)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            Bill bill = new Bill();
                            bill.setId(document.getId());
                            bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                            bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                            listData.add(bill);
                        }
                        closeLoadingDialog();
                        listBillAdapter.notifyDataSetChanged();
                    }
                }
            });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bill_fragment_button_date){
            Calendar cal = Calendar.getInstance();
            dateSpinner.context(view.getContext())
                    .callback(this)
                    .showTitle(true)
                    .defaultDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))
                    .build().show();

        }
        else if(id == R.id.bill_fragment_search){

        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if(checkCorrectDate(dateChoose)){
            String dateText = view.getResources().getString(R.string.full_date,new Object[]{dateChoose.get(Calendar.DAY_OF_MONTH),
                    dateChoose.get(Calendar.MONTH)+1,
                    dateChoose.get(Calendar.YEAR)});
            buttonDate.setText(dateText);
            renderData(dateText);
        }
    }

    /*
    * @author: ManhLD
    * @param: dateChoosen
    * @purpose: Check the selected day by the user is correct.
    * The day can be called correct is the day can not after today.
    * */
    private boolean checkCorrectDate(Calendar dateChoosen){
        Calendar currentDate = Calendar.getInstance();
        if(dateChoosen.after(currentDate)){
            Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateChoose_after_currentDate),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(view.getContext())
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }
}
