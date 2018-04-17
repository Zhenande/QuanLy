package fragment;



import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import util.DayAxisValueFormatter;
import util.MoneyFormatter;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    @BindView(R.id.income_button_startDate)
    public Button buttonStartDate;
    @BindView(R.id.income_button_endDate)
    public Button buttonEndDate;
    private View view;
    private boolean flag = false;
    @BindView(R.id.income_spinner_ReportTime)
    public Spinner spinnerReportTime;
    @BindView(R.id.income_spinner_kindOfChart)
    public Spinner spinnerKindOfChart;
    public BarChart chart;
    @BindView(R.id.linear_report_date)
    LinearLayout dateLayout;
    @BindView(R.id.linear_report_month)
    LinearLayout monthLayout;
    @BindView(R.id.linear_report_year)
    LinearLayout yearLayout;
    @BindView(R.id.income_button_Month)
    Button buttonMonth;
    @BindView(R.id.income_button_Year)
    Button buttonYear;
    @BindView(R.id.income_button_Action)
    Button buttonAction;
    private SpinnerDatePickerDialogBuilder dateSpinner;
    private List<BarEntry> listBar = new ArrayList<>();
    private FirebaseFirestore db;
    private SimpleDateFormat sdf_full_date = new SimpleDateFormat("dd/MM/yyyy");
    private BarData barData;
    private BarDataSet barDataSet;
    private MaterialDialog dialogLoading;


    public IncomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_income, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.table_color));

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        dateSpinner = new SpinnerDatePickerDialogBuilder();
        ButterKnife.bind(this,view);

        db = FirebaseFirestore.getInstance();

        chart = view.findViewById(R.id.income_chart);

        ArrayAdapter<CharSequence> adapterReportTime = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_reportTime,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> adapterKindOfChart = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_kindOfChart,
                android.R.layout.simple_spinner_item);

        adapterReportTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterKindOfChart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportTime.setAdapter(adapterReportTime);
        spinnerKindOfChart.setAdapter(adapterKindOfChart);


        initDateStartEnd();

        spinnerReportTime.setOnItemSelectedListener(IncomeFragment.this);
        buttonStartDate.setOnClickListener(IncomeFragment.this);
        buttonEndDate.setOnClickListener(IncomeFragment.this);
        buttonMonth.setOnClickListener(IncomeFragment.this);
        buttonYear.setOnClickListener(IncomeFragment.this);
        buttonAction.setOnClickListener(IncomeFragment.this);

        return view;
    }


    @Override
    public void onClick(View v) {
        Calendar cal = Calendar.getInstance();
        dateSpinner.context(view.getContext())
                .callback(this)
                .defaultDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        int id = v.getId();
        if(id == buttonStartDate.getId()){
            flag = true;
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonEndDate.getId()){
            flag = false;
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonMonth.getId()){
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonYear.getId()){
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonAction.getId()){
            createChart();
        }
    }

    private void createChart() {
        switch (spinnerKindOfChart.getSelectedItemPosition()){
            case 0: drawChartRevenue();
                    break;
            case 1: drawChartOrder();
                    break;
            case 2: drawChartBoth();
                    break;
        }
    }

    private void drawChartRevenue() {
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: drawChart0_0();
                    break;
            case 1:
                    break;
            case 2:
                    break;
        }
    }

    private void drawChart0_0() {
        showLoadingDialog();
        db.collection(QuanLyConstants.ORDER)
//            .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
            .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, buttonStartDate.getText())
            //.whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, buttonEndDate.getText())
            .orderBy(QuanLyConstants.ORDER_DATE)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
//                        Calendar startDate = Calendar.getInstance();
//                        startDate.set(Calendar.YEAR, Integer.parseInt(buttonStartDate.getText().toString().split("/")[2]));
//                        startDate.set(Calendar.MONTH, Integer.parseInt(buttonStartDate.getText().toString().split("/")[1]));
//                        startDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(buttonStartDate.getText().toString().split("/")[0]) );
                        float counting = 1;
                        String compareDate = buttonStartDate.getText().toString();
                        ArrayList<String> xValue = new ArrayList<>();
                        xValue.add(buttonStartDate.getText().toString());


                        double income = 0;
                        for(DocumentSnapshot document : task.getResult()){
                            if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                income += Double.parseDouble(MoneyFormatter.backToString(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString()));
                            }
                            else{
                                listBar.add(new BarEntry(counting,(float)income));
                                counting++;
                                compareDate = increaseDate(compareDate);
                                xValue.add(compareDate);
                                income = Double.parseDouble(MoneyFormatter.backToString(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString()));
                            }
                        }
                        barDataSet = new BarDataSet(listBar, "Testing");
                        barData = new BarData(barDataSet);
                        chart.setData(barData);
                        chart.invalidate();
                        closeLoadingDialog();
                    }
                }
            });
    }

    private String increaseDate(String compareDate) {
        char[] date = compareDate.toCharArray();
        if(date[1] == '9'){
            date[0]++;
            date[1] = '0';
        }
        else{
            date[1]++;
        }
        return date.toString();
    }

    private void drawChartOrder() {

    }

    private void drawChartBoth() {

    }

    /*
    * @author: ManhLD
    * @purpose: Check the date selected is correct or not.
    * Both date can not be after the current day
    * The day start must after the date end.
    * */
    private boolean checkCorrectDate(Calendar dateChoosen){
        Calendar currentDate = Calendar.getInstance();
        if(currentDate.before(dateChoosen)){
            Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateChoose_after_currentDate),Toast.LENGTH_LONG).show();
            return false;
        }
        String[] date;
        if(flag){
            date = buttonEndDate.getText().toString().split("/");
            Calendar dateCheck = Calendar.getInstance();
            dateCheck.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            dateCheck.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
            dateCheck.set(Calendar.YEAR, Integer.parseInt(date[2]));
            if(dateChoosen.after(dateCheck)){
                Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateStart_after_dateEnd),Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else{
            date = buttonStartDate.getText().toString().split("/");
            Calendar dateCheck = Calendar.getInstance();
            dateCheck.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            dateCheck.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
            dateCheck.set(Calendar.YEAR, Integer.parseInt(date[2]));
            if(dateChoosen.before(dateCheck)){
                Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateStart_after_dateEnd),Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }


    /*
    * @author: ManhLD
    * @purpose: Check the Year selected is correct or not. The month selected can not after the current Year.
    * */
    private boolean checkCorrectYear(int year) {
        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.YEAR) < year){
            Toast.makeText(getActivity().getApplicationContext(),"Year",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
    * @author: ManhLD
    * @purpose: Check the Month selected is correct or not. The month selected can not after the current Month.
    * */
    private boolean checkCorrectMonth(Calendar dateChoose) {
        Calendar dateCheck = Calendar.getInstance();
        dateCheck.get(Calendar.MONTH);
        dateCheck.get(Calendar.YEAR);
        if(dateChoose.get(Calendar.YEAR) > dateCheck.get(Calendar.YEAR)){
            Toast.makeText(getActivity().getApplicationContext(),"Year of month",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(dateChoose.get(Calendar.YEAR) == dateCheck.get(Calendar.YEAR)){
            if(dateChoose.get(Calendar.MONTH) > dateCheck.get(Calendar.MONTH)){
                Toast.makeText(getActivity().getApplicationContext(),"Year of month",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /*
    * @author: ManhLD
    * @purpose: Render the Date Start = The first day of the month (1/1/2018)
    *           Render the Date End = The current day
    * */
    private void initDateStartEnd(){
        Calendar currentDate = Calendar.getInstance();
        String dateStart = "";
        dateStart += "1/";
        dateStart += currentDate.get(Calendar.MONTH)+1 + "/";
        dateStart += currentDate.get(Calendar.YEAR);
        buttonStartDate.setText(dateStart);

        String dateEnd = "";
        dateEnd += currentDate.get(Calendar.DAY_OF_MONTH) + "/";
        dateEnd += currentDate.get(Calendar.MONTH)+1 + "/";
        dateEnd += currentDate.get(Calendar.YEAR);
        buttonEndDate.setText(dateEnd);
    }

    /*
    * @author: ManhLD
    * @purpose: Render the current Month and Year to the Button
    * */
    private void initMonth() {
        Calendar currentMonth = Calendar.getInstance();
        String curMonth = currentMonth.get(Calendar.MONTH)+1 + "/";
        curMonth += currentMonth.get(Calendar.YEAR);
        buttonMonth.setText(curMonth);
    }

    /*
    * @author: ManhLD
    * @purpose: Render the current Year to the Button
    * */
    private void initYear() {
        Calendar currentMonth = Calendar.getInstance();
        String curYear = currentMonth.get(Calendar.YEAR) + "";
        buttonYear.setText(curYear);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0: dateLayout.setVisibility(View.VISIBLE);
                monthLayout.setVisibility(View.GONE);
                yearLayout.setVisibility(View.GONE);
                initDateStartEnd();
                break;
            case 1: dateLayout.setVisibility(View.GONE);
                monthLayout.setVisibility(View.VISIBLE);
                yearLayout.setVisibility(View.GONE);
                initMonth();
                break;
            case 2: dateLayout.setVisibility(View.GONE);
                monthLayout.setVisibility(View.GONE);
                yearLayout.setVisibility(View.VISIBLE);
                initYear();
                break;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /*
    * @author: ManhLD
    * @param: view
    * @param: year
    * @param: monthOfYear
    * @param: dayOfMonth
    * @purpose: Render date selected to the button
    * */
    @Override
    public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: if(checkCorrectDate(dateChoose)) {
                if (flag) {
//                    buttonStartDate.setText(view.getContext().getResources().getString(R.string.full_date,new Object[]{dayOfMonth,monthOfYear+1,year}));
                    buttonStartDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_full_date.format(dateChoose)+""));
                } else {
//                    buttonEndDate.setText(view.getContext().getResources().getString(R.string.full_date,new Object[]{dayOfMonth,monthOfYear+1,year}));
                    buttonEndDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_full_date.format(dateChoose)+""));
                }
            }
                break;
            case 1: if(checkCorrectMonth(dateChoose)){
                buttonMonth.setText(view.getContext().getResources().getString(R.string.month_year_date,new Object[]{monthOfYear+1,year}));
                }
                break;
            case 2: if(checkCorrectYear(year)){
                buttonYear.setText(view.getContext().getResources().getString(R.string.year_date,new Object[]{year}));
                }
                break;
        }
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
