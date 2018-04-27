package fragment;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import adapter.BillListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.OrderDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Bill;


/**
 * A simple {@link Fragment} subclass.
 */
public class BillFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemClickListener {


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
    private ArrayList<Bill> listShow = new ArrayList<>();
    private SpinnerDatePickerDialogBuilder dateSpinner;
    private View view;
    private FirebaseFirestore db;
    private MaterialDialog dialogLoading;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Date = new SimpleDateFormat("yyyy/MM/dd");
    private boolean flag_loading;

    public BillFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bill, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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
        listBillAdapter = new BillListAdapter(view.getContext(),listShow);
        listBill.setAdapter(listBillAdapter);

        listBill.setOnItemClickListener(this);


        Calendar cal = Calendar.getInstance();
        buttonDate.setText(view.getResources().getString(R.string.full_date,
                cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH)+1,
                        cal.get(Calendar.YEAR)));
        renderData(sdf_Date.format(cal.getTime()));

        listBill.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0){
                    if(!flag_loading && listData.size() > 0){
                        flag_loading = true;
                        getMoreItems();
                    }
                }
            }
        });

        return view;
    }


    private void renderData(String date) {
        showLoadingDialog();
        listData.clear();
        listShow.clear();
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
                            bill.setBillNumber(document.get(QuanLyConstants.BILL_NUMBER).toString());
                            bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                            bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                            listData.add(bill);
                        }
                        closeLoadingDialog();
                        Collections.sort(listData, new Comparator<Bill>() {
                            @Override
                            public int compare(Bill o1, Bill o2) {
                                return o1.getBillNumber().compareTo(o2.getBillNumber());
                            }
                        });
                        getMoreItems();
                    }
                }
            });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bill_fragment_button_date){
            Calendar cal = Calendar.getInstance();
            String[] selectedDay = buttonDate.getText().toString().split("/");
            cal.set(Calendar.YEAR, Integer.parseInt(selectedDay[2]));
            cal.set(Calendar.MONTH, Integer.parseInt(selectedDay[1])-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(selectedDay[0]));
            dateSpinner.context(view.getContext())
                    .callback(this)
                    .showTitle(true)
                    .defaultDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))
                    .build().show();

        }
        else if(id == R.id.bill_fragment_search){
            doButtonSearch();
        }
    }

    private void doButtonSearch() {
        showLoadingDialog();
        final String inputBillNumber = edBillNumber.getText().toString();
//        String date = inputBillNumber.substring(0,4);
//        String timeTest = inputBillNumber.substring(4,8);
//        String timeSearch = timeTest.substring(0,2) + ":" + timeTest.substring(2,4);
        if(inputBillNumber.length() < 4){
            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.bill_fragment_error_search), Toast.LENGTH_SHORT).show();
            closeLoadingDialog();
            return;
        }
        db.collection(QuanLyConstants.ORDER)
            .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_TIME,inputBillNumber)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        listData.clear();
                        listShow.clear();
                        for(DocumentSnapshot document : task.getResult()){
                            if(document.get(QuanLyConstants.BILL_NUMBER).toString().contains(inputBillNumber)){
                                Bill bill = new Bill();
                                bill.setId(document.getId());
                                bill.setBillNumber(document.get(QuanLyConstants.BILL_NUMBER).toString());
                                bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                                bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                listData.add(bill);
                                getMoreItems();
                                closeLoadingDialog();
                            }
                        }
                    }
                }
            });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if(checkCorrectDate(dateChoose)){
            buttonDate.setText(view.getResources().getString(R.string.full_date,dateChoose.get(Calendar.DAY_OF_MONTH),
                    dateChoose.get(Calendar.MONTH)+1,
                    dateChoose.get(Calendar.YEAR)));
            renderData(sdf_Date.format(dateChoose.getTime()));
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
                .backgroundColor(getResources().getColor(R.color.primary_dark))
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(view.getContext(), OrderDetailActivity.class);
        i.putExtra(QuanLyConstants.TABLE_ORDER_ID,listShow.get(position-1).getId());
        startActivity(i);
    }

    private void getMoreItems(){
        if(!dialogLoading.isShowing()){
            showLoadingDialog();
        }
        int count = 0;
        while(count < 10 && listData.size() > 0){
            listShow.add(listData.get(0));
            listData.remove(0);
            count++;
        }
        if(dialogLoading.isShowing()){
            closeLoadingDialog();
        }
        flag_loading = false;
        listBillAdapter.notifyDataSetChanged();
    }
}
