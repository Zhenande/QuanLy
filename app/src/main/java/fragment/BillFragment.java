package fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import adapter.BillListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.BillDetailActivity;
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
    @BindView(R.id.bill_fragment_checkbox_advanced_search)
    public CheckBox cbAdvancedSearch;
    @BindView(R.id.bill_fragment_checkbox_searchByWaiterID)
    public RadioButton cbSearchByWaiterID;
    @BindView(R.id.bill_fragment_checkbox_searchByTime)
    public RadioButton cbSearchByTime;
    @BindView(R.id.bill_fragment_linear_advanced_search)
    public LinearLayout llAdvancedSearch;
    @BindView(R.id.bill_fragment_textView_search_content)
    public TextView txtSearchContent;

    private BillListAdapter listBillAdapter;
    private ArrayList<Bill> listData;
    private ArrayList<Bill> listShow = new ArrayList<>();
    private SpinnerDatePickerDialogBuilder dateSpinner;
    private View view;
    private FirebaseFirestore db;
    private MaterialDialog dialogLoading;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Date = new SimpleDateFormat("yyyy/MM/dd");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Display = new SimpleDateFormat("dd/MM/yyyy");
    private boolean flag_loading;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Time = new SimpleDateFormat("kk:mm");
    private String restaurantID;

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
        restaurantID = getRestaurantID();

        //Create header for list bill -- Start
        ViewGroup myHeader = (ViewGroup)inflater.inflate(R.layout.bill_list_header, listBill,false);
        listBill.addHeaderView(myHeader,null,false);

        //Create header for list bill -- End

        listData = new ArrayList<>();
        listBillAdapter = new BillListAdapter(view.getContext(),listShow);
        listBill.setAdapter(listBillAdapter);

        listBill.setOnItemClickListener(this);
        cbAdvancedSearch.setOnClickListener(this);
        cbSearchByTime.setOnClickListener(this);
        cbSearchByWaiterID.setOnClickListener(this);

        Calendar cal = Calendar.getInstance();
        buttonDate.setText(view.getResources().getString(R.string.year_date,
                sdf_Display.format(cal.getTime())));
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
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
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
                            bill.setWaiterName(document.get(QuanLyConstants.ORDER_WAITER_NAME).toString());
                            listData.add(bill);
                        }
                        Collections.sort(listData, new Comparator<Bill>() {
                            @Override
                            public int compare(Bill o1, Bill o2) {
                                return o1.getBillNumber().compareTo(o2.getBillNumber());
                            }
                        });
                        closeLoadingDialog();
                        getMoreItems();
                    }
                }
            });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.bill_fragment_button_date:
                    setCalenderDate();
                    break;
            case R.id.bill_fragment_search:
                    doButtonSearch();
                    break;
            case R.id.bill_fragment_checkbox_advanced_search:
                    doTickAdvancedSearch();
                    break;
            case R.id.bill_fragment_checkbox_searchByWaiterID:
                    doSearchByWaiterID();
                    break;
            case R.id.bill_fragment_checkbox_searchByTime:
                    doSearchByTime();
                    break;
        }

    }

    private void doSearchByTime() {
        txtSearchContent.setText(getResources().getString(R.string.bill_fragment_time));
        cbSearchByTime.setChecked(true);
        edBillNumber.setText("");
    }

    private void doSearchByWaiterID() {
        txtSearchContent.setText(getResources().getString(R.string.bill_fragment_waiter_id));
        cbSearchByWaiterID.setChecked(true);
        edBillNumber.setText("");
    }

    private void doTickAdvancedSearch() {
        if(cbAdvancedSearch.isChecked()){
            llAdvancedSearch.setVisibility(View.VISIBLE);
            if(cbSearchByWaiterID.isChecked() || !cbSearchByTime.isChecked()){
                doSearchByWaiterID();
            }
            else{
                doSearchByTime();
            }
        }
        else{
            llAdvancedSearch.setVisibility(View.GONE);
            txtSearchContent.setText(getResources().getString(R.string.bill_fragment_id));
        }
        edBillNumber.setText("");
    }

    private boolean CheckInputTime() {
        try{
            Calendar cal = Calendar.getInstance();
            Date dateSelcted = sdf_Display.parse(buttonDate.getText().toString());
            if(!cal.after(dateSelcted) && !cal.before(dateSelcted)){
                String curTime = sdf_Time.format(cal.getTime());
                String inputTime = edBillNumber.getText().toString();
                if(curTime.compareTo(inputTime) < 0){
                    // meaning inputtime is after curtime
                    closeLoadingDialog();
                    return false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void setCalenderDate() {
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

    private void doButtonSearch() {
        showLoadingDialog();
        if(!cbAdvancedSearch.isChecked()){
            searchNormal();
        }
        else if(cbSearchByTime.isChecked()){
            if(CheckInputTime()) {
                searchByTime();
            }
            else{
                Toast.makeText(view.getContext(),
                        view.getContext().getResources().getString(R.string.bill_fragment_over_current_time), Toast.LENGTH_SHORT).show();
            }
        }
        else if(cbSearchByWaiterID.isChecked()){
            searchByWaiterID();
        }
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    /*
    * @author: ManhLD
    * Search by Waiter ID to get the bill make by Waiter
    * */
    private void searchByWaiterID() {
        String billNumberStart = getDateFind();
        db.collection(QuanLyConstants.ORDER)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
            .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
            .whereGreaterThanOrEqualTo(QuanLyConstants.BILL_NUMBER,billNumberStart)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        listShow.clear();
                        listData.clear();
                        String waiterName = edBillNumber.getText().toString();
                        for(DocumentSnapshot document : task.getResult()){
                            String waiterOrderName = document.get(QuanLyConstants.ORDER_WAITER_NAME).toString();
                            if(waiterOrderName.contains(waiterName)) {
                                Bill bill = new Bill();
                                bill.setId(document.getId());
                                bill.setWaiterName(waiterOrderName);
                                bill.setBillNumber(document.get(QuanLyConstants.BILL_NUMBER).toString());
                                bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                                bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                listData.add(bill);
                            }
                        }
                        getMoreItems();
                        closeLoadingDialog();
                    }
                    else{
                        closeLoadingDialog();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG,e.getMessage());
                }
            });
    }

    /*
    * @author: ManhLD
    * Search by time start.
    * Ex: CurrentDate 28/04 time 22:39. If i input 12:00. It will find the bill was made from 12:00 to 22:39 in 28/04
    * */
    private void searchByTime() {
        final String timeInput = edBillNumber.getText().toString();
        String billNumberStart = getDateFind();
        if(!timeInput.matches("[0-2]{1}[0-9]{1}:[0-5]{1}[0-9]{1}")){
            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.bill_fragment_error_input_time), Toast.LENGTH_SHORT).show();
        }
        else{
            db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.BILL_NUMBER,billNumberStart)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            listData.clear();
                            listShow.clear();
                            for(DocumentSnapshot document : task.getResult()){
                                String timeOrder = document.get(QuanLyConstants.ORDER_TIME).toString();
                                if(timeOrder.compareTo(timeInput) >= 0) {
                                    Bill bill = new Bill();
                                    bill.setId(document.getId());
                                    bill.setWaiterName(document.get(QuanLyConstants.ORDER_WAITER_NAME).toString());
                                    bill.setBillNumber(document.get(QuanLyConstants.BILL_NUMBER).toString());
                                    bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                                    bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                    listData.add(bill);
                                }
                            }
                            getMoreItems();
                            closeLoadingDialog();
                        }
                    }
                });
        }
    }

    /*
    * @author: ManhLD
    * Search by bill number ID
    * */
    private void searchNormal() {
        final String inputBillNumber = edBillNumber.getText().toString();
        if(inputBillNumber.length() < 4){
            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.bill_fragment_error_search), Toast.LENGTH_SHORT).show();
            closeLoadingDialog();
            return;
        }
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
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
                                    bill.setWaiterName(document.get(QuanLyConstants.ORDER_WAITER_NAME).toString());
                                    bill.setBillNumber(document.get(QuanLyConstants.BILL_NUMBER).toString());
                                    bill.setTime(document.get(QuanLyConstants.ORDER_TIME).toString());
                                    bill.setCostTotal(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                    listData.add(bill);
                                }
                            }
                            getMoreItems();
                            closeLoadingDialog();
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
            buttonDate.setText(view.getResources().getString(R.string.year_date,sdf_Display.format(dateChoose.getTime())));
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
                .backgroundColor(view.getContext().getResources().getColor(R.color.primary_dark))
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(view.getContext(), BillDetailActivity.class);
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

    /*
    * @author: ManhLD
    * Get the date to find right bill
    * */
    public String getDateFind(){
        StringBuilder result = new StringBuilder();
        String[] selectedDate = buttonDate.getText().toString().split("/");
        // inverse from dd/MM/yy to yyMMdd
        result.append(selectedDate[2].substring(2));
        result.append(selectedDate[1]);
        result.append(selectedDate[0]);
        return result.toString();
    }
}
